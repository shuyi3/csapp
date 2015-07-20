package com.csapp.mvp.dkb.data;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class OrderData {
	
	private boolean buyOrSell;
	private boolean closeTodayOrHistoryPos;
	private String instrumentID;
	private boolean newOrClose;
	private String orderSysID;
	private double originPrice;
	private String originTime;
	private int originVolume;
	private int status;
	private double tradedPrice;
	private String tradedTime;
	private int tradedVolume;
	
	public OrderData( @NotNull JSONObject orderObject){
		try {
			buyOrSell = orderObject.getBoolean("BuyOrSell");
			closeTodayOrHistoryPos = orderObject.getBoolean("CloseTodayOrHistoryPos");
			instrumentID = orderObject.getString("InstrumentID");
			newOrClose = orderObject.getBoolean("NewOrClose");
			orderSysID = orderObject.getString("OrderSysID").trim();
			originPrice = orderObject.getDouble("OriginPrice");
			originTime = orderObject.getString("OriginTime");
			originVolume = orderObject.getInt("OriginVolume");
			status = orderObject.getInt("Status");
			tradedPrice = orderObject.getDouble("TradedPrice");
			tradedTime = orderObject.getString("TradedTime");
			tradedVolume = orderObject.getInt("TradedVolume");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean isBuyOrSell() {
		return buyOrSell;
	}

	public void setBuyOrSell(boolean buyOrSell) {
		this.buyOrSell = buyOrSell;
	}

	public boolean isCloseTodayOrHistoryPos() {
		return closeTodayOrHistoryPos;
	}

	public void setCloseTodayOrHistoryPos(boolean closeTodayOrHistoryPos) {
		this.closeTodayOrHistoryPos = closeTodayOrHistoryPos;
	}

	public String getInstrumentID() {
		return instrumentID;
	}

	public void setInstrumentID(String instrumentID) {
		this.instrumentID = instrumentID;
	}

	public boolean isNewOrClose() {
		return newOrClose;
	}

	public void setNewOrClose(boolean newOrClose) {
		this.newOrClose = newOrClose;
	}

	public String getOrderSysID() {
		return orderSysID;
	}

	public void setOrderSysID( @NotNull String orderSysID) {
		this.orderSysID = orderSysID.trim();
	}

	public double getOriginPrice() {
		return Util.formatDouble(originPrice);
	}

	public void setOriginPrice(double originPrice) {
		this.originPrice = originPrice;
	}

	public String getOriginTime() {
		return originTime;
	}

	public void setOriginTime(String originTime) {
		this.originTime = originTime;
	}

	public int getOriginVolume() {
		return originVolume;
	}

	public void setOriginVolume(int originVolume) {
		this.originVolume = originVolume;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public double getTradedPrice() {
		return Util.formatDouble(tradedPrice);
	}

	public void setTradedPrice(double tradedPrice) {
		this.tradedPrice = tradedPrice;
	}

	public String getTradedTime() {
		return tradedTime;
	}

	public void setTradedTime(String tradedTime) {
		this.tradedTime = tradedTime;
	}

	public int getTradedVolume() {
		return tradedVolume;
	}

	public void setTradedVolume(int tradedVolume) {
		this.tradedVolume = tradedVolume;
	}

}
