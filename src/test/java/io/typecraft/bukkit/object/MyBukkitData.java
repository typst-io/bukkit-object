package io.typecraft.bukkit.object;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.bukkit.configuration.ConfigurationSection;

@Data
@With
@Builder
public class MyBukkitData {
    private final String name;
    private final MyBukkitSubData subBukkitData;
    private final ConfigurationSection section;
}
