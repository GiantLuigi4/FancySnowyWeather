package com.tfc.fancysnowyweather.config;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;

public class Config {
	private static int warmBlockRange = 5;
	private static int minDuration = 100000;
	private static int durationRange = 10000;
	private static boolean doDamage = true;
	private static boolean doSlow = true;
	private static boolean flameWarms = true;
	private static boolean doLightSnow = true;
	private static boolean doHeavySnow = true;
	private static int minDist = 4;
	private static int maxDist = 40000000;
	
	public static void readAndWrite() {
		File f = new File("config/fancy_snowy_weather.properties");
		try {
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
				StringBuilder config = new StringBuilder();
				for (Field field : Config.class.getDeclaredFields()) {
					field.setAccessible(true);
					config.append(field.getName()).append("=").append(field.get(null)).append("\n");
				}
				FileWriter writer = new FileWriter(f);
				writer.write(config.toString());
				writer.close();
			}
			PropertiesReader reader = new PropertiesReader(f);
			for (String entry : reader.getEntries()) {
				Field field = Config.class.getDeclaredField(entry);
				field.setAccessible(true);
				Object val = reader.getValue(entry);
				if (field.getType().equals(int.class)) val = Integer.valueOf((String) val);
				else if (field.getType().equals(boolean.class)) val = Boolean.valueOf((String) val);
				else if (field.getType().equals(double.class)) val = Double.valueOf((String) val);
				else if (field.getType().equals(float.class)) val = Float.valueOf((String) val);
				else if (field.getType().equals(Long.class)) val = Long.valueOf((String) val);
				else if (field.getType().equals(Byte.class)) val = Byte.valueOf((String) val);
				field.set(null, val);
			}
		} catch (Throwable err) {
			err.printStackTrace();
			throw new RuntimeException(err);
		}
	}
	
	public static boolean shouldDoLightSnow() {
		return doLightSnow;
	}
	
	public static boolean shouldDoHeavySnow() {
		return doHeavySnow;
	}
	
	public static int getMinDist() {
		return minDist;
	}
	
	public static int getMaxDist() {
		return maxDist;
	}
	
	public static int getWarmBlockRange() {
		return warmBlockRange;
	}
	
	public static boolean shouldDoDamage() {
		return doDamage;
	}
	
	public static boolean shouldDoSlow() {
		return doSlow;
	}
	
	public static boolean isFlameWarms() {
		return flameWarms;
	}
	
	public static int getMinDuration() {
		return minDuration;
	}
	
	public static int getDurationRange() {
		return durationRange;
	}
}
