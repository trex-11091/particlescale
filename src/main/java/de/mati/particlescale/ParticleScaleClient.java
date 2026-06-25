package de.mati.particlescale;

import de.mati.particlescale.gui.ParticleScaleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ParticleScaleClient implements ClientModInitializer {

    // Eigene Sektion im Steuerungs-Menü (Übersetzung: key.category.particlescale.main)
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("particlescale", "main"));

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        // Gespeicherte Größen laden
        ParticleScaleConfig.load();

        // Taste zum Öffnen des GUI (Standard: P, im Steuerungs-Menü änderbar)
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.particlescale.open_gui",
                GLFW.GLFW_KEY_P,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                client.setScreen(new ParticleScaleScreen(client.currentScreen));
            }
        });
    }
}
