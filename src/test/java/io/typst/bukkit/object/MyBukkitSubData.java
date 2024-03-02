package io.typst.bukkit.object;

import lombok.Value;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@Value
@SerializableAs("bukkit-data")
public class MyBukkitSubData implements ConfigurationSerializable {
    String name;
    MyBukkitSubSubData subData;

    public MyBukkitSubData(String name, MyBukkitSubSubData subData) {
        this.name = name;
        this.subData = subData;
    }

    public MyBukkitSubData(Map<String, Object> xs) {
        this(
                xs.getOrDefault("name", "").toString(),
                (MyBukkitSubSubData) xs.get("subData")
        );
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("name", getName());
        ret.put("subData", getSubData());
        return ret;
    }
}
