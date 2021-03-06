package mods.cartlivery;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import mods.cartlivery.client.LiveryTextureRegistry;
import mods.cartlivery.client.model.ModelCartLivery;
import mods.cartlivery.client.model.RailcraftModel;
import mods.cartlivery.common.CartLivery;
import mods.cartlivery.common.item.ItemCutter;
import mods.cartlivery.common.item.ItemSticker;
import mods.cartlivery.common.item.LiveryStickerColoringRecipe;
import mods.cartlivery.common.network.LiveryGuiPatternHandler;
import mods.cartlivery.common.network.LiveryGuiPatternMessage;
import mods.cartlivery.common.network.LiveryRequestHandler;
import mods.cartlivery.common.network.LiveryRequestMessage;
import mods.cartlivery.common.network.LiveryUpdateHandler;
import mods.cartlivery.common.network.LiveryUpdateMessage;
import mods.cartlivery.common.utils.NetworkUtil;
import mods.railcraft.client.emblems.EmblemToolsClient;
import mods.railcraft.common.emblems.EmblemToolsServer;
import mods.railcraft.common.emblems.ItemEmblem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.entity.ai.EntityMinecartMobSpawner;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.RecipeSorter.Category;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RailcraftCommonProxy extends CommonProxy{
	public RailcraftCommonProxy() {
		//this.init();
	}
	
	@SubscribeEvent
	public void handleMinecartEmblemRemove(EntityInteractEvent event) {
		if(CartConfig.ENABLE_EMBLEMS){
			if (event.entityPlayer.worldObj.isRemote) return;
	
			if (event.entityPlayer.isSneaking() && event.target.getExtendedProperties(CartLivery.EXT_PROP_NAME) != null) {
				ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
				if (stack == null || !(stack.getItem() instanceof ItemCutter)) return;
				
				CartLivery livery = (CartLivery) event.target.getExtendedProperties(CartLivery.EXT_PROP_NAME);
				if (Loader.isModLoaded("Railcraft") && livery.emblem != null && !livery.emblem.isEmpty() /*&& EmblemToolsClient.packageManager != null*/){
					dropEmblem(event, livery);
					
					ItemStack tool = event.entityPlayer.inventory.getStackInSlot(event.entityPlayer.inventory.currentItem);
					tool.setItemDamage(tool.getItemDamage() + 1);
					if (tool.getItemDamage() > tool.getMaxDamage()) {
						event.entityPlayer.inventory.setInventorySlotContents(event.entityPlayer.inventory.currentItem, null);
					}
					
					if(CartConfig.PLAY_SOUNDS)
						event.target.playSound("CartLivery:emblem_cut", 1.0F, 1.0F);
					CommonProxy.network.sendToAllAround(new LiveryUpdateMessage(event.target, livery), NetworkUtil.targetEntity(event.target));
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void handleMinecartEmblemApply(EntityInteractEvent event) {
		if(CartConfig.ENABLE_EMBLEMS){
			if (event.entityPlayer.worldObj.isRemote) return;
			
			if (event.entityPlayer.isSneaking() && event.target.getExtendedProperties(CartLivery.EXT_PROP_NAME) != null) {
				ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
				if (stack == null || !Loader.isModLoaded("Railcraft") || !(stack.getItem() instanceof ItemEmblem) || stack.getTagCompound() == null) return;
				if (Loader.isModLoaded("Railcraft") && stack.getItem() instanceof ItemEmblem){
					String emblem = EmblemToolsServer.getEmblemIdentifier(stack);
					if (emblem.isEmpty()) return;
					
					CartLivery livery = (CartLivery) event.target.getExtendedProperties(CartLivery.EXT_PROP_NAME);
					if(Loader.isModLoaded("Railcraft") && !livery.emblem.equals(emblem)){
						if (Loader.isModLoaded("Railcraft") && livery.emblem != null && !livery.emblem.isEmpty() /*&& EmblemToolsClient.packageManager != null*/){
							dropEmblem(event, livery);
						}
						
						livery.emblem = emblem;
						
						stack.stackSize--;
						if (stack.stackSize == 0) event.entityPlayer.setCurrentItemOrArmor(0, null);
						
						if(CartConfig.PLAY_SOUNDS)
							event.target.playSound("CartLivery:emblem_apply", 1.0F, 1.0F);
						CommonProxy.network.sendToAllAround(new LiveryUpdateMessage(event.target, livery), NetworkUtil.targetEntity(event.target));
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	public static void dropEmblem(EntityInteractEvent event, CartLivery livery){
		EntityItem ent = event.target.entityDropItem(ItemEmblem.getEmblem(livery.emblem), 1.0F);
		Random rand = new Random();
		ent.motionY += rand.nextFloat() * 0.05F;
        ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
        ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
		livery.emblem = "";
	}
}
