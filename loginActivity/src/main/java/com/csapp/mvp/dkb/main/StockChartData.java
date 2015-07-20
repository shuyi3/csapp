package com.csapp.mvp.dkb.main;

import java.util.ArrayList;

import com.csapp.mvp.dkb.data.Util;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.jetbrains.annotations.NotNull;


public class StockChartData extends LineData {

	public StockChartData(ArrayList<String> xVals) {
        super(xVals);
    }
    
    public StockChartData(String[] xVals) {
        super(xVals);
    }

    public StockChartData(ArrayList<String> xVals, ArrayList<LineDataSet> dataSets) {
        super(xVals, dataSets);
    }

    public StockChartData(String[] xVals, ArrayList<LineDataSet> dataSets) {
        super(xVals, dataSets);
    }
    
    public StockChartData(ArrayList<String> xVals, LineDataSet dataSet) {
        super(xVals, toArrayList(dataSet));        
    }
    
    public StockChartData(String[] xVals, LineDataSet dataSet) {
        super(xVals, toArrayList(dataSet));
    }
    

    @NotNull
    private static ArrayList<LineDataSet> toArrayList(LineDataSet dataSet) {
        ArrayList<LineDataSet> sets = new ArrayList<LineDataSet>();
        sets.add(dataSet);
        return sets;
    }

    
    public int getXindexByXval(String xVal){
    	long[] xValArray = new long[mXVals.size()];
    	for (int i = 0; i < mXVals.size(); i++){
    		xValArray[i] = Util.dateStringToLong(mXVals.get(i));
    	}
    	long key = Util.dateStringToLong(xVal);
    	return Util.binarySearch(xValArray, key);
    }
}    
	
