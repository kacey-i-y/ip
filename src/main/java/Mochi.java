import java.util.*;
import java.io.*;
public class Mochi {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out);
        String logo =
                "             .@@@@@@@@@@@@@@@.             \n"
                        + "          .@@@@@@@@@@@@@@@@@@@@@.          \n"
                        + "        .@@@@@@@@@@@@@@@@@@@@@@@@@.        \n"
                        + "       @@@@@@@@@@@@@@@@@@@@@@@@@@@@@       \n"
                        + "      @@@@@@   (* )     ( *)   @@@@@@      \n"
                        + "      @@@@@@       .  ^  .       @@@@@@     \n"
                        + "       @@@@@@@      ' w '      @@@@@@@      \n"
                        + "        @@@@@@@@.           .@@@@@@@@      \n"
                        + "          \"@@@@@@@@@@@@@@@@@@@@@@@\"        \n"
                        + "      __  __   ____    ____  _   _  ___       \n"
                        + "     |  \\/  | / __ \\  / ___|| | | ||_ _|      \n"
                        + "     | |\\/| || |  | || |    | |_| | | |       \n"
                        + "     | |  | || |__| || |___ |  _  | | |       \n"
                        + "     |_|  |_| \\____/  \\____||_| |_||___|      \n";
        System.out.println("____________________________________________________________");
        System.out.print(logo);
        System.out.println("____________________________________________________________");
        System.out.println("Hello I'm MOCHI, your cutest personal chatbot (˶˃ ᵕ ˂˶)");
        System.out.println("Is there anything I can help you with today?");
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
                System.out.println("Great! I have successfully marked this task as completed:");
                lst.get(Integer.parseInt(echo.split(" ")[1]) - 1).mark();
                System.out.println(lst.get(Integer.parseInt(echo.split(" ")[1]) - 1));
            } else if (echo.split(" ")[0].equals("unmark")) {
                System.out.println("Awesome! I have successfully marked this task as not yet completed:");
                lst.get(Integer.parseInt(echo.split(" ")[1]) - 1).unmark();
                System.out.println(lst.get(Integer.parseInt(echo.split(" ")[1]) - 1));
            } else {
                System.out.println("Success! I just added it to the task list");
                lst.add(new Task(echo));
                System.out.println("added: " + echo);
            }
            System.out.println("____________________________________________________________\n");
        }
        System.out.println("____________________________________________________________");
        System.out.println("Bye-bye, Please come back soon ദ്ദി(˵ •̀ ᴗ - ˵ ) ✧!!!");
        System.out.println("____________________________________________________________");
    }
}
