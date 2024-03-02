package io.typst.bukkit.object;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@UtilityClass
class Reflections {
    public static final Map<Class<?>, Function<String, Optional<Object>>> parserByPrimitives = getParsersByPrimitive();

    private static Map<Class<?>, Function<String, Optional<Object>>> getParsersByPrimitive() {
        Map<Class<?>, Function<String, Optional<Object>>> map = new HashMap<>();
        map.put(Integer.class, parseF(Integer::parseInt));
        map.put(Long.class, parseF(Long::parseLong));
        map.put(Double.class, parseF(Double::parseDouble));
        map.put(Float.class, parseF(Float::parseFloat));
        map.put(Boolean.class, parseF(Boolean::parseBoolean));
        map.put(Character.class, parseF(s -> s.charAt(0)));
        map.put(Byte.class, parseF(Byte::parseByte));
        map.put(Short.class, parseF(Short::parseShort));
        map.put(String.class, parseF(s -> s));
        return map;
    }

    private static <A> Optional<A> parseO(String s, Function<String, A> f) {
        try {
            return Optional.ofNullable(f.apply(s));
        } catch (Exception ex) {
            // ignored
        }
        return Optional.empty();
    }

    private static <A> Function<String, Optional<A>> parseF(Function<String, A> f) {
        return s -> parseO(s, f);
    }

    public static Optional<Object> invokeMethod(Object instance, String methodName, FieldValue... params) {
        try {
            Class<?>[] paramTypes = new Class[params.length];
            Object[] paramValues = new Object[params.length];
            for (int i = 0; i < paramValues.length; i++) {
                paramTypes[i] = params[i].getFieldType().getJavaClass();
                paramValues[i] = params[i].getFieldValue();
            }
            Method method = instance.getClass().getMethod(methodName, paramTypes);
            return Optional.ofNullable(method.invoke(instance, paramValues));
        } catch (Exception ex) {
            throw new IllegalStateException(String.format("%s, %s, %s", instance.getClass().getName(), methodName, Arrays.toString(params)), ex);
        }
    }

    public static boolean checkPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || parserByPrimitives.containsKey(clazz);
    }

    public static Optional<Class<?>> findClass(String name) {
        try {
            return Optional.of(Class.forName(name));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    public static Optional<Class<?>> getRawTypeClass(Type type) {
        if (type instanceof Class) {
            return Optional.of(((Class<?>) type));
        }
        if (type instanceof ParameterizedType) {
            return Optional.ofNullable(((Class<?>) ((ParameterizedType) type).getRawType()));
        }
        return Optional.empty();
    }
}
