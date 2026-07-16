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
package org.eclipse.fennec.odata.persistence.inmemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.odata.persistence.api.ChangeJournal;
import org.eclipse.fennec.odata.persistence.api.DeltaGoneException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Direct unit tests for the {@link ChangeJournal} delta-tracking core (it previously had only
 * indirect coverage through delta round-trips): capacity eviction → 410 Gone, malformed/foreign
 * tokens → 410, transactional buffering (commit publishes, rollback discards), latest-outcome
 * collapse, type filtering and bounded-span windowing. Uses synthetic EClasses (no ecore file).
 */
@DisplayName("ChangeJournal: delta-tracking core")
class ChangeJournalTest {

	private final EClass item = named("Item");
	private final EClass special = subtype("SpecialItem", item);

	private static EClass named(String name) {
		EClass c = EcoreFactory.eINSTANCE.createEClass();
		c.setName(name);
		return c;
	}

	private static EClass subtype(String name, EClass base) {
		EClass c = named(name);
		c.getESuperTypes().add(base);
		return c;
	}

	private void record(ChangeJournal journal, EClass type, String key) {
		journal.record(type, key, Map.of("id", key), false);
	}

	@Test
	@DisplayName("commit publishes buffered changes; rollback discards them")
	void transactionalBuffering() {
		ChangeJournal journal = new ChangeJournal(100);
		String start = journal.token();

		journal.begin();
		record(journal, item, "a");
		// while buffered, the change is not yet visible
		assertEquals(0, journal.since(start, item).changes().size());
		journal.rollback();
		assertEquals(0, journal.since(start, item).changes().size(), "rollback discards the buffer");

		journal.begin();
		record(journal, item, "b");
		journal.commit();
		assertEquals(1, journal.since(start, item).changes().size(), "commit publishes the buffer");
	}

	@Test
	@DisplayName("a token aged out of the retention window is 410 Gone")
	void evictedTokenIsGone() {
		ChangeJournal journal = new ChangeJournal(2); // retains only the last 2 changes
		String old = journal.token();
		for (int i = 0; i < 5; i++) {
			record(journal, item, "k" + i); // pushes the old token's range out of retention
		}
		assertThrows(DeltaGoneException.class, () -> journal.since(old, item),
				"the token's range was evicted — it must be honestly gone, not silently short");
	}

	@Test
	@DisplayName("a malformed or out-of-range token is 410 Gone")
	void malformedTokenIsGone() {
		ChangeJournal journal = new ChangeJournal(100);
		assertThrows(DeltaGoneException.class, () -> journal.since("not-a-number", item));
		assertThrows(DeltaGoneException.class, () -> journal.since("999999", item),
				"a token from the future is invalid");
	}

	@Test
	@DisplayName("repeated changes to one entity collapse to its latest outcome")
	void latestOutcomeCollapse() {
		ChangeJournal journal = new ChangeJournal(100);
		String start = journal.token();
		record(journal, item, "a");
		record(journal, item, "a");
		journal.record(item, "a", Map.of("id", "a"), true); // finally deleted
		var window = journal.since(start, item);
		assertEquals(1, window.changes().size(), "one entity → one entry");
		assertTrue(window.changes().get(0).deleted(), "the LAST outcome (deleted) wins");
	}

	@Test
	@DisplayName("type filter is polymorphic; a bounded span reports 'more'")
	void typeFilterAndBoundedSpan() {
		ChangeJournal journal = new ChangeJournal(100);
		String start = journal.token();
		record(journal, item, "a");
		record(journal, special, "s"); // a subtype instance
		record(journal, named("Other"), "o"); // an unrelated type

		assertEquals(2, journal.since(start, item).changes().size(),
				"a base-type query sees the subtype too, not the unrelated type");

		record(journal, item, "b");
		record(journal, item, "c");
		var bounded = journal.since(start, item, 2); // cover only 2 sequence numbers
		assertTrue(bounded.more(), "a bounded span short of the tail must report more");
	}
}
