package io.typecraft.bukkit.object;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Constructor;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class BukkitObjectMapper {
    private final Map<String, ObjectDef> objectDefMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public Map<String, Object> encode(Object x) {
        Object object = encodeObject(x);
        return object instanceof Map
                ? ((Map<String, Object>) object)
                : Collections.emptyMap();
    }

    public <A> Optional<A> decode(Map<String, Object> xs, Class<A> clazz) {
        return ConfigurationSerializable.class.isAssignableFrom(clazz)
                ? decodeBukkitObject(xs).flatMap(a -> clazz.isInstance(a) ? Optional.of(clazz.cast(a)) : Optional.empty())
                : decodeLombokObject(xs, clazz);
    }

    private <A> Optional<A> decodeLombokObject(Map<String, Object> xs, Class<A> clazz) {
        ObjectDef def = objectDefMap.computeIfAbsent(clazz.getTypeName(), k -> ObjectDef.from(clazz));
        if (def.isEmpty()) {
            return Optional.empty();
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
                Object subValue = decodeObject(x, field.getFieldType());
                FieldValue subFieldValue = FieldValue.of(field.getFieldType(), subValue);
                Reflections.invokeMethod(builderInstance, field.getName(), subFieldValue);
            }
            Object value = Reflections.invokeMethod(builderInstance, "build").orElse(null);
            return clazz.isInstance(value)
                    ? Optional.of(clazz.cast(value))
                    : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Object decodeObject(Object x, TypeDef typeDef) {
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
            ).orElse(null);
        }
        List<TypeDef> typeParams = typeDef.getTypeParameters();
        if (x instanceof Map) {
            TypeDef kType = typeParams.size() >= 1 ? typeParams.get(0) : TypeDef.object;
            TypeDef vType = typeParams.size() >= 2 ? typeParams.get(1) : TypeDef.object;
            return ((Map<?, ?>) x).entrySet().stream()
                    .map(pair -> new SimpleEntry<>(
                            decodeObject(pair.getKey(), kType),
                            decodeObject(pair.getValue(), vType)
                    ))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        } else if (x instanceof Collection) {
            TypeDef vType = typeParams.size() >= 1 ? typeParams.get(0) : TypeDef.object;
            Collector<Object, ?, ? extends Collection<Object>> collector = Set.class.isAssignableFrom(typeDef.getJavaClass())
                    ? Collectors.toSet()
                    : Collectors.toList();
            return ((Collection<?>) x).stream()
                    .map(a -> decodeObject(a, vType))
                    .collect(collector);
        } else if (fieldClass == UUID.class) {
            return UUID.fromString(x.toString());
        } else if (fieldClass == Map.class && x instanceof ConfigurationSection) {
            return ((ConfigurationSection) x).getValues(false);
        } else if (Enum.class.isAssignableFrom(fieldClass)) {
            try {
                return Enum.valueOf((Class<Enum>) fieldClass, x.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Function<String, Optional<Object>> parser = Reflections.parserByPrimitives.get(fieldClass);
        return parser != null ? parser.apply(x.toString()).orElse(null) : x;
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
                    .map(pair -> new SimpleEntry<>(pair.getKey().toString(), encodeObject(pair.getValue())))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }
        return encodeLombokObject(x);
    }

    private Object encodeLombokObject(Object x) {
        // primitive
        if (Reflections.checkPrimitive(x.getClass()) || x instanceof ConfigurationSection) {
            return x;
        }
        // custom types - save
        if (x instanceof UUID) {
            return x.toString();
        } else if (x instanceof Enum) {
            return ((Enum<?>) x).name();
        }
        // object
        Class<?> clazz = x.getClass();
        ObjectDef info = objectDefMap.computeIfAbsent(clazz.getTypeName(), k -> ObjectDef.from(clazz));
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
