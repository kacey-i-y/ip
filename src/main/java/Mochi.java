import java.util.*;
import java.io.*;
public class Mochi {

    private static void error() {
        System.out.println("Error, please follow the specified formats:");
        System.out.println("todo <task>");
        System.out.println("deadline <task> /by <deadline>");
        System.out.println("event <task> /from <from> /to <to>");
    }
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out);
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
        System.out.println("____________________________________________________________");
        String echo = "init";
        ArrayList<Task> lst = new ArrayList<>();
        while (true) {
            echo = br.readLine();
            if (echo.equals("bye")) break;
            System.out.println("____________________________________________________________");
            if (echo.equals("list")) {
                System.out.println("The following tasks are listed in the task list:");
                for (int i = 0; i < lst.size(); i++) System.out.println((i + 1) + "." + lst.get(i));
            } else if (echo.split(" ")[0].equals("mark")) {
                try {
                    lst.get(Integer.parseInt(echo.split(" ")[1]) - 1).mark();
                    System.out.println(lst.get(Integer.parseInt(echo.split(" ")[1]) - 1));
                    System.out.println("Great! I have successfully marked this task as completed");
                } catch (RuntimeException e) {
                    Mochi.error();
                }
            } else if (echo.split(" ")[0].equals("unmark")) {
                try {
                    lst.get(Integer.parseInt(echo.split(" ")[1]) - 1).unmark();
                    System.out.println(lst.get(Integer.parseInt(echo.split(" ")[1]) - 1));
                    System.out.println("Awesome! I have successfully marked this task as not yet completed");
                } catch (RuntimeException e) {
                    Mochi.error();
                }
            } else if (echo.split(" ")[0].equals("todo")) {
                try {
                    if (!echo.split(" ")[1].equals("")) {
                        System.out.println("Success! I just added it to the task list");
                        lst.add(new Todo(echo.split("todo ")[1]));
                        System.out.println(lst.get(lst.size() - 1));
                        System.out.println("Currently, we have " + lst.size() + " tasks on the list");
                    } else Mochi.error();
                } catch (ArrayIndexOutOfBoundsException e) {
                    Mochi.error();
                }
            } else if (echo.split(" ")[0].equals("deadline")) {
                try {
                    if (!echo.split("deadline ")[1].split(" /by ")[0].equals("")
                            && !echo.split(" /by ")[1].equals("")) {
                        System.out.println("Success! I just added it to the task list");
                        lst.add(new Deadline(echo.split("deadline ")[1].split(" /by ")[0],
                                echo.split(" /by ")[1]));
                        System.out.println(lst.get(lst.size() - 1));
                        System.out.println("Currently, we have " + lst.size() + " tasks on the list");
                    } else Mochi.error();
                } catch (ArrayIndexOutOfBoundsException e) {
                    Mochi.error();
                }
            } else if (echo.split(" ")[0].equals("event")) {
                try {
                    if (!echo.split("event ")[1].split(" /from ")[0].equals("")
                            && !echo.split(" /from ")[1].split(" /to ")[0].equals("")
                            && !echo.split(" /to ")[1].equals("")) {
                        System.out.println("Success! I just added it to the task list");
                        lst.add(new Event(echo.split("event ")[1].split(" /from ")[0],
                                echo.split(" /from ")[1].split(" /to ")[0], echo.split(" /to ")[1]));
                        System.out.println(lst.get(lst.size() - 1));
                        System.out.println("Currently, we have " + lst.size() + " tasks on the list");
                    } else Mochi.error();
                } catch (ArrayIndexOutOfBoundsException e) {
                    Mochi.error();
                }
            } /*else if (echo.split(" ")[0].equals("delete") {
                try {
                    lst.get(Integer.parseInt(echo.split(" ")[1]) - 1);
                    System.out.println(lst.get(Integer.parseInt(echo.split(" ")[1]) - 1));
                    System.out.println("Okay! I have successfully removed this task from the task list");
                } catch (RuntimeException e) {
                    Mochi.error();
                }
            } */else {
                Mochi.error();
            }
            System.out.println("____________________________________________________________\n");
        }
        System.out.println("____________________________________________________________");
        System.out.println("Bye-bye, Please come back soon ദ്ദി(˵ •̀ ᴗ - ˵ ) ✧!!!");
        System.out.println("____________________________________________________________");
    }
}
