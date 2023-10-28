package xyz.yellowstrawberry.commander.utils;

import org.bukkit.command.CommandSender;

public interface ReturnComputer {
    <T> boolean compute(CommandSender sender, T t);
}
