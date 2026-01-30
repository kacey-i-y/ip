package mochi.task;

/**
 * Represents a task without a date/time attached to it.
 */
public class Todo extends Task {

    /**
     * Creates a to-do task.
     *
     * @param echo The task description.
     */
    public Todo(String echo) {
        super(echo);
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }

    @Override
    public String toWrite() {
        return "T | " + super.toWrite();
    }
}
