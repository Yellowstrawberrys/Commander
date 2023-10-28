# Commander
> Makes Minecraft command more developer-friendly.


## Use
> [!WARNING]
> This library requires Paper-api 1.20.1+

You can implement this library with
#### Gradle
```groovy
repositories {
    maven {
        name = "roinSnapshots"
        url = uri("https://repo.roinz.xyz/snapshots")
    }
}

dependencies {
    implementation "xyz.yellowstrawberry:Commander:1.0.0-SNAPSHOT"
}
```

#### Maven
```xml
<repositories>
    <repository>
        <id>roin-snapshots</id>
        <name>Roin Repository</name>
        <url>https://repo.roinz.xyz/snapshots</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>xyz.yellowstrawberry</groupId>
        <artifactId>Commander</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## Code Example
#### YourPlugin.java
```java
public class YourPlugin extends JavaPlugin {
    Commander commander = new Commander(this);
    
    public void onEnable() {
        commander.registerCommands(new CustomCommand());
    }
    
    // ...
}
```

#### CustomCommand

```java
@Command("hello")
public class CustomCommand {

    @CommandListener(target = CommandTarget.PLAYER)
    public Component execute() {
        return Component.text("Hello, Commander!");
    }

    @SubCommand("world")
    public Component subcommand() {
        return Component.text("Hello, World!");
    }
    
    // ...
}
```