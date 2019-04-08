package ftpserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class FTPMain {

    public void start() throws Exception {
        try (var listener = new ServerSocket(59898)) {
            System.out.println("Starting FTP Server ...");
            LogHandler log = new LogHandler();
            log.StartLog();

            ThreadPool threadPool = new ThreadPool(3, 20);

            while (true) {
                threadPool.submitTask(new RequestHandler(listener.accept()));
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
                            String parts[] = mensaje.split(" ");
                            File file = new File("files/" + parts[1]);
                            if (file.delete()) {
                                out.println("Archivo " + parts[1] + " eliminado");
                            } else {
                                out.println("Error al eliminar " + parts[1]);
                            }
                            log.writeLog("command", "servidor envía respuesta a " + ip);

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
