package net.modfest.timelock;

import dev.doublekekse.area_lib.component.AreaDataComponentType;
import dev.doublekekse.area_lib.registry.AreaDataComponentTypeRegistry;
import net.modfest.timelock.command.TimelockCommand;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.modfest.timelock.world.TimelockAreaComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Timelock implements ModInitializer {

    public static final String ID = "timelock";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static Identifier id(String path) {
        return Identifier.of(ID, path);
    }

    public static final AreaDataComponentType<TimelockAreaComponent> TIMELOCK_AREA_COMPONENT =
            AreaDataComponentTypeRegistry.registerTracking(Timelock.id("time_zone"), TimelockAreaComponent::new);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Timelock...");
        TimelockCommand.register();
    }
}
