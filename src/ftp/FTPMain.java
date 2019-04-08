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
import java.util.LinkedList;
import java.util.Queue;

public class FTPMain {
    public class BlockingQueue<Type> {
        private Queue<Type> queue = new LinkedList<Type>();
        private int EMPTY = 0;
        private int MAX_TASK_IN_QUEUE = -1;

        public BlockingQueue(int size) {
            this.MAX_TASK_IN_QUEUE = size;
        }

        public synchronized void enqueue(Type task) throws InterruptedException {
            while (this.queue.size() == this.MAX_TASK_IN_QUEUE) {
                wait();
            }
            if (this.queue.size() == EMPTY) {
                notifyAll();
            }
            this.queue.offer(task);
        }

        public synchronized Type dequeue() throws InterruptedException {
            while (this.queue.size() == EMPTY) {
                wait();
            }
            if (this.queue.size() == this.MAX_TASK_IN_QUEUE) {
                notifyAll();
            }
            return this.queue.poll();
        }
    }

    public class TaskExecutor implements Runnable {
        BlockingQueue<Runnable> queue;

        public TaskExecutor(BlockingQueue<Runnable> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String name = Thread.currentThread().getName();
                    Runnable task = queue.dequeue();
                    System.out.println("Task Started by Thread :" + name);
                    task.run();
                    System.out.println("Task Finished by Thread :" + name);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public class ThreadPool {

        BlockingQueue<Runnable> queue;

        public ThreadPool(int queueSize, int nThread) {
            queue = new BlockingQueue<>(queueSize);
            String threadName = null;
            TaskExecutor task = null;
            for (int count = 0; count < nThread; count++) {
                threadName = "Thread-" + count;
                task = new TaskExecutor(queue);
                Thread thread = new Thread(task, threadName);
                thread.start();
            }
        }

        public void submitTask(Runnable task) throws InterruptedException {
            queue.enqueue(task);
        }
    }

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
