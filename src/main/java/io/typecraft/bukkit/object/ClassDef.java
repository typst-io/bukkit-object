package io.typecraft.bukkit.object;

import lombok.Value;
import lombok.With;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Value(staticConstructor = "of")
@With
public class ClassDef {
    Set<FieldDef> fields;
    Class<?> classType;
    Class<?> builderClass;
    public static final ClassDef empty = new ClassDef(Collections.emptySet(), Void.class, Void.class);

    public boolean isEmpty() {
        return builderClass == Void.class;
    }

    public static ClassDef from(Class<?> clazz) {
        // fields
        Set<FieldDef> fields = new HashSet<>();
        for (Method method : clazz.getDeclaredMethods()) {
            String name = method.getName();
            if (name.startsWith("get") && name.length() >= 4) {
                String fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                fields.add(FieldDef.of(fieldName, method.getReturnType(), name));
            }
        }
        // builder class
        Class<?> builderclass = Reflections.findClass(String.format("%s$%sBuilder", clazz.getName(), clazz.getSimpleName())).orElse(null);
        if (builderclass != null) {
            return new ClassDef(fields, clazz, builderclass);
        }
        return ClassDef.empty;
    }
}
