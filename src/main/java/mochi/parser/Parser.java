package mochi.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import mochi.task.Deadline;
import mochi.task.Event;
import mochi.task.Task;
import mochi.task.Todo;

/**
 * Parses user input into structured commands for Mochi.
 *
 * <p>This class converts raw CLI strings (e.g. {@code "deadline read book /by 2026-01-30"})
 * into a {@link ParsedCommand} object that contains:
 * <ul>
 *   <li>a {@link Command} type</li>
 *   <li>an optional 0-based index for commands like mark/unmark/delete</li>
 *   <li>an optional {@link Task} object for commands that create tasks</li>
 *   <li>an optional keyword for {@code find} commands</li>
 * </ul>
 *
 * <p>Design note: This is a utility class (no instances). All parsing logic is centralized
 * here to keep UI and storage responsibilities separate.
 *
 * @author Kacey Isaiah Yonathan
 */
public class Parser {

    /** Date-time format accepted for event times: {@code yyyy-MM-dd HHmm}. */
    private static final DateTimeFormatter EVENT_INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /**
     * Prevents instantiation of this utility class.
     */
    private Parser() {
        // Utility class: prevent instantiation.
    }

    /**
     * Supported user commands recognized by the parser.
     */
    public enum Command {
        LIST, MARK, UNMARK, TODO, DEADLINE, EVENT, DELETE, FIND, BYE
    }

    /**
     * Represents a parsed command produced from user input.
     *
     * <p>Conventions:
     * <ul>
     *   <li>{@code index} is 0-based when used; otherwise {@code -1}</li>
     *   <li>{@code task} is non-null only for TODO/DEADLINE/EVENT; otherwise {@code null}</li>
     *   <li>{@code keyword} is non-null only for FIND; otherwise {@code null}</li>
     * </ul>
     *
     * @param command Parsed command type (never null).
     * @param index   0-based index for index-based commands, or -1 if not applicable.
     * @param task    Parsed task for add commands, or null if not applicable.
     * @param keyword Keyword for find command, or null if not applicable.
     */
    public record ParsedCommand(Command command, int index, Task task, String keyword) {
    }

    /**
     * Parses raw user input into a {@link ParsedCommand}.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code list}</li>
     *   <li>{@code mark 2}</li>
     *   <li>{@code todo borrow book}</li>
     *   <li>{@code deadline return book /by 2026-01-30}</li>
     *   <li>{@code event meeting /from 2026-01-30 1800 /to 2026-01-30 2000}</li>
     *   <li>{@code find book}</li>
     * </ul>
     *
     * @param input User input line.
     * @return Parsed command object.
     * @throws IllegalArgumentException If the input is empty, malformed, or unknown.
     */
    public static ParsedCommand parse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input is null");
        }

        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Input is empty");
        }

        String firstToken = trimmed.split("\\s+")[0].toLowerCase();

        return switch (firstToken) {
        case "list" -> new ParsedCommand(Command.LIST, -1, null, null);
        case "bye" -> new ParsedCommand(Command.BYE, -1, null, null);

        case "mark" -> new ParsedCommand(Command.MARK, parseIndex(trimmed), null, null);
        case "unmark" -> new ParsedCommand(Command.UNMARK, parseIndex(trimmed), null, null);
        case "delete" -> new ParsedCommand(Command.DELETE, parseIndex(trimmed), null, null);

        case "todo" -> new ParsedCommand(Command.TODO, -1, parseTodo(trimmed), null);
        case "deadline" -> new ParsedCommand(Command.DEADLINE, -1, parseDeadline(trimmed), null);
        case "event" -> new ParsedCommand(Command.EVENT, -1, parseEvent(trimmed), null);

        case "find" -> new ParsedCommand(Command.FIND, -1, null, parseFindKeyword(trimmed));

        default -> throw new IllegalArgumentException("Unknown command");
        };
    }

    /**
     * Parses a 1-based index from commands such as {@code "mark 2"} and returns it as 0-based.
     *
     * @param input Full user input line.
     * @return 0-based index (>= 0).
     * @throws IllegalArgumentException If index is missing, not a number, or <= 0.
     */
    private static int parseIndex(String input) {
        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid format");
            }

            int oneBased = Integer.parseInt(parts[1]);
            if (oneBased <= 0) {
                throw new IllegalArgumentException("Index must be >= 1");
            }

            return oneBased - 1;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Index must be a number", e);
        }
    }

    /**
     * Parses a {@code find} command keyword.
     *
     * <p>Format: {@code find <keyword>}
     *
     * @param input Full user input line.
     * @return Keyword string.
     * @throws IllegalArgumentException If keyword is missing/blank.
     */
    private static String parseFindKeyword(String input) {
        String[] parts = input.split("find\\s+", 2);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            throw new IllegalArgumentException("Find keyword missing");
        }
        return parts[1].trim();
    }

    /**
     * Parses a {@code todo} command.
     *
     * <p>Format: {@code todo <description>}
     *
     * @param input Full user input line.
     * @return A {@link Todo} task.
     * @throws IllegalArgumentException If description is missing/blank.
     */
    private static Task parseTodo(String input) {
        String[] parts = input.split("todo\\s+", 2);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            throw new IllegalArgumentException("Todo description missing");
        }

        return new Todo(parts[1].trim());
    }

    /**
     * Parses a {@code deadline} command.
     *
     * <p>Format: {@code deadline <description> /by <yyyy-MM-dd>}
     *
     * @param input Full user input line.
     * @return A {@link Deadline} task.
     * @throws IllegalArgumentException If format is invalid or date cannot be parsed.
     */
    private static Task parseDeadline(String input) {
        String[] parts = input.split("deadline\\s+", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Deadline body missing");
        }

        String[] bodyParts = parts[1].split("\\s*/by\\s*", 2);
        if (bodyParts.length < 2) {
            throw new IllegalArgumentException("Missing /by");
        }

        String description = bodyParts[0].trim();
        String byRaw = bodyParts[1].trim();

        if (description.isEmpty() || byRaw.isEmpty()) {
            throw new IllegalArgumentException("Deadline description/date missing");
        }

        try {
            LocalDate byDate = LocalDate.parse(byRaw); // yyyy-MM-dd
            return new Deadline(description, byDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Deadline date must be yyyy-MM-dd", e);
        }
    }

    /**
     * Parses an {@code event} command.
     *
     * <p>Format:
     * {@code event <description> /from <yyyy-MM-dd HHmm> /to <yyyy-MM-dd HHmm>}
     *
     * <p>Validation rule: {@code /to} must be strictly after {@code /from}.
     *
     * @param input Full user input line.
     * @return An {@link Event} task.
     * @throws IllegalArgumentException If format is invalid, date cannot be parsed,
     *                                  or {@code /to} is not after {@code /from}.
     */
    private static Task parseEvent(String input) {
        String[] parts = input.split("event\\s+", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Event body missing");
        }

        String[] first = parts[1].split("\\s*/from\\s*", 2);
        if (first.length < 2) {
            throw new IllegalArgumentException("Missing /from");
        }

        String[] second = first[1].split("\\s*/to\\s*", 2);
        if (second.length < 2) {
            throw new IllegalArgumentException("Missing /to");
        }

        String description = first[0].trim();
        String fromRaw = second[0].trim();
        String toRaw = second[1].trim();

        if (description.isEmpty() || fromRaw.isEmpty() || toRaw.isEmpty()) {
            throw new IllegalArgumentException("Event description/from/to missing");
        }

        try {
            LocalDateTime fromDateTime = LocalDateTime.parse(fromRaw, EVENT_INPUT_FORMAT);
            LocalDateTime toDateTime = LocalDateTime.parse(toRaw, EVENT_INPUT_FORMAT);

            if (!toDateTime.isAfter(fromDateTime)) {
                throw new IllegalArgumentException("/to must be after /from");
            }

            return new Event(description, fromDateTime, toDateTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Event date/time must be yyyy-MM-dd HHmm (e.g. 2026-01-30 1800)", e);
        }
    }
}
