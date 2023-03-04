package io.typecraft.bukkit.object.plugin;

import io.typecraft.bukkit.object.BukkitObjectMapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ObjectPlugin extends JavaPlugin {
    private final BukkitObjectMapper mapper = new BukkitObjectMapper();

    @Override
    public void onEnable() {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = sender instanceof Player ? ((Player) sender) : null;
        if (!sender.isOp() || p == null) {
            return true;
        }
        String head = args.length >= 1 ? args[0] : "";
        if (head.equalsIgnoreCase("save")) {
            ItemStack handItem = p.getInventory().getItemInMainHand();
            YamlConfiguration config = new YamlConfiguration();
            Map<String, Object> encoded = mapper.encode(new MyData(p.getName(), handItem));
            encoded.forEach(config::set);
            try {
                config.save(new File(getDataFolder(), "config.yml"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (head.equalsIgnoreCase("load")) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
            MyData myData = mapper.decode(config.getValues(false), MyData.class).orElse(null);
            if (myData != null) {
                sender.sendMessage("Author: " + myData.getName());
                p.getInventory().addItem(myData.getItem());
            }
        } else if (head.equalsIgnoreCase("saveraw")) {
            ItemStack handItem = p.getInventory().getItemInMainHand();
            YamlConfiguration config = new YamlConfiguration();
            config.set("item", handItem);
            try {
                config.save(new File(getDataFolder(), "configraw.yml"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (head.equalsIgnoreCase("loadraw")) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "configraw.yml"));
            ItemStack item = config.getItemStack("item");
            if (item != null) {
                p.getInventory().addItem(item);
            }
        }
        return true;
    }
}
