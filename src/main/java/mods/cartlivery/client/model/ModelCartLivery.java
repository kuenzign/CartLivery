package mods.cartlivery.client.model;

import mods.cartlivery.client.LiveryTextureRegistry;
import mods.cartlivery.common.CartLivery;
import mods.cartlivery.common.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelMinecart;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class ModelCartLivery extends ModelBase {

	ModelMinecart baseModel = new ModelMinecart();
	ModelRenderer liveryBox = new ModelRenderer(this, "livery");
	
	ResourceLocation baseTexture = new ResourceLocation("cartlivery:textures/entity/minecart_bleached.png");
	protected float emblemSize = 0.2F;
	protected float emblemOffsetX = 0F;
	protected float emblemOffsetY = 0F;
	protected float emblemOffsetZ = -0.51F;
	
	public ModelCartLivery() {
		liveryBox = new ModelRenderer(this, "livery");
		setTextureOffset("livery", 0, 0);
		liveryBox.textureWidth = 72.0f;
		liveryBox.textureHeight = 26.0f;
		
		liveryBox.addBox(-10.0f, -5.0f, -8.0f, 20, 10, 16);
	}
	
	public void setEmblemPosition(float size, float offsetX, float offsetY, float offsetZ)
	{
	    this.emblemSize = size;
	    this.emblemOffsetX = offsetX;
	    this.emblemOffsetY = offsetY;
	    this.emblemOffsetZ = offsetZ;
	}
	
	@Override
	public void render(Entity entity, float par2, float par3, float par4, float par5, float par6, float par7) {
		if (entity.getExtendedProperties(CartLivery.EXT_PROP_NAME) == null) {
			// fallback, should not happen
			baseModel.render(entity, par2, par3, par4, par5, par6, par7);
		} else {
			CartLivery livery = (CartLivery) entity.getExtendedProperties(CartLivery.EXT_PROP_NAME);
			render(livery.baseColor, livery.patternColor, livery.pattern, livery.emblem, par2, par3, par4, par5, par6, par7);
		}
	}

	public void render(int baseColor, int patternColor, String pattern, String emblem, float par2, float par3, float par4, float par5, float par6, float par7) {
		setGLColor(baseColor);
		Minecraft.getMinecraft().renderEngine.bindTexture(baseTexture);
		baseModel.render(null, par2, par3, par4, par5, par6, par7);

		ResourceLocation liveryTexture = LiveryTextureRegistry.getTexture(pattern);
		if (liveryTexture != null) {
			GL11.glPushMatrix();
			setGLColor(patternColor);
			Minecraft.getMinecraft().renderEngine.bindTexture(liveryTexture);

			GL11.glScalef(1.01f, 1.01f, 1.01f);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			liveryBox.render(par7);
			GL11.glPopMatrix();
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
			
	protected void setGLColor(int dyeColor) {
		int c = ColorUtils.getColor(dyeColor);
        float dim = 1.0F;
        float c1 = (float) (c >> 16 & 255) / 255.0F;
        float c2 = (float) (c >> 8 & 255) / 255.0F;
        float c3 = (float) (c & 255) / 255.0F;
        GL11.glColor4f(c1 * dim, c2 * dim, c3 * dim, 1);
		/*int color = (dyeColor < 0 || dyeColor > 15) ? 7 : dyeColor;
		GL11.glColor4f(EntitySheep.fleeceColorTable[15 - color][0], EntitySheep.fleeceColorTable[15 - color][1], EntitySheep.fleeceColorTable[15 - color][2], 1.0f);*/
	}
}
