package ru.ifmo.ctddev.kustareva.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Implementor implements Impler {

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Incorrect input arguments");
        }
        File file = new File(root.toFile(), token.getCanonicalName().replace(".", File.separator) + "Impl.java").getAbsoluteFile();
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new ImplerException("Cannot create directories");
        }
        try (FileWriter writer =  new FileWriter(file, true)) {
            if (token.isPrimitive()) {
                throw new ImplerException("Cannot implement primitives");
            }
            if (Modifier.isFinal(token.getModifiers())) {
                throw new ImplerException("Cannot implement final classes");
            }
            ImplementorImplementation implementation = new ImplementorImplementation(token, writer);
            implementation.implement();
        } catch (IOException e) {
            throw new ImplerException("Cannot open output file");
        }
    }
}
