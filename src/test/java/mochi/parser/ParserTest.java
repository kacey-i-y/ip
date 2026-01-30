package mochi.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import mochi.task.Deadline;
import mochi.task.Event;
import mochi.task.Todo;

public class ParserTest {

    @Test
    public void parse_todo_success() {
        Parser.ParsedCommand cmd = Parser.parse("todo read book");

        assertEquals(Parser.Command.TODO, cmd.command());
        assertEquals(-1, cmd.index());
        assertTrue(cmd.task() instanceof Todo);
        assertEquals("[T] [ ] read book", cmd.task().toString());
    }

    @Test
    public void parse_deadline_success() {
        Parser.ParsedCommand cmd = Parser.parse("deadline submit /by 2026-01-30");

        assertEquals(Parser.Command.DEADLINE, cmd.command());
        assertTrue(cmd.task() instanceof Deadline);
        assertTrue(cmd.task().toWrite().contains("D |"));
        assertTrue(cmd.task().toWrite().contains("2026-01-30"));
    }

    @Test
    public void parse_event_invalidTimeRange_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                Parser.parse("event meet /from 2026-01-30 1800 /to 2026-01-30 1700"));
    }

    @Test
    public void parse_mark_invalidIndex_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                Parser.parse("mark 0"));
    }

    @Test
    public void parse_unknownCommand_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                Parser.parse("blah blah"));
    }
}