package io.typecraft.bukkit.object;

import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
public class MySubData {
    String name;
    int number;
}
