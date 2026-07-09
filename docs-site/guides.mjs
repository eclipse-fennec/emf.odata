// The published, user-facing docs (allowlist). Shared by the sync script and the
// VitePress config so the set and its order are defined exactly once.
//   file  — source markdown under ../docs (may live in a sub-folder, e.g. manual/)
//   title — sidebar / nav label
//   group — sidebar section the entry belongs to
//
// The route name is derived from the file's base name (see `slugFor`), so the
// numbered manual files keep their natural order (00-…, 01-…, …). Internal dev
// docs in ../docs (architecture change-log, backlogs, ADRs, conformance notes)
// are deliberately NOT listed here and stay unpublished (browsed on GitHub).
export const GUIDES = [
  // Guide — orientation.
  { file: 'manual/00-introduction.md', title: 'Introduction', group: 'Guide' },
  { file: 'manual/01-architecture.md', title: 'Architecture', group: 'Guide' },

  // User Manual — how to run and consume the server and the client.
  { file: 'manual/02-server.md', title: 'Server', group: 'User Manual' },
  { file: 'manual/03-client.md', title: 'Client', group: 'User Manual' },
  { file: 'manual/04-configuration.md', title: 'Configuration & Security', group: 'User Manual' },

  // Specification — what the spec requires vs. what we implement.
  { file: 'manual/05-conformance.md', title: 'Conformance Levels', group: 'Specification' },
  { file: 'manual/06-feature-matrix.md', title: 'Feature Matrix', group: 'Specification' },
];

// Route name for a guide: the file's base name without the .md extension.
// e.g. 'manual/01-architecture.md' -> '01-architecture', served at /guides/01-architecture.
export function slugFor(file) {
  return file.replace(/^.*\//, '').replace(/\.md$/, '');
}
