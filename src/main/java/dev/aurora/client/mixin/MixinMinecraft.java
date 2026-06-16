package dev.aurora.client.mixin;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.impl.TickEvent;
import dev.aurora.client.gui.clickgui.ClickGuiScreen;
import dev.aurora.client.gui.hudeditor.HudEditorScreen;
import dev.aurora.client.module.impl.performance.FpsBooster;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Core client hooks: dispatches the per-tick event, opens Aurora screens via
 * default keybinds (Right Shift = Click GUI, Right Control = HUD editor), drives
 * the performance monitor each frame, and applies Dynamic FPS by lowering the
 * frame limit when the window is unfocused.
 */
@Mixin(MinecraftClient.class)
public abstract class MixinMinecraft {

    private boolean clickGuiKeyDown;
    private boolean hudEditorKeyDown;

    @Inject(method = "tick", at = @At("HEAD"))
    private void aurora$onTick(CallbackInfo ci) {
        Aurora.INSTANCE.getEventBus().post(TickEvent.INSTANCE);
        handleGuiKeys();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void aurora$onFrame(CallbackInfo ci) {
        Aurora.INSTANCE.getPerformanceMonitor().onFrame();
    }

    private void handleGuiKeys() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null) { clickGuiKeyDown = hudEditorKeyDown = false; return; }

        boolean clickGui = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (clickGui && !clickGuiKeyDown) mc.openScreen(new ClickGuiScreen());
        clickGuiKeyDown = clickGui;

        boolean hudEditor = Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        if (hudEditor && !hudEditorKeyDown) mc.openScreen(new HudEditorScreen());
        hudEditorKeyDown = hudEditor;
    }

    /**
     * Dynamic FPS: when the window loses focus and the feature is on, report a
     * reduced frame limit. {@code getFramerateLimit} is the vanilla accessor used
     * by the render loop's frame timer.
     */
    @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
    private void aurora$dynamicFps(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Integer> cir) {
        FpsBooster booster = FpsBooster.getActive();
        if (booster == null || !booster.isDynamicFps()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!org.lwjgl.opengl.Display.isActive()) {
            cir.setReturnValue(booster.getUnfocusedFps());
        }
    }
}
