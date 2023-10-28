package xyz.yellowstrawberry.commander;

import org.bukkit.command.CommandSender;
import xyz.yellowstrawberry.commander.annotations.SubCommand;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SubCommandHandler {
    private final Object o;
    private final Method method;
    private final boolean visible;

    public SubCommandHandler(SubCommand command, Method method, Object o) {
        this.o = o;
        this.method = method;
        this.visible = command.visible();
    }

    protected boolean isVisible() {
        return visible;
    }

    public Object execute(CommandSender sender, String[] args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(o, CommandHandler.insertParameters(method, sender, args));
    }
}
