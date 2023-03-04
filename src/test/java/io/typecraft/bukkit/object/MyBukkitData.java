package io.typecraft.bukkit.object;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
public class MyBukkitData {
    private final String name;
    private final MyBukkitSubData subBukkitData;
}
