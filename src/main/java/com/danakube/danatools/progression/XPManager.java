package com.danakube.danatools.progression;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.CustomTool;
import com.danakube.danatools.model.ToolInstance;

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

    public int applyLearningBoost(ToolInstance tool, int baseXP) {
        if (tool == null || baseXP <= 0) return baseXP;
        int xpGain = baseXP;
        if (tool.hasModifier("learning")) {
            int learningLvl = tool.getModifierLevel("learning");
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
