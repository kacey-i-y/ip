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
 *   <li>an optional keyword for {@code find}</li>
 * </ul>
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
        LIST, SORT, MARK, UNMARK, TODO, DEADLINE, EVENT, DELETE, FIND, HELP, BYE
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
        case "sort" -> new ParsedCommand(Command.SORT, -1, null, null);
        case "bye" -> new ParsedCommand(Command.BYE, -1, null, null);

        case "mark" -> new ParsedCommand(Command.MARK, parseIndex(trimmed), null, null);
        case "unmark" -> new ParsedCommand(Command.UNMARK, parseIndex(trimmed), null, null);
        case "delete" -> new ParsedCommand(Command.DELETE, parseIndex(trimmed), null, null);

        case "todo" -> new ParsedCommand(Command.TODO, -1, parseTodo(trimmed), null);
        case "deadline" -> new ParsedCommand(Command.DEADLINE, -1, parseDeadline(trimmed), null);
        case "event" -> new ParsedCommand(Command.EVENT, -1, parseEvent(trimmed), null);

        case "find" -> new ParsedCommand(Command.FIND, -1, null, parseFindKeyword(trimmed));

        case "help" -> new ParsedCommand(Command.HELP, -1, null, null);

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
     * Parses the keyword portion of a {@code find} command.
     *
     * @param input Full user input line.
     * @return Keyword string.
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
     * @param input Full user input line.
     * @return A {@link Todo} task.
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
     * @param input Full user input line.
     * @return A {@link Deadline} task.
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
     * <p>Expected format:
     * {@code event <description> /from <yyyy-MM-dd HHmm> /to <yyyy-MM-dd HHmm>}
     *
     * @param input Full user input line.
     * @return An {@link Event} task.
     * @throws IllegalArgumentException If required segments are missing, date/time cannot be parsed,
     *                                  or {@code /to} is not after {@code /from}.
     */
    private static Task parseEvent(String input) {
        String body = extractBody(input, "event", "Event body missing");

        String description = extractDescription(body);
        String fromRaw = extractSegment(body, "/from", "/to", "Missing /from", "Missing /to");
        String toRaw = extractAfter(body, "/to", "Missing /to");

        validateEventFields(description, fromRaw, toRaw);

        LocalDateTime fromDateTime = parseEventDateTime(fromRaw);
        LocalDateTime toDateTime = parseEventDateTime(toRaw);

        validateEventTimeRange(fromDateTime, toDateTime);

        return new Event(description, fromDateTime, toDateTime);
    }

    /**
     * Extracts the body after a given command keyword.
     *
     * @param input Full user input.
     * @param command Keyword such as {@code "event"}.
     * @param errorMessage Error message if the body is missing.
     * @return Command body string (trimmed).
     * @throws IllegalArgumentException If the body is missing.
     */
    private static String extractBody(String input, String command, String errorMessage) {
        String[] parts = input.split(command + "\\s+", 2);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return parts[1].trim();
    }

    /**
     * Extracts the event description (text before {@code /from}).
     *
     * @param body Event command body.
     * @return Description string (trimmed, may be empty).
     */
    private static String extractDescription(String body) {
        String[] parts = body.split("\\s*/from\\s*", 2);
        return parts[0].trim();
    }

    /**
     * Extracts the substring between two markers, validating that both exist.
     *
     * @param body Command body.
     * @param startMarker Marker to start after (e.g. {@code "/from"}).
     * @param endMarker Marker to stop before (e.g. {@code "/to"}).
     * @param missingStartMessage Error message if {@code startMarker} is missing.
     * @param missingEndMessage Error message if {@code endMarker} is missing.
     * @return The trimmed substring between markers.
     * @throws IllegalArgumentException If either marker is missing.
     */
    private static String extractSegment(
            String body,
            String startMarker,
            String endMarker,
            String missingStartMessage,
            String missingEndMessage
    ) {
        String[] first = body.split("\\s*" + java.util.regex.Pattern.quote(startMarker) + "\\s*", 2);
        if (first.length < 2) {
            throw new IllegalArgumentException(missingStartMessage);
        }

        String[] second = first[1].split("\\s*" + java.util.regex.Pattern.quote(endMarker) + "\\s*", 2);
        if (second.length < 2) {
            throw new IllegalArgumentException(missingEndMessage);
        }

        return second[0].trim();
    }

    /**
     * Extracts the substring after a marker.
     *
     * @param body Command body.
     * @param marker Marker such as {@code "/to"}.
     * @param missingMessage Error message if marker is missing or value is blank.
     * @return The trimmed substring after the marker.
     * @throws IllegalArgumentException If marker is missing or value is blank.
     */
    private static String extractAfter(String body, String marker, String missingMessage) {
        String[] parts = body.split("\\s*" + java.util.regex.Pattern.quote(marker) + "\\s*", 2);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            throw new IllegalArgumentException(missingMessage);
        }
        return parts[1].trim();
    }

    /**
     * Validates that description, from, and to fields are all present.
     *
     * @param description Event description.
     * @param fromRaw Raw {@code /from} text.
     * @param toRaw Raw {@code /to} text.
     * @throws IllegalArgumentException If any field is blank.
     */
    private static void validateEventFields(String description, String fromRaw, String toRaw) {
        if (description.isEmpty() || fromRaw.isEmpty() || toRaw.isEmpty()) {
            throw new IllegalArgumentException("Event description/from/to missing");
        }
    }

    /**
     * Parses an event date-time using {@code EVENT_INPUT_FORMAT}.
     *
     * @param raw Date-time string in {@code yyyy-MM-dd HHmm}.
     * @return Parsed {@link LocalDateTime}.
     * @throws IllegalArgumentException If parsing fails.
     */
    private static LocalDateTime parseEventDateTime(String raw) {
        try {
            return LocalDateTime.parse(raw, EVENT_INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Event date/time must be yyyy-MM-dd HHmm (e.g. 2026-01-30 1800)", e);
        }
    }

    /**
     * Validates that {@code to} is strictly after {@code from}.
     *
     * @param from Start date-time.
     * @param to End date-time.
     * @throws IllegalArgumentException If {@code to} is not after {@code from}.
     */
    private static void validateEventTimeRange(LocalDateTime from, LocalDateTime to) {
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("/to must be after /from");
        }
    }
}
