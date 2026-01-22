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
        while (!echo.equals("bye")) {
            echo = br.readLine();
            if (echo.equals("bye")) break;
            System.out.println("____________________________________________________________");
            System.out.println(echo);
            System.out.println("____________________________________________________________\n");
        }
        System.out.println("____________________________________________________________");
        System.out.println("Bye-bye, Please come back soon ദ്ദി(˵ •̀ ᴗ - ˵ ) ✧!!!");
        System.out.println("____________________________________________________________");
    }
}
