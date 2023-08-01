package com.acikek.timelock;

import com.acikek.timelock.command.TimelockCommand;
import com.acikek.timelock.network.TimelockNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Timelock implements ModInitializer {

    public static final String ID = "timelock";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Timelock...");
        TimelockNetworking.register();
        TimelockCommand.register();
    }
}
