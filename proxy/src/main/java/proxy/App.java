package proxy;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Arrays;

public class App {

    public void start() throws Exception {
        try (var listener = new ServerSocket(59800)) {
            System.out.println("Server Started ...");
            LogHandler log = new LogHandler();
            log.StartLog();

            ThreadPool threadPool = new ThreadPool(3, 20);

            while (true) {
                threadPool.submitTask(new RequestHandler(listener.accept()));
            }
        }
    }

    public static void main(String[] args) throws Exception {

        App programm = new App();
        programm.start();

    }

    private class RequestHandler implements Runnable {
        private Socket socket;
        private DBHandler dbhandler;

        RequestHandler(Socket socket) {
            this.socket = socket;
            this.dbhandler = new DBHandler();
        }

        private String encodeFileToBase64Binary(String fileName) throws IOException {
            byte[] encoded = Base64.getEncoder().encode(Files.readAllBytes(Paths.get(fileName)));
            return new String(encoded);
        }

        private String encodeBytesToString(byte[] data) {
            byte[] encoded = Base64.getEncoder().encode(data);
            return new String(encoded);
        }

        private byte[] DecodeBase64ToByte(String Text) {
            byte[] decoded = Base64.getDecoder().decode(Text);
            return decoded;
        }

        private Boolean FileAvailable(String nombre) {
            HashMap<String, EdgeHandler> servers = new HashMap<String, EdgeHandler>();

            Boolean flag;

            ArrayList<String> chunks = this.dbhandler.getChunks(nombre);

            flag = true;

            for (String chunk : chunks) {
                String[] data = chunk.split("\\|");
                String server = data[1];
                String chunkname = data[0];
                if (!servers.containsKey(server)) {
                    servers.put(server, new EdgeHandler(server));
                }
                if (!servers.get(server).fileExists(chunkname)) {
                    flag = false;
                }
            }

            for (String server : servers.keySet()) {
                servers.get(server).disconnect();
            }
            return flag;
        }

        @Override
        public void run() {

            LogHandler log = new LogHandler();
            String ip = socket.getInetAddress().toString();
            ip = ip.replace("/", "");
            log.writeLog("connection", ip + " conexión entrante");
            int mensajes = 0;
            Boolean waitForFile = false;
            String fileName = "";
            try {
                Scanner in = new Scanner(socket.getInputStream());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                out.println("HELLO");
                while (in.hasNextLine()) {
                    String mensaje = in.nextLine();

                    // Manejar los datos recibidos despues del put
                    if (waitForFile) {
                        Boolean saved = false;
                        if (!this.dbhandler.fileExist(fileName)) {

                            this.dbhandler.saveFile(fileName);
                            HashMap<String, EdgeHandler> servers = new HashMap<String, EdgeHandler>();

                            ArrayList<String> servidores_names = this.dbhandler.getServers();

                            byte[] archivo = DecodeBase64ToByte(mensaje);

                            Integer largo = archivo.length;
                            Integer inicioBloque = 0;
                            Integer chunkNumber = 0;

                            String[] fileParts = fileName.split("\\.");

                            while (inicioBloque < largo) {
                                Integer finRango = Math.min(inicioBloque + 64000, largo);
                                String chunkName = fileParts[0] + "_" + chunkNumber.toString() + ".part";

                                String server = servidores_names
                                        .get(ThreadLocalRandom.current().nextInt(0, servidores_names.size()));

                                System.out.println(chunkName + ":" + largo + " " + inicioBloque + " " + finRango);
                                if (!servers.containsKey(server)) {
                                    servers.put(server, new EdgeHandler(server));
                                }

                                byte[] toSend = Arrays.copyOfRange(archivo, inicioBloque, finRango);

                                saved = servers.get(server).putChunk(encodeBytesToString(toSend), chunkName);
                                if (!saved) {
                                    break;

                                }
                                this.dbhandler.SaveChunk(fileName, chunkName, server);
                                inicioBloque += 64000;
                                chunkNumber += 1;
                            }

                            for (String server : servers.keySet()) {
                                servers.get(server).disconnect();
                            }
                        }
                        if (!saved) {
                            out.println("KO");
                            this.dbhandler.deleteFile(fileName);
                        } else {
                            out.println("OK");
                        }

                        log.writeLog("command", "servidor envía respuesta a " + ip);
                        waitForFile = false;

                    } else if (!mensaje.equals("HELLO") && mensajes == 0) {
                        System.out.println("mensajes:" + mensajes);
                        log.writeLog("error", "conexión rechazada por" + ip);
                        out.println("HANDSHAKEERROR");
                        throw new IllegalArgumentException("Error en handshake");
                    } else if (mensajes > 0) {
                        log.writeLog("command", ip + " " + mensaje);

                        // Es un ls
                        if (mensaje.equals("ls")) {

                            // Obtener todos los archivos
                            HashMap<String, ArrayList<String>> files = this.dbhandler.getFiles();
                            HashMap<String, EdgeHandler> servers = new HashMap<String, EdgeHandler>();

                            ArrayList<String> existentes = new ArrayList<String>();
                            Boolean flag;

                            // Preguntar a cada servidor si tiene las partes que necesitamos para cada
                            for (String file : files.keySet()) {
                                String nombre = file;
                                ArrayList<String> chunks = files.get(file);

                                flag = true;

                                for (String chunk : chunks) {
                                    String[] data = chunk.split("\\|");
                                    String server = data[1];
                                    String chunkname = data[0];
                                    System.out.println(server);
                                    if (!servers.containsKey(server)) {
                                        servers.put(server, new EdgeHandler(server));

                                    }
                                    if (!servers.get(server).fileExists(chunkname)) {
                                        flag = false;
                                    }
                                }
                                if (flag) {
                                    existentes.add(nombre);
                                }

                            }

                            for (String server : servers.keySet()) {
                                servers.get(server).disconnect();
                            }

                            for (String file : existentes) {
                                out.println(file);
                            }

                            // archivo

                            //

                            out.println("END");
                            log.writeLog("response", "servidor envía respuesta a " + ip);
                        } else if (mensaje.startsWith("get")) {

                            log.writeLog("response", "servidor envía respuesta a " + ip);

                            // Primera linea del put, lo preparamos para escribir
                        } else if (mensaje.startsWith("put")) {

                            String parts[] = mensaje.split(" ");
                            waitForFile = true;
                            fileName = parts[1];

                        } else if (mensaje.startsWith("delete")) {

                            String parts[] = mensaje.split(" ");
                            String file = parts[1];
                            HashMap<String, EdgeHandler> servers = new HashMap<String, EdgeHandler>();
                            if (FileAvailable(file)) {

                                // Borrar de los edge
                                ArrayList<String> chunks = this.dbhandler.getChunks(file);

                                for (String chunk : chunks) {
                                    String[] data = chunk.split("\\|");
                                    String server = data[1];
                                    String chunkname = data[0];
                                    if (!servers.containsKey(server)) {
                                        servers.put(server, new EdgeHandler(server));
                                    }
                                    servers.get(server).deleteChunk(chunkname);
                                }

                                for (String server : servers.keySet()) {
                                    servers.get(server).disconnect();
                                }

                                // Borrar de la DB
                                this.dbhandler.deleteFile(file);

                                out.println("DONE");
                            } else {
                                out.println("FILE_NOT_FOUND");
                            }

                            log.writeLog("response", "servidor envía respuesta a " + ip);

                        } else {

                            out.println("Comando no reconocido");
                        }
                    }

                    mensajes += 1;
                    // out.println(in.nextLine().toUpperCase());
                }
                in.close();
            } catch (Exception e) {
                System.out.println("Error:" + e);
                System.out.println(socket);
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
