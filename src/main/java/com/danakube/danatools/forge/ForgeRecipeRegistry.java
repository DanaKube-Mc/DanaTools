package com.danakube.danatools.forge;

import com.danakube.danatools.DanaTools;
import com.danakube.danatools.model.CustomModifier;
import com.danakube.danatools.model.CustomTool;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;

import java.util.ArrayList;
import java.util.Collection;
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
            Collection<String> toolIds = modifier.getCompatibleTools();
            List<Material> compatibleMaterials = new ArrayList<>();

            if (toolIds == null || toolIds.isEmpty()) {
                for (CustomTool t : plugin.getToolConfigManager().getTools()) {
                    compatibleMaterials.add(t.getMaterial());
                }
            } else {
                for (String toolId : toolIds) {
                    CustomTool tool = plugin.getToolConfigManager().getTool(toolId);
                    if (tool != null) {
                        compatibleMaterials.add(tool.getMaterial());
                    }
                }
            }

            if (compatibleMaterials.isEmpty())
                continue;

            NamespacedKey key = new NamespacedKey(plugin, "forge_grouped_" + modifier.getId());

            try {
                RecipeChoice templateChoice = new RecipeChoice.MaterialChoice(modifier.getTemplateMaterial());
                RecipeChoice baseChoice = new RecipeChoice.MaterialChoice(compatibleMaterials);
                RecipeChoice additionChoice = new RecipeChoice.MaterialChoice(modifier.getIngredientMaterial());

                ItemStack dummyResult = new ItemStack(compatibleMaterials.get(0));

                SmithingTransformRecipe recipe = new SmithingTransformRecipe(
                        key,
                        dummyResult,
                        templateChoice,
                        baseChoice,
                        additionChoice);

                Bukkit.addRecipe(recipe);
                registeredKeys.add(key);
            } catch (Exception e) {
                plugin.getLogger().warning("Impossible d'enregistrer la recette groupée de forge pour "
                        + modifier.getId() + ": " + e.getMessage());
            }
        }

        if (!registeredKeys.isEmpty()) {
            plugin.getLogger().info(registeredKeys.size() + " recettes de forge groupées enregistrées pour le client.");
        }
    }

    public void unregisterRecipes() {
        for (NamespacedKey key : registeredKeys) {
            Bukkit.removeRecipe(key);
        }
        registeredKeys.clear();
    }
}
