package ru.ifmo.ctddev.kustareva.implementor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * Class for ability to compare methods.
 * <p>
 * It overrides <code>hashCode</code> and <code>equals</code> methods.
 */
public class MethodStructure {
    /**
     * Method for representing.
     */
    private final Method method;

    /**
     * Class constructor, it recognizes which method should be represented.
     * @param m
     *        Method for processing.
     */
    public MethodStructure(Method m){
        this.method = m;
    }

    @Override
    public int hashCode() {
        int h = method.getName().hashCode();
        for (Parameter p : method.getParameters()) {
            h = h ^ p.getType().hashCode();
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof MethodStructure) {
            if (method.getClass() != ((MethodStructure) o).method.getClass()) {
                System.out.println( method.getClass() + " " + ((MethodStructure) o).method.getClass());
                return false;
            }
        } else {
            return false;
        }
        if (o == this) {
            return true;
        }
        MethodStructure anotherMethod = (MethodStructure) o;
        if (!method.getName().equals(anotherMethod.method.getName()) ||
            !method.getReturnType().equals(anotherMethod.method.getReturnType())) {
            return false;
        }
        Class<?>[] paramTypes = method.getParameterTypes();
        Class<?>[] anotherParamTypes = anotherMethod.method.getParameterTypes();
        return Arrays.equals(paramTypes, anotherParamTypes);
    }
}
