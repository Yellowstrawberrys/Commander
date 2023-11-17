package xyz.yellowstrawberry.commander;

import org.bukkit.command.CommandSender;
import xyz.yellowstrawberry.commander.annotations.SubCommand;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class SubCommandHandler {
    private final Object o;
    private final Method method;
    private final boolean visible;
    private final Map<Integer, List<String>> parameters;

    public SubCommandHandler(SubCommand command, Method method, Object o, Map<Integer, List<String>> parameters) {
        this.o = o;
        this.method = method;
        this.visible = command.visible();
        this.parameters = parameters;
    }

    protected boolean isVisible() {
        return visible;
    }

    protected Method getMethod() {
        return method;
    }

    public Object execute(CommandSender sender, String[] args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(o, CommandHandler.insertParameters(method, sender, args));
    }

    public List<String> tabComplete(String[] args) {
        return
    }
}
