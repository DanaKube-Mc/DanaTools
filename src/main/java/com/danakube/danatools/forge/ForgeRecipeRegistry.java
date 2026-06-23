package com.danakube.danatools.forge;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.CustomTool;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;

import java.util.ArrayList;
import java.util.List;

public class ForgeRecipeRegistry {

    private final DanaTools plugin;
    private final List<NamespacedKey> registeredKeys = new ArrayList<>();

    public ForgeRecipeRegistry(DanaTools plugin) {
        this.plugin = plugin;
    }

    public void registerRecipes() {
        unregisterRecipes();

        for (CustomModifier modifier : plugin.getModifierConfigManager().getModifiers()) {
            java.util.Collection<String> toolIds = modifier.getCompatibleTools();
            if (toolIds == null || toolIds.isEmpty()) {
                toolIds = new java.util.ArrayList<>();
                for (CustomTool t : plugin.getToolConfigManager().getTools()) {
                    toolIds.add(t.getId());
                }
            }

            for (String toolId : toolIds) {
                CustomTool tool = plugin.getToolConfigManager().getTool(toolId);
                if (tool == null) continue;

                NamespacedKey key = new NamespacedKey(plugin, "forge_" + modifier.getId() + "_" + tool.getId());

                try {
                    RecipeChoice templateChoice = new RecipeChoice.MaterialChoice(modifier.getTemplateMaterial());
                    RecipeChoice baseChoice = new RecipeChoice.MaterialChoice(tool.getMaterial());
                    RecipeChoice additionChoice = new RecipeChoice.MaterialChoice(modifier.getIngredientMaterial());

                    ItemStack result = new ItemStack(tool.getMaterial());

                    SmithingTransformRecipe recipe = new SmithingTransformRecipe(
                            key,
                            result,
                            templateChoice,
                            baseChoice,
                            additionChoice
                    );

                    Bukkit.addRecipe(recipe);
                    registeredKeys.add(key);
                } catch (Exception e) {
                    plugin.getLogger().warning("Impossible d'enregistrer la recette de forge pour " + modifier.getId() + " et " + tool.getId() + ": " + e.getMessage());
                }
            }
        }

        if (!registeredKeys.isEmpty()) {
            plugin.getLogger().info(registeredKeys.size() + " recettes de forge enregistrees pour le client.");
        }
    }

    public void unregisterRecipes() {
        for (NamespacedKey key : registeredKeys) {
            Bukkit.removeRecipe(key);
        }
        registeredKeys.clear();
    }
}
