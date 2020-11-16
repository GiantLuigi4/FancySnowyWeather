package com.tfc.fancysnowyweather;

import net.minecraft.nbt.CompoundNBT;
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
}
