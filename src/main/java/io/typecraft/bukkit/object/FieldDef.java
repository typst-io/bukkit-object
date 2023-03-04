package io.typecraft.bukkit.object;

import lombok.Value;

@Value(staticConstructor = "of")
public class FieldDef {
    String name;
    Class<?> fieldType;
    String getterName;
}
