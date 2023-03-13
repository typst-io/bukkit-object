package io.typecraft.bukkit.object;


import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class MyData {
    UUID id;
    String name;
    MyEnum myEnum;
    Map<MyEnum, MyMapData> myEnumMap;
    MySubData subData;
}
