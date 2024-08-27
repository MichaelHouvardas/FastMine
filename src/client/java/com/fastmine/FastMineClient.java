package com.fastmine;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.lwjgl.glfw.GLFW;

public class FastMineClient implements ClientModInitializer {

	private static KeyBinding toggleBreakBlockKeybind;
	private static KeyBinding breakBlockOnceKeybind;
	private boolean isRepeating = false; // Toggle flag for repeating block breaking

	@Override
	public void onInitializeClient() {
		// Register the keybindings
		toggleBreakBlockKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.betterbridging.toggle_break_block", // Translation key
				GLFW.GLFW_KEY_B, // Default key is 'B'
				"category.betterbridging" // Category
		));

		breakBlockOnceKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.betterbridging.break_block_once", // Translation key
				GLFW.GLFW_KEY_N, // Default key is 'N'
				"category.betterbridging" // Category
		));

		// Register a tick event listener to check for key presses
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) {
				return;
			}

			if (toggleBreakBlockKeybind.wasPressed()) {
				// Toggle the isRepeating flag when the key is pressed
				isRepeating = !isRepeating;

				// Notify the player about the toggle state
				client.player.sendMessage(Text.literal(isRepeating ? "Block breaking toggled ON" : "Block breaking toggled OFF"), true);
			}

			// If isRepeating is true, continue breaking blocks
			if (isRepeating) {
				breakBlocksAroundPlayer(client);
			}

			// Check if the single-break keybind is pressed
			if (breakBlockOnceKeybind.wasPressed()) {
				breakBlocksAroundPlayer(client); // Break blocks once without toggling
			}
		});
	}

	private void breakBlocksAroundPlayer(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			return;
		}

		World world = client.world;
		BlockPos playerPos = client.player.getBlockPos();
		int radius = 4;

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos pos = playerPos.add(x, y, z);
					Block block = world.getBlockState(pos).getBlock();

					// Check for block type based on tool
					if (isPickaxe(client.player.getMainHandStack().getItem()) && isMineable(block) && !isBelowPlayer(pos, playerPos)) {
						breakBlock(client, pos);
					} else if (isShovel(client.player.getMainHandStack().getItem()) && isShovelable(block) && !isBelowPlayer(pos, playerPos)) {
						breakBlock(client, pos);
					}
				}
			}
		}
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

	private boolean isMineable(Block block) {
		return block == Blocks.STONE || block == Blocks.DIORITE ||
				block == Blocks.ANDESITE || block == Blocks.SANDSTONE ||
				block == Blocks.GRANITE || block == Blocks.NETHERRACK;
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
