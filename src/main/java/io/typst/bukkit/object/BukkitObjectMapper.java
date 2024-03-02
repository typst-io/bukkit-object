package io.typst.bukkit.object;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class BukkitObjectMapper {
    // TODO: async?
    private final Map<String, ObjectDef> objectDefMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public Result<Map<String, Object>> encode(Object x) {
        return encodeObject(x)
                .map(a -> a instanceof Map
                        ? ((Map<String, Object>) a)
                        : Collections.emptyMap());
    }

    public <A> Result<A> decode(Map<String, Object> xs, Class<A> clazz) {
        return ConfigurationSerializable.class.isAssignableFrom(clazz)
                ? Result.fromOptional(decodeBukkitObject(xs).flatMap(a -> clazz.isInstance(a) ? Optional.of(clazz.cast(a)) : Optional.empty()), IllegalStateException::new)
                : decodeLombokObject(xs, clazz);
    }

    private <A> Result<A> decodeLombokObject(Map<String, Object> xs, Class<A> clazz) {
        ObjectDef def = objectDefMap.computeIfAbsent(clazz.getTypeName(), k -> ObjectDef.from(clazz));
        if (def.isEmpty()) {
            return Result.failure(new IllegalArgumentException("Unknown object: " + clazz.getName()));
        }
        try {
            Constructor<?> constructor = def.getBuilderClass().getDeclaredConstructor();
            constructor.setAccessible(true);
            Object builderInstance = constructor.newInstance();
            for (FieldDef field : def.getFields()) {
                Object x = xs.get(field.getName());
                if (x == null) {
                    continue;
                }
                Result<Object> result = decodeObject(x, field.getFieldType());
                Throwable failure = result.getFailure().orElse(null);
                if (failure == null) {
                    Object subValue = result.get();
                    FieldValue subFieldValue = FieldValue.of(field.getFieldType(), subValue);
                    Reflections.invokeMethod(builderInstance, field.getName(), subFieldValue);
                } else {
                    return Result.failure(failure);
                }
            }
            Object value = Reflections.invokeMethod(builderInstance, "build").orElse(null);
            return clazz.isInstance(value)
                    ? Result.success(clazz.cast(value))
                    : Result.failure(new IllegalArgumentException(String.format("Expected %s, but %s.", clazz.getName(), value)));
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Result<Object> decodeObject(Object x, TypeDef typeDef) {
        if (x instanceof ConfigurationSection && !ConfigurationSection.class.isAssignableFrom(typeDef.getJavaClass())) {
            x = ((ConfigurationSection) x).getValues(false);
        }
        // for non typed
        Class<?> fieldClass = typeDef.getJavaClass();
        ObjectDef objectDef = objectDefMap.computeIfAbsent(
                fieldClass.getTypeName(),
                k -> ObjectDef.from(fieldClass)
        );
        if (!objectDef.isEmpty()) {
            return decode(
                    (Map<String, Object>) x,
                    objectDef.getObjectType().getJavaClass()
            ).map(a -> a);
        }
        List<TypeDef> typeParams = typeDef.getTypeParameters();
        // custom type - load
        if (x instanceof Map) {
            TypeDef kType = typeParams.size() >= 1 ? typeParams.get(0) : TypeDef.object;
            TypeDef vType = typeParams.size() >= 2 ? typeParams.get(1) : TypeDef.object;
            Map<?, ?> xs = (Map<?, ?>) x;
            Map<Object, Object> ret = new HashMap<>(xs.size());
            for (Entry<?, ?> pair : xs.entrySet()) {
                Result<Object> keyResult = decodeObject(pair.getKey(), kType);
                Result<Object> valueResult = decodeObject(pair.getValue(), vType);
                Throwable keyFailure = keyResult.getFailure().orElse(null);
                Throwable valueFailure = valueResult.getFailure().orElse(null);
                if (keyFailure != null) {
                    return keyResult;
                } else if (valueFailure != null) {
                    return valueResult;
                } else {
                    ret.put(keyResult.get(), valueResult.get());
                }
            }
            return Result.success(ret);
        } else if (x instanceof Collection) {
            TypeDef vType = typeParams.size() >= 1 ? typeParams.get(0) : TypeDef.object;
            Collection<?> xs = (Collection<?>) x;
            Collection<Object> ret = Set.class.isAssignableFrom(typeDef.getJavaClass())
                    ? new HashSet<>(xs.size())
                    : new ArrayList<>(xs.size());
            for (Object a : xs) {
                Result<Object> result = decodeObject(a, vType);
                Throwable failure = result.getFailure().orElse(null);
                if (failure == null) {
                    ret.add(result.get());
                } else {
                    return result;
                }
            }
            return Result.success(ret);
        } else if (fieldClass == UUID.class) {
            return Result.success(UUID.fromString(x.toString()));
        } else if (fieldClass == Map.class && x instanceof ConfigurationSection) {
            return Result.success(((ConfigurationSection) x).getValues(false));
        } else if (Enum.class.isAssignableFrom(fieldClass)) {
            try {
                return Result.success(Enum.valueOf((Class<Enum>) fieldClass, x.toString()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (fieldClass == LocalDateTime.class) {
            return Result.success(LocalDateTime.parse(x.toString()));
        } else if (fieldClass == LocalDate.class) {
            return Result.success(LocalDate.parse(x.toString()));
        } else if (fieldClass == LocalTime.class) {
            return Result.success(LocalTime.parse(x.toString()));
        } else if (fieldClass == Duration.class) {
            return Result.success(Duration.parse(x.toString()));
        }
        Function<String, Optional<Object>> parser = Reflections.parserByPrimitives.get(fieldClass);
        Object finalX = x;
        return parser != null
                ? Result.fromOptional(parser.apply(x.toString()), () -> new IllegalArgumentException(String.format("Expected %s, but %s.", fieldClass, finalX)))
                : Result.success(x);
    }

    private Optional<ConfigurationSerializable> decodeBukkitObject(Map<String, Object> xs) {
        return Optional.ofNullable(ConfigurationSerialization.deserializeObject(xs));
    }

    private Result<Object> encodeObject(Object x) {
        if (x instanceof ConfigurationSerializable) {
            return encodeBukkitObject(((ConfigurationSerializable) x)).map(a -> a);
        }
        if (x instanceof Collection) {
            Collection<?> xs = (Collection<?>) x;
            List<Object> ret = new ArrayList<>(xs.size());
            for (Object a : xs) {
                Result<Object> result = encodeObject(a);
                Throwable failure = result.getFailure().orElse(null);
                if (failure == null) {
                    ret.add(result.get());
                } else {
                    return Result.failure(failure);
                }
            }
            return Result.success(ret);
        }
        if (x instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) x;
            Map<String, Object> ret = new HashMap<>(map.size());
            for (Entry<?, ?> pair : map.entrySet()) {
                Result<Object> result = encodeObject(pair.getValue());
                Throwable failure = result.getFailure().orElse(null);
                if (failure == null) {
                    ret.put(pair.getKey().toString(), result.get());
                } else {
                    return Result.failure(failure);
                }
            }
            return Result.success(ret);
        }
        return encodeLombokObject(x);
    }

    private Result<Object> encodeLombokObject(Object x) {
        // primitive
        if (Reflections.checkPrimitive(x.getClass()) || x instanceof ConfigurationSection) {
            return Result.success(x);
        }
        // custom types - save
        if (x instanceof UUID) {
            return Result.success(x.toString());
        } else if (x instanceof Enum) {
            return Result.success(((Enum<?>) x).name());
        } else if (x instanceof LocalDateTime ||
                x instanceof LocalDate ||
                x instanceof LocalTime ||
                x instanceof Duration) {
            return Result.success(x.toString());
        }
        // object
        Class<?> clazz = x.getClass();
        ObjectDef info = objectDefMap.computeIfAbsent(clazz.getTypeName(), k -> ObjectDef.from(clazz));
        if (info.getFields().isEmpty()) {
            return Result.failure(new IllegalArgumentException("Unknown object: " + x.getClass().getName()));
        }
        Map<String, Object> ret = new HashMap<>();
        for (FieldDef field : info.getFields()) {
            Object value = Reflections.invokeMethod(x, field.getGetterName()).orElse(null);
            if (value == null) {
                continue;
            }
            Result<Object> result = encodeObject(value);
            Throwable failure = result.getFailure().orElse(null);
            if (failure == null) {
                ret.put(field.getName(), result.get());
            } else {
                return result;
            }
        }
        return Result.success(ret);
    }

    private Result<Map<String, Object>> encodeBukkitObject(ConfigurationSerializable x) {
        Map<String, Object> serialized = x.serialize();
        Map<String, Object> ret = new HashMap<>(serialized.size());
        for (Entry<String, Object> pair : serialized.entrySet()) {
            Result<Object> result = encodeObject(pair.getValue());
            Throwable failure = result.getFailure().orElse(null);
            if (failure == null) {
                ret.put(pair.getKey(), result.get());
            } else {
                return Result.failure(failure);
            }
        }
        ret.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(x.getClass()));
        return Result.success(ret);
    }
}
