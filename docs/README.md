# Mochi User Guide 🐾

Mochi is a simple task manager chatbot with a GUI. It helps you track **to-dos**, **deadlines**, and **events**.  
You interact with Mochi by typing commands into the input box and pressing **Enter** (or clicking **Send**).

---

## Quick Start

1. Ensure you have **Java 17 or above** installed on your computer.  
   Mac users: Ensure you have the precise JDK version prescribed here: https://se-education.org/guides/tutorials/javaInstallationMac.html
2. Download the latest `Mochi.jar`.
3. Copy the file to the folder you want to use as the home folder for Mochi.
4. Open a terminal and `cd` into the folder containing the jar file.
5. Run the application:

```bash
java -jar Mochi.jar
```

6. Type commands into the input box.
7. Press **Enter** (or click **Send**) to execute.

Example:

```text
todo borrow book
```

---

## Command Summary

- `list` — show all tasks
- `sort` — show tasks in a sorted order
- `todo <description>` — add a to-do
- `deadline <description> /by <yyyy-MM-dd>` — add a deadline
- `event <description> /from <yyyy-MM-dd HHmm> /to <yyyy-MM-dd HHmm>` — add an event
- `mark <task number>` — mark as done
- `unmark <task number>` — mark as not done
- `delete <task number>` — delete a task
- `find <keyword>` — find tasks containing a keyword
- `help` — show help
- `bye` — exit the app

---

## Features

### 1. Show All Tasks: `list`

Shows all tasks currently stored.

**Format**
```text
list
```

---

### 2. Show Sorted Tasks: `sort`

Shows all tasks in a sorted order.

**Format**
```text
sort
```

---

### 3. Add a To-Do Task: `todo`

Adds a task with a description.

**Format**
```text
todo DESCRIPTION
```

**Example**
```text
todo finish CS2103 tutorial
```

---

### 4. Add a Deadline Task: `deadline`

Adds a task with a description and a deadline date.

**Format**
```text
deadline DESCRIPTION /by yyyy-MM-dd
```

**Example**
```text
deadline submit assignment /by 2026-03-19
```

**Notes**
- Date must be in `yyyy-MM-dd` format.

---

### 5. Add an Event Task: `event`

Adds a task with a description, a start time, and an end time.

**Format**
```text
event DESCRIPTION /from yyyy-MM-dd HHmm /to yyyy-MM-dd HHmm
```

**Example**
```text
event tutorial /from 2026-03-19 1400 /to 2026-03-19 1500
```

**Notes**
- Date/time must be in `yyyy-MM-dd HHmm` format (24-hour time).
- `/to` must be after `/from`.

---

### 6. Mark a Task as Done: `mark`

Marks the task at the given index as completed.

**Format**
```text
mark INDEX
```

**Example**
```text
mark 2
```

**Notes**
- `INDEX` refers to the task number shown in `list`.

---

### 7. Unmark a Task: `unmark`

Marks the task at the given index as not completed.

**Format**
```text
unmark INDEX
```

**Example**
```text
unmark 2
```

---

### 8. Delete a Task: `delete`

Deletes the task at the given index.

**Format**
```text
delete INDEX
```

**Example**
```text
delete 3
```

---

### 9. Find Tasks by Keyword: `find`

Searches for tasks containing the given keyword.

**Format**
```text
find KEYWORD
```

**Example**
```text
find cs2103
```

**Notes**
- Matching is case-insensitive (e.g., `CS2103` matches `cs2103`).

---

### 10. Show Help: `help`

Displays the full list of commands and formats.

**Format**
```text
help
```

---

### 11. Exit the Application: `bye`

Exits Mochi.

**Format**
```text
bye
```

---

## Data File

Mochi saves your tasks automatically to:

- `data/tasks.txt`

If the file does not exist yet, Mochi will create it when saving your first change.
