package com.caburum.clickthrough.mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Based on code from https://github.com/gbl/ClickThrough/blob/fabric_1_19/src/main/java/de/guntram/mcmod/clickthrough/mixins/ItemUseMixin.java
// Copyright (c) 2020 Guntram Blohm, licensed under the MIT license

@Mixin(MinecraftClient.class)
public class ItemUseMixin {
	@Shadow
	public HitResult crosshairTarget;
	@Shadow
	public ClientPlayerEntity player;
	@Shadow
	public ClientWorld world;

	@Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
	public void switchCrosshairTarget(CallbackInfo ci) {
		if (crosshairTarget != null && crosshairTarget.getType() == HitResult.Type.ENTITY && ((EntityHitResult) crosshairTarget).getEntity() instanceof ItemFrameEntity itemFrame) {
			// copied from AbstractDecorationEntity#canStayAttached
			var attachedPos = itemFrame.getDecorationBlockPos().offset(itemFrame.getHorizontalFacing().getOpposite());

			var state = world.getBlockState(attachedPos);
			// ClickThroughMod.LOGGER.debug("Item frame attached to " + state.getBlock().getTranslationKey() + " at " + attachedPos.toShortString());
			var block = state.getBlock();
			var entity = world.getBlockEntity(attachedPos);

			// pretty rough check, but covers the most common block
			// it would be better to check if the block is actually interactable, but whatever
			boolean canInteract = entity instanceof LockableContainerBlockEntity // some chests, shulker, furnace, brewing stand, dispenser, hopper
				|| block instanceof AbstractChestBlock // other chests
				|| block instanceof CraftingTableBlock
				|| block instanceof AnvilBlock
				|| block instanceof LecternBlock
				|| block instanceof JukeboxBlock
				|| block instanceof StonecutterBlock
				|| block instanceof CartographyTableBlock
				|| block instanceof ComposterBlock
				|| block instanceof LoomBlock
				|| block instanceof GrindstoneBlock
				|| block instanceof EnchantingTableBlock
				|| block instanceof BeaconBlock;

			if (!player.isSneaking() && canInteract) {
				this.crosshairTarget = new BlockHitResult(crosshairTarget.getPos(), itemFrame.getHorizontalFacing(), attachedPos, false);
			}
		}
	}
}