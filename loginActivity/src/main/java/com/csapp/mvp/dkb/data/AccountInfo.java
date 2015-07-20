package com.csapp.mvp.dkb.data;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountInfo {
	
	private double closePosProfit;
	private double commission;
	private double currentTotal;
	private double margin;
	private double posProfit;
    private String moneyRatio;

	public AccountInfo( @NotNull JSONObject AMIObject){
		try {
			closePosProfit = Util.formatDouble(AMIObject.getDouble("ClosePosProfit"));
			commission = Util.formatDouble(AMIObject.getDouble("Commission"));
			currentTotal = Util.formatDouble(AMIObject.getDouble("CurrentTotal"));
			margin = Util.formatDouble(AMIObject.getDouble("Margin"));
			posProfit = Util.formatDouble(AMIObject.getDouble("PosProfit"));
            moneyRatio = AMIObject.getString("MoneyRatio");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public double getClosePosProfit() {
		return closePosProfit;
	}

	public void setClosePosProfit(double closePosProfit) {
		this.closePosProfit = closePosProfit;
	}

	public double getCommission() {
		return commission;
	}

	public void setCommission(double commission) {
		this.commission = commission;
	}

	public double getCurrentTotal() {
		return currentTotal;
	}

	public void setCurrentTotal(double currentTotal) {
		this.currentTotal = currentTotal;
	}

	public double getMargin() {
		return margin;
	}

	public void setMargin(double margin) {
		this.margin = margin;
	}

	public double getPosProfit() {
		return posProfit;
	}

	public void setPosProfit(double posProfit) {
		this.posProfit = posProfit;
	}

    public String getMoneyRatio(){
        return moneyRatio;
    }
}
