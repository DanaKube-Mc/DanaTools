package com.danakube.danatools.modifier;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.DanaItemInstance;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Collection;
import java.util.Map;

public class DropManager {

    public static void breakBlock(Player player, Block block, ItemStack toolItem, int expToDrop) {
        DanaItemInstance tool = DanaItemInstance.fromItemStack(toolItem);
        
        if (tool != null && (tool.hasBehavior("AUTO_SMELT") || tool.hasBehavior("AUTO_SELL") || tool.hasBehavior("AUTO_REPLANT"))) {
            if (tool.hasBehavior("AUTO_REPLANT") && isReplantableCrop(block.getType())) {
                BlockData blockData = block.getBlockData();
                if (blockData instanceof Ageable ageable) {
                    if (ageable.getAge() == ageable.getMaximumAge()) {
                        Material seedMaterial = getRequiredSeed(block.getType());
                        if (seedMaterial != null) {
                            Collection<ItemStack> drops = block.getDrops(toolItem);
                            boolean seedConsumed = false;
                            for (ItemStack drop : drops) {
                                if (drop.getType() == seedMaterial) {
                                    drop.setAmount(drop.getAmount() - 1);
                                    seedConsumed = true;
                                    break;
                                }
                            }
                            if (seedConsumed) {
                                drops.removeIf(item -> item.getAmount() <= 0);
                                
                                double wisdomBoost = 0.0;
                                if (tool.hasBehavior("WISDOM")) {
                                    int wisdomLvl = tool.getBehaviorLevel("WISDOM");
                                    CustomModifier wisdomConfig = getModifierByBehavior("WISDOM");
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
                                double sellMultiplier = 1.0;
                                boolean hasAutoSell = tool.hasBehavior("AUTO_SELL");
                                if (hasAutoSell) {
                                    int autoSellLvl = tool.getBehaviorLevel("AUTO_SELL");
                                    CustomModifier autoSellConfig = getModifierByBehavior("AUTO_SELL");
                                    if (autoSellConfig != null) {
                                        CustomModifier.LevelSettings settings = autoSellConfig.getLevel(autoSellLvl);
                                        if (settings != null) {
                                            sellMultiplier = settings.getBehaviorDouble("multiplier", 1.0);
                                        }
                                    }
                                }

                                for (ItemStack drop : drops) {
                                    ItemStack finalDrop = drop;
                                    if (tool.hasBehavior("AUTO_SMELT")) {
                                        SmeltResult smelt = getSmeltResult(drop.getType());
                                        if (smelt != null) {
                                            double smeltingXp = drop.getAmount() * smelt.getXp();
                                            totalXp += smeltingXp * (1.0 + wisdomBoost);
                                            finalDrop = new ItemStack(smelt.getResult(), drop.getAmount());
                                        }
                                    }

                                    boolean sold = false;
                                    if (hasAutoSell) {
                                        sold = DanaTools.getInstance().getAutoSellManager().sellItem(player, finalDrop, sellMultiplier);
                                    }

                                    if (!sold) {
                                        block.getWorld().dropItemNaturally(block.getLocation(), finalDrop);
                                    }
                                }

                                ageable.setAge(0);
                                block.setBlockData(ageable, true);
                                block.getWorld().playSound(block.getLocation(), Sound.ITEM_CROP_PLANT, 1.0f, 1.0f);
                                if (totalXp > 0) {
                                    spawnXP(block.getLocation(), totalXp);
                                }
                                return;
                            }
                        }
                    }
                }
            }

            Collection<ItemStack> drops = block.getDrops(toolItem);
            
            double wisdomBoost = 0.0;
            if (tool.hasBehavior("WISDOM")) {
                int wisdomLvl = tool.getBehaviorLevel("WISDOM");
                CustomModifier wisdomConfig = getModifierByBehavior("WISDOM");
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
            
            double sellMultiplier = 1.0;
            boolean hasAutoSell = tool.hasBehavior("AUTO_SELL");
            if (hasAutoSell) {
                int autoSellLvl = tool.getBehaviorLevel("AUTO_SELL");
                CustomModifier autoSellConfig = getModifierByBehavior("AUTO_SELL");
                if (autoSellConfig != null) {
                    CustomModifier.LevelSettings settings = autoSellConfig.getLevel(autoSellLvl);
                    if (settings != null) {
                        sellMultiplier = settings.getBehaviorDouble("multiplier", 1.0);
                    }
                }
            }

            for (ItemStack drop : drops) {
                ItemStack finalDrop = drop;
                
                if (tool.hasBehavior("AUTO_SMELT")) {
                    SmeltResult smelt = getSmeltResult(drop.getType());
                    if (smelt != null) {
                        double smeltingXp = drop.getAmount() * smelt.getXp();
                        totalXp += smeltingXp * (1.0 + wisdomBoost);
                        finalDrop = new ItemStack(smelt.getResult(), drop.getAmount());
                    }
                }

                boolean sold = false;
                if (hasAutoSell) {
                    sold = DanaTools.getInstance().getAutoSellManager().sellItem(player, finalDrop, sellMultiplier);
                }

                if (!sold) {
                    block.getWorld().dropItemNaturally(block.getLocation(), finalDrop);
                }
            }
            
            block.setType(Material.AIR);
            if (totalXp > 0) {
                spawnXP(block.getLocation(), totalXp);
            }
            return;
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

    public static CustomModifier getModifierByBehavior(String behaviorType) {
        for (CustomModifier modifier : DanaTools.getInstance().getModifierConfigManager().getModifiers()) {
            CustomModifier.LevelSettings settings = modifier.getLevel(1);
            if (settings != null && behaviorType.equalsIgnoreCase(settings.getBehaviorType())) {
                return modifier;
            }
        }
        return null;
    }

    public static boolean isReplantableCrop(Material blockType) {
        CustomModifier modifier = getModifierByBehavior("AUTO_REPLANT");
        if (modifier == null) return false;
        CustomModifier.LevelSettings settings = modifier.getLevel(1);
        if (settings == null) return false;

        Object cropsObj = settings.getBehaviorSettings().get("crops");
        if (cropsObj instanceof ConfigurationSection sec) {
            return sec.contains(blockType.name());
        } else if (cropsObj instanceof Map<?, ?> map) {
            return map.containsKey(blockType.name());
        }
        return false;
    }

    public static Material getRequiredSeed(Material blockType) {
        CustomModifier modifier = getModifierByBehavior("AUTO_REPLANT");
        if (modifier == null) return null;
        CustomModifier.LevelSettings settings = modifier.getLevel(1);
        if (settings == null) return null;

        Object cropsObj = settings.getBehaviorSettings().get("crops");
        String seedName = null;
        if (cropsObj instanceof ConfigurationSection sec) {
            seedName = sec.getString(blockType.name());
        } else if (cropsObj instanceof Map<?, ?> map) {
            Object val = map.get(blockType.name());
            if (val != null) {
                seedName = val.toString();
            }
        }
        if (seedName != null) {
            return Material.matchMaterial(seedName);
        }
        return null;
    }
}
