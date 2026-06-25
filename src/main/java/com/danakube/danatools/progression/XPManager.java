package com.danakube.danatools.progression;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.CustomTool;
import org.bukkit.entity.Player;
import com.danakube.danatools.modifier.DanaModifier;

public class XPManager {

    private final DanaTools plugin;

    public XPManager(DanaTools plugin) {
        this.plugin = plugin;
    }

    public int getXpRequiredFor(CustomTool tool, int currentLevel) {
        int base = tool.getXpCurveBase();
        double multiplier = tool.getXpCurveMultiplier();
        return (int) Math.round(base * Math.pow(currentLevel, multiplier));
    }

    public int applyLearningBoost(Player player, int baseXP) {
        if (player == null || baseXP <= 0) return baseXP;
        int xpGain = baseXP;
        int learningLvl = DanaModifier.getHighestModifierLevel(player, "learning");
        if (learningLvl > 0) {
            CustomModifier learningConfig = plugin.getModifierConfigManager().getModifier("learning");
            if (learningConfig != null) {
                CustomModifier.LevelSettings settings = learningConfig.getLevel(learningLvl);
                if (settings != null) {
                    Object boostObj = settings.getBehaviorSettings().get("xp-boost");
                    if (boostObj instanceof Number num) {
                        xpGain = (int) Math.round(xpGain * (1.0 + num.doubleValue()));
                    }
                }
            }
        }
        return xpGain;
    }
}
