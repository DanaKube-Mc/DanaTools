package com.danakube.danatools.progression;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomTool;

public class XPManager {

    private final DanaTools plugin;

    public XPManager(DanaTools plugin) {
        this.plugin = plugin;
    }

    /**
     * Calcule l'XP requise pour passer du niveau actuel au niveau suivant.
     * @param tool outil concerné
     * @param currentLevel niveau actuel de l'outil
     * @return montant d'xp requis pour monter d'un niveau
     */
    public int getXpRequiredFor(CustomTool tool, int currentLevel) {
        int base = tool.getXpCurveBase();
        double multiplier = tool.getXpCurveMultiplier();
        return (int) Math.round(base * Math.pow(currentLevel, multiplier));
    }
}
