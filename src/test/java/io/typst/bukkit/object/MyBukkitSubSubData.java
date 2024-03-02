package io.typst.bukkit.object;

import lombok.Value;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@Value
@SerializableAs("bukkit-sub-data")
public class MyBukkitSubSubData implements ConfigurationSerializable {
    String name;
    int number;

    public MyBukkitSubSubData(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public MyBukkitSubSubData(Map<String, Object> xs) {
        this(
                xs.getOrDefault("name", "").toString(),
                Integer.parseInt(xs.getOrDefault("number", "").toString())
        );
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("name", getName());
        ret.put("number", getNumber());
        return ret;
    }
}
