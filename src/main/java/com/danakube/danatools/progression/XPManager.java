package com.danakube.danatools.progression;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomTool;

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
}
