package dev.meowmayo.mmcore.gui.componenets;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class MeowScreen extends Screen { // pointless class just to store a name for the gui building LOL!! ill probably add more functionality in the future
    public String name;
    protected MeowScreen(Component title, String name) {
        super(title);
        this.name = name;
    }
}
