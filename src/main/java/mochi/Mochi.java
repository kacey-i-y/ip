package mochi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import mochi.task.Deadline;
import mochi.task.Event;
import mochi.task.Task;
import mochi.task.Todo;

/**
 * Mochi is a simple CLI chatbot that manages a list of tasks
 * (to-dos, deadlines, and events), with automatic save/load to disk.
 */
public class Mochi {
    private static final DateTimeFormatter EVENT_INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
    private static final String DATA_DIR_NAME = "data";
    private static final String SAVqE_FILE_NAME = "tasks.txt";
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String PIPE_SPLIT_REGEX = "\\s*\\|\\s*";

    /**
     * Supported user commands.
     */
    private enum Command {
        LIST, MARK, UNMARK, TODO, DEADLINE, EVENT, DELETE, BYE, UNKNOWN;

        /**
         * Parses the first token of the user input and maps it to a command.
         *
         * @param input The full user input line.
         * @return The parsed Command, or UNKNOWN if not recognized.
         */
        static Command from(String input) {
            if (input == null) {
                return UNKNOWN;
            }

            String trimmed = input.trim();
            if (trimmed.isEmpty()) {
                return UNKNOWN;
            }

            String first = trimmed.split("\\s+")[0].toLowerCase();
            return switch (first) {
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
     * Entry point of the program.
     *
     * @param args Command line arguments (unused).
     * @throws IOException If an I/O error occurs while reading user input.
     */
    public static void main(String[] args) throws IOException {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        printWelcome();
        ArrayList<Task> tasks = loadTasks();

        printHelp();

        while (true) {
            String input = console.readLine();
            if (input == null) {
                break;
            }

            Command command = Command.from(input);

            if (command == Command.BYE) {
                break;
            }

            printSeparator();
            boolean changed = handleCommand(command, input, tasks);

            if (changed) {
                saveTasks(tasks);
            }

            printSeparator();
        }

        printGoodbye();
    }

    /**
     * Handles a single user command.
     *
     * @param command Parsed command type.
     * @param input Raw user input.
     * @param tasks Task list in memory.
     * @return True if tasks were modified (should trigger saving), false otherwise.
     */
    private static boolean handleCommand(Command command, String input, ArrayList<Task> tasks) {
        return switch (command) {
            case LIST -> {
                printTasks(tasks);
                yield false;
            }
            case MARK -> {
                if (markTask(tasks, input, true)) {
                    yield true;
                }
                yield false;
            }
            case UNMARK -> {
                if (markTask(tasks, input, false)) {
                    yield true;
                }
                yield false;
            }
            case TODO -> {
                if (addTodo(tasks, input)) {
                    yield true;
                }
                yield false;
            }
            case DEADLINE -> {
                if (addDeadline(tasks, input)) {
                    yield true;
                }
                yield false;
            }
            case EVENT -> {
                if (addEvent(tasks, input)) {
                    yield true;
                }
                yield false;
            }
            case DELETE -> {
                if (deleteTask(tasks, input)) {
                    yield true;
                }
                yield false;
            }
            default -> {
                printError();
                yield false;
            }
        };
    }

    /**
     * Loads tasks from the save file, if present.
     * If the file is missing/corrupted, starts with an empty list.
     *
     * @return List of tasks loaded from disk.
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
                    Task task = parseTaskLine(line);
                    tasks.add(task);
                } catch (IllegalArgumentException e) {
                    System.out.println("Skipping corrupted save line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading save file: " + e.getMessage());
            System.out.println("Starting with an empty task list.");
            tasks.clear();
        }

        if (!tasks.isEmpty()) {
            System.out.println("Loaded " + tasks.size() + " task(s) from disk.");
        } else {
            System.out.println("No tasks loaded from disk.");
        }

        return tasks;
    }

    /**
     * Saves all tasks to disk by rewriting the full save file.
     *
     * @param tasks Task list to save.
     */
    private static void saveTasks(ArrayList<Task> tasks) {
        File file = getSaveFile();
        File dir = file.getParentFile();

        if (dir != null && !dir.exists()) {
            if (!dir.mkdirs()) {
                System.out.println("Failed to create data directory: " + dir.getPath());
                return;
            }
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
     * Parses one save-file line into a Task.
     *
     * Expected formats:
     * T | 1 | description
     * D | 0 | description | by
     * E | 0 | description | from | to
     *
     * @param line One line from save file.
     * @return Parsed Task.
     * @throws IllegalArgumentException If the line is malformed.
     */
    private static Task parseTaskLine(String line) {
        String[] parts = line.split(PIPE_SPLIT_REGEX);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Too few fields");
        }

        String type = parts[0].trim().toUpperCase();
        String doneField = parts[1].trim();

        Task task = switch (type) {
            case "T" -> new Todo(parts[2]);
            case "D" -> {
                if (parts.length < 4) {
                    throw new IllegalArgumentException("Deadline missing by");
                }
                yield new Deadline(parts[2], parts[3]);
            }
            case "E" -> {
                if (parts.length < 5) {
                    throw new IllegalArgumentException("Event missing from/to");
                }
                yield new Event(parts[2], parts[3], parts[4]);
            }
            default -> throw new IllegalArgumentException("Unknown task type: " + type);
        };

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
     * Returns the save file location (relative path, OS-independent).
     *
     * @return File object pointing to ./data/mochi.txt
     */
    private static File getSaveFile() {
        return new File(DATA_DIR_NAME, SAVE_FILE_NAME);
    }

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

    private static boolean markTask(ArrayList<Task> tasks, String input, boolean mark) {
        try {
            int index = Integer.parseInt(input.split("\\s+")[1]) - 1;
            Task task = tasks.get(index);

            if (mark) {
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
            String desc = input.split("todo\\s+", 2)[1].trim();
            if (desc.isEmpty()) {
                printError();
                return false;
            }

            Task task = new Todo(desc);
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

            String desc = parts[0].trim();
            String by = parts[1].trim();

            if (desc.isEmpty() || by.isEmpty()) {
                printError();
                return false;
            }

            Task task = new Deadline(desc, by);
            tasks.add(task);
            System.out.println("Added: " + task);
            System.out.println("Currently, we have " + tasks.size() + " task(s) on the list.");
            return true;
        } catch (RuntimeException e) {
            printError();
            return false;
        }
    }

    private static boolean addEvent(ArrayList<Task> tasks, String input) {
        try {
            String body = input.split("event\\s+", 2)[1];
            String[] first = body.split("\\s*/from\\s*", 2);
            String[] second = first[1].split("\\s*/to\\s*", 2);

            String desc = first[0].trim();
            String from = second[0].trim();
            String to = second[1].trim();

            if (desc.isEmpty() || from.isEmpty() || to.isEmpty()) {
                printError();
                return false;
            }

            Task task = new Event(desc, from, to);
            tasks.add(task);
            System.out.println("Added: " + task);
            System.out.println("Currently, we have " + tasks.size() + " task(s) on the list.");
            return true;
        } catch (RuntimeException e) {
            printError();
            return false;
        }
    }

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

    private static void printHelp() {
        System.out.println("Please follow the following formats:");
        System.out.println("todo <task>");
        System.out.println("deadline <task> /by <deadline>");
        System.out.println("event <task> /from <from> /to <to>");
        System.out.println("mark <number>");
        System.out.println("unmark <number>");
        System.out.println("delete <number>");
        System.out.println("list");
        System.out.println("bye");
        printSeparator();
    }

    private static void printError() {
        System.out.println("Error, please follow the specified formats:");
        System.out.println("todo <task>");
        System.out.println("deadline <task> /by <deadline>");
        System.out.println("event <task> /from <from> /to <to>");
        System.out.println("mark <number>");
        System.out.println("unmark <number>");
        System.out.println("delete <number>");
        System.out.println("list");
        System.out.println("bye");
    }

    private static void printGoodbye() {
        printSeparator();
        System.out.println("Bye-bye, Please come back soon ദ্দি(˵ •̀ ᴗ - ˵ ) ✧!!!");
        printSeparator();
    }

    private static void printSeparator() {
        System.out.println(SEPARATOR);
    }
}