package io.typst.bukkit.object;

import lombok.Value;

@Value(staticConstructor = "of")
public class FieldDef {
    String name;
    TypeDef fieldType;
    String getterName;
}
