package dev.meowmayo.mmcore;

import dev.meowmayo.mmcore.commands.CoreCommands;
import dev.meowmayo.mmcore.rendering.MayoWorldRenderer;
import dev.meowmayo.mmcore.screen.MayoScreenRenderer;
import dev.meowmayo.mmcore.utils.PartyUtils;
import dev.meowmayo.mmcore.utils.ScoreboardUtils;
import net.fabricmc.api.ClientModInitializer;

public class MeowMayoCoreClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CoreCommands.register();

        PartyUtils.init();
        ScoreboardUtils.init();
        MayoWorldRenderer.init();
        MayoScreenRenderer.init();
	}
}