package mochi.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import mochi.task.Deadline;
import mochi.task.Event;
import mochi.task.Task;
import mochi.task.Todo;

public class Parser {
    private static final DateTimeFormatter EVENT_INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    private Parser() {
        // Utility class: prevent instantiation.
    }

    public enum Command {
        LIST, MARK, UNMARK, TODO, DEADLINE, EVENT, DELETE, BYE;
    }

    public record ParsedCommand(Command command, int index, Task task) {
    }

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
            case "list" -> new ParsedCommand(Command.LIST, -1, null);
            case "bye" -> new ParsedCommand(Command.BYE, -1, null);
            case "mark" -> new ParsedCommand(Command.MARK, parseIndex(trimmed), null);
            case "unmark" -> new ParsedCommand(Command.UNMARK, parseIndex(trimmed), null);
            case "delete" -> new ParsedCommand(Command.DELETE, parseIndex(trimmed), null);
            case "todo" -> new ParsedCommand(Command.TODO, -1, parseTodo(trimmed));
            case "deadline" -> new ParsedCommand(Command.DEADLINE, -1, parseDeadline(trimmed));
            case "event" -> new ParsedCommand(Command.EVENT, -1, parseEvent(trimmed));
            default -> throw new IllegalArgumentException("Unknown command");
        };
    }

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

    private static Task parseTodo(String input) {
        String[] parts = input.split("todo\\s+", 2);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            throw new IllegalArgumentException("Todo description missing");
        }
        return new Todo(parts[1].trim());
    }

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
