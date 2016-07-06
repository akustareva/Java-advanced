package ru.ifmo.ctddev.kustareva.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.*;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {

    @Override
    public void start(String host, int port, String prefix, int requests, int threads) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            InetAddress ip = InetAddress.getByName(host);
            for (int i = 0; i < threads; i++) {
                final int id = i;
                executor.submit(() -> {
                    try {
                        DatagramSocket socket = new DatagramSocket();
                        socket.setSoTimeout(50);
                        int count = 0;
                        byte buffer[] = new byte[socket.getReceiveBufferSize()];
                        while (count != requests) {
                            String req = prefix + Integer.toString(id) + "_" + Integer.toString(count);
                            System.out.println(req);
                            DatagramPacket request = new DatagramPacket(req.getBytes("UTF-8"), req.length(), ip, port);
                            socket.send(request);
                            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                            try {
                                socket.receive(response);
                                String resp = new String(response.getData(), 0, response.getLength(), Charset.forName("UTF-8"));
                                String tmp = new String(req.getBytes("UTF-8"), 0, req.length(),  Charset.forName("UTF-8"));
                                if (resp.equals("Hello, " + tmp)) {
                                    System.out.println(resp);
                                    count++;
                                }
                            } catch (SocketTimeoutException ignored) {}
                        }
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (UnknownHostException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Incorrect input parameters");
            return;
        }
        new HelloUDPClient().start(args[0], Integer.parseInt(args[1]), args[2],
                Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }
}
