package net.modfest.timelock.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TimelockClientCommand {

    public static final SimpleCommandExceptionType NO_SELECTION = new SimpleCommandExceptionType(Text.translatable("error.timelock.no_selection_client"));

    public static int abortSelection(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        var zone = TimelockClient.getSelectionZone();
        if (zone == null) {
            throw NO_SELECTION.create();
        }
        TimelockClient.clearSelection();
        TimelockClient.resetTimelock();
        context.getSource().sendFeedback(Text.translatable("command.timelock.selection.abort_client", zone.toString()));
        return Command.SINGLE_SUCCESS;
    }

    public static int commitSelection(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (TimelockClient.getSelectionZone() == null) {
            throw NO_SELECTION.create();
        }
        TimelockClient.sendSelection();
        return Command.SINGLE_SUCCESS;
    }

    public static int debug(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        TimelockClient.debug();
        return Command.SINGLE_SUCCESS;
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("timelockc")
                        .then(literal("abort")
                                .executes(TimelockClientCommand::abortSelection))
                        .then(literal("commit")
                                .executes(TimelockClientCommand::commitSelection))
                        .then(literal("debug")
                                .executes(TimelockClientCommand::debug))));
    }
}
