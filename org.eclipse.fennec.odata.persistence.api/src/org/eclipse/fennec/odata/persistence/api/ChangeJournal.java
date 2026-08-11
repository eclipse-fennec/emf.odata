/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 */
package org.eclipse.fennec.odata.persistence.api;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * A bounded, transaction-aware change journal — the state a {@link DeltaService} backend keeps
 * to answer "what changed since this token". Backends {@link #record record} every structural
 * write that went through them (changes applied behind the backend's back are invisible — a
 * service-layer journal, documented per backend); {@link #since} replays the retained window.
 *
 * <p>Sequence numbers are assigned when a change becomes VISIBLE: directly on record, or at
 * {@link #commit()} for changes buffered inside a {@link #begin() transaction} — assigning at
 * write time would let a concurrently issued token overtake uncommitted entries and silently
 * skip them. A rolled-back transaction never happened, journal-wise.
 *
 * <p>The journal is bounded: once the retention window is exceeded, tokens older than the
 * eviction horizon raise {@link DeltaGoneException} (410 — the client refetches) instead of
 * silently missing changes.
 */
public final class ChangeJournal {

	/** One structural change to a set member, in journal order. */
	public record Change(long seq, EClass type, String storeKey,
			Map<String, Object> keyValues, boolean deleted) {

		public Change {
			keyValues = keyValues == null ? Map.of() : Map.copyOf(keyValues); // defensive, immutable
		}
	}

	/**
	 * The deduplicated changes of one token window plus the follow-up token; {@code more} marks
	 * a TRUNCATED window (the span cap hit before "now" — the next window continues from
	 * {@code nextToken}).
	 */
	public record Window(List<Change> changes, String nextToken, boolean more) {
	}

	private final Deque<Change> journal = new ArrayDeque<>();
	private final int capacity;
	private long nextSeq = 1;
	/** Highest sequence number evicted from the bounded journal — tokens at or below it are gone. */
	private long evictedUpTo = 0;
	/** Changes of an open thread-bound transaction — flushed on commit, dropped on rollback. */
	private final ThreadLocal<List<Change>> pending = new ThreadLocal<>();

	public ChangeJournal(int capacity) {
		this.capacity = capacity;
	}

	/** The token capturing "now" — the starting point a delta link issued now tracks from. */
	public String token() {
		synchronized (journal) {
			return Long.toString(nextSeq - 1);
		}
	}

	/** Records one mutation; buffered while a thread-bound transaction is open. */
	public void record(EClass storeType, String storeKey, Map<String, Object> keyValues,
			boolean deleted) {
		Change change = new Change(0, storeType, storeKey, keyValues, deleted);
		List<Change> buffered = pending.get();
		if (buffered != null) {
			buffered.add(change);
			return;
		}
		append(List.of(change));
	}

	/** Opens the thread-bound buffer — pair with {@link #commit()} or {@link #rollback()}. */
	public void begin() {
		pending.set(new ArrayList<>());
	}

	/** Publishes the buffered changes (sequence numbers are assigned NOW, atomically). */
	public void commit() {
		List<Change> buffered = pending.get();
		if (buffered != null) {
			append(buffered);
		}
		pending.remove();
	}

	/** Drops the buffered changes — a rolled-back transaction never happened. */
	public void rollback() {
		pending.remove();
	}

	/**
	 * The changes since {@code token} whose store type is (a subtype of) {@code entityType}
	 * ({@code null} = every type), collapsed to each entity's LATEST outcome at its last
	 * position, plus the follow-up token.
	 *
	 * @throws DeltaGoneException when the token is malformed, foreign, or aged out of the
	 *                            retention window
	 */
	public Window since(String token, EClass entityType) {
		return since(token, entityType, Long.MAX_VALUE);
	}

	/**
	 * {@link #since(String, EClass)} bounded to a RAW-JOURNAL span: the window covers at most
	 * {@code maxSpan} sequence numbers past the token, so every caller using the same token and
	 * span sees the SAME upper bound (delta pages stay consistent across the per-type and
	 * expand-owner sub-queries).
	 */
	public Window since(String token, EClass entityType, long maxSpan) {
		long sinceSeq;
		try {
			sinceSeq = Long.parseLong(token);
		} catch (RuntimeException e) {
			throw new DeltaGoneException("the delta token is not valid");
		}
		List<Change> relevant = new ArrayList<>();
		long now;
		synchronized (journal) {
			// every change in (since, now] must still be retained — an evicted range would
			// silently lose changes, so the token is honestly gone (410)
			if (sinceSeq < 0 || sinceSeq > nextSeq - 1 || sinceSeq < evictedUpTo) {
				throw new DeltaGoneException("the delta token is no longer valid");
			}
			now = nextSeq - 1;
			long upper = maxSpan >= now - sinceSeq ? now : sinceSeq + maxSpan;
			for (Change change : journal) {
				if (change.seq() > sinceSeq && change.seq() <= upper
						&& (entityType == null || entityType.isSuperTypeOf(change.type()))) {
					relevant.add(change);
				}
			}
			now = upper; // the window's follow-up token is its own upper bound
		}
		Map<List<Object>, Change> latest = new LinkedHashMap<>();
		for (Change change : relevant) {
			List<Object> key = List.of(change.type(), change.storeKey());
			latest.remove(key);
			latest.put(key, change); // the LAST outcome, at its last position
		}
		boolean more;
		synchronized (journal) {
			more = now < nextSeq - 1;
		}
		return new Window(List.copyOf(latest.values()), Long.toString(now), more);
	}

	private void append(List<Change> changes) {
		synchronized (journal) {
			for (Change change : changes) {
				journal.addLast(new Change(nextSeq++, change.type(), change.storeKey(),
						change.keyValues(), change.deleted()));
				if (journal.size() > capacity) {
					evictedUpTo = journal.removeFirst().seq();
				}
			}
		}
	}

	/** Key-property name → value in id-attribute order — delta layers render ids from it. */
	public static Map<String, Object> keyValuesOf(EObject entity) {
		Map<String, Object> keys = new LinkedHashMap<>();
		for (EAttribute id : EntityKeys.of(entity.eClass())) {
			keys.put(id.getName(), entity.eGet(id));
		}
		return keys;
	}
}
