package com.danakube.danatools.modifier;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DropManager {

    public static int calculateBlockExp(Block block, ItemStack tool) {
        if (tool != null && tool.containsEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH)) {
            return 0;
        }
        Material type = block.getType();
        return switch (type) {
            case COAL_ORE, DEEPSLATE_COAL_ORE -> randomBetween(0, 2);
            case NETHER_GOLD_ORE -> randomBetween(0, 1);
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE, EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> randomBetween(3, 7);
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE, NETHER_QUARTZ_ORE -> randomBetween(2, 5);
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> randomBetween(1, 5);
            case SPAWNER -> randomBetween(15, 43);
            case SCULK -> 1;
            case SCULK_CATALYST, SCULK_SHRIEKER, SCULK_SENSOR -> 5;
            default -> 0;
        };
    }

    private static int randomBetween(int min, int max) {
        return java.util.concurrent.ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static void breakBlock(Player player, Block block, ItemStack toolItem, int expToDrop) {
        // 1. Si le joueur est en créatif : aucun drop ni XP, suppression du bloc
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            block.setType(Material.AIR);
            return;
        }

        // 2. Récupération de l'état initial avant destruction (crucial pour Lumberjack qui vérifie state.getType())
        BlockState state = block.getState();
        Location dropLoc = block.getLocation().add(0.5, 0.5, 0.5);
        World world = block.getWorld();

        // 3. Calcul des drops en tenant compte des enchantements vanilla de l'outil (Fortune, Silk Touch, etc.)
        Collection<ItemStack> drops = block.getDrops(toolItem, player);

        // 4. Instanciation des entités Item en mémoire via world.createEntity (non ajoutées au monde)
        List<Item> itemEntities = new ArrayList<>();
        if (drops != null) {
            for (ItemStack drop : drops) {
                if (drop == null || drop.getType().isAir() || drop.getAmount() <= 0) continue;
                Item item = world.createEntity(dropLoc, Item.class);
                item.setItemStack(drop);
                itemEntities.add(item);
            }
        }

        // 5. Déclenchement de l'événement officiel Paper BlockDropItemEvent
        BlockDropItemEvent dropEvent = new BlockDropItemEvent(block, state, player, itemEntities);
        Bukkit.getPluginManager().callEvent(dropEvent);

        // 6. Si l'événement n'est pas annulé, faire spawner dans le monde les entités restantes
        if (!dropEvent.isCancelled()) {
            for (Item item : dropEvent.getItems()) {
                if (item != null && !item.isDead() && item.getItemStack() != null 
                        && !item.getItemStack().getType().isAir() 
                        && item.getItemStack().getAmount() > 0) {
                    world.addEntity(item);
                }
            }
        }

        // 7. Suppression du bloc d'origine (ou gestion du replantage)
        // Si AutoReplant a replanté, sa tâche asynchrone/synchrone remettra le jeune plant.
        block.setType(Material.AIR);

        // 8. Spawn de l'orbe d'expérience (incluant le multiplicateur Sagesse)
        if (expToDrop > 0) {
            spawnXP(dropLoc, expToDrop);
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
