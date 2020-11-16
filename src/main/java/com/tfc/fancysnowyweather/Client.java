package com.tfc.fancysnowyweather;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.tfc.fancysnowyweather.config.Config;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.Random;

import static net.minecraft.client.renderer.RenderType.makeType;

public class Client {
	public static int WEIGHT = 0;
	public static boolean IS_ACTIVE = false;
	
	private static final String[] weights = new String[]{
			Config.shouldDoLightSnow() ?
					"fancy_snowy_weather:textures/weather/snow/light_snow.png" :
					"fancy_snowy_weather:textures/weather/snow/heavy_snow.png",
			Config.shouldDoHeavySnow() ?
					"fancy_snowy_weather:textures/weather/snow/heavy_snow.png" :
					"fancy_snowy_weather:textures/weather/snow/light_snow.png"
	};
	
	public static void rwl(RenderWorldLastEvent event) {
		boolean isActive = IS_ACTIVE;
		if ((isActive && (Config.shouldDoHeavySnow() || Config.shouldDoLightSnow()))) {
			ModelRenderer renderer = new ModelRenderer(1,1,0,0);
			renderer.addBox(0,0,0,32,32,32,false);
			
			RenderTypeBuffers buffers = Minecraft.getInstance().getRenderTypeBuffers();
			IRenderTypeBuffer.Impl buffer = buffers.getBufferSource();
			
			event.getMatrixStack().translate(
					0,
					-Minecraft.getInstance().getRenderManager().info.getProjectedView().y,
					0
			);
			
			Direction[] dirs = new Direction[]{
					Direction.SOUTH,
					Direction.EAST,
					Direction.NORTH,
					Direction.WEST,
			};
			
			event.getMatrixStack().push();
			
			event.getMatrixStack().scale(-1,-1,-1);
			event.getMatrixStack().rotate(new Quaternion(0,45,0,true));
			event.getMatrixStack().translate(-1f,-1f,-1f);
			
			IBakedModel stone = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(Blocks.STONE.getDefaultState());
			
			for (int i=0; i<2;i++) {
				event.getMatrixStack().push();
				IVertexBuilder builder = buffer.getBuffer(getSnow(new ResourceLocation(
						weights[WEIGHT]
				),WEIGHT,53-(i+2)));
//				IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
				
				event.getMatrixStack().push();
				float scl = 2560;
				event.getMatrixStack().translate(0,1f,0);
				event.getMatrixStack().scale(1,scl,1);
				event.getMatrixStack().translate(0.5f,-1f,0.5f);
				event.getMatrixStack().translate(-Config.getDist()/2f,0,Config.getDist()/2f);
				event.getMatrixStack().push();
				float offX = 0;
				float offZ = -(Config.getDist()*0.6875f);
				int angle = 0;
				if (Config.enableFarPlane()) {
					for (Direction dir : dirs) {
						for (int offset=0; offset<Config.getDist();offset++) {
							if (angle == 0) {
								offX += -0.7f;
								offZ += 0.7f;
							} else if (angle <= 100) {
								offX += 0.7f;
								offZ += 0.7f;
							} else if (angle <= 190) {
								offX += 0.7f;
								offZ += -0.7f;
							} else {
								offX += -0.7f;
								offZ += -0.7f;
							}
							event.getMatrixStack().translate(1,0,0);
							event.getMatrixStack().push();
							event.getMatrixStack().rotate(new Quaternion(0,-angle,0,true));
							event.getMatrixStack().translate(
									0,
									Minecraft.getInstance().player.world.getHeight(Heightmap.Type.MOTION_BLOCKING,
											Minecraft.getInstance().player.getPosition().add(offX,0,offZ)
									).getY()/-scl,
									0
							);
							builder.addQuad(
									event.getMatrixStack().getLast(),
									stone.getQuads(Blocks.STONE.getDefaultState(),dir,new Random(0)).get(0),
									1,1,1, LightTexture.packLight(15,15), OverlayTexture.NO_OVERLAY
							);
							event.getMatrixStack().pop();
						}
						angle += 90;
						event.getMatrixStack().rotate(new Quaternion(0,90,0,true));
						event.getMatrixStack().translate(-1,0,0);
					}
				}
				event.getMatrixStack().pop();
				event.getMatrixStack().pop();
				
				if (Config.isIntrusiveMode()) {
					event.getMatrixStack().push();
					
					builder = buffer.getBuffer(getSnow(new ResourceLocation(
							weights[WEIGHT]
					),WEIGHT,53-i));
					event.getMatrixStack().scale(1,scl,1);
					event.getMatrixStack().translate(0.5f,-1,0.5f);
					event.getMatrixStack().translate(0,
							Minecraft.getInstance().player.world.getHeight(
									Heightmap.Type.MOTION_BLOCKING,
									Minecraft.getInstance().player.getPosition()
							).getY()/-scl, 0
					);
					for (Direction dir : dirs) {
						builder.addQuad(
								event.getMatrixStack().getLast(),
								stone.getQuads(Blocks.STONE.getDefaultState(),dir,new Random(0)).get(0),
								1,1,1,LightTexture.packLight(15,15),OverlayTexture.NO_OVERLAY
						);
					}
					
					event.getMatrixStack().pop();
				}
				
				buffer.finish();
				event.getMatrixStack().pop();
			}
			
			event.getMatrixStack().pop();
		}
	}
	
	public static RenderType getSnow(ResourceLocation locationIn, int weight, int iter) {
		RenderType.State rendertype$state = RenderType.State.getBuilder().texture(new RenderState.TextureState(locationIn, false, false)).transparency(RenderType.TRANSLUCENT_TRANSPARENCY).diffuseLighting(RenderType.DIFFUSE_LIGHTING_DISABLED).alpha(RenderType.DEFAULT_ALPHA).lightmap(RenderType.LIGHTMAP_DISABLED).overlay(RenderType.OVERLAY_ENABLED)
				.texturing(new RenderState.TexturingState("snow_"+iter+"_"+weight,() -> {
					RenderSystem.matrixMode(5890);
					RenderSystem.pushMatrix();
					RenderSystem.loadIdentity();
					RenderSystem.translatef(
							((float)Minecraft.getInstance().player.getPosX()+(float)Minecraft.getInstance().player.getPosZ())/Config.getMovementScale(),
							(float)Minecraft.getInstance().player.getPosX()+(float)Minecraft.getInstance().player.getPosZ()/Config.getMovementScale(),
							0
					);
					if (weight == 0) {
						RenderSystem.translatef(0.5F, 0.5F, 0.0F);
						RenderSystem.scalef(0.5F, 0.5F, 1.0F);
						RenderSystem.translatef(17.0F / (float) iter, (2.0F + (float) iter / 1.5F) * ((float)(Util.milliTime() % 800000L) / 800000.0F), 0.0F);
						RenderSystem.rotatef(((float)(iter * iter) * 4321.0F + (float) iter * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
						RenderSystem.scalef(4.5F - (float) iter / 4.0F, 4.5F - (float) iter / 4.0F, 1.0F);
						RenderSystem.translatef(0.5F, 0.5F, 0.0F);
						RenderSystem.scalef(0.5F, 0.5F, 1.0F);
						RenderSystem.translatef(17.0F / (float) iter, (2.0F + (float) iter / 1.5F) * ((float)(Util.milliTime() % 800000L) / 800000.0F), 0.0F);
						RenderSystem.rotatef(((float)(iter * iter) * 4321.0F + (float) iter * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
						RenderSystem.scalef(4.5F - (float) iter / 4.0F, 4.5F - (float) iter / 4.0F, 1.0F);
					} else {
						RenderSystem.translatef(0.5F, 0.5F, 0.0F);
						RenderSystem.scalef(0.5F, 0.5F, 1.0F);
						RenderSystem.translatef(17.0F / (float) iter, (2.0F + (float) iter / 1.5F) * ((float)(Util.milliTime() % 800000L) / 800000.0F), 0.0F);
						RenderSystem.rotatef(((float)(iter * iter) * 4321.0F + (float) iter * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
						RenderSystem.scalef(4.5F - (float) iter / 4.0F, 4.5F - (float) iter / 4.0F, 1.0F);
						RenderSystem.translatef(0.5F, 0.5F, 0.0F);
						RenderSystem.scalef(0.5F, 0.5F, 1.0F);
						RenderSystem.translatef(17.0F / (float) iter, (2.0F + (float) iter / 1.5F) * ((float)(Util.milliTime() % 800000L) / 800000.0F), 0.0F);
						RenderSystem.rotatef(((float)(iter * iter) * 4321.0F + (float) iter * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
						RenderSystem.scalef(4.5F - (float) iter / 4.0F, 4.5F - (float) iter / 4.0F, 1.0F);
						
						RenderSystem.translatef((Util.milliTime()% Config.getHeavySnowSpeed())/Config.getHeavySnowSpeed(),(Util.milliTime()%Config.getHeavySnowSpeed())/Config.getHeavySnowSpeed(), 0.0F);
					}
					
					RenderSystem.scalef(Config.getTextureScale(),Config.getTextureScale(),1);
					
					RenderSystem.mulTextureByProjModelView();
					RenderSystem.matrixMode(5888);
					RenderSystem.setupEndPortalTexGen();
				}, () -> {
					RenderSystem.matrixMode(5890);
					RenderSystem.popMatrix();
					RenderSystem.matrixMode(5888);
					RenderSystem.clearTexGen();
				})).build(true);
		return makeType("snow_cutout", DefaultVertexFormats.ENTITY, 7, 256, true, false, rendertype$state);
	}
}
