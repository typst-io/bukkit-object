# bukkit-object

## Setup

```groovy
implementation 'io.typecraft:bukkit-object:0.2.0'
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
        myData = mapper.decode(config.getValues(false), MyData.class);
    }

    @Override
    public void onDisable() {
        // save
        YamlConfiguration config = new YamlConfiguration();
        mapper.encode(myData).forEach(config::set);
        config.save(file);
    }

    public File getConfigFile() {
        return new File(getDataFolder(), "config.yml");
    }
}
```
