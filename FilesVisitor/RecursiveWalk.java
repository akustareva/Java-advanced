package ru.ifmo.ctddev.kustareva.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {
    public static void main(String[] args) {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "utf-8"));
             BufferedWriter writer =
                     new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "utf-8"))) {
            String str;
            while ((str = reader.readLine()) != null) {
                Path path = Paths.get(str);
                FilesVisitor visitor = new FilesVisitor(writer);
                Files.walkFileTree(path, visitor);
            }
        } catch (IOException e) {
            System.err.println("In main IOException: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("In main RuntimeException: " + e.getMessage());
        }
    }
}