package ru.ifmo.ctddev.kustareva.walk;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FilesVisitor extends SimpleFileVisitor<Path> {
    BufferedWriter writer;
    String error = "00000000000000000000000000000000";

    FilesVisitor(BufferedWriter newWriter) {
        super();
        writer = newWriter;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String hash = error;
        String path = file.toString();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream input = new FileInputStream(path);

            byte[] b = new byte[1024];
            int c = 0;
            while ((c = input.read(b)) >= 0) {
                md.update(b, 0, c);
            }

            byte[] data = md.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < data.length; i++) {
                sb.append(String.format("%02X", data[i]));
            }

            hash = new String(sb);
        } catch (IOException e) {
            System.err.println("In visitFile IOException: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("In visitFile RuntimeException: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.err.println("In visitFile NoSuchAlgorithmException: " + e.getMessage());
        }
        writer.write(hash + " " + path);
        writer.newLine();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.err.println("In visitFileFailed IOException: " + exc.getMessage());
        String path = file.toString();
        writer.write(error + " " + path);
        writer.newLine();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc != null) {
            System.err.println("In postVisitDirectory IOException: " + exc.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }
}