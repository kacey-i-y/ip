package mochi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import mochi.storage.Storage;

import mochi.task.Task;
import mochi.task.TaskList;

import mochi.parser.Parser;
import mochi.parser.Parser.ParsedCommand;

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

    /**
     * Runs the chatbot in a read-eval-print loop.
     *
     * @param args Command line arguments (unused).
     * @throws IOException If an I/O error occurs while reading user input.
     */
    public static void main(String[] args) throws IOException {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        Storage storage = new Storage(DATA_DIR_NAME, SAVE_FILE_NAME);

        printWelcome();

        TaskList taskList = storage.load();
        if (taskList.size() == 0) {
            System.out.println("No tasks loaded from disk.");
        } else {
            System.out.println("Loaded " + taskList.size() + " task(s) from disk.");
        }

        printHelp();

        while (true) {
            String userInput = consoleReader.readLine();
            if (userInput == null) {
                break;
            }

            ParsedCommand parsed;
            try {
                parsed = Parser.parse(userInput);
            } catch (IllegalArgumentException e) {
                printSeparator();
                printError();
                printSeparator();
                continue;
            }

            if (parsed.command() == Parser.Command.BYE) {
                break;
            }

            printSeparator();

            boolean hasChanged = handleCommand(parsed, taskList);
            if (hasChanged) {
                try {
                    storage.save(taskList);
                } catch (IOException e) {
                    System.out.println("Error saving tasks: " + e.getMessage());
                }
            }

            printSeparator();
        }

        printGoodbye();
    }


    private static boolean handleCommand(ParsedCommand parsed, TaskList tasks) {
        return switch (parsed.command()) {
            case LIST -> {
                printTasks(tasks);
                yield false;
            }
            case MARK -> markTask(tasks, parsed.index(), true);
            case UNMARK -> markTask(tasks, parsed.index(), false);
            case DELETE -> deleteTask(tasks, parsed.index());

            case TODO, DEADLINE, EVENT -> {
                tasks.add(parsed.task());
                System.out.println("Added: " + parsed.task());
                System.out.println("Currently, we have " + tasks.size() + " task(s) on the list.");
                yield true;
            }

            case BYE -> false; // already handled in main loop
        };
    }

    /**
     * Prints all tasks with 1-based indexing.
     *
     * @param tasks Task list to print.
     */
    private static void printTasks(TaskList tasks) {
        if (tasks.isEmpty()) {
            System.out.println("Your task list is empty.");
            return;
        }

        System.out.println("The following tasks are listed in the task list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + ". " + tasks.get(i));
        }
    }

    private static boolean markTask(TaskList tasks, int index, boolean shouldMark) {
        try {
            Task task = tasks.get(index);

            if (shouldMark) {
                task.mark();
                System.out.println("Marked as done: " + task);
            } else {
                task.unmark();
                System.out.println("Marked as not done: " + task);
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            printError();
            return false;
        }
    }

    private static boolean deleteTask(TaskList tasks, int index) {
        try {
            Task removed = tasks.remove(index);

            System.out.println("Removed: " + removed);
            System.out.println("Currently, we have " + tasks.size() + " task(s) on the list.");
            return true;
        } catch (IndexOutOfBoundsException e) {
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