package com.csapp.mvp.dkb.main;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;

public class DynamicLineChart extends LineChart{

	protected String xValToHighlight;

	public String getxValToHighlight() {
		return xValToHighlight;
	}

	public void setxValToHighlight(String xValToHighlight) {
		this.xValToHighlight = xValToHighlight;
	}

	public DynamicLineChart(Context context) {
		super(context);
	}

	public DynamicLineChart(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DynamicLineChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void updateHighlights(int minusDelta){
		
//        for (int i = 0; i < mIndicesToHightlight.length; i++) {
		if (mIndicesToHightlight != null)
		if ( mIndicesToHightlight.length > 0){
        	int mIndex = ((StockChartData)getData()).getXindexByXval(xValToHighlight);
        	 if (mIndex >= 0)
					mIndicesToHightlight[0].setmXIndex(mIndex);
		}
        	 
//        }
        
//        for (int i = 0; i < mIndicesToHightlight.length; i++) {
//        	int mIndex = ((StockChartData)getData()).getXindexByXval(xValToHighlight);
//        	 if (mIndex >= 0)
//					mIndicesToHightlight[i].setmXIndex(mIndex);
//        	 
//        }
	}
   	
}

