package com.tfc.fancysnowyweather;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ScoreHolderArgument;
import net.minecraft.command.impl.ScoreboardCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

public class FancySnowyWeathersCommand {
	public static LiteralArgumentBuilder construct() {
		LiteralArgumentBuilder builder = Commands.literal("snowy_weather").requires(commandSource -> commandSource.hasPermissionLevel(2));
		
		builder.then(Commands.literal("weather")
				.then(Commands.literal("enable")
						.then(Commands.literal("heavy")
								.executes(context -> {
									CommandSource source = context.getSource();
									Scoreboard scoreboard = source.getWorld().getScoreboard();
									ScoreObjective objective = WeatherSaveData.setupScores(scoreboard);
									scoreboard.getOrCreateScore("heavy_enabled", objective).setScorePoints(1);
									source.sendFeedback(new StringTextComponent("Heavy Weather has been enabled successfully!"), true);
									return 1;
								}))
						.then(Commands.literal("light")
								.executes(context -> {
									CommandSource source = context.getSource();
									Scoreboard scoreboard = source.getWorld().getScoreboard();
									ScoreObjective objective = WeatherSaveData.setupScores(scoreboard);
									scoreboard.getOrCreateScore("light_enabled", objective).setScorePoints(1);
									source.sendFeedback(new StringTextComponent("Light Weather has been enabled successfully!"), true);
									return 1;
								})))
				.then(Commands.literal("disable")
						.then(Commands.literal("heavy")
								.executes(context -> {
									CommandSource source = context.getSource();
									Scoreboard scoreboard = source.getWorld().getScoreboard();
									ScoreObjective objective = WeatherSaveData.setupScores(scoreboard);
									scoreboard.getOrCreateScore("heavy_enabled", objective).setScorePoints(0);
									source.sendFeedback(new StringTextComponent("Heavy Weather has been disabled successfully!"), true);
									return 1;
								}))
						.then(Commands.literal("light")
								.executes(context -> {
									CommandSource source = context.getSource();
									Scoreboard scoreboard = source.getWorld().getScoreboard();
									ScoreObjective objective = WeatherSaveData.setupScores(scoreboard);
									scoreboard.getOrCreateScore("light_enabled", objective).setScorePoints(0);
									source.sendFeedback(new StringTextComponent("Light Weather has been disabled successfully!"), true);
									return 1;
								})))
				.then(Commands.literal("activate")
						.then(Commands.literal("heavy")
								.executes(context -> {
									CommandSource source = context.getSource();
									WeatherSaveData data = WeatherSaveData.get(source.getWorld());
									data.IS_ACTIVE = true;
									data.DURATION = 10000000;
									data.WEIGHT = 1;
									source.getWorld().getPlayers().forEach((player) -> FancySnowyWeather.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new WeatherPacket(data.IS_ACTIVE, data.WEIGHT)));
									source.sendFeedback(new StringTextComponent("Heavy Weather has been activated successfully for " + data.DURATION + " ticks!"), true);
									return data.DURATION;
								})
								.then(Commands.argument("duration", IntegerArgumentType.integer(1))
										.executes(context -> {
											CommandSource source = context.getSource();
											WeatherSaveData data = WeatherSaveData.get(source.getWorld());
											data.IS_ACTIVE = true;
											data.DURATION = IntegerArgumentType.getInteger(context, "duration");
											data.WEIGHT = 1;
											source.getWorld().getPlayers().forEach((player) -> FancySnowyWeather.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new WeatherPacket(data.IS_ACTIVE, data.WEIGHT)));
											source.sendFeedback(new StringTextComponent("Heavy Weather has been activated successfully for " + data.DURATION + " ticks!"), true);
											return data.DURATION;
										})))
						.then(Commands.literal("light")
								.executes(context -> {
									CommandSource source = context.getSource();
									WeatherSaveData data = WeatherSaveData.get(source.getWorld());
									data.IS_ACTIVE = true;
									data.DURATION = 10000000;
									data.WEIGHT = 0;
									source.getWorld().getPlayers().forEach((player) -> FancySnowyWeather.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new WeatherPacket(data.IS_ACTIVE, data.WEIGHT)));
									source.sendFeedback(new StringTextComponent("Light Weather has been activated successfully for " + data.DURATION + " ticks!"), true);
									return data.DURATION;
								})
								.then(Commands.argument("duration", IntegerArgumentType.integer(1))
										.executes(context -> {
											CommandSource source = context.getSource();
											WeatherSaveData data = WeatherSaveData.get(source.getWorld());
											data.IS_ACTIVE = true;
											data.DURATION = IntegerArgumentType.getInteger(context, "duration");
											data.WEIGHT = 0;
											source.getWorld().getPlayers().forEach((player) -> FancySnowyWeather.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new WeatherPacket(data.IS_ACTIVE, data.WEIGHT)));
											source.sendFeedback(new StringTextComponent("Light Weather has been activated successfully for " + data.DURATION + " ticks!"), true);
											return data.DURATION;
										}))))
				.then(Commands.literal("deactivate")
						.executes(context -> {
							CommandSource source = context.getSource();
							WeatherSaveData data = WeatherSaveData.get(source.getWorld());
							int returnVal = data.WEIGHT + (data.DURATION * 10);
							data.IS_ACTIVE = false;
							data.DURATION = returnVal / 10;
							data.WEIGHT = 0;
							source.getWorld().getPlayers().forEach(
									(player) -> FancySnowyWeather.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new WeatherPacket(data.IS_ACTIVE, data.WEIGHT))
							);
							source.sendFeedback(new StringTextComponent("Enjoy not having snow for the next " + returnVal + " ticks!"), true);
							return returnVal;
						})));
		return builder;
	}
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(construct());
	}
}
