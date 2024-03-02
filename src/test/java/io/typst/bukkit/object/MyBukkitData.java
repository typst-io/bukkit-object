package io.typst.bukkit.object;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;
import java.util.UUID;

@Data
@With
@Builder
public class MyBukkitData {
    private final String name;
    private final MyBukkitSubData subBukkitData;
    private final ConfigurationSection section;
    private final Set<UUID> uuids;
}
