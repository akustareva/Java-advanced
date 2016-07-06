package ru.ifmo.ctddev.kustareva.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * This class implements given class or interface and returns correct java code.
 *
 * Also this class can create <code>.jar</code>-file with implementing given class or interface.
 *
 * @author Anna Kustareva
 */
public class Implementor implements Impler, JarImpler {

    /**
     * Class constructor for creating class instance.
     */
    public Implementor() {
    }

    /**
     * Implements class or interface and puts result in a file in the required path.
     * @param token
     *        Class or interface to implement
     * @param root
     *        The path in which a generated file should to be
     * @throws ImplerException
     *         If there is no correct implementation for token
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Incorrect input arguments");
        }
        File file = new File(root.toFile(), token.getCanonicalName().replace(".", File.separator) + "Impl.java").getAbsoluteFile();
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new ImplerException("Cannot create directories");
        }
        try (FileWriter writer =  new FileWriter(file, false)) {
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

    /**
     * Method for create <code>.jar</code>-file with implementing given class or interface.
     * @param token
     *        Class or interface for implementing.
     * @param jarFilePath
     *        The path in which a generated file should to be
     * @throws ImplerException
     *         If there is no correct implementation for token
     */
    @Override
    public void implementJar(Class<?> token, Path jarFilePath) throws ImplerException {
        if (token == null || jarFilePath == null) {
            throw new ImplerException("Incorrect input arguments");
        }
        Path tmpP = Paths.get(".").resolve("tmpDir");
        implement(token, tmpP);
        String className = token.getPackage().getName().replace(".", File.separator) + File.separator + token.getSimpleName() + "Impl";
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int res = compiler.run(null, null, null,
                                tmpP + File.separator + className + ".java",
                                "-cp",
                                tmpP + File.pathSeparator + System.getProperty("java.class.path"));
        if (res != 0) {
            throw new ImplerException("Cannot compile the file");
        }
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jarFilePath), manifest)) {
            String param = className + ".class";
            param = param.replace("\\", "/");
            JarEntry entry = new JarEntry(param);
            output.putNextEntry(entry);
            try (FileInputStream input = new FileInputStream(tmpP.toAbsolutePath().toString() + "/" +
                                                             className + ".class")) {
                byte buf[] = new byte[1024];
                int count;
                while ((count = input.read(buf)) >= 0) {
                    output.write(buf, 0, count);
                }
            }
        } catch (IOException e) {
            throw new ImplerException("Cannot create jar file");
        }
    }
}
