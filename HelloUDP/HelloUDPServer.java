package ru.ifmo.ctddev.kustareva.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {
    private List<DatagramSocket> sockets = new ArrayList<>();
    private List<ExecutorService> executors = new ArrayList<>();
    private boolean closed = false;

    @Override
    public synchronized void start(int port, int threads) {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            sockets.add(socket);
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            executors.add(executor);
            byte buffer[] = new byte[socket.getReceiveBufferSize()];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            executor.submit(() -> {
                while (!closed) {
                    try {
                        socket.receive(request);
                        String resp = new String(request.getData(), 0, request.getLength(), Charset.forName("UTF-8"));
                        resp = "Hello, " + resp;
                        DatagramPacket response = new DatagramPacket(resp.getBytes("UTF-8"),
                                resp.getBytes().length, request.getAddress(), request.getPort());
                        socket.send(response);
                    } catch (IOException ignored) {
                    }
                }
            });
        } catch (IOException ignored) {
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
        for (DatagramSocket socket: sockets) {
            socket.close();
        }
        for (ExecutorService executor: executors) {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executor.shutdownNow();
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Incorrect input parameters");
            return;
        }
        new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }
}
