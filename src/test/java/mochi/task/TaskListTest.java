package mochi.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class TaskListTest {

    @Test
    public void addAndGet_success() {
        TaskList list = new TaskList();
        Task t = new Todo("read book");

        list.add(t);

        assertEquals(1, list.size());
        assertEquals(t, list.get(0));
    }

    @Test
    public void remove_success() {
        TaskList list = new TaskList();
        list.add(new Todo("a"));
        list.add(new Todo("b"));

        Task removed = list.remove(0);

        assertEquals("[T] [ ] a", removed.toString());
        assertEquals(1, list.size());
        assertEquals("[T] [ ] b", list.get(0).toString());
    }

    @Test
    public void find_caseInsensitive_matches() {
        TaskList list = new TaskList();
        list.add(new Todo("Read Book"));
        list.add(new Deadline("submit report", LocalDate.of(2026, 1, 30)));
        list.add(new Event("BOOK club",
                LocalDateTime.of(2026, 1, 30, 18, 0),
                LocalDateTime.of(2026, 1, 30, 19, 0)));

        TaskList matches = list.find("book");

        assertEquals(2, matches.size());
        assertTrue(matches.get(0).toString().toLowerCase().contains("book"));
        assertTrue(matches.get(1).toString().toLowerCase().contains("book"));
    }

    @Test
    public void isEmpty_newList_true() {
        TaskList list = new TaskList();
        assertTrue(list.isEmpty());
    }

    @Test
    public void isEmpty_afterAdd_false() {
        TaskList list = new TaskList();
        list.add(new Todo("x"));
        assertFalse(list.isEmpty());
    }
}
