package io.github.thebusybiscuit.slimefun4.implementation.items.multiblocks;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.Lists.Categories;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.multiblocks.MultiBlockMachine;

public class Compressor extends MultiBlockMachine {

	public Compressor() {
		super(
				Categories.MACHINES_1, 
				SlimefunItems.COMPRESSOR, 
				new ItemStack[] {null, null, null, null, new ItemStack(Material.NETHER_BRICK_FENCE), null, new ItemStack(Material.PISTON), new CustomItem(Material.DISPENSER, "Dispenser (Facing up)"), new ItemStack(Material.PISTON)},
				new ItemStack[] {
					new CustomItem(SlimefunItems.STONE_CHUNK, 4), new ItemStack(Material.COBBLESTONE),
					new ItemStack(Material.FLINT, 8), new ItemStack(Material.COBBLESTONE)
				},
				BlockFace.SELF
		);
	}
	
	@Override
	public List<ItemStack> getDisplayRecipes() {
		return recipes.stream().map(items -> items[0]).collect(Collectors.toList());
	}
	
	@Override
	public void onInteract(Player p, Block b) {
		Block dispBlock = b.getRelative(BlockFace.DOWN);
		Dispenser disp = (Dispenser) dispBlock.getState();
		Inventory inv = disp.getInventory();
		
		for (ItemStack item : inv.getContents()) {
			for (ItemStack recipeInput : RecipeType.getRecipeInputs(this)) {
				if (recipeInput != null && SlimefunUtils.isItemSimilar(item, recipeInput, true)) {
					ItemStack output = RecipeType.getRecipeOutput(this, recipeInput);
					Inventory outputInv = findOutputInventory(output, dispBlock, inv);
					
					if (outputInv != null) {
					    ItemStack removing = item.clone();
				        removing.setAmount(recipeInput.getAmount());
				        inv.removeItem(removing);

						craft(p, output, outputInv);
					}
					else {
					    SlimefunPlugin.getLocal().sendMessage(p, "machines.full-inventory", true);
					}
					
					return;
				}
			}
		}
		
		SlimefunPlugin.getLocal().sendMessage(p, "machines.unknown-material", true);
	}

    private void craft(Player p, ItemStack output, Inventory outputInv) {
        for (int i = 0; i < 4; i++) {
            int j = i;
            
            Bukkit.getScheduler().runTaskLater(SlimefunPlugin.instance, () -> {
                if (j < 3) {
                    p.getWorld().playSound(p.getLocation(), j == 1 ? Sound.BLOCK_PISTON_CONTRACT : Sound.BLOCK_PISTON_EXTEND, 1F, j == 0 ? 1F : 2F);
                } 
                else {
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
                    outputInv.addItem(output);
                }
            }, i * 20L);
        }
    }

}
