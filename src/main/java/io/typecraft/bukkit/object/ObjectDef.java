package io.typecraft.bukkit.object;

import lombok.Value;
import lombok.With;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Value(staticConstructor = "of")
@With
public class ObjectDef {
    Set<FieldDef> fields;
    TypeDef objectType;
    Class<?> builderClass;
    public static final ObjectDef empty = new ObjectDef(Collections.emptySet(), TypeDef.empty, Void.class);

    public boolean isEmpty() {
        return builderClass == Void.class;
    }

    public static ObjectDef from(Class<?> clazz) {
        // check lombok @Builder
        Class<?> builderclass = Reflections.findClass(String.format("%s$%sBuilder", clazz.getName(), clazz.getSimpleName())).orElse(null);
        TypeDef objectType = TypeDef.from(clazz).orElse(null);
        if (builderclass == null || objectType == null) {
            return ObjectDef.empty;
        }
        Set<FieldDef> fields = new HashSet<>();
        for (Method method : clazz.getDeclaredMethods()) {
            String name = method.getName();
            if (name.startsWith("get") && name.length() >= 4) {
                String fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                TypeDef fieldType = TypeDef.from(method.getGenericReturnType()).orElse(null);
                if (fieldType != null) {
                    fields.add(FieldDef.of(fieldName, fieldType, name));
                }
            }
        }
        return new ObjectDef(fields, objectType, builderclass);
    }
}
