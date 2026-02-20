package mochi.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class TaskFormatTest {

    @Test
    public void todo_toStringAndToWrite_formatCorrect() {
        Todo t = new Todo("read book");
        assertEquals("[T] [ ] read book", t.toString());
        assertTrue(t.toWrite().startsWith("T | "));
    }

    @Test
    public void deadline_toWrite_containsIsoDate() {
        Deadline d = new Deadline("submit", LocalDate.of(2026, 1, 30));
        assertTrue(d.toWrite().contains("2026-01-30"));
        assertTrue(d.toWrite().startsWith("D | "));
    }

    @Test
    public void event_toWrite_containsFormattedDateTime() {
        Event e = new Event("meet",
                LocalDateTime.of(2026, 1, 30, 18, 0),
                LocalDateTime.of(2026, 1, 30, 19, 0));

        String saved = e.toWrite();
        assertTrue(saved.startsWith("E | "));
        assertTrue(saved.contains("2026-01-30 1800"));
        assertTrue(saved.contains("2026-01-30 1900"));
    }

    @Test
    public void markAndUnmark_changesStatus() {
        Todo t = new Todo("x");
        assertEquals("[T] [ ] x", t.toString());

        t.mark();
        assertEquals("[T] [X] x", t.toString());

        t.unmark();
        assertEquals("[T] [ ] x", t.toString());
    }
}
