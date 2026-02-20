package mochi.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import mochi.task.Deadline;
import mochi.task.Event;
import mochi.task.TaskList;
import mochi.task.Todo;

public class StorageTest {

    @TempDir
    Path tempDir;

    @Test
    public void saveAndLoad_roundTrip_success() throws IOException {
        Storage storage = new Storage(tempDir.toString(), "tasks.txt");

        TaskList list = new TaskList();
        list.add(new Todo("read"));
        list.add(new Deadline("submit", LocalDate.of(2026, 1, 30)));
        list.add(new Event("meeting",
                LocalDateTime.of(2026, 1, 30, 18, 0),
                LocalDateTime.of(2026, 1, 30, 19, 0)));

        storage.save(list);

        TaskList loaded = storage.load();
        assertEquals(3, loaded.size());
        assertEquals(list.get(0).toWrite(), loaded.get(0).toWrite());
        assertEquals(list.get(1).toWrite(), loaded.get(1).toWrite());
        assertEquals(list.get(2).toWrite(), loaded.get(2).toWrite());
    }

    @Test
    public void load_missingFile_returnsEmptyList() {
        Storage storage = new Storage(tempDir.toString(), "does_not_exist.txt");

        TaskList loaded = storage.load();
        assertTrue(loaded.isEmpty());
    }

    @Test
    public void loadCorruptedLines_skipsCorrupted_keepsValid() throws IOException {
        Storage storage = new Storage(tempDir.toString(), "tasks.txt");

        Path savePath = tempDir.resolve("tasks.txt");
        Files.writeString(savePath,
                // valid todo
                "T | 0 | read\n"
                        // corrupted (too few fields)
                        + "D | 1\n"
                        // valid deadline
                        + "D | 1 | submit | 2026-01-30\n"
                        // corrupted (bad date)
                        + "D | 0 | oops | 2026-99-99\n"
                        // valid event
                        + "E | 0 | meet | 2026-01-30 1800 | 2026-01-30 1900\n"
        );

        TaskList loaded = storage.load();
        assertEquals(3, loaded.size());
        assertTrue(loaded.get(0).toWrite().contains("T |"));
        assertTrue(loaded.get(1).toWrite().contains("D |"));
        assertTrue(loaded.get(2).toWrite().contains("E |"));
    }
}
