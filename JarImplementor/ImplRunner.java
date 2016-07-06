package ru.ifmo.ctddev.kustareva.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class which provides following opportunity:
 * <p>
 * you can launch <code>Implementor</code> with arguments
 * <code>-jar ClassName file.jar </code> to generate
 * <code>.jar</code>-file with implementing given class or interface.
 */
public class ImplRunner {

    /**
     * Class constructor. Never used.
     */
    private ImplRunner () {
    }

    /**
     * Method for launch <code>Implementor</code> with
     *  given arguments.
     * @param args
     *        Arguments for creating <code>.jar</code>-file.
     */
    public static void main(String[] args) {
        if (args == null || args.length < 3 || args[0] == null || args[1] == null || args[2] == null) {
            System.err.println("Incorrect input arguments");
            return;
        }
        if (!args[0].equals("-jar")) {
            System.err.println("First arg should be -jar");
            return;
        }
        try {
            Class c = Class.forName(args[1]);
            Path jarFilePath = Paths.get(args[2]);
            new Implementor().implementJar(c, jarFilePath);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found");
        } catch (ImplerException e) {
            System.err.println("Cannot create jar file");
        }
    }
}
