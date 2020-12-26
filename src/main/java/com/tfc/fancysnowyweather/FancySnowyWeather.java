package com.tfc.fancysnowyweather;

import com.tfc.fancysnowyweather.config.Config;
import net.minecraft.block.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.Tag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("fancy_snowy_weather")
public class FancySnowyWeather {
	
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	@CapabilityInject(World.class)
	static Capability<World> WORLD_CAPABILITY = null;
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation("fancy_snowy_weather", "main"),
			() -> "1",
			"1"::equals,
			"1"::equals
	);
	
	public FancySnowyWeather() {
		INSTANCE.registerMessage(0, WeatherPacket.class, WeatherPacket::writePacketData, WeatherPacket::new, (packet, contex) -> {
		});
		
		Config.readAndWrite();
		
		MinecraftForge.EVENT_BUS.addListener(FancySnowyWeather::onServerTick);
		MinecraftForge.EVENT_BUS.addListener(FancySnowyWeather::onPlayerLoggedOn);
		MinecraftForge.EVENT_BUS.addListener(FancySnowyWeather::onPlayerSwitchDimensions);
		MinecraftForge.EVENT_BUS.addListener(FancySnowyWeather::onServerStarting);
		if (FMLEnvironment.dist.isClient())
			MinecraftForge.EVENT_BUS.addListener(Client::rwl);
	}
	
	public static void onServerStarting(FMLServerStartingEvent event) {
		FancySnowyWeathersCommand.register(event.getServer().getCommandManager().getDispatcher());
	}
	
	public static void onServerTick(TickEvent.WorldTickEvent event) {
		if (!event.world.isRemote) {
			//In case someone's ticking a fake world that doesn't exist server world
			if (event.world instanceof ServerWorld) {
				WeatherSaveData data = WeatherSaveData.get(event.world);
				
				data.DURATION--;
				if (data.DURATION <= 0) {
					data.DURATION = event.world.rand.nextInt(Config.getDurationRange()) + Config.getMinDuration();
					data.IS_ACTIVE = !data.IS_ACTIVE;

					if (data.IS_ACTIVE) data.WEIGHT = event.world.rand.nextInt(2) % 2;

					if (data.WEIGHT >= 1) data.DURATION /= (data.WEIGHT * 4);
					
					event.world.getPlayers().forEach(
							(player) -> INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new WeatherPacket(data.IS_ACTIVE, data.WEIGHT))
					);
				}
				
				if (data.IS_ACTIVE)
					if (data.WEIGHT == 0)
						if (!WeatherSaveData.get("light_enabled", event.world)) {
							if (!WeatherSaveData.get("heavy_enabled", event.world))
								data.IS_ACTIVE = false;
							else data.WEIGHT = 1;
							event.world.getPlayers().forEach(
									(player) -> INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new WeatherPacket(data.IS_ACTIVE, data.WEIGHT))
							);
						} else if (data.WEIGHT == 1)
							if (!WeatherSaveData.get("heavy_enabled", event.world)) {
								if (!WeatherSaveData.get("light_enabled", event.world))
									data.IS_ACTIVE = false;
								else data.WEIGHT = 0;
								event.world.getPlayers().forEach(
										(player) -> INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new WeatherPacket(data.IS_ACTIVE, data.WEIGHT))
								);
							}
				
				if (data.IS_ACTIVE) {
					if (data.WEIGHT >= 1) {
						event.world.getPlayers().forEach(
								(player) -> {
									Biome biome = event.world.getBiome(player.getPosition());
									if (biome.getPrecipitation() != Biome.RainType.NONE) {
										if (event.world.getHeight(Heightmap.Type.MOTION_BLOCKING, player.getPosition()).getY() <= player.getPosition().getY()) {
											if (Config.shouldDoSlow())
												player.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 1, 1, false, false));
											
											if (Config.shouldDoDamage() && data.DURATION % 1000 == 0) {
												for (int x = -5; x <= 5; x++) {
													for (int y = -5; y <= 5; y++) {
														for (int z = -5; z <= 5; z++) {
															BlockPos posCheck = player.getPosition().add(x,y,z);
															BlockState block = player.world.getBlockState(posCheck);
															if (
																	block.isFireSource(player.world, posCheck, Direction.UP)||
																			(
																					block.getBlock() instanceof CampfireBlock &&
																							block.get(CampfireBlock.LIT)
																			) ||
																			block.getBlock() instanceof MagmaBlock ||
																			block.getFluidState().getFluid().isEquivalentTo(Fluids.LAVA) ||
																			block.getFluidState().getFluid().isEquivalentTo(Fluids.FLOWING_LAVA) ||
																			(player.getFireTimer() > 0 && Config.isFlameWarms())
															) {
																return;
															}
														}
													}
												}
												player.attackEntityFrom(new DamageSource("weather.freeze.name"),1);
											}
										}
									}
								}
						);
					}
					if (data.WEIGHT == 1 || event.world.rand.nextInt(100) <= 25) {
						for (int i = 0; i < 3; i ++) {
							ServerPlayerEntity playerEntity = ((ServerWorld) event.world).getRandomPlayer();
							if (playerEntity != null) {
								BlockPos pos = new BlockPos(
										playerEntity.getPosXRandom(64),
										playerEntity.getPosY(),
										playerEntity.getPosZRandom(64)
								);
								pos = event.world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos);
								Biome biome = event.world.getBiome(pos);
								if (biome.getPrecipitation() != Biome.RainType.NONE) {
									if (
											!event.world.getBlockState(pos.down()).getFluidState().isEmpty() &&
													event.world.getBlockState(pos.down()).getFluidState().isSource() &&
													event.world.getBlockState(pos.down()).getFluidState().getFluid().isEquivalentTo(Fluids.WATER)
									) {
										if (
												event.world.getBlockState(pos.down().east()).isSolid() ||
														event.world.getBlockState(pos.down().east()).getBlock().equals(Blocks.ICE) ||
														event.world.getBlockState(pos.down().south()).isSolid() ||
														event.world.getBlockState(pos.down().south()).getBlock().equals(Blocks.ICE) ||
														event.world.getBlockState(pos.down().north()).isSolid() ||
														event.world.getBlockState(pos.down().north()).getBlock().equals(Blocks.ICE) ||
														event.world.getBlockState(pos.down().west()).isSolid() ||
														event.world.getBlockState(pos.down().west()).getBlock().equals(Blocks.ICE)
										) {
											event.world.setBlockState(pos.down(), Blocks.ICE.getDefaultState());
										}
									} else if (event.world.getBlockState(pos).isAir()) {
										if (Blocks.SNOW.isValidPosition(Blocks.SNOW.getDefaultState(), event.world, pos)) {
											event.world.setBlockState(pos, Blocks.SNOW.getDefaultState());
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static void onPlayerLoggedOn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			WeatherSaveData data = WeatherSaveData.get(event.getEntity().world);
			
			INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()), new WeatherPacket(data.IS_ACTIVE, data.WEIGHT));
		}
	}
	
	public static void onPlayerSwitchDimensions(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			WeatherSaveData data = WeatherSaveData.get(event.getEntity().world);
			
			INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()), new WeatherPacket(data.IS_ACTIVE, data.WEIGHT));
		}
	}
}
