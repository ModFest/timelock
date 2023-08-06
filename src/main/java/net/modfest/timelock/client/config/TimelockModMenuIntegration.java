package net.modfest.timelock.client.config;

import net.modfest.timelock.client.TimelockClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

public class TimelockModMenuIntegration implements ModMenuApi {

    public YetAnotherConfigLib createConfig() {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Timelock"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("Timelock"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.timelock.enable.name"))
                                .description(OptionDescription.of(Text.translatable("config.timelock.enable.description")))
                                .flag(OptionFlag.WORLD_RENDER_UPDATE)
                                .binding(TimelockClient.ENABLE_BINDING)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")
                ? screen -> createConfig().generateScreen(screen)
                : screen -> null;
    }
}
