package com.acikek.timelock.command;

import com.acikek.timelock.Timelock;
import com.acikek.timelock.network.TimelockNetworking;
import com.acikek.timelock.world.TimelockData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TimelockCommand {

    public static final DynamicCommandExceptionType INVALID_ZONE = new DynamicCommandExceptionType(
            id -> Text.translatable("error.timelock.invalid_zone", id)
    );

    public static int setZone(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        long time = LongArgumentType.getLong(context, "time");
        var data = TimelockData.get(context.getSource().getWorld());
        data.zones().put(id, time);
        data.markDirty();
        context.getSource().sendFeedback(() -> Text.translatable("command.timelock.zone.set", id, time), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int deleteZone(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        var data = TimelockData.get(context.getSource().getWorld());
        System.out.println(data.chunks());
        var zone = data.zones().remove(id);
        if (zone == null) {
            throw INVALID_ZONE.create(id);
        }
        var chunks = data.chunks().removeAll(id);
        data.markDirty();
        context.getSource().sendFeedback(() -> Text.translatable("command.timelock.zone.delete", id, chunks.size()), true);
        TimelockNetworking.s2cUpdateData(context.getSource().getWorld().getPlayers(), chunks, Optional.empty());
        return Command.SINGLE_SUCCESS;
    }

    public static int inspectZone(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        var data = TimelockData.get(context.getSource().getWorld());
        var time = data.zones().get(id);
        if (time == null) {
            throw INVALID_ZONE.create(id);
        }
        var chunks = data.chunks().get(id).size();
        context.getSource().sendFeedback(() -> Text.translatable("command.timelock.zone.inspect", id, time, chunks), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int startSelection(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "zone");
        if (!TimelockData.get(context.getSource().getWorld()).zones().containsKey(id)) {
            throw INVALID_ZONE.create(id);
        }
        Timelock.LOGGER.debug("Sending selection start to client ({})", context.getSource().getPlayer().getUuid());
        TimelockNetworking.s2cStartSelection(context.getSource().getPlayer(), id);
        return Command.SINGLE_SUCCESS;
    }

    public static CompletableFuture<Suggestions> suggestZones(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        var data = TimelockData.get(context.getSource().getWorld());
        for (var id : data.zones().keySet()) {
            builder.suggest(id.toString());
        }
        return builder.buildFuture();
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) ->
                dispatcher.register(literal("timelock")
                        .then(literal("zone")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .suggests(TimelockCommand::suggestZones)
                                        .then(literal("set")
                                                .then(argument("time", LongArgumentType.longArg(0L, 24000L))
                                                        .executes(TimelockCommand::setZone)))
                                        .then(literal("delete")
                                                .executes(TimelockCommand::deleteZone))
                                        .then(literal("info")
                                                .executes(TimelockCommand::inspectZone))))
                        .then(literal("start")
                                .then(argument("zone", IdentifierArgumentType.identifier())
                                        .suggests(TimelockCommand::suggestZones)
                                        .executes(TimelockCommand::startSelection)))
                        .requires(source -> source.hasPermissionLevel(4))));
    }
}
