# AI.md

I used ChatGPT to support development and debugging for this project.

- I used it to explain Checkstyle feedback and how to resolve issues such as
  indentation for switch/case blocks, import ordering (static vs non-static),
  method naming patterns, and reducing indentation depth by extracting helpers.
- I asked for refactoring guidance to improve code quality, including extracting
  parsing logic into smaller methods (e.g., event parsing and save-file parsing),
  extracting save/load helpers, and separating UI formatting from core logic.
- I used it to draft concise JavaDoc comments for classes, methods, and helper
  utilities (e.g., parsing helpers, UI helpers, image cropping/circular avatar).
- I used it heavily for JavaFX migration: converting my CLI design to a GUI by
  making the core logic UI-agnostic (via `getResponse()`), wiring `Main`,
  `MainWindow`, FXML controllers, and dialog components together correctly, and
  troubleshooting `FXMLLoader` errors (controller class names, fx:id bindings,
  missing @FXML fields, etc.).
- I asked it for help implementing GUI behaviours and styling: auto-scrolling,
  differentiating error bubbles, aligning chat bubbles, setting wrap widths for
  resizing, improving spacing/padding, and styling input controls/buttons.
- I used it to debug visual issues like duplicated startup messages, circular
  avatar clipping/cropping, text wrapping, resizing behaviour, and TextArea
  behaviour (Enter vs Shift+Enter).
- I asked for guidance on persistence and robustness: showing a clearer message
  when save data is missing/failed to load, and handling IO errors gracefully.
- I asked it to propose a `help` command and a `sort` feature design, including
  sorting rules and where to place responsibilities (TaskList vs Ui vs Mochi).
- I used it to generate merge commit message bodies following the conventions
  I referenced earlier, and also asked Git workflow questions (PR creation,
  undoing merges/rollbacks, restoring after checkout).
- I asked it to propose additional tests, including what to test with JUnit and
  what to test manually (GUI and cross-environment behaviour).

After applying suggestions, I still manually reviewed the final decisions and verified by:
- running `./gradlew test`
- running `./gradlew checkstyleMain checkstyleTest`
- running `./gradlew run` and manually checking GUI behaviour (layout, wrapping,
  error styling, scrolling, and window resizing)