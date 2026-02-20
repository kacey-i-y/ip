package mochi.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import mochi.task.Deadline;
import mochi.task.Event;
import mochi.task.Task;
import mochi.task.Todo;

/**
 * Parses user input into structured commands for Mochi.
 *
 * <p>This class converts raw CLI strings (e.g. {@code "deadline read book /by 2026-01-30"})
 * into a {@link ParsedCommand} object.
 */
public class Parser {

    /** Date-time format accepted for event times: {@code yyyy-MM-dd HHmm}. */
    private static final DateTimeFormatter EVENT_INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /** Prevents instantiation of this utility class. */
    private Parser() {
        // Utility class: prevent instantiation.
    }

    /** Supported user commands recognized by the parser. */
    public enum Command {
        LIST, SORT, MARK, UNMARK, TODO, DEADLINE, EVENT, DELETE, FIND, HELP, BYE
    }

    /**
     * Represents a parsed command produced from user input.
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
        String trimmed = requireNonBlank(input, "Input is empty");
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
     * Ensures the input is non-null and non-blank.
     *
     * @param value Raw string.
     * @param message Error message if invalid.
     * @return Trimmed non-blank string.
     * @throws IllegalArgumentException If null or blank.
     */
    private static String requireNonBlank(String value, String message) {
        if (value == null) {
            throw new IllegalArgumentException("Input is null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }

    /**
     * Parses a 1-based index from commands such as {@code "mark 2"} and returns it as 0-based.
     *
     * @param input Full user input line.
     * @return 0-based index (>= 0).
     * @throws IllegalArgumentException If index is missing, not a number, or <= 0.
     */
    private static int parseIndex(String input) {
        String[] parts = input.split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid format");
        }

        int oneBased = parsePositiveInt(parts[1], "Index must be a number");
        if (oneBased <= 0) {
            throw new IllegalArgumentException("Index must be >= 1");
        }

        return oneBased - 1;
    }

    /**
     * Parses a positive integer from a string.
     *
     * @param raw Raw number string.
     * @param errorMessage Error message if parsing fails.
     * @return Parsed integer.
     * @throws IllegalArgumentException If not a valid integer.
     */
    private static int parsePositiveInt(String raw, String errorMessage) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage, e);
        }
    }

    /**
     * Parses the keyword portion of a {@code find} command.
     *
     * @param input Full user input line.
     * @return Keyword string.
     * @throws IllegalArgumentException If the keyword is missing.
     */
    private static String parseFindKeyword(String input) {
        return extractBody(input, "find", "Find keyword missing");
    }

    /**
     * Parses a {@code todo} command.
     *
     * @param input Full user input line.
     * @return A {@link Todo} task.
     */
    private static Task parseTodo(String input) {
        String description = extractBody(input, "todo", "Todo description missing");
        return new Todo(description);
    }

    /**
     * Parses a {@code deadline} command.
     *
     * @param input Full user input line.
     * @return A {@link Deadline} task.
     */
    private static Task parseDeadline(String input) {
        String body = extractBody(input, "deadline", "Deadline body missing");

        String description = extractBefore(body, "/by", "Missing /by").trim();
        String byRaw = extractAfter(body, "/by", "Missing /by").trim();

        requireNonBlank(description, "Deadline description/date missing");
        requireNonBlank(byRaw, "Deadline description/date missing");

        LocalDate byDate = parseDate(byRaw);
        return new Deadline(description, byDate);
    }

    /**
     * Parses an {@code event} command.
     *
     * @param input Full user input line.
     * @return An {@link Event} task.
     */
    private static Task parseEvent(String input) {
        EventFields fields = parseEventFields(input);
        LocalDateTime from = parseEventDateTime(fields.fromRaw());
        LocalDateTime to = parseEventDateTime(fields.toRaw());
        validateEventTimeRange(from, to);
        return new Event(fields.description(), from, to);
    }

    /**
     * Parses and validates the raw pieces required to construct an event.
     *
     * @param input Full user input line.
     * @return Parsed event fields (description, from, to).
     * @throws IllegalArgumentException If required segments are missing.
     */
    private static EventFields parseEventFields(String input) {
        String body = extractBody(input, "event", "Event body missing");

        String description = extractBefore(body, "/from", "Missing /from").trim();
        String fromRaw = extractBetween(body, "/from", "/to", "Missing /from", "Missing /to").trim();
        String toRaw = extractAfter(body, "/to", "Missing /to").trim();

        requireNonBlank(description, "Event description/from/to missing");
        requireNonBlank(fromRaw, "Event description/from/to missing");
        requireNonBlank(toRaw, "Event description/from/to missing");

        return new EventFields(description, fromRaw, toRaw);
    }

    /**
     * Container for raw event inputs extracted from the command.
     *
     * @param description Event description.
     * @param fromRaw Raw {@code /from} value.
     * @param toRaw Raw {@code /to} value.
     */
    private record EventFields(String description, String fromRaw, String toRaw) {
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
     * Extracts the substring before a marker.
     *
     * @param body Command body.
     * @param marker Marker such as {@code "/from"}.
     * @param missingMessage Error message if marker is missing.
     * @return Substring before the marker.
     * @throws IllegalArgumentException If marker is missing.
     */
    private static String extractBefore(String body, String marker, String missingMessage) {
        String[] parts = body.split("\\s*" + Pattern.quote(marker) + "\\s*", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException(missingMessage);
        }
        return parts[0];
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
        String[] parts = body.split("\\s*" + Pattern.quote(marker) + "\\s*", 2);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            throw new IllegalArgumentException(missingMessage);
        }
        return parts[1];
    }

    /**
     * Extracts the substring between two markers.
     *
     * @param body Command body.
     * @param startMarker Marker to start after (e.g. {@code "/from"}).
     * @param endMarker Marker to stop before (e.g. {@code "/to"}).
     * @param missingStartMessage Error message if {@code startMarker} is missing.
     * @param missingEndMessage Error message if {@code endMarker} is missing.
     * @return Substring between the markers.
     * @throws IllegalArgumentException If either marker is missing.
     */
    private static String extractBetween(
            String body,
            String startMarker,
            String endMarker,
            String missingStartMessage,
            String missingEndMessage
    ) {
        String afterStart = extractAfter(body, startMarker, missingStartMessage);
        String[] parts = afterStart.split("\\s*" + Pattern.quote(endMarker) + "\\s*", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException(missingEndMessage);
        }
        return parts[0];
    }

    /**
     * Parses a deadline date in ISO-8601 {@code yyyy-MM-dd}.
     *
     * @param raw Date string.
     * @return Parsed {@link LocalDate}.
     * @throws IllegalArgumentException If parsing fails.
     */
    private static LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Deadline date must be yyyy-MM-dd", e);
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
