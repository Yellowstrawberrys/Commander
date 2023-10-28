package xyz.yellowstrawberry.commander;

import org.bukkit.plugin.Plugin;
import xyz.yellowstrawberry.commander.annotations.Command;
import xyz.yellowstrawberry.commander.utils.ParameterConverter;
import xyz.yellowstrawberry.commander.utils.ReturnComputer;

import java.lang.reflect.Method;

public final class Commander {
    private final Plugin plugin;
    public Commander(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommands(Object o) {
        for(Method method : o.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                plugin.getServer().getCommandMap().register(plugin.getName(), new CommandHandler(method.getAnnotation(Command.class), o));
            }
        }
    }

    public static void addReturnComputers(Class<?> target, ReturnComputer returnComputer) {
        CommandHandler.addReturnComputers(target, returnComputer);
    }
    public static void addParameterConverter(Class<?> target, ParameterConverter<?> converter) {
        CommandHandler.addParameterConverter(target, converter);
    }
}
