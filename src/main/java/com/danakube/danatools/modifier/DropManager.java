package com.danakube.danatools.modifier;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.ToolInstance;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Collection;
import java.util.Map;

public class DropManager {

    public static void breakBlock(Player player, Block block, ItemStack toolItem, int expToDrop) {
        ToolInstance tool = ToolInstance.fromItemStack(toolItem);
        
        if (tool != null && tool.hasModifier("auto_smelt")) {
            Collection<ItemStack> drops = block.getDrops(toolItem);
            boolean customDropPerformed = false;
            
            double wisdomBoost = 0.0;
            if (tool.hasModifier("wisdom")) {
                int wisdomLvl = tool.getModifierLevel("wisdom");
                CustomModifier wisdomConfig = DanaTools.getInstance().getModifierConfigManager().getModifier("wisdom");
                if (wisdomConfig != null) {
                    CustomModifier.LevelSettings settings = wisdomConfig.getLevel(wisdomLvl);
                    if (settings != null) {
                        Object boostObj = settings.getBehaviorSettings().get("xp-boost");
                        if (boostObj instanceof Number num) {
                            wisdomBoost = num.doubleValue();
                        }
                    }
                }
            }

            double totalXp = expToDrop;

            for (ItemStack drop : drops) {
                SmeltResult smelt = getSmeltResult(drop.getType());
                if (smelt != null) {
                    double smeltingXp = drop.getAmount() * smelt.getXp();
                    totalXp += smeltingXp * (1.0 + wisdomBoost);
                    ItemStack cookedDrop = new ItemStack(smelt.getResult(), drop.getAmount());
                    block.getWorld().dropItemNaturally(block.getLocation(), cookedDrop);
                    customDropPerformed = true;
                } else {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                    customDropPerformed = true;
                }
            }
            
            if (customDropPerformed) {
                block.setType(Material.AIR);
                if (totalXp > 0) {
                    spawnXP(block.getLocation(), totalXp);
                }
                return;
            }
        }
        
        block.breakNaturally(toolItem);
        if (expToDrop > 0) {
            spawnXP(block.getLocation(), expToDrop);
        }
    }

    public static class SmeltResult {
        private final Material result;
        private final double xp;

        public SmeltResult(Material result, double xp) {
            this.result = result;
            this.xp = xp;
        }

        public Material getResult() {
            return result;
        }

        public double getXp() {
            return xp;
        }
    }

    public static SmeltResult getSmeltResult(Material raw) {
        CustomModifier config = DanaTools.getInstance().getModifierConfigManager().getModifier("auto_smelt");
        if (config == null) return null;

        CustomModifier.LevelSettings settings = config.getLevel(1);
        if (settings == null) return null;

        Object recipesObj = settings.getBehaviorSettings().get("recipes");
        if (recipesObj instanceof ConfigurationSection sec) {
            String name = raw.name();
            if (sec.contains(name)) {
                String resultMatStr = sec.getString(name + ".result");
                double xp = sec.getDouble(name + ".xp", 0.0);
                if (resultMatStr != null) {
                    Material resultMat = Material.matchMaterial(resultMatStr);
                    if (resultMat != null) {
                        return new SmeltResult(resultMat, xp);
                    }
                }
            }
        } else if (recipesObj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) recipesObj;
            Object entry = map.get(raw.name());
            if (entry instanceof Map) {
                Map<?, ?> entryMap = (Map<?, ?>) entry;
                Object resultObj = entryMap.get("result");
                Object xpObj = entryMap.get("xp");
                if (resultObj != null) {
                    Material resultMat = Material.matchMaterial(resultObj.toString());
                    double xp = 0.0;
                    if (xpObj instanceof Number) {
                        xp = ((Number) xpObj).doubleValue();
                    }
                    if (resultMat != null) {
                        return new SmeltResult(resultMat, xp);
                    }
                }
            }
        }
        return null;
    }

    public static void spawnXP(Location loc, double amount) {
        int xp = (int) amount;
        double remainder = amount - xp;
        if (Math.random() < remainder) {
            xp++;
        }
        if (xp > 0) {
            int finalXp = xp;
            loc.getWorld().spawn(loc, ExperienceOrb.class, orb -> orb.setExperience(finalXp));
        }
    }
}
