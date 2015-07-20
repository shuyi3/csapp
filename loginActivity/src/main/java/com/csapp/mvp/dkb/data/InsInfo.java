package com.csapp.mvp.dkb.data;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class InsInfo {
	
	private String instrumentID;
	private String instrumentName;
    private String exchangeName;
	private double multiplier;
	private double priceStep;
	private double margin;
	private double stopProfit;
	private double stopLoss;
	private StockData lastData;

	public InsInfo( @NotNull JSONObject insInfoObject){
		try {
			this.instrumentID = insInfoObject.getString("InstrumentID");
			this.instrumentName = insInfoObject.getString("InstrumentName");
            this.exchangeName = insInfoObject.getString("ExchangeName");
            this.multiplier = insInfoObject.getDouble("Multiplier");
			this.priceStep = insInfoObject.getDouble("PriceStep");
			this.margin = insInfoObject.getDouble("Margin");
			this.stopProfit = insInfoObject.getDouble("StopProfit");
			this.stopLoss = insInfoObject.getDouble("StopLoss");
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

	public String getInstrumentName() {
		return instrumentName;
	}

	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	public double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	public double getPriceStep() {
		return priceStep;
	}

	public void setPriceStep(double priceStep) {
		this.priceStep = priceStep;
	}	
	
	public StockData getLastData() {
		return lastData;
	}

	public void setLastData(StockData lastData) {
		this.lastData = lastData;
	}

    public String getExchangeName() {
        return exchangeName;
    }

	public double getMargin() {
		return margin;
	}

	public double getStopProfit() {
		return stopProfit;
	}

	public double getStopLoss() {
		return stopLoss;
	}

}
