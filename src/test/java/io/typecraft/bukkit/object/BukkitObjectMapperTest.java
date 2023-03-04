package io.typecraft.bukkit.object;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Map;

public class BukkitObjectMapperTest {
    @Test
    public void test() {
        BukkitObjectMapper mapper = new BukkitObjectMapper();
        MyData source = new MyData("test", new MySubData("sub", 1));
        Map<String, Object> serialized = mapper.encode(source);
        System.out.println(serialized);
        MyData deserialized = mapper.decode(serialized, MyData.class).orElse(null);
        Assertions.assertEquals(
                source,
                deserialized
        );
    }

    @Test
    public void bukkitTest() {
        ConfigurationSerialization.registerClass(MyBukkitSubData.class);
        ConfigurationSerialization.registerClass(MyBukkitSubSubData.class);
        BukkitObjectMapper mapper = new BukkitObjectMapper();
        MyBukkitData source = new MyBukkitData(
                "test",
                new MyBukkitSubData(
                        "sub",
                        new MyBukkitSubSubData("grandSub", 1)
                )
        );
        Map<String, Object> serialized = mapper.encode(source);
        YamlConfiguration configSave = new YamlConfiguration();
        serialized.forEach(configSave::set);
        String yaml = configSave.saveToString();
        System.out.println(yaml);
        YamlConfiguration configLoad = YamlConfiguration.loadConfiguration(new StringReader(yaml));
        MyBukkitData deserialized = mapper.decode(configLoad.getValues(false), MyBukkitData.class).orElse(null);
        Assertions.assertEquals(
                source,
                deserialized
        );
    }
}
