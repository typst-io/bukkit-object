package io.typecraft.bukkit.object;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BukkitObjectMapper {
    private final Map<Class<?>, ClassDef> classDefMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public Map<String, Object> encode(Object x) {
        Object object = encodeObject(x);
        return object instanceof Map
                ? ((Map<String, Object>) object)
                : Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public <A> Optional<A> decode(Map<String, Object> xs, Class<A> clazz) {
        return ConfigurationSerializable.class.isAssignableFrom(clazz)
                ? decodeBukkitObject(xs).flatMap(a -> clazz.isInstance(a) ? Optional.of(clazz.cast(a)) : Optional.empty())
                : decodeJacksonObject(xs, clazz);
    }

    private <A> Optional<A> decodeJacksonObject(Map<String, Object> xs, Class<A> clazz) {
        ClassDef def = classDefMap.computeIfAbsent(clazz, k -> ClassDef.from(clazz));
        if (def.isEmpty()) {
            return Optional.empty();
        }
        try {
            Constructor<?> constructor = def.getBuilderClass().getDeclaredConstructor();
            constructor.setAccessible(true);
            Object builderInstance = constructor.newInstance();
            for (FieldDef field : def.getFields()) {
                Object value = xs.get(field.getName());
                if (value == null) {
                    continue;
                }
                if (value instanceof ConfigurationSection) {
                    value = ((ConfigurationSection) value).getValues(false);
                }
                ClassDef fieldClassDef = classDefMap.getOrDefault(field.getFieldType(), ClassDef.empty);
                if (fieldClassDef.isEmpty()) {
                    FieldValue fieldValue = FieldValue.of(field.getFieldType(), value);
                    Reflections.invokeMethod(builderInstance, field.getName(), fieldValue);
                } else {
                    Object subValue = decode((Map<String, Object>) value, fieldClassDef.getClassType()).orElse(null);
                    FieldValue subFieldValue = FieldValue.of(fieldClassDef.getClassType(), subValue);
                    Reflections.invokeMethod(builderInstance, field.getName(), subFieldValue);
                }
            }
            Object value = Reflections.invokeMethod(builderInstance, "build").orElse(null);
            return clazz.isInstance(value)
                    ? Optional.of(clazz.cast(value))
                    : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<ConfigurationSerializable> decodeBukkitObject(Map<String, Object> xs) {
        return Optional.ofNullable(ConfigurationSerialization.deserializeObject(xs));
    }

    private Object encodeObject(Object x) {
        if (x instanceof ConfigurationSerializable) {
            return encodeBukkitObject(((ConfigurationSerializable) x));
        }
        if (x instanceof Collection) {
            return ((Collection<?>) x).stream()
                    .map(this::encodeObject)
                    .collect(Collectors.toList());
        }
        if (x instanceof Map) {
            return ((Map<?, ?>) x).entrySet().stream()
                    .map(pair -> new SimpleEntry<>(pair.getKey(), encodeObject(pair.getValue())))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }
        return encodeJacksonObject(x);
    }

    private Object encodeJacksonObject(Object x) {
        // primitive
        if (Reflections.checkPrimitive(x.getClass())) {
            return x;
        }
        // object
        Class<?> clazz = x.getClass();
        ClassDef info = classDefMap.computeIfAbsent(clazz, k -> ClassDef.from(clazz));
        if (info.getFields().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> ret = new HashMap<>();
        for (FieldDef field : info.getFields()) {
            Object value = Reflections.invokeMethod(x, field.getGetterName()).orElse(null);
            Object encoded = value != null ? encodeObject(value) : null;
            if (encoded != null) {
                ret.put(field.getName(), encoded);
            }
        }
        return ret;
    }

    private Map<String, Object> encodeBukkitObject(ConfigurationSerializable x) {
        Map<String, Object> serialized = x.serialize();
        Map<String, Object> ret = new HashMap<>(serialized.size());
        for (Entry<String, Object> pair : serialized.entrySet()) {
            ret.put(pair.getKey(), encodeObject(pair.getValue()));
        }
        ret.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(x.getClass()));
        return ret;
    }
}
