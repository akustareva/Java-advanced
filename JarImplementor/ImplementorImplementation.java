package ru.ifmo.ctddev.kustareva.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;

/**
 * This class generate implementation of given class or interface.
 *
 * @see ru.ifmo.ctddev.kustareva.implementor.Implementor
 */

public class ImplementorImplementation {
    /**
     * Class or interface for processing.
     */
    private final Class clazz;
    /**
     * It contains generated class.
     */
    private final Appendable out;

    /**
     * Four spaces, it used for formatting generated code.
     */
    private final String TAB = "    ";
    /**
     * Simple name of given class.
     */
    private final String className;

    /**
     * Abstract methods of given class which should be overridden.
     */
    private final Set<MethodStructure> methods;

    /**
     * Class constructor, it recognizes which class should be implemented (<code>clazz</code>)
     * and name of generated class (<code>className</code>).
     * @param c
     *        class or interface which should be implemented
     * @param writer
     *        entity which contains generated class
     */
    public ImplementorImplementation(Class c, Appendable writer) {
        this.clazz = c;
        this.out = writer;
        this.className = clazz.getSimpleName() + "Impl";
        this.methods = new HashSet<>();
    }

    /**
     * Writes package name of class into {@link #out}.
     * @throws IOException
     *         If an I/O error occurs
     */
    private void getPackage() throws IOException {
        Package pack = clazz.getPackage();
        if (pack != null) {
            out.append("package ").append(pack.getName()).append(";\n\n");
        }
    }

    /**
     * Writes class declaration into {@link #out}.
     *
     * @throws IOException
     *         If an I/O error occurs
     */
    private void getName() throws IOException {
        boolean isInterface = clazz.isInterface();
        int m;
        if (isInterface) {
            m = Modifier.interfaceModifiers() & clazz.getModifiers() & ~Modifier.ABSTRACT;
        } else {
            m = Modifier.classModifiers() & clazz.getModifiers() & ~Modifier.ABSTRACT;
        }
        String mods = Modifier.toString(m);
        if (!mods.equals("")) {
            mods += " ";
        }
        out.append(mods).append("class ").append(className);
        out.append(isInterface ? " implements " : " extends ").append(clazz.getCanonicalName()).append(" {\n");
    }

    /**
     * Writes types and names of variables for a given array of {@link java.lang.reflect.Parameter}.
     *
     * @param parameters
     *        Given array of {@link java.lang.reflect.Parameter}.
     * @throws IOException
     *        If an I/O error occurs
     */
    private void printParameters(Parameter[] parameters) throws IOException {
        for (int i = 0; i < parameters.length; i++) {
            String t = parameters[i].getType().getCanonicalName() + " " + parameters[i].getName();
            if (i != parameters.length - 1) {
                t += ", ";
            }
            out.append(t);
        }
    }

    /**
     * Writes constructors of class into {@link #out}.
     *
     * @throws IOException
     *         If an I/O error occurs
     * @throws ImplerException
     *         If all constructors of class are private
     */
    private void getConstructors() throws IOException, ImplerException {
        int constructorsCount = 0;
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            int m = constructor.getModifiers();
            if (Modifier.isPrivate(m)) {
                continue;
            }
            constructorsCount++;
            m &= ~Modifier.TRANSIENT;
            String mods = Modifier.toString(m);
            if (!mods.equals("")) {
                mods += " ";
            }
            out.append(TAB).append(mods).append(className).append("(");
            Parameter[] parameters = constructor.getParameters();
            printParameters(parameters);
            out.append(")");
            Class<?>[] excTypes = constructor.getExceptionTypes();
            if (excTypes.length > 0) {
                out.append(" throws ");
                for (int i = 0; i < excTypes.length; i++) {
                    String arg = excTypes[i].getCanonicalName();
                    if (i != excTypes.length - 1) {
                        arg += ", ";
                    }
                    out.append(arg);
                }
            }
            out.append(" {\n");
            out.append(TAB).append(TAB).append("super(");
            for (int i = 0; i < parameters.length; i++) {
                String arg = parameters[i].getName();
                if (i != parameters.length - 1) {
                    arg += ", ";
                }
                out.append(arg);
            }
            out.append(");\n");
            out.append(TAB).append("}\n");
        }
        if (constructorsCount == 0 && constructors.length > 0) {
            throw new ImplerException("All constructors are private");
        }
        out.append("\n");
    }

    /**
     * Return default value for given class.
     * <p>
     * It returns only <code>0</code>, <code>false</code> or <code>null</code>.
     * @param c
     *        Given class
     * @return
     *        Default value for given class
     */
    private String getDefault(Class<?> c) {
        if (c.isPrimitive()) {
            return c.equals(Boolean.TYPE) ? "false" : "0";
        } else {
            return "null";
        }
    }

    /**
     * Writes given method into {@link #out}.
     *
     * @param method
     *        Given method which should be written
     * @throws IOException
     *         If an I/O error occurs
     * @see #getMethods
     */
    private void getMethod(Method method) throws IOException {
        int m = method.getModifiers();
        if (Modifier.isPrivate(m)) {
            return;
        }
        m &= ~Modifier.TRANSIENT & ~Modifier.ABSTRACT;
        methods.add(new MethodStructure(method));
        String mods = Modifier.toString(m);
        if (!mods.equals("")) {
            mods += " ";
        }
        out.append(TAB).append(mods).append(method.getReturnType().getCanonicalName()).
                append(" ").append(method.getName()).append("(");
        printParameters(method.getParameters());
        out.append(") {\n");
        if (!method.getReturnType().equals(Void.TYPE)) {
            out.append(TAB).append(TAB).append("return ").
                    append(getDefault(method.getReturnType())).append(";\n");
        }
        out.append(TAB).append("}\n");
    }

    /**
     * Chooses abstract non-private methods of given class and
     * passes it into {@link #getMethod}.
     *
     * @param c
     *        Given class
     * @throws IOException
     *         If an I/O error occurs
     * @see #getMethod
     */
    private void getMethods(Class<?> c) throws IOException {
        if (!Modifier.isAbstract(clazz.getModifiers())) {
            return;
        }
        for (Method m : c.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                MethodStructure method = new MethodStructure(m);
                if (!methods.contains(method)) {
                    getMethod(m);
                }
            }
        }
        for (Method m : c.getDeclaredMethods()) {
            MethodStructure method = new MethodStructure(m);
            if (!methods.contains(method)) {
                int mods = m.getModifiers();
                if (Modifier.isAbstract(mods) &&!Modifier.isPrivate(mods)) {
                    getMethod(m);
                }
            }
        }
        if (c.getSuperclass() != null) {
            getMethods(c.getSuperclass());
        }
    }

    /**
     * Calls all necessary methods to implement class.
     *
     * @throws IOException
     *         If an I/O error occurs
     * @throws ImplerException
     *         If all constructors of class are private
     */
    public void implement() throws IOException, ImplerException {
        getPackage();
        getName();
        getConstructors();
        getMethods(clazz);
        out.append("}");
    }
}
