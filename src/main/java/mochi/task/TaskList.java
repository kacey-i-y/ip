package mochi.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of tasks and provides basic operations on it.
 */
public class TaskList {

    private final List<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Adds a task to the list.
     *
     * @param task Task to add.
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Gets a task by 0-based index.
     *
     * @param index 0-based index.
     * @return Task at the index.
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Removes a task by 0-based index.
     *
     * @param index 0-based index.
     * @return Removed task.
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns number of tasks in the list.
     *
     * @return Size of the list.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether the list is empty.
     *
     * @return True if empty, false otherwise.
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Finds tasks whose descriptions contain the given keyword (case-insensitive).
     *
     * @param keyword Keyword to search for.
     * @return A new TaskList of matching tasks.
     */
    public TaskList find(String keyword) {
        TaskList matches = new TaskList();
        if (keyword == null) {
            return matches;
        }

        String key = keyword.trim().toLowerCase();
        if (key.isEmpty()) {
            return matches;
        }

        for (Task t : tasks) {
            String s = t.toString().toLowerCase();
            if (s.contains(key)) {
                matches.add(t);
            }
        }
        return matches;
    }
}
