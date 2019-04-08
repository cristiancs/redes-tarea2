package ftpserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class FTPMain {

    public class LogHandler {
        public void StartLog() {
            try {
                File file = new File("log.txt");
                file.delete();
                file.createNewFile();
                FileWriter fr = new FileWriter(file, true);
                fr.write("DATETIME  EVENT   DESCRIPTION\n");
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void writeLog(String event, String message) {
            try {
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                Date date = new Date();

                File file = new File("log.txt");
                FileWriter fr = new FileWriter(file, true);
                fr.write(dateFormat.format(date) + "  " + event + "   " + message + "\n");
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() throws Exception {
        try (var listener = new ServerSocket(59898)) {
            System.out.println("Starting FTP Server ...");
            LogHandler log = new LogHandler();
            log.StartLog();
            var pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new RequestHandler(listener.accept()));
            }
        }
    }

    public static void main(String[] args) throws Exception {

        FTPMain programm = new FTPMain();
        programm.start();

    }

    private class RequestHandler implements Runnable {
        private Socket socket;

        RequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            LogHandler log = new LogHandler();
            String ip = socket.getInetAddress().toString();
            ip = ip.replace("/", "");
            log.writeLog("connection", ip + " conexión entrante");
            int mensajes = 0;
            try {
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);

                out.println("HELLO");
                while (in.hasNextLine()) {
                    String mensaje = in.nextLine();
                    System.out.println("Mensaje:" + mensaje);

                    if (!mensaje.equals("HELLO") && mensajes == 0) {
                        System.out.println("mensajes:" + mensajes);
                        log.writeLog("error", "conexión rechazada por" + ip);
                        out.println("Invalid Handshake message");
                        throw new IllegalArgumentException("Error en handshake");
                    } else if (mensajes > 0) {
                        log.writeLog("command", ip + " " + mensaje);
                        if (mensaje.equals("ls")) {

                            File curDir = new File("./files");
                            File[] filesList = curDir.listFiles();
                            for (File f : filesList) {
                                out.println(f.getName());
                            }
                            log.writeLog("command", "servidor envía respuesta a " + ip);
                        } else if (mensaje.startsWith("get")) {
                            out.println("Deberia Enviar archivo");
                        } else if (mensaje.startsWith("put")) {
                            out.println("Deberia subir archivo");
                        } else if (mensaje.startsWith("delete")) {
                            out.println("Deberia eliminar archivo");
                        } else {
                            out.println("Comando no reconocido");
                        }
                    }

                    mensajes += 1;
                    // out.println(in.nextLine().toUpperCase());
                }
            } catch (Exception e) {
                System.out.println("Error:" + socket);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                System.out.println("Closed: " + socket);
            }
        }
    }
}
