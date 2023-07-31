package com.acikek.timelock;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Timelock implements ModInitializer {

    public static final String ID = "timelock";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Timelock...");
    }
}
