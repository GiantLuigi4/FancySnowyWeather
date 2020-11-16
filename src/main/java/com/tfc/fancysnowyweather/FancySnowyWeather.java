package com.tfc.fancysnowyweather;

import com.tfc.fancysnowyweather.config.Config;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
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
		if (FMLEnvironment.dist.isClient()) {
			MinecraftForge.EVENT_BUS.addListener(Client::rwl);
		}
	}
	
	public static void onServerTick(TickEvent.WorldTickEvent event) {
		if (!event.world.isRemote) {
			//In case someone's ticking a fake world that doesn't exist server world
			if (event.world instanceof ServerWorld) {
				WeatherSaveData data = WeatherSaveData.get(event.world);
				
				data.DURATION--;
				data.DURATION = Math.min(400, data.DURATION);
				if (data.DURATION <= 0) {
					data.DURATION = event.world.rand.nextInt(10000) + 1000;
					data.IS_ACTIVE = !data.IS_ACTIVE;
					data.WEIGHT = event.world.rand.nextInt(2) % 2;
					if (data.WEIGHT >= 1) {
						data.DURATION /= (data.WEIGHT*4);
					}
					
					event.world.getPlayers().forEach(
							(player) ->
									INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new WeatherPacket(data.IS_ACTIVE, data.WEIGHT))
					);
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
