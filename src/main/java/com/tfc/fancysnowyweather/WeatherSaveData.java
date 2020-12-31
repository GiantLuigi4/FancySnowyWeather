package com.tfc.fancysnowyweather;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class WeatherSaveData extends WorldSavedData {
	public int WEIGHT = 0;
	public int DURATION = 0;
	public boolean IS_ACTIVE = true;
	
	public static final String DATA_NAME = "fancy_snowy_weather" + "_world_data";
	
	public WeatherSaveData() {
		super(DATA_NAME);
	}
	
	public WeatherSaveData(String name) {
		super(name);
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		WEIGHT = nbt.getInt("weight");
		DURATION = nbt.getInt("duration");
		IS_ACTIVE = nbt.getBoolean("active");
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("weight", WEIGHT);
		compound.putInt("duration", DURATION);
		compound.putBoolean("active", IS_ACTIVE);
		return compound;
	}
	
	public static WeatherSaveData get(World world) {
		ServerWorld serverWorld = ((ServerWorld) world);
		DimensionSavedDataManager manager = serverWorld.getSavedData();
		return manager.getOrCreate(() -> new WeatherSaveData(WeatherSaveData.DATA_NAME), WeatherSaveData.DATA_NAME);
	}
	
	public static boolean get(String gamerule, World world) {
		if (has("fsw_gamerules", world)) {
			ScoreObjective objective = world.getServer().getScoreboard().getObjective("fsw_gamerules");
			Score score = world.getServer().getScoreboard().getOrCreateScore(gamerule, objective);
			System.out.println(score.getScorePoints());
			return score.getScorePoints() != 0;
		} else {
			setupScores(world);
			return true;
		}
	}
	
	public static ScoreObjective setupScores(Scoreboard scoreboard) {
		if (!has("fsw_gamerules", scoreboard)) {
			scoreboard.addObjective("fsw_gamerules", ScoreCriteria.DUMMY, new TranslationTextComponent("fancy_snowy_weathers.gamerule_scoreboard_temp"), ScoreCriteria.RenderType.INTEGER);
			ScoreObjective objective = scoreboard.getOrCreateObjective("fsw_gamerules");
			scoreboard.getOrCreateScore("light_enabled", objective).setScorePoints(1);
			scoreboard.getOrCreateScore("heavy_enabled", objective).setScorePoints(1);
		}
		
		ScoreObjective objective = scoreboard.getOrCreateObjective("fsw_gamerules");
		return objective;
	}
	
	public static ScoreObjective setupScores(World world) {
		return setupScores(world.getScoreboard());
	}
	
	private static boolean has(String scoreboardName, World world) {
		return has(scoreboardName,world.getScoreboard());
	}
	
	private static boolean has(String scoreboardName, Scoreboard scoreboard) {
		return scoreboard.getObjective(scoreboardName) != null;
	}
}
