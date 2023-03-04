package io.typecraft.bukkit.object;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@UtilityClass
class Reflections {
    private static final Set<Class<?>> primitiveClasses = getPrimitiveClasses();

    private static Set<Class<?>> getPrimitiveClasses() {
        Set<Class<?>> ret = new HashSet<>();
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Double.class);
        ret.add(Float.class);
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(String.class);
        return ret;
    }

    public static Optional<Object> invokeMethod(Object instance, String methodName, FieldValue... params) {
        try {
            Class<?>[] paramTypes = new Class[params.length];
            Object[] paramValues = new Object[params.length];
            for (int i = 0; i < paramValues.length; i++) {
                paramTypes[i] = params[i].getFieldType();
                paramValues[i] = params[i].getFieldValue();
            }
            Method method = instance.getClass().getMethod(methodName, paramTypes);
            return Optional.ofNullable(method.invoke(instance, paramValues));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static boolean checkPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || primitiveClasses.contains(clazz);
    }

    public static Optional<Class<?>> findClass(String name) {
        try {
            return Optional.of(Class.forName(name));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
