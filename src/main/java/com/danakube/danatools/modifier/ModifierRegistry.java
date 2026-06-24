package com.danakube.danatools.modifier;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.modifier.impl.AutoSmeltModifier;
import com.danakube.danatools.modifier.impl.CompactorModifier;
import com.danakube.danatools.modifier.impl.AutoSellModifier;
import com.danakube.danatools.modifier.impl.LearningModifier;
import com.danakube.danatools.modifier.impl.TrenchModifier;
import com.danakube.danatools.modifier.impl.VeinMinerModifier;
import com.danakube.danatools.modifier.impl.WisdomModifier;
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
        register(new WisdomModifier());
        register(new LearningModifier());
        register(new AutoSmeltModifier());
        register(new CompactorModifier());
        register(new AutoSellModifier());
    }

    private void register(DanaModifier modifier) {
        modifiers.put(modifier.getId(), modifier);
        Bukkit.getPluginManager().registerEvents(modifier, plugin);
    }

    public DanaModifier getModifier(String id) {
        return modifiers.get(id);
    }
}
