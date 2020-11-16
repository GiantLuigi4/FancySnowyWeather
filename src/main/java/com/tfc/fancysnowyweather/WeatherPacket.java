package com.tfc.fancysnowyweather;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;

public class WeatherPacket implements IPacket {
	public boolean isStarting;
	public int weight;
	
	public WeatherPacket(boolean isStarting, int weight) {
		this.isStarting = isStarting;
		this.weight = weight;
	}
	
	public WeatherPacket(PacketBuffer buffer) {
		readPacketData(buffer);
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		this.isStarting = buf.readBoolean();
		Client.IS_ACTIVE = isStarting;
		this.weight = buf.readInt();
		Client.WEIGHT = weight;
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeBoolean(isStarting);
		buf.writeInt(weight);
	}
	
	@Override
	public void processPacket(INetHandler handler) {
	}
}
