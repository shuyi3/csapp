package com.csapp.mvp.dkb.data;

public class TXData {
	
	private int serialNumber;
	private String TXName;
	private boolean isDeal;
	private boolean isBull;
	private boolean isOpen;
	private float settlementPrice;
	private int orderNumber;
	private String orderTime;
	
	public TXData(int serialNumber, String TXName, boolean isDeal, boolean isBull, boolean isOpen, float settlementPrice, int orderNumber, String orderTime){
		this.serialNumber = serialNumber;
		this.TXName = TXName;
		this.isDeal = isDeal;
		this.isBull = isBull;
		this.isOpen = isOpen;
		this.settlementPrice = settlementPrice;
		this.orderNumber = orderNumber;
		this.orderTime = orderTime;
	}
	
	public int getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getTXName() {
		return TXName;
	}
	public void setTXName(String tXName) {
		TXName = tXName;
	}
	public boolean isDeal() {
		return isDeal;
	}
	public void setDeal(boolean isDeal) {
		this.isDeal = isDeal;
	}
	public boolean isBull() {
		return isBull;
	}
	public void setBull(boolean isBull) {
		this.isBull = isBull;
	}
	public boolean isOpen() {
		return isOpen;
	}
	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
	public float getSettlementPrice() {
		return settlementPrice;
	}
	public void setSettlementPrice(float settlementPrice) {
		this.settlementPrice = settlementPrice;
	}
	public int getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	public String getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
	
	
	

}
