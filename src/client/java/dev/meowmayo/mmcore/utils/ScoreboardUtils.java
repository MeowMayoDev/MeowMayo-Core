package dev.meowmayo.mmcore.utils;

import dev.meowmayo.mmcore.events.MMPacketEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;

import java.util.Optional;

public class ScoreboardUtils {
    static String location = "";

    public static String getLocation() {
        return location;
    }

    public static void init() {
        MMPacketEvents.RECEIVE.register(packet -> {
            if (packet instanceof ClientboundSetPlayerTeamPacket teamPacket) {
                Optional<ClientboundSetPlayerTeamPacket.Parameters> params = teamPacket.getParameters();

                if (params.isPresent()) {
                    ClientboundSetPlayerTeamPacket.Parameters parameters = params.get();

                    String pre = parameters.playerPrefix().getString();
                    String post = parameters.playerSuffix().getString();

                    String line = ChatFormatting.stripFormatting(pre + post).trim();
                    if (line.startsWith("\uE067")) {
                        location = line;
                    }
                }
            }
        });
    }
}
