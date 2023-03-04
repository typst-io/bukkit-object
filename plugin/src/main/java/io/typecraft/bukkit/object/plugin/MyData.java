package io.typecraft.bukkit.object.plugin;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.bukkit.inventory.ItemStack;

@Data
@Builder
public class MyData {
    private final String name;
    private final ItemStack item;
}
