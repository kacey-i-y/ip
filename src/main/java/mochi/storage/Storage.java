package mochi.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import mochi.task.Deadline;
import mochi.task.Event;
import mochi.task.Task;
import mochi.task.TaskList;
import mochi.task.Todo;

/**
 * Handles loading tasks from disk and saving tasks to disk.
 */
public class Storage {

    /** Regex used to split saved lines by '|' with optional surrounding whitespace. */
    private static final String PIPE_SPLIT_REGEX = "\\s*\\|\\s*";

    /** Date-time format used when persisting event times. */
    private static final DateTimeFormatter EVENT_SAVE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /** Save file location. */
    private final File saveFile;

    /** Message describing the last load status (null if load succeeded with no message). */
    private String lastLoadMessage;

    /**
     * Creates a Storage that saves to {@code ./<dataDirName>/<fileName>}.
     *
     * @param dataDirName Directory name (relative to project root).
     * @param fileName Save file name.
     */
    public Storage(String dataDirName, String fileName) {
        this.saveFile = new File(dataDirName, fileName);
    }

    /**
     * Loads tasks from disk.
     *
     * <p>If the file does not exist, returns an empty list and sets a user-facing message.
     * If reading fails, returns an empty list and sets a user-facing message.
     * Corrupted lines are skipped.
     *
     * @return TaskList loaded from disk (possibly empty).
     */
    public TaskList load() {
        resetLastLoadMessage();

        if (!saveFile.exists()) {
            return emptyWithMessage(missingFileMessage());
        }

        return readTasksFromFile();
    }

    /**
     * Saves the given TaskList to disk by rewriting the entire file.
     * Creates the data folder if it does not exist.
     *
     * @param tasks TaskList to save.
     * @throws IOException If writing fails.
     */
    public void save(TaskList tasks) throws IOException {
        assert tasks != null : "Cannot save a null TaskList";

        ensureParentDirectoryExists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            writeAllTasks(tasks, writer);
        }
    }

    /**
     * Returns the status message recorded during the last load attempt.
     *
     * @return Message string if a load warning/error occurred; {@code null} otherwise.
     */
    public String getLastLoadMessage() {
        return lastLoadMessage;
    }

    /**
     * Resets the recorded load message for a new load attempt.
     */
    private void resetLastLoadMessage() {
        lastLoadMessage = null;
    }

    /**
     * Reads tasks from the save file and returns them as a TaskList.
     *
     * <p>Corrupted lines are skipped. If an IOException occurs, returns an empty list and
     * sets {@link #lastLoadMessage}.
     *
     * @return TaskList read from file (possibly empty).
     */
    private TaskList readTasksFromFile() {
        TaskList tasks = new TaskList();

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                addIfParsable(tasks, line);
            }
        } catch (IOException e) {
            return emptyWithMessage(unreadableFileMessage());
        }

        return tasks;
    }

    /**
     * Attempts to parse a saved line and add it to the task list.
     *
     * @param tasks Task list to add into.
     * @param line Raw line from save file.
     */
    private void addIfParsable(TaskList tasks, String line) {
        try {
            tasks.add(parseLine(line));
        } catch (IllegalArgumentException e) {
            // corrupted line: skip
        }
    }

    /**
     * Parses one line from the save file into a Task.
     *
     * <p>Expected formats:
     * <ul>
     *   <li>{@code T | 1 | description}</li>
     *   <li>{@code D | 0 | description | yyyy-MM-dd}</li>
     *   <li>{@code E | 0 | description | yyyy-MM-dd HHmm | yyyy-MM-dd HHmm}</li>
     * </ul>
     *
     * @param line Save file line.
     * @return Parsed Task.
     * @throws IllegalArgumentException If the line is malformed.
     */
    private Task parseLine(String line) {
        String[] parts = splitLine(line);

        String type = parseType(parts);
        String done = parseDoneFlag(parts);

        Task task = parseTask(parts, type);
        applyDoneFlag(task, done);

        return task;
    }

    /**
     * Splits a save-file line into fields.
     *
     * @param line Save file line.
     * @return Array of split fields.
     * @throws IllegalArgumentException If the line has too few fields.
     */
    private static String[] splitLine(String line) {
        String[] parts = line.split(PIPE_SPLIT_REGEX);
        requireMinFields(parts, 3, "Too few fields");
        return parts;
    }

    /**
     * Extracts and normalizes the task type token from a saved line.
     *
     * @param parts Split fields from the save file line.
     * @return Uppercased task type token.
     */
    private static String parseType(String[] parts) {
        return parts[0].trim().toUpperCase();
    }

    /**
     * Extracts the done flag token from a saved line.
     *
     * @param parts Split fields from the save file line.
     * @return Raw done flag string ("0" or "1").
     */
    private static String parseDoneFlag(String[] parts) {
        return parts[1].trim();
    }

    /**
     * Applies the done flag ("0" or "1") to the given task.
     *
     * @param task Task to update.
     * @param done Done flag string.
     * @throws IllegalArgumentException If the done flag is not "0" or "1".
     */
    private static void applyDoneFlag(Task task, String done) {
        if ("1".equals(done)) {
            task.mark();
            return;
        }

        if ("0".equals(done)) {
            task.unmark();
            return;
        }

        throw new IllegalArgumentException("Bad done flag");
    }

    /**
     * Parses the task portion of a saved line based on the task type.
     *
     * @param parts Split fields from the save file line.
     * @param type  Task type token (e.g. "T", "D", "E").
     * @return Parsed {@link Task}.
     * @throws IllegalArgumentException If the type is unknown or fields are invalid.
     */
    private static Task parseTask(String[] parts, String type) {
        return switch (type) {
        case "T" -> parseTodo(parts);
        case "D" -> parseDeadline(parts);
        case "E" -> parseEvent(parts);
        default -> throw new IllegalArgumentException("Unknown type");
        };
    }

    /**
     * Parses a Todo task from the given fields.
     *
     * @param parts Split fields from the save file line.
     * @return A {@link Todo}.
     * @throws IllegalArgumentException If required fields are missing.
     */
    private static Task parseTodo(String[] parts) {
        requireMinFields(parts, 3, "Todo missing description");
        return new Todo(parts[2].trim());
    }

    /**
     * Parses a Deadline task from the given fields.
     *
     * @param parts Split fields from the save file line.
     * @return A {@link Deadline}.
     * @throws IllegalArgumentException If required fields are missing or date is invalid.
     */
    private static Task parseDeadline(String[] parts) {
        requireMinFields(parts, 4, "Deadline missing by");

        String description = parts[2].trim();
        LocalDate byDate = parseLocalDate(parts[3].trim());

        return new Deadline(description, byDate);
    }

    /**
     * Parses an Event task from the given fields.
     *
     * @param parts Split fields from the save file line.
     * @return An {@link Event}.
     * @throws IllegalArgumentException If required fields are missing, date/time is invalid,
     *                                  or the time range is not valid.
     */
    private static Task parseEvent(String[] parts) {
        requireMinFields(parts, 5, "Event missing from/to");

        String description = parts[2].trim();
        LocalDateTime fromDateTime = parseLocalDateTime(parts[3].trim());
        LocalDateTime toDateTime = parseLocalDateTime(parts[4].trim());

        validateEventTimeRange(fromDateTime, toDateTime);
        return new Event(description, fromDateTime, toDateTime);
    }

    /**
     * Ensures the saved line has the minimum number of fields.
     *
     * @param parts     Split fields from the save file line.
     * @param minLength Minimum required length.
     * @param message   Error message if validation fails.
     * @throws IllegalArgumentException If {@code parts.length < minLength}.
     */
    private static void requireMinFields(String[] parts, int minLength, String message) {
        if (parts.length < minLength) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Parses a date in {@code yyyy-MM-dd} format.
     *
     * @param raw Date string.
     * @return Parsed {@link LocalDate}.
     * @throws IllegalArgumentException If the date cannot be parsed.
     */
    private static LocalDate parseLocalDate(String raw) {
        try {
            return LocalDate.parse(raw); // yyyy-MM-dd
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Bad date/time format", e);
        }
    }

    /**
     * Parses an event date-time using {@code EVENT_SAVE_FORMAT}.
     *
     * @param raw Date-time string.
     * @return Parsed {@link LocalDateTime}.
     * @throws IllegalArgumentException If the date-time cannot be parsed.
     */
    private static LocalDateTime parseLocalDateTime(String raw) {
        try {
            return LocalDateTime.parse(raw, EVENT_SAVE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Bad date/time format", e);
        }
    }

    /**
     * Validates that an event end time is strictly after the start time.
     *
     * @param from Start date-time.
     * @param to   End date-time.
     * @throws IllegalArgumentException If {@code to} is not after {@code from}.
     */
    private static void validateEventTimeRange(LocalDateTime from, LocalDateTime to) {
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("Event end must be after start");
        }
    }

    /**
     * Ensures the save file's parent directory exists if a parent directory is present.
     */
    private void ensureParentDirectoryExists() {
        File dir = saveFile.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Writes all tasks in the list to the given writer, one task per line.
     *
     * @param tasks Task list to write.
     * @param writer Writer to output to.
     * @throws IOException If writing fails.
     */
    private static void writeAllTasks(TaskList tasks, BufferedWriter writer) throws IOException {
        for (int i = 0; i < tasks.size(); i++) {
            writer.write(tasks.get(i).toWrite());
            writer.newLine();
        }
    }

    /**
     * Returns an empty task list and records the provided load message.
     *
     * @param message Load status message.
     * @return A new empty TaskList.
     */
    private TaskList emptyWithMessage(String message) {
        lastLoadMessage = message;
        return new TaskList();
    }

    /**
     * Builds the user-facing message for a missing save file.
     *
     * @return Missing file message.
     */
    private String missingFileMessage() {
        return "I can't find " + saveFile.getPath()
                + ". Starting with an empty task list.";
    }

    /**
     * Builds the user-facing message for an unreadable save file.
     *
     * @return Unreadable file message.
     */
    private String unreadableFileMessage() {
        return "I couldn't read " + saveFile.getPath()
                + ". Starting with an empty task list.";
    }
}
