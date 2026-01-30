package mochi.task;

import java.util.ArrayList;

/**
 * Represents a list of tasks and provides basic operations on them.
 */
public class TaskList {
    private final ArrayList<Task> tasks;

    /**
     * Creates an empty TaskList.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Adds a task to the list.
     *
     * @param task The task to add.
     */
    public void add(Task task) {
        this.tasks.add(task);
    }

    /**
     * Returns the task at the given 0-based index.
     *
     * @param index The 0-based index.
     * @return The task.
     * @throws IndexOutOfBoundsException If index is invalid.
     */
    public Task get(int index) {
        return this.tasks.get(index);
    }

    /**
     * Deletes and returns the task at the given 0-based index.
     *
     * @param index The 0-based index.
     * @return The removed task.
     * @throws IndexOutOfBoundsException If index is invalid.
     */
    public Task remove(int index) {
        return this.tasks.remove(index);
    }

    /**
     * Marks a task at the given 0-based index as done.
     *
     * @param index The 0-based index.
     */
    public void mark(int index) {
        this.tasks.get(index).mark();
    }

    /**
     * Unmarks a task at the given 0-based index as not done.
     *
     * @param index The 0-based index.
     */
    public void unmark(int index) {
        this.tasks.get(index).unmark();
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return The task count.
     */
    public int size() {
        return this.tasks.size();
    }

    /**
     * Returns the underlying list (for saving/loading).
     *
     * @return The task list.
     */
    public ArrayList<Task> asList() {
        return this.tasks;
    }

    /**
     * Returns the boolean for whether the lisk is empty or not
     *
     * @return boolean of true or false
     */
    public boolean isEmpty() {
        return this.tasks.isEmpty();
    }
}