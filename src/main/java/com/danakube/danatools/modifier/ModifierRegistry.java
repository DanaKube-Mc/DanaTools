package com.danakube.danatools.modifier;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.modifier.impl.TrenchModifier;
import com.danakube.danatools.modifier.impl.VeinMinerModifier;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class ModifierRegistry {
    private final DanaTools plugin;
    private final Map<String, DanaModifier> modifiers = new HashMap<>();

    public ModifierRegistry(DanaTools plugin) {
        this.plugin = plugin;
    }

    public void registerDefaultModifiers() {
        register(new VeinMinerModifier());
        register(new TrenchModifier());
    }

    private void register(DanaModifier modifier) {
        modifiers.put(modifier.getId(), modifier);
        Bukkit.getPluginManager().registerEvents(modifier, plugin);
    }

    public DanaModifier getModifier(String id) {
        return modifiers.get(id);
    }
}
