package io.typecraft.bukkit.object;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BukkitObjectMapperTest {
    @Test
    public void test() {
        BukkitObjectMapper mapper = new BukkitObjectMapper();
        Map<MyEnum, MyMapData> myEnumMap = new HashMap<>();
        Map<MyEnum, String> mapA = new HashMap<>();
        mapA.put(MyEnum.A, "a");
        Map<MyEnum, String> mapB = new HashMap<>();
        mapB.put(MyEnum.B, "b");
        myEnumMap.put(MyEnum.A, MyMapData.builder().map(mapA).build());
        myEnumMap.put(MyEnum.B, MyMapData.builder().map(mapB).build());
        Map<Integer, MySubData> intMap = new HashMap<>();
        intMap.put(1, new MySubData("a", 1));
        intMap.put(2, new MySubData("b", 2));
        MyData source = new MyData(
                UUID.randomUUID(),
                "test",
                MyEnum.B,
                myEnumMap,
                new MySubData("sub", 1),
                intMap,
                LocalDateTime.now(),
                LocalDate.now(),
                LocalTime.now()
        );
        Map<String, Object> serialized = mapper.encode(source).getOrThrow();
        System.out.println(serialized);
        MyData deserialized = mapper.decode(serialized, MyData.class).getOrThrow();
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
        MemoryConfiguration section = new MemoryConfiguration();
        section.set("test", "a");
        MyBukkitData source = new MyBukkitData(
                "test",
                new MyBukkitSubData(
                        "sub",
                        new MyBukkitSubSubData("grandSub", 1)
                ),
                section
        );
        Map<String, Object> serialized = mapper.encode(source).getOrElse(Collections.emptyMap());
        YamlConfiguration configSave = new YamlConfiguration();
        serialized.forEach(configSave::set);
        String yaml = configSave.saveToString();
        System.out.println(yaml);
        YamlConfiguration configLoad = YamlConfiguration.loadConfiguration(new StringReader(yaml));
        MyBukkitData deserialized = mapper.decode(configLoad.getValues(false), MyBukkitData.class).getOrThrow();
        Assertions.assertEquals(
                source,
                deserialized != null && source.getSection().getValues(false).equals(deserialized.getSection().getValues(false))
                        ? deserialized.withSection(source.getSection())
                        : deserialized
        );
    }
}
