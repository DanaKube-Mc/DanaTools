package com.danakube.danatools.modifier;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.util.HashMap;
import java.util.Map;

public class CompactorManager {

    private final Map<Material, CompactionRecipe> recipes = new HashMap<>();

    public void loadRecipes() {
        recipes.clear();
        CustomModifier modifier = DanaTools.getInstance().getModifierConfigManager().getModifier("compactor");
        if (modifier == null) return;

        CustomModifier.LevelSettings settings = modifier.getLevel(1);
        if (settings == null) return;

        Map<String, Object> behaviorSettings = settings.getBehaviorSettings();
        Object recipesObj = behaviorSettings.get("recipes");
        
        if (recipesObj instanceof ConfigurationSection sec) {
            for (String resultStr : sec.getKeys(false)) {
                Material resultMat = Material.matchMaterial(resultStr);
                if (resultMat == null) continue;
                
                Object ingredientObj = sec.get(resultStr);
                if (ingredientObj instanceof ConfigurationSection ingSec) {
                    for (String ingStr : ingSec.getKeys(false)) {
                        Material ingMat = Material.matchMaterial(ingStr);
                        if (ingMat == null) continue;
                        
                        int reqAmount = ingSec.getInt(ingStr);
                        recipes.put(ingMat, new CompactionRecipe(resultMat, reqAmount));
                    }
                } else if (ingredientObj instanceof Map<?, ?> ingMap) {
                    for (Map.Entry<?, ?> ingEntry : ingMap.entrySet()) {
                        Material ingMat = Material.matchMaterial(ingEntry.getKey().toString());
                        if (ingMat == null) continue;
                        
                        int reqAmount = ((Number) ingEntry.getValue()).intValue();
                        recipes.put(ingMat, new CompactionRecipe(resultMat, reqAmount));
                    }
                }
            }
        } else if (recipesObj instanceof Map<?, ?> yamlRecipes) {
            for (Map.Entry<?, ?> entry : yamlRecipes.entrySet()) {
                Material resultMat = Material.matchMaterial(entry.getKey().toString());
                if (resultMat == null) continue;
                
                Object val = entry.getValue();
                if (val instanceof Map<?, ?> ingredientMap) {
                    for (Map.Entry<?, ?> ingEntry : ingredientMap.entrySet()) {
                        Material ingMat = Material.matchMaterial(ingEntry.getKey().toString());
                        if (ingMat == null) continue;
                        
                        int reqAmount = ((Number) ingEntry.getValue()).intValue();
                        recipes.put(ingMat, new CompactionRecipe(resultMat, reqAmount));
                    }
                } else if (val instanceof ConfigurationSection ingSec) {
                    for (String ingStr : ingSec.getKeys(false)) {
                        Material ingMat = Material.matchMaterial(ingStr);
                        if (ingMat == null) continue;
                        
                        int reqAmount = ingSec.getInt(ingStr);
                        recipes.put(ingMat, new CompactionRecipe(resultMat, reqAmount));
                    }
                }
            }
        }
        
        DanaTools.getInstance().getLogger().info("Charge " + recipes.size() + " recettes de compaction.");
    }

    public void tryCompact(Player player, Material ingredient) {
        CompactionRecipe recipe = recipes.get(ingredient);
        if (recipe == null) return;

        PlayerInventory inv = player.getInventory();
        int totalAmount = 0;

        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == ingredient) {
                totalAmount += item.getAmount();
            }
        }

        if (totalAmount < recipe.requiredAmount()) {
            return;
        }

        int blocksToCraft = totalAmount / recipe.requiredAmount();
        int itemsToRemove = blocksToCraft * recipe.requiredAmount();

        int remainingToRemove = itemsToRemove;
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == ingredient) {
                if (item.getAmount() <= remainingToRemove) {
                    remainingToRemove -= item.getAmount();
                    contents[i] = null;
                } else {
                    item.setAmount(item.getAmount() - remainingToRemove);
                    remainingToRemove = 0;
                }
            }
            if (remainingToRemove <= 0) break;
        }
        inv.setContents(contents);

        ItemStack resultStack = new ItemStack(recipe.result(), blocksToCraft);
        HashMap<Integer, ItemStack> overflow = inv.addItem(resultStack);
        
        if (!overflow.isEmpty()) {
            for (ItemStack leftOver : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftOver);
            }
        }

        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.4f, 1.5f);
    }

    private record CompactionRecipe(Material result, int requiredAmount) {}
}
