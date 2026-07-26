package dev.meowmayo.mmcore.config;

import org.lwjgl.glfw.GLFW;

public class ChatMacro {
    public int key;
    public boolean useShift;
    public boolean useCtrl;
    public String command;

    public ChatMacro(int key, boolean useShift, boolean useCtrl, String command) {
        this.key = key;
        this.useShift = useShift;
        this.useCtrl = useCtrl;
        this.command = command;
    }

    public String getKeyName() {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "NONE";
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name == null) name = "Key " + key;

        StringBuilder sb = new StringBuilder();
        if (useCtrl) sb.append("Ctrl+");
        if (useShift) sb.append("Shift+");
        sb.append(name.toUpperCase());
        return sb.toString();
    }
}
