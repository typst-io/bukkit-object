package io.typecraft.bukkit.object;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MySubData {
    String name;
    int number;
}
