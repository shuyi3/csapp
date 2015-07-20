package com.csapp.mvp.dkb.data;

import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StockData {
	
	private String time;
	private double askPrice;
	private double bidPrice;
	private double lastPrice;
	private double preSettlementPrice;
	
	public double getPreSettlementPrice() {
		preSettlementPrice = Util.formatPrice(preSettlementPrice);
		return preSettlementPrice;
	}

	public void setPreSettlementPrice(double preSettlementPrice) {
		this.preSettlementPrice = preSettlementPrice;
	}

	public double getLastPrice() {
		lastPrice = Util.formatPrice(lastPrice);
		return lastPrice;
	}

	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public double getAskPrice() {
		askPrice = Util.formatPrice(askPrice);
		return askPrice;
	}

	public void setAskPrice(double askPrice) {
		this.askPrice = askPrice;
	}

	public double getBidPrice() {
		bidPrice = Util.formatPrice(bidPrice);
		return bidPrice;
	}

	public void setBidPrice(double bidPrice) {
		this.bidPrice = bidPrice;
	}

	public StockData(String time, double askPrice, double bidPrice, double lastPrice, double preSettlementPrice){
		this.time = time;
		this.askPrice = Util.formatPrice(askPrice);
		this.bidPrice = Util.formatPrice(bidPrice);
		this.lastPrice = Util.formatPrice(lastPrice);
		this.preSettlementPrice = Util.formatPrice(preSettlementPrice);
	}

	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
		
	public int getSeconds()
	{
		if (time != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.CHINA);
			Date date = null;
			try {
				date = sdf.parse(this.time);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if (date != null)
			    return (int) (date.getTime()/1000.0);
		}
		return -1;
	}
	
	public int getMinutes()
	{
		if (time != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.CHINA);
			Date date = null;
			try {
				date = sdf.parse(this.time);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if (date != null)
                return (int) (date.getTime()/60000.0);
		}
		return -1;
	}

    @Nullable
    public String getFormattedTime(boolean isMinute){

        String format;
        if (isMinute)
            format = "HH:mm";
        else
            format = "HH:mm:ss";

        if (time != null) {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.CHINA);
            SimpleDateFormat targetFormat = new SimpleDateFormat(format, Locale.CHINA);
            Date date = null;
            try {
                 date = originalFormat.parse(time);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return targetFormat.format(date);
        }
        return null;
    }
	

}
