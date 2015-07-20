package com.csapp.mvp.dkb.data;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class PosData {
	
	private String instrumentID;
	private boolean longOrShort;
	private double openPrice;
	private boolean todayOrHistoryPos;
	private int volume;
	
	public PosData(String instrumentId, boolean longOrShort, double openPrice, int volume){
		this.instrumentID = instrumentId;
		this.longOrShort = longOrShort;
		this.openPrice = openPrice;
		this.volume = volume;
	}
	
	public PosData( @NotNull JSONObject posObject){
		
		try {
			instrumentID = posObject.getString("InstrumentID");
			longOrShort = posObject.getBoolean("LongOrShort");
			openPrice = posObject.getDouble("OpenPrice");
			todayOrHistoryPos = posObject.getBoolean("TodayOrHistoryPos");
			volume = posObject.getInt("Volume");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}

	public String getInstrumentID() {
		return instrumentID;
	}

	public void setInstrumentID(String instrumentID) {
		this.instrumentID = instrumentID;
	}

	public boolean isLongOrShort() {
		return longOrShort;
	}

	public void setLongOrShort(boolean longOrShort) {
		this.longOrShort = longOrShort;
	}

	public double getOpenPrice() {
		return Util.formatDouble(openPrice);
	}

	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}

	public boolean isTodayOrHistoryPos() {
		return todayOrHistoryPos;
	}

	public void setTodayOrHistoryPos(boolean todayOrHistoryPos) {
		this.todayOrHistoryPos = todayOrHistoryPos;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

}
