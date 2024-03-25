# bukkit-object

## Setup

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.typst:bukkit-object:1.0.1'
}
```


## Usage

```java
import java.io.File;

class MyPlugin extends JavaPlugin {
    private final BukkitObjectMapper mapper = new BukkitObjectMapper();
    private MyData myData = MyData.empty;

    @Override
    public void onEnable() {
        // load
        YamlConfiguration config = YamlConfiguration.loadConfiguration(getConfigFile());
        myData = mapper.decode(config.getValues(false), MyData.class).getOrThrow();
    }

    @Override
    public void onDisable() {
        // save
        YamlConfiguration config = new YamlConfiguration();
        mapper.encode(myData).getOrThrow().forEach(config::set);
        config.save(file);
    }

    public File getConfigFile() {
        return new File(getDataFolder(), "config.yml");
    }
}
```
