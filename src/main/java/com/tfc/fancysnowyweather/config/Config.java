package com.tfc.fancysnowyweather.config;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;

public class Config {
	private static int dist = 16;
	private static float heavySnowSpeed = 1600;
	private static float movementScale = 32;
	private static float textureScale = 32;
	private static boolean enableFarPlane = true;
	private static boolean intrusiveMode = false;
	private static boolean doLightSnow = true;
	private static boolean doHeavySnow = true;
	
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
	
	public static int getDist() {
		return dist;
	}
	
	public static float getHeavySnowSpeed() {
		return heavySnowSpeed;
	}
	
	public static boolean enableFarPlane() {
		return enableFarPlane;
	}
	
	public static boolean isIntrusiveMode() {
		return intrusiveMode;
	}
	
	public static boolean shouldDoLightSnow() {
		return doLightSnow;
	}
	
	public static boolean shouldDoHeavySnow() {
		return doHeavySnow;
	}
	
	public static float getMovementScale() {
		return movementScale;
	}
	
	public static float getTextureScale() {
		return textureScale;
	}
}
