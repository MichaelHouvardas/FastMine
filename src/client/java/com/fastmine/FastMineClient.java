package com.fastmine;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class FastMineClient implements ClientModInitializer {

	private static final KeyBinding BREAK_BLOCKS_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.fastmine.break_blocks", // Key name in the options menu
			GLFW.GLFW_KEY_B, // Key code for the key you want to bind
			"category.fastmine" // Category for the keybinding
	));

	private static final KeyBinding TOGGLE_REPEAT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.fastmine.toggle_repeat", // Key name in the options menu
			GLFW.GLFW_KEY_N, // Key code for the toggle key
			"category.fastmine" // Category for the keybinding
	));

	private boolean isRepeating = false;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) {
				return;
			}

			if (TOGGLE_REPEAT_KEY.wasPressed()) {
				isRepeating = !isRepeating;
			}

			if (BREAK_BLOCKS_KEY.wasPressed()) {
				Item heldItem = client.player.getMainHandStack().getItem();
				if (isPickaxe(heldItem) || isShovel(heldItem)) {
					breakBlocksAroundPlayer(client, client.world, heldItem);
				}
			}

			if (isRepeating) {
				Item heldItem = client.player.getMainHandStack().getItem();
				if (isPickaxe(heldItem) || isShovel(heldItem)) {
					breakBlocksAroundPlayer(client, client.world, heldItem);
				}
			}
		});
	}

	private boolean isPickaxe(Item item) {
		return item == Items.WOODEN_PICKAXE || item == Items.STONE_PICKAXE ||
				item == Items.IRON_PICKAXE || item == Items.GOLDEN_PICKAXE ||
				item == Items.DIAMOND_PICKAXE || item == Items.NETHERITE_PICKAXE;
	}

	private boolean isShovel(Item item) {
		return item == Items.WOODEN_SHOVEL || item == Items.STONE_SHOVEL ||
				item == Items.IRON_SHOVEL || item == Items.GOLDEN_SHOVEL ||
				item == Items.DIAMOND_SHOVEL || item == Items.NETHERITE_SHOVEL;
	}

	private void breakBlocksAroundPlayer(MinecraftClient client, World world, Item heldItem) {
		BlockPos playerPos = client.player.getBlockPos();
		int radius = 4;

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos pos = playerPos.add(x, y, z);
					Block block = world.getBlockState(pos).getBlock();

					if (isPickaxe(heldItem) && isMineable(block) && !isBelowPlayer(pos, playerPos)) {
						breakBlock(client, pos);
					}

					if (isShovel(heldItem) && isShovelable(block) && !isBelowPlayer(pos, playerPos)) {
						breakBlock(client, pos);
					}
				}
			}
		}
	}

	private boolean isMineable(Block block) {
		return block == Blocks.STONE || block == Blocks.DIORITE ||
				block == Blocks.ANDESITE || block == Blocks.SANDSTONE ||
				block == Blocks.GRANITE;
	}

	private boolean isShovelable(Block block) {
		return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK ||
				block == Blocks.SAND || block == Blocks.GRAVEL;
	}

	private boolean isBelowPlayer(BlockPos pos, BlockPos playerPos) {
		return pos.getY() < playerPos.getY();
	}

	private void breakBlock(MinecraftClient client, BlockPos pos) {
		client.interactionManager.attackBlock(pos, Direction.UP);
		client.player.swingHand(Hand.MAIN_HAND);
	}
}
