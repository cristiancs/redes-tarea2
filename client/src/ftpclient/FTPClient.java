package ftpclient;

import java.io.*;
import java.net.*;

import java.util.Base64;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.util.Scanner;

class FTPClient {
    private String encodeFileToBase64Binary(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] encoded = Base64.getEncoder().encode(Files.readAllBytes(Paths.get(fileName)));
        return new String(encoded);
    }

    private String DecodeBase64ToString(String Text) {
        byte[] decoded = Base64.getDecoder().decode(Text);
        return new String(decoded);
    }

    public static void main(String argv[]) throws Exception {
        FTPClient programm = new FTPClient();
        programm.start();

    }

    public void start() {
        String sentence;
        String modifiedSentence;
        ;

        try {
            Socket clientSocket = new Socket("127.0.0.1", 59898);
            PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            Scanner inFromServer = new Scanner(clientSocket.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            Scanner userInput = new Scanner(System.in);
            String inText = inFromServer.nextLine();
            outToServer.println("HELLO");

            while (true) {
                String inConsole = System.console().readLine();
                if (inConsole.length() == 0) {
                    break;
                }
                if (inConsole.equals("ls")) {
                    outToServer.println(inConsole);

                    while (!inText.equals("END")) {
                        inText = inFromServer.nextLine();
                        if (!inText.equals("END")) {
                            System.out.println(inText);
                        }

                    }
                    inText = "";
                } else if (inConsole.startsWith("delete")) {
                    outToServer.println(inConsole);
                } else if (inConsole.startsWith("get")) {
                    outToServer.println(inConsole);
                    String parts[] = inConsole.split(" ");
                    String filesString = inFromServer.nextLine();
                    File file = new File("files/" + parts[1]);
                    file.createNewFile();
                    FileWriter fr = new FileWriter(file, true);
                    fr.write(DecodeBase64ToString(filesString));
                    fr.close();

                } else {
                    // outToServer.println(inConsole);
                    System.out.println(inText);
                }

            }
            clientSocket.close();

        } catch (Exception e) {
            System.out.println(e);
        }

    }
}
