package io.typecraft.bukkit.object;

import lombok.Value;

@Value(staticConstructor = "of")
public class FieldValue {
    Class<?> fieldType;
    Object fieldValue;
}
