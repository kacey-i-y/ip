import java.util.*;
import java.io.*;

public class Mochi {

    private enum Command {
        LIST, MARK, UNMARK, TODO, DEADLINE, EVENT, DELETE, BYE, UNKNOWN;

        static Command from(String input) {
            if (input == null) return UNKNOWN;
            String trimmed = input.trim();
            if (trimmed.isEmpty()) return UNKNOWN;
            String first = trimmed.split(" ")[0].toLowerCase();
            return switch (first) {
                case "list" -> LIST;
                case "mark" -> MARK;
                case "unmark" -> UNMARK;
                case "todo" -> TODO;
                case "deadline" -> DEADLINE;
                case "event" -> EVENT;
                case "delete" -> DELETE;
                case "bye" -> BYE;
                default -> UNKNOWN;
            };
        }
    }

    private static void error() {
        System.out.println("Error, please follow the specified formats:");
        System.out.println("todo <task>");
        System.out.println("deadline <task> /by <deadline>");
        System.out.println("event <task> /from <from> /to <to>");
        System.out.println("mark <number>");
        System.out.println("unmark <number>");
        System.out.println("delete <number>");
        System.out.println("list");
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out); // still unused, preserved

        String logo =
                "             .@@@@@@@@@@@@@@@.\n"
                        + "          .@@@@@@@@@@@@@@@@@@@@@.\n"
                        + "        .@@@@@@@@@@@@@@@@@@@@@@@@@.\n"
                        + "       @@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                        + "      @@@@@@   (* )     ( *)   @@@@@@\n"
                        + "      @@@@@@       .  ^  .       @@@@@@\n"
                        + "       @@@@@@@      ' w '      @@@@@@@\n"
                        + "        @@@@@@@@.           .@@@@@@@@\n"
                        + "          \"@@@@@@@@@@@@@@@@@@@@@@@\"\n"
                        + "      __  __   ____    ____  _   _  ___\n"
                        + "     |  \\/  | / __ \\  / ___|| | | ||_ _|\n"
                        + "     | |\\/| || |  | || |    | |_| | | |\n"
                        + "     | |  | || |__| || |___ |  _  | | |\n"
                        + "     |_|  |_| \\____/  \\____||_| |_||___|\n";

        System.out.println("____________________________________________________________");
        System.out.print(logo);
        System.out.println("____________________________________________________________");
        System.out.println("Hello I'm MOCHI, your cutest personal chatbot (˶˃ ᵕ ˂˶)");
        System.out.println("Is there anything I can help you with today?");
        System.out.println("____________________________________________________________");
        System.out.println("Please follow the following formats:");
        System.out.println("todo <task>");
        System.out.println("deadline <task> /by <deadline>");
        System.out.println("event <task> /from <from> /to <to>");
        System.out.println("mark <number>");
        System.out.println("unmark <number>");
        System.out.println("delete <number>");
        System.out.println("list");
        System.out.println("____________________________________________________________");

        String echo = "init";
        ArrayList<Task> lst = new ArrayList<>();

        while (true) {
            echo = br.readLine();

            Command cmd = Command.from(echo);
            if (cmd == Command.BYE) break;

            System.out.println("____________________________________________________________");

            switch (cmd) {
                case LIST -> {
                    System.out.println("The following tasks are listed in the task list:");
                    for (int i = 0; i < lst.size(); i++) {
                        System.out.println((i + 1) + "." + lst.get(i));
                    }
                }
                case MARK -> {
                    try {
                        lst.get(Integer.parseInt(echo.split(" ")[1]) - 1).mark();
                        System.out.println(lst.get(Integer.parseInt(echo.split(" ")[1]) - 1));
                        System.out.println("Great! I have successfully marked this task as completed");
                    } catch (RuntimeException e) {
                        Mochi.error();
                    }
                }
                case UNMARK -> {
                    try {
                        lst.get(Integer.parseInt(echo.split(" ")[1]) - 1).unmark();
                        System.out.println(lst.get(Integer.parseInt(echo.split(" ")[1]) - 1));
                        System.out.println("Awesome! I have successfully marked this task as not yet completed");
                    } catch (RuntimeException e) {
                        Mochi.error();
                    }
                }
                case TODO -> {
                    try {
                        if (!echo.split(" ")[1].equals("")) {
                            System.out.println("Success! I just added it to the task list");
                            lst.add(new Todo(echo.split("todo ")[1]));
                            System.out.println(lst.get(lst.size() - 1));
                            System.out.println("Currently, we have " + lst.size() + " tasks on the list");
                        } else {
                            Mochi.error();
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Mochi.error();
                    }
                }
                case DEADLINE -> {
                    try {
                        if (!echo.split("deadline ")[1].split(" /by ")[0].equals("")
                                && !echo.split(" /by ")[1].equals("")) {
                            System.out.println("Success! I just added it to the task list");
                            lst.add(new Deadline(echo.split("deadline ")[1].split(" /by ")[0],
                                    echo.split(" /by ")[1]));
                            System.out.println(lst.get(lst.size() - 1));
                            System.out.println("Currently, we have " + lst.size() + " tasks on the list");
                        } else {
                            Mochi.error();
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Mochi.error();
                    }
                }
                case EVENT -> {
                    try {
                        if (!echo.split("event ")[1].split(" /from ")[0].equals("")
                                && !echo.split(" /from ")[1].split(" /to ")[0].equals("")
                                && !echo.split(" /to ")[1].equals("")) {
                            System.out.println("Success! I just added it to the task list");
                            lst.add(new Event(echo.split("event ")[1].split(" /from ")[0],
                                    echo.split(" /from ")[1].split(" /to ")[0],
                                    echo.split(" /to ")[1]));
                            System.out.println(lst.get(lst.size() - 1));
                            System.out.println("Currently, we have " + lst.size() + " tasks on the list");
                        } else {
                            Mochi.error();
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Mochi.error();
                    }
                }
                case DELETE -> {
                    try {
                        Task temp = lst.get(Integer.parseInt(echo.split(" ")[1]) - 1);
                        lst.remove(Integer.parseInt(echo.split(" ")[1]) - 1);
                        System.out.println(temp);
                        System.out.println("Okay! I have successfully removed this task from the task list");
                        System.out.println("Currently, we have " + lst.size() + " tasks on the list");
                    } catch (RuntimeException e) {
                        Mochi.error();
                    }
                }
                default -> Mochi.error();
            }

            System.out.println("____________________________________________________________\n");
        }

        System.out.println("____________________________________________________________");
        System.out.println("Bye-bye, Please come back soon দ্দি(˵ •̀ ᴗ - ˵ ) ✧!!!");
        System.out.println("____________________________________________________________");
    }
}