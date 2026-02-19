# AI.md

I used ChatGPT to help me while working on this project.

- I used it to explain Checkstyle errors and suggest how to fix common ones
  like wrong indentation, missing newline at end of file, and long methods.
- I asked it for refactoring ideas, like extracting helper methods for parsing
  and reducing nested logic.
- I used it to draft short JavaDoc comments so my code has clearer headers.
- I used it for GUI-related guidance (JavaFX), mainly how to connect my core
  logic to the GUI using `getResponse()` and how to show error messages in a
  different style.
- I also asked it for suggestions on user-facing commands like `help` and the
  idea for `sort`.
- I also asked it to help me with debugging my code when I can see logic issues
  within the app.

After applying suggestions, I still manually checked the code and verified by:
- running `./gradlew test`
- running `./gradlew checkstyleMain checkstyleTest`
- running the app to confirm behaviour