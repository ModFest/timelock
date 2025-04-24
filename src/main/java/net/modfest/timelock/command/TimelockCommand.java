package net.modfest.timelock.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.command.argument.AreaArgument;
import net.modfest.timelock.Timelock;
import net.modfest.timelock.world.TimelockAreaComponent;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TimelockCommand {
    public static int setZone(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Area area = AreaArgument.getArea(context, "area");
        long ticks = LongArgumentType.getLong(context, "ticks");
        boolean offset = BoolArgumentType.getBool(context, "offset");
        area.put(context.getSource().getServer(), Timelock.TIMELOCK_AREA_COMPONENT, new TimelockAreaComponent(ticks, offset));
        context.getSource().sendFeedback(() -> Text.translatable("timelock.command.set.success"), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int clearZone(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Area area = AreaArgument.getArea(context, "area");
        area.remove(context.getSource().getServer(), Timelock.TIMELOCK_AREA_COMPONENT);
        context.getSource().sendFeedback(() -> Text.translatable("timelock.command.remove.success"), true);
        return Command.SINGLE_SUCCESS;
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) ->
                dispatcher.register(literal("timelock")
                        .then(literal("zone")
                                .then(literal("set")
                                        .then(argument("area", AreaArgument.area())
                                                .then(argument("ticks", LongArgumentType.longArg(0L, 24000L))
                                                        .then(argument("offset", BoolArgumentType.bool())
                                                                .executes(TimelockCommand::setZone)))))
                                .then(literal("clear")
                                        .then(argument("area", AreaArgument.area())
                                                .executes(TimelockCommand::clearZone)))
                        .requires(source -> source.hasPermissionLevel(2)))));
    }
}
