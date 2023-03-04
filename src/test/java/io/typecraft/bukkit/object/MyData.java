package io.typecraft.bukkit.object;


import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
public class MyData {
    String name;
    MySubData subData;
}
