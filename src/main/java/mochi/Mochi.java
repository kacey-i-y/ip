package mochi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import mochi.task.Deadline;
import mochi.task.Event;
import mochi.task.Task;
import mochi.task.Todo;

/**
 * Main entry point for the Mochi CLI chatbot.
 *
 * <p>Mochi manages a list of tasks (to-dos, deadlines, and events) and supports
 * automatic loading from and saving to a local file on disk.
 */
public class Mochi {
    private static final String DATA_DIR_NAME = "data";
    private static final String SAVE_FILE_NAME = "tasks.txt";
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String PIPE_SPLIT_REGEX = "\\s*\\|\\s*";
    private static final DateTimeFormatter EVENT_INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /**
     * Supported user commands.
     */
    private enum Command {
        LIST, MARK, UNMARK, TODO, DEADLINE, EVENT, DELETE, BYE, UNKNOWN;

        /**
         * Parses the first token of the user input and maps it to a command.
         *
         * @param input Full user input line.
         * @return Parsed command, or {@code UNKNOWN} if not recognized.
         */
        static Command from(String input) {
            if (input == null) {
                return UNKNOWN;
            }

            String trimmed = input.trim();
            if (trimmed.isEmpty()) {
                return UNKNOWN;
            }

            String firstToken = trimmed.split("\\s+")[0].toLowerCase();
            return switch (firstToken) {
                case "list" -> LIST;
                case "mark" -> MARK;
                case "unmark" -> UNMARK;
                case "todo" -> TODO;
                case "deadline" -> DEADLINE;
                case "event" -> EVENT;
                case "delete" -> DELETE;
                case "bye" -> BYE;
                default -> UNKNOWN;
            };
        }
    }

    /**
     * Runs the chatbot in a read-eval-print loop.
     *
     * @param args Command line arguments (unused).
     * @throws IOException If an I/O error occurs while reading user input.
     */
    public static void main(String[] args) throws IOException {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        printWelcome();
        ArrayList<Task> taskList = loadTasks();
        printHelp();

        while (true) {
            String userInput = consoleReader.readLine();
            if (userInput == null) {
                break;
            }

            Command command = Command.from(userInput);
            if (command == Command.BYE) {
                break;
            }

            printSeparator();
            boolean hasChanged = handleCommand(command, userInput, taskList);

            if (hasChanged) {
                saveTasks(taskList);
            }

            printSeparator();
        }

        printGoodbye();
    }

    /**
     * Handles a single command, printing responses and modifying the task list if needed.
     *
     * @param command Parsed command type.
     * @param input Raw user input.
     * @param tasks Current in-memory task list.
     * @return {@code true} if the task list was modified, {@code false} otherwise.
     */
    private static boolean handleCommand(Command command, String input, ArrayList<Task> tasks) {
        return switch (command) {
            case LIST -> {
                printTasks(tasks);
                yield false;
            }
            case MARK -> markTask(tasks, input, true);
            case UNMARK -> markTask(tasks, input, false);
            case TODO -> addTodo(tasks, input);
            case DEADLINE -> addDeadline(tasks, input);
            case EVENT -> addEvent(tasks, input);
            case DELETE -> deleteTask(tasks, input);
            default -> {
                printError();
                yield false;
            }
        };
    }

    /**
     * Loads tasks from disk if the save file exists.
     *
     * @return List of tasks loaded from disk, or an empty list if file is missing/corrupted.
     */
    private static ArrayList<Task> loadTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        File file = getSaveFile();

        if (!file.exists()) {
            System.out.println("Save file not found. Starting with an empty task list.");
            return tasks;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    tasks.add(parseTaskLine(line));
                } catch (IllegalArgumentException e) {
                    System.out.println("Skipping corrupted save line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading save file: " + e.getMessage());
            System.out.println("Starting with an empty task list.");
            tasks.clear();
        }

        System.out.println(tasks.isEmpty()
                ? "No tasks loaded from disk."
                : "Loaded " + tasks.size() + " task(s) from disk.");

        return tasks;
    }

    /**
     * Saves all tasks to disk by rewriting the full save file.
     *
     * @param tasks Task list to save.
     */
    private static void saveTasks(ArrayList<Task> tasks) {
        File file = getSaveFile();
        File dataDir = file.getParentFile();

        if (dataDir != null && !dataDir.exists() && !dataDir.mkdirs()) {
            System.out.println("Failed to create data directory: " + dataDir.getPath());
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Task task : tasks) {
                writer.write(task.toWrite());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    /**
     * Parses one save-file line into a {@link Task}.
     *
     * <p>Expected formats:
     * <ul>
     *   <li>{@code T | 1 | description}</li>
     *   <li>{@code D | 0 | description | yyyy-MM-dd}</li>
     *   <li>{@code E | 0 | description | yyyy-MM-dd HHmm | yyyy-MM-dd HHmm}</li>
     * </ul>
     *
     * @param line One line from the save file.
     * @return Parsed task.
     * @throws IllegalArgumentException If the line is malformed.
     */
    private static Task parseTaskLine(String line) {
        String[] parts = line.split(PIPE_SPLIT_REGEX);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Too few fields");
        }

        String type = parts[0].trim().toUpperCase();
        String doneField = parts[1].trim();

        Task task;
        try {
            task = switch (type) {
                case "T" -> new Todo(parts[2].trim());
                case "D" -> {
                    if (parts.length < 4) {
                        throw new IllegalArgumentException("Deadline missing by");
                    }
                    LocalDate byDate = LocalDate.parse(parts[3].trim());
                    yield new Deadline(parts[2].trim(), byDate);
                }
                case "E" -> {
                    if (parts.length < 5) {
                        throw new IllegalArgumentException("Event missing from/to");
                    }

                    LocalDateTime fromDateTime =
                            LocalDateTime.parse(parts[3].trim(), EVENT_INPUT_FORMAT);
                    LocalDateTime toDateTime =
                            LocalDateTime.parse(parts[4].trim(), EVENT_INPUT_FORMAT);

                    if (!toDateTime.isAfter(fromDateTime)) {
                        throw new IllegalArgumentException("Event end must be after start");
                    }

                    yield new Event(parts[2].trim(), fromDateTime, toDateTime);
                }
                default -> throw new IllegalArgumentException("Unknown task type: " + type);
            };
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Bad date/time format", e);
        }

        if ("1".equals(doneField)) {
            task.mark();
        } else if ("0".equals(doneField)) {
            task.unmark();
        } else {
            throw new IllegalArgumentException("Bad done field: " + doneField);
        }

        return task;
    }

    /**
     * Returns the save file location as a relative, OS-independent path.
     *
     * @return Save file at {@code ./data/tasks.txt}.
     */
    private static File getSaveFile() {
        return new File(DATA_DIR_NAME, SAVE_FILE_NAME);
    }

    /**
     * Prints all tasks with 1-based indexing.
     *
     * @param tasks Task list to print.
     */
    private static void printTasks(ArrayList<Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("Your task list is empty.");
            return;
        }

        System.out.println("The following tasks are listed in the task list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + ". " + tasks.get(i));
        }
    }

    private static boolean markTask(ArrayList<Task> tasks, String input, boolean shouldMark) {
        try {
            int index = Integer.parseInt(input.split("\\s+")[1]) - 1;
            Task task = tasks.get(index);

            if (shouldMark) {
                task.mark();
                System.out.println("Marked as done: " + task);
            } else {
                task.unmark();
                System.out.println("Marked as not done: " + task);
            }
            return true;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            printError();
            return false;
        }
    }

    private static boolean deleteTask(ArrayList<Task> tasks, String input) {
        try {
            int index = Integer.parseInt(input.split("\\s+")[1]) - 1;
            Task removed = tasks.remove(index);

            System.out.println("Removed: " + removed);
            System.out.println("Currently, we have " + tasks.size() + " task(s) on the list.");
            return true;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            printError();
            return false;
        }
    }

    private static boolean addTodo(ArrayList<Task> tasks, String input) {
        try {
            String description = input.split("todo\\s+", 2)[1].trim();
            if (description.isEmpty()) {
                printError();
                return false;
            }

            Task task = new Todo(description);
            tasks.add(task);

            System.out.println("Added: " + task);
            System.out.println("Currently, we have " + tasks.size() + " task(s) on the list.");
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            printError();
            return false;
        }
    }

    private static boolean addDeadline(ArrayList<Task> tasks, String input) {
        try {
            String body = input.split("deadline\\s+", 2)[1];
            String[] parts = body.split("\\s*/by\\s*", 2);

            String description = parts[0].trim();
            String byRaw = parts[1].trim();

            if (description.isEmpty() || byRaw.isEmpty()) {
                printError();
                return false;
            }

            LocalDate byDate = LocalDate.parse(byRaw);
            Task task = new Deadline(description, byDate);
            tasks.add(task);

            System.out.println("Added: " + task);
            System.out.println("Currently, we have " + tasks.size() + " task(s) on the list.");
            return true;
        } catch (ArrayIndexOutOfBoundsException | DateTimeParseException e) {
            printError();
            return false;
        }
    }

    private static boolean addEvent(ArrayList<Task> tasks, String input) {
        try {
            String body = input.split("event\\s+", 2)[1];
            String[] first = body.split("\\s*/from\\s*", 2);
            String[] second = first[1].split("\\s*/to\\s*", 2);

            String description = first[0].trim();
            String fromRaw = second[0].trim();
            String toRaw = second[1].trim();

            if (description.isEmpty() || fromRaw.isEmpty() || toRaw.isEmpty()) {
                printError();
                return false;
            }

            LocalDateTime fromDateTime = LocalDateTime.parse(fromRaw, EVENT_INPUT_FORMAT);
            LocalDateTime toDateTime = LocalDateTime.parse(toRaw, EVENT_INPUT_FORMAT);

            if (!toDateTime.isAfter(fromDateTime)) {
                System.out.println("Error: /to must be after /from.");
                return false;
            }

            Task task = new Event(description, fromDateTime, toDateTime);
            tasks.add(task);

            System.out.println("Added: " + task);
            System.out.println("Currently, we have " + tasks.size() + " task(s) on the list.");
            return true;
        } catch (ArrayIndexOutOfBoundsException | DateTimeParseException e) {
            printError();
            return false;
        }
    }

    /**
     * Prints the ASCII logo and greeting message.
     */
    private static void printWelcome() {
        String logo =
                "             .@@@@@@@@@@@@@@@.\n"
                        + "          .@@@@@@@@@@@@@@@@@@@@@.\n"
                        + "        .@@@@@@@@@@@@@@@@@@@@@@@@@.\n"
                        + "       @@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                        + "      @@@@@@   (* )     ( *)   @@@@@@\n"
                        + "      @@@@@@       .  ^  .       @@@@@@\n"
                        + "       @@@@@@@      ' w '      @@@@@@@\n"
                        + "        @@@@@@@@.           .@@@@@@@@\n"
                        + "          \"@@@@@@@@@@@@@@@@@@@@@@@\"" + "\n"
                        + "      __  __   ____    ____  _   _  ___\n"
                        + "     |  \\/  | / __ \\  / ___|| | | ||_ _|\n"
                        + "     | |\\/| || |  | || |    | |_| | | |\n"
                        + "     | |  | || |__| || |___ |  _  | | |\n"
                        + "     |_|  |_| \\____/  \\____||_| |_||___|\n";

        printSeparator();
        System.out.print(logo);
        printSeparator();
        System.out.println("Hello I'm MOCHI, your cutest personal chatbot (˶˃ ᵕ ˂˶)");
        System.out.println("Is there anything I can help you with today?");
        printSeparator();
    }

    /**
     * Prints the list of accepted command formats.
     */
    private static void printHelp() {
        System.out.println("Please follow the following formats:");
        System.out.println("todo <task>");
        System.out.println("deadline <task> /by yyyy-MM-dd");
        System.out.println("event <task> /from yyyy-MM-dd HHmm /to yyyy-MM-dd HHmm");
        System.out.println("mark <number>");
        System.out.println("unmark <number>");
        System.out.println("delete <number>");
        System.out.println("list");
        System.out.println("bye");
        printSeparator();
    }

    /**
     * Prints a generic error message for invalid commands.
     */
    private static void printError() {
        System.out.println("Error, please follow the specified formats:");
        System.out.println("todo <task>");
        System.out.println("deadline <task> /by yyyy-MM-dd");
        System.out.println("event <task> /from yyyy-MM-dd HHmm /to yyyy-MM-dd HHmm");
        System.out.println("mark <number>");
        System.out.println("unmark <number>");
        System.out.println("delete <number>");
        System.out.println("list");
        System.out.println("bye");
    }

    /**
     * Prints the goodbye message.
     */
    private static void printGoodbye() {
        printSeparator();
        System.out.println("Bye-bye, Please come back soon দ্দി(˵ •̀ ᴗ - ˵ ) ✧!!!");
        printSeparator();
    }

    /**
     * Prints a horizontal line separator.
     */
    private static void printSeparator() {
        System.out.println(SEPARATOR);
    }
}