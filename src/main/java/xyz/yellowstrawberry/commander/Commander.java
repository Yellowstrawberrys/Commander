package xyz.yellowstrawberry.commander;

import org.bukkit.plugin.Plugin;
import xyz.yellowstrawberry.commander.annotations.Command;
import xyz.yellowstrawberry.commander.utils.ParameterConverter;
import xyz.yellowstrawberry.commander.utils.ReturnComputer;

public final class Commander {
    private final Plugin plugin;
    public Commander(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommands(Object o) {
        if (o.getClass().isAnnotationPresent(Command.class)) {
            plugin.getServer().getCommandMap().register(plugin.getName(), new CommandHandler(o.getClass().getAnnotation(Command.class), o));
        }else throw new IllegalArgumentException(o.getClass().getSimpleName()+" doesn't have Command annotation");
    }

    public static void addReturnComputers(Class<?> target, ReturnComputer returnComputer) {
        CommandHandler.addReturnComputers(target, returnComputer);
    }
    public static void addParameterConverter(Class<?> target, ParameterConverter<?> converter) {
        CommandHandler.addParameterConverter(target, converter);
    }
}
