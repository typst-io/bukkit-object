package io.typst.bukkit.object;


import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Value
@Builder
public class MyData {
    UUID id;
    String name;
    MyEnum myEnum;
    Map<MyEnum, MyMapData> myEnumMap;
    MySubData subData;
    Map<Integer, MySubData> intMap;
    LocalDateTime localDateTime;
    LocalDate localDate;
    LocalTime localTime;
    Set<UUID> uuids;

    public String getCustomTransientGetter() {
        return "this not to be serialized";
    }
}
