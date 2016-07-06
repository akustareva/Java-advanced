package ru.ifmo.ctddev.kustareva.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodStructure {
    private final Method method;

    public MethodStructure(Method m){
        this.method = m;
    }

    @Override
    public int hashCode() {
        return method.getName().hashCode();
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
