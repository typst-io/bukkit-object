package io.typst.bukkit.object;

import lombok.Value;

@Value(staticConstructor = "of")
public class FieldValue {
    TypeDef fieldType;
    Object fieldValue;
}
