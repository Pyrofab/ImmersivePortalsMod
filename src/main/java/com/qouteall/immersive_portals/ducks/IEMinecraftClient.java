package com.qouteall.immersive_portals.ducks;

import net.minecraft.client.gl.GlFramebuffer;
import net.minecraft.client.gui.screen.Screen;

public interface IEMinecraftClient {
    void setFrameBuffer(GlFramebuffer buffer);
    
    Screen getCurrentScreen();
}
