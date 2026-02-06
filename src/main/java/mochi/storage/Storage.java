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
    private static final String PIPE_SPLIT_REGEX = "\\s*\\|\\s*";
    private static final DateTimeFormatter EVENT_SAVE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    private final File saveFile;

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
     * Loads tasks from disk. If the file does not exist, returns an empty TaskList.
     * Corrupted lines are skipped.
     *
     * @return TaskList loaded from disk.
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
                    Task task = parseLine(line);
                    tasks.add(task);
                } catch (IllegalArgumentException e) {
                    // corrupted line: skip
                }
            }
        } catch (IOException e) {
            return new TaskList();
        }

        return tasks;
    }

    /**
     * Saves the given TaskList to disk by rewriting the entire file.
     * Creates the data folder if it does not exist.
     *
     * @param tasks TaskList to save.
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
        String[] parts = line.split(PIPE_SPLIT_REGEX);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Too few fields");
        }

        String type = parts[0].trim().toUpperCase();
        String done = parts[1].trim();

        Task task;
        try {
            task = switch (type) {
                case "T" -> new Todo(parts[2].trim());
                case "D" -> {
                    if (parts.length < 4) {
                        throw new IllegalArgumentException("Deadline missing by");
                    }
                    LocalDate byDate = LocalDate.parse(parts[3].trim()); // yyyy-MM-dd
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
                default -> throw new IllegalArgumentException("Unknown type");
            };
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Bad date/time format", e);
        }

        if ("1".equals(done)) {
            task.mark();
        } else if ("0".equals(done)) {
            task.unmark();
        } else {
            throw new IllegalArgumentException("Bad done flag");
        }

        return task;
    }
}
