package xyz.yellowstrawberry.commander;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.yellowstrawberry.commander.annotations.Command;
import xyz.yellowstrawberry.commander.annotations.CommandListener;
import xyz.yellowstrawberry.commander.annotations.SubCommand;
import xyz.yellowstrawberry.commander.enums.CommandEvent;
import xyz.yellowstrawberry.commander.enums.CommandTarget;
import xyz.yellowstrawberry.commander.utils.ParameterConverter;
import xyz.yellowstrawberry.commander.utils.ReturnComputer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandHandler extends BukkitCommand {
    private final static Map<Class<?>, ReturnComputer> returnComputers = new HashMap<>();
    private final static Map<Class<?>, ParameterConverter<?>> converters = new HashMap<>();

    protected static void addReturnComputers(Class<?> target, ReturnComputer returnComputer) {
        returnComputers.put(target, returnComputer);
    }
    protected static void addParameterConverter(Class<?> target, ParameterConverter<?> converter) {
        converters.put(target, converter);
    }

    private final Object o;
    private final Map<CommandEvent, List<Method>> listeners = new HashMap<>();
    private final Map<String, SubCommandHandler> subcommands = new HashMap<>();
    public CommandHandler(Command command, Object o) {
        super(command.value(), command.description(), command.usageMessage(), Arrays.asList(command.aliases()));
        this.o = o;
        for(Method method : o.getClass().getDeclaredMethods()) {
            if(method.isAnnotationPresent(CommandListener.class)) {
                CommandListener commandListener = method.getAnnotation(CommandListener.class);
                method.setAccessible(true);
                if(!listeners.containsKey(commandListener.listeningTo())) listeners.put(commandListener.listeningTo(), new ArrayList<>());
                listeners.get(commandListener.listeningTo()).add(method);
            }else if(method.isAnnotationPresent(SubCommand.class)) {
                SubCommand subCommand = method.getAnnotation(SubCommand.class);
                method.setAccessible(true);
                subcommands.put(subCommand.value(), new SubCommandHandler(subCommand, method, o));
            }
        }
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        Object l = null;
        Class<?> returnType = null;
        if (args.length >= 1 && subcommands.containsKey(args[0])) {
            try {
                l = subcommands.get(args[0]).execute(sender, args);
                returnType = l!=null?l.getClass():null;
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }else if(listeners.containsKey(CommandEvent.COMMAND)) {
            for (Method m : listeners.get(CommandEvent.COMMAND)) {
                try {
                    if (m.getAnnotation(CommandListener.class).target() == CommandTarget.PLAYER && !(sender instanceof Player))
                        return false;
                    l = m.invoke(o, CommandHandler.insertParameters(m, sender, args));
                    returnType = l!=null?l.getClass():null;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return l != null && computeReturn(returnType, l, sender);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, @Nullable Location location) throws IllegalArgumentException {
        List<String> tab = new ArrayList<>();
        if(args.length==1) {
            for(Map.Entry<String, SubCommandHandler> entry : subcommands.entrySet()) {
                if(entry.getValue().isVisible()) tab.add(entry.getKey());
            }
        }
        if(listeners.containsKey(CommandEvent.TABCOMPLETE)) {
            for (Method m : listeners.get(CommandEvent.TABCOMPLETE)) {
                if(m.getAnnotation(CommandListener.class).target()==CommandTarget.PLAYER && !(sender instanceof Player)) continue;
                if(List.class.isAssignableFrom(m.getReturnType())) {
                    try {
                        tab.addAll(List.class.cast(m.invoke(o, insertTabCompleteParameters(m, sender, alias, args, location))));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }else Logger.getAnonymousLogger().log(Level.WARNING, "Ignoring TABCOMPLETE event method '%s' due to method's return type(%s) cannot be assignable with java.utils.List".formatted(m.getClass().getSimpleName()+"#"+m.getName(), m.getReturnType().getName()));
            }
        }
        return tab;
    }
    private static boolean computeReturn(Class<?> returnType, Object l, CommandSender sender) {
        if(returnComputers.containsKey(returnType)) {
            return returnComputers.get(returnType).compute(sender, returnType.cast(l));
        }else if(Component.class.isAssignableFrom(returnType)) {
            sender.sendMessage((Component) l);
        }else if(boolean.class.isAssignableFrom(returnType) || Boolean.class.isAssignableFrom(returnType)) {
            System.out.println(l);
            return (boolean) l;
        }else {
            for(Map.Entry<Class<?>, ReturnComputer> entry : returnComputers.entrySet()) {
                if(entry.getKey().isAssignableFrom(l.getClass())) {
                    return entry.getValue().compute(sender, entry.getKey().cast(l));
                }
            }
            if(!returnType.equals(Void.class) && l != null) sender.sendMessage((String) l);
        }
        return false;
    }

    private static Object[] insertTabCompleteParameters(Method m, CommandSender sender, String alias, String[] args, Location loc) {
        Object[] parameters = new Object[m.getParameters().length];
        for(int i = 0; i<parameters.length; i++) {
            Class<?> type = m.getParameters()[i].getType();
            if (type.isAssignableFrom(Player.class)) {
                parameters[i] = (Player) sender;
            } else if (type.isAssignableFrom(CommandSender.class)) {
                parameters[i] = sender;
            } else if (type.isAssignableFrom(String[].class)) {
                parameters[i] = args;
            } else if (type.isAssignableFrom(Location.class)) {
                parameters[i] = loc;
            } else if (type.isAssignableFrom(String.class)) {
                parameters[i] = alias;
            }
        }
        return parameters;
    }

    protected static Object[] insertParameters(Method m, CommandSender sender, String[] args) {
        Object[] parameters = new Object[m.getParameters().length];
        for(int i = 0; i<parameters.length; i++) {
            java.lang.reflect.Parameter p = m.getParameters()[i];
            Class<?> type = m.getParameters()[i].getType();
            if(p.isAnnotationPresent(xyz.yellowstrawberry.commander.annotations.Parameter.class)) {
                xyz.yellowstrawberry.commander.annotations.Parameter param = p.getAnnotation(xyz.yellowstrawberry.commander.annotations.Parameter.class);
                if(args.length > param.value()) {
                    if(p.getType().equals(String.class)) parameters[i] = type.cast(args[param.value()]);
                    else if(converters.containsKey(type)) {
                        parameters[i] = converters.get(type).convert(args[param.value()]);
                    } else {
                        boolean isReturned = false;
                        for(Map.Entry<Class<?>, ParameterConverter<?>> entry : converters.entrySet()) {
                            if(entry.getKey().isAssignableFrom(type)) {
                                parameters[i] = entry.getValue().convert(args[param.value()]);
                                isReturned = true;
                                break;
                            }
                        }
                        if(!isReturned) throw new ClassCastException("String is not castable with '%s'.".formatted(type.getName()));
                    }
                }
            }else if(type.isAssignableFrom(Player.class)) {
                parameters[i] = (Player) sender;
            }else if(type.isAssignableFrom(CommandSender.class)) {
                parameters[i] = sender;
            }else if(type.isAssignableFrom(String[].class)) {
                parameters[i] = args;
            }
        }
        return parameters;
    }
}
