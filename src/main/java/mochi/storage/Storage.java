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
 * Handles persistence of {@link Task} objects by loading from and saving to disk.
 *
 * <p>The save file is stored at {@code ./<dataDirName>/<fileName>} and each task is stored
 * on its own line using a pipe-delimited format:
 * <ul>
 *   <li>{@code T | 1 | description}</li>
 *   <li>{@code D | 0 | description | yyyy-MM-dd}</li>
 *   <li>{@code E | 0 | description | yyyy-MM-dd HHmm | yyyy-MM-dd HHmm}</li>
 * </ul>
 *
 * <p>Corrupted or malformed lines are skipped during loading to keep the application robust.
 *
 * @author Kacey Isaiah Yonathan
 */
public class Storage {

    /** Regex used to split save-file fields separated by {@code |}, allowing surrounding spaces. */
    private static final String PIPE_SPLIT_REGEX = "\\s*\\|\\s*";

    /** Date/time format used when reading and writing event start/end values. */
    private static final DateTimeFormatter EVENT_SAVE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    private final File saveFile;

    /**
     * Creates a {@code Storage} that reads from and writes to {@code ./<dataDirName>/<fileName>}.
     *
     * @param dataDirName Directory name (relative to project root).
     * @param fileName    Save file name.
     */
    public Storage(String dataDirName, String fileName) {
        this.saveFile = new File(dataDirName, fileName);
    }

    /**
     * Loads tasks from disk into a {@link TaskList}.
     *
     * <p>If the save file does not exist, an empty task list is returned.
     * Any corrupted/malformed lines are skipped.
     *
     * @return Task list loaded from disk.
     */
    public TaskList load() {
        TaskList tasks = new TaskList();

        if (!saveFile.exists()) {
            return tasks;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    tasks.add(parseLine(line));
                } catch (IllegalArgumentException e) {
                    // Ignore corrupted lines to keep loading resilient.
                }
            }
        } catch (IOException e) {
            // If read fails, return empty rather than crashing.
            return new TaskList();
        }

        return tasks;
    }

    /**
     * Saves the given task list to disk by rewriting the entire save file.
     *
     * <p>If the parent directory does not exist, it will be created.
     *
     * @param tasks Task list to save.
     * @throws IOException If writing fails.
     */
    public void save(TaskList tasks) throws IOException {
        File dir = saveFile.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            for (int i = 0; i < tasks.size(); i++) {
                writer.write(tasks.get(i).toWrite());
                writer.newLine();
            }
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
     * <p>Event validation: {@code /to} must be strictly after {@code /from}.
     *
     * @param line One line from the save file.
     * @return Parsed task.
     * @throws IllegalArgumentException If the line is malformed or contains invalid data.
     */
    private Task parseLine(String line) {
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
                        LocalDateTime.parse(parts[3].trim(), EVENT_SAVE_FORMAT);
                LocalDateTime toDateTime =
                        LocalDateTime.parse(parts[4].trim(), EVENT_SAVE_FORMAT);

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

        applyDoneFlag(task, doneField);
        return task;
    }

    /**
     * Applies the done flag ({@code 1} or {@code 0}) to the given task.
     *
     * @param task The task to update.
     * @param doneField Done flag string.
     * @throws IllegalArgumentException If {@code doneField} is not {@code 0} or {@code 1}.
     */
    private static void applyDoneFlag(Task task, String doneField) {
        if ("1".equals(doneField)) {
            task.mark();
            return;
        }

        if ("0".equals(doneField)) {
            task.unmark();
            return;
        }

        throw new IllegalArgumentException("Bad done flag: " + doneField);
    }
}
