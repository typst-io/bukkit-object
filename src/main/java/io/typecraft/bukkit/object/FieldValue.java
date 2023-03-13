package io.typecraft.bukkit.object;

import lombok.Value;

import java.lang.reflect.Type;

@Value(staticConstructor = "of")
public class FieldValue {
    TypeDef fieldType;
    Object fieldValue;
}
