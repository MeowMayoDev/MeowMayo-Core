package dev.meowmayo.mmcore.commands;

import dev.meowmayo.mmcore.gui.MainGui;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

public class CoreCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("meowmayo")
                    .executes(context -> {
                        Minecraft.getInstance().execute(() -> {
                            Minecraft.getInstance().setScreen(new MainGui());
                        });
                        return 1;
                    })
            );

            dispatcher.register(ClientCommands.literal("mm")
                    .executes(context -> {
                        Minecraft.getInstance().execute(() -> {
                            Minecraft.getInstance().setScreen(new MainGui());
                        });
                        return 1;
                    })
            );
        });
    }
}
