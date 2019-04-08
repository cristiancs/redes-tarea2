package ftpserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class FTPMain {
    public static void main(String[] args) throws Exception {
        try (var listener = new ServerSocket(59898)) {
            System.out.println("The capitalization server is running...");
            var pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new Capitalizer(listener.accept()));
            }
        }
    }

    private static class Capitalizer implements Runnable {
        private Socket socket;

        Capitalizer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Connected: " + socket);
            try {
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);
                while (in.hasNextLine()) {
                    String mensaje = in.nextLine();
                    System.out.println("Mensaje:" + mensaje);
                    if (mensaje.equals("ls")) {
                        out.println("Deberia Listar Archivos");
                    } else if (mensaje.startsWith("get")) {
                        out.println("Deberia Enviar archivo");
                    } else if (mensaje.startsWith("put")) {
                        out.println("Deberia subir archivo");
                    } else if (mensaje.startsWith("delete")) {
                        out.println("Deberia eliminar archivo");
                    } else {
                        out.println("Comando no reconocido");
                    }

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
