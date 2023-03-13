package io.typecraft.bukkit.object;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class MyMapData {
    Map<MyEnum, String> map;
}
