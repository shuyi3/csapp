package com.csapp.mvp.dkb.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.csapp.mvp.dkb.data.StockData;
import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.Util;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MarkerView;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.github.mikephil.charting.utils.XLabels.XLabelPosition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class FigureFragment extends Fragment implements OnChartValueSelectedListener {

    private DynamicLineChart mChart;
    private TextView title, price;
    private int maxDataSize;
    private Activity mActivity;
    @Nullable
    private String instrumentID = null;

    @Nullable
    public String getInstrumentID() {
        return instrumentID;
    }

    public void setInstrumentID(String instrumentID) {
        this.instrumentID = instrumentID;
    }

    public enum GraphType {
        TICK_GRAPH,
        MINUTE_GRAPH
    }

    private GraphType graphType;

    public GraphType getGraphType() {
        return graphType;
    }

    public void setGraphType(GraphType graphType) {
        this.graphType = graphType;
    }

    public FigureFragment() {

    }


    @NotNull
    public static FigureFragment newInstance(String instrumentID, GraphType graphType, Activity activity) {
        FigureFragment myFragment = new FigureFragment();

        myFragment.graphType = graphType;
        myFragment.mActivity = activity;
        myFragment.instrumentID = instrumentID;
        if (graphType == GraphType.TICK_GRAPH) {
            myFragment.maxDataSize = 60;
        } else {
            myFragment.maxDataSize = 500;
        }

        return myFragment;
    }


    @Override
    public View onCreateView( @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_simple_line, container, false);
        super.onCreate(savedInstanceState);

        title = (TextView) v.findViewById(R.id.figure_title);
        price = (TextView) v.findViewById(R.id.last_price);

        mChart = (DynamicLineChart) v.findViewById(R.id.lineChart1);
        mChart.setDrawYValues(false);
        mChart.setDrawGridBackground(false);
        mChart.setNoDataText("暂时没有可用数据");
        mChart.setDescription("");
        mChart.getXLabels().setPosition(XLabelPosition.BOTTOM);
        mChart.setDrawLegend(false);
        mChart.getYLabels().setSeparateThousands(false);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.getYLabels().setFormatter(new MyValueFormatter());
        mChart.getYLabels().setTextColor(Color.RED);
        mChart.getXLabels().setTextColor(Color.RED);
        mChart.setBorderWidth(1);
        mChart.setBorderColor(Color.WHITE);

        MyMarkerView mv = new MyMarkerView(mActivity, R.layout.custom_marker_view);

        // define an offset to change the original position of the marker
        // (optional)
        mv.setOffsets(-mv.getMeasuredWidth() / 2, -mv.getMeasuredHeight());

        // set the marker to the chart
        mChart.setMarkerView(mv);

        refreshData(instrumentID, true);

        return v;
    }

    int[] mColors = ColorTemplate.COLORFUL_COLORS;

    public void addEntry(double closePrice) {

        StockChartData data = (StockChartData) mChart.getData();

        if (data != null) {

            LineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry((float) closePrice, set.getEntryCount()), 0);

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // redraw the chart
            mChart.invalidate();
        }
    }

    public synchronized void refreshData( @NotNull String instrumentID, boolean isDataChanged) {

//    	Log.d("MPChart", "refresh called");

        boolean isInit = false;
        if (!this.instrumentID.equals(instrumentID)) {
            isInit = true;
            this.instrumentID = instrumentID;
        }

        ArrayList<StockData> stockDataArray;
        String graphName = null;
//        if (graphType == GraphType.TICK_GRAPH) {
//            stockDataArray = DataHolder.getInstance().getInstantStock().get(instrumentID);
//            if (DataHolder.getInstance().isRealStock())
//                graphName = Util.convertStockCode(instrumentID) + " Tick图";
//            else
//                graphName = Util.convertStockCode(instrumentID) + "(虚拟盘) Tick图";

//        } else {
            stockDataArray = DataHolder.getInstance().getLongtermStock().get(instrumentID);
            if (DataHolder.getInstance().isRealStock())
                graphName = Util.convertStockCode(instrumentID) + " 分时图";
            else
                graphName = Util.convertStockCode(instrumentID) + "(虚拟盘) 分时图";
//        }

        if (title != null)
            title.setText(graphName);

        String priceMessage = "最新价: ";
        StockData lastData = null;
        if (price != null) {
            lastData = DataHolder.getInstance().getInsInfoMap().get(instrumentID).getLastData();
            if (lastData != null) {
                double lastPrice = lastData.getLastPrice();
                if (lastPrice != 0 && lastPrice < Double.MAX_VALUE) {
                    priceMessage += lastPrice;
                }
            }
            price.setText(priceMessage);
        }

        if (stockDataArray.size() <= 1) {
            mChart.clear();
            return;
        }

        if (isDataChanged || isInit || mChart.isEmpty()) {

            ArrayList<String> xVals = new ArrayList<String>();
            ArrayList<Entry> yVals = new ArrayList<Entry>();

            if (stockDataArray.size() > maxDataSize) {
                int j = 0;
                for (int i = stockDataArray.size() - maxDataSize; i < stockDataArray.size(); i++) {
                    xVals.add(stockDataArray.get(i).getFormattedTime(graphType == GraphType.MINUTE_GRAPH? true : false));
                    yVals.add(new Entry((float) stockDataArray.get(i).getLastPrice(), j));
                    j++;
                }
            } else {
                int i = 0;
                for (StockData data : stockDataArray) {
                    xVals.add(data.getFormattedTime(graphType == GraphType.MINUTE_GRAPH? true : false));
                    yVals.add(new Entry((float) data.getLastPrice(), i));
                    i++;
                }
            }

//     	 if (isDataChanged)
//            mChart.updateHighlights(1);

            StockChartData data = new StockChartData(xVals);
            LineDataSet set = new LineDataSet(yVals, graphName);

            set.setDrawCircles(false);
            set.setLineWidth(1.5f);
            int color = mColors[2];

            set.setColor(color);
            set.setCircleColor(color);
            set.setHighLightColor(mColors[3]);

            data.addDataSet(set);

            mChart.setData(data);
            float diff = (set.getYMax() - set.getYMin()) * 0.2f;
//            float ceiling;
//            if (diff > 10) {
//                ceiling = set.getYMax() + diff;
//            } else {
//                ceiling = set.getYMax() + 10;
//            }


            float ceiling = getCeiling(set.getYMin(), set.getYMax());
            float floor = getFloor(set.getYMin(), set.getYMax());

            mChart.setYRange(floor, ceiling, true);

            mChart.notifyDataSetChanged();

        } else if (graphType == GraphType.MINUTE_GRAPH) {
            if (lastData != null) {
                float lastPrice = (float) lastData.getLastPrice();
                LineDataSet set = mChart.getData().getDataSetByIndex(0);
                int size = set.getYVals().size();
                set.getYVals().get(size - 1).setVal(lastPrice);
//                float ceiling = getCeiling(set.getYMin(), set.getYMax());
//                if (lastPrice > set.getYMax()){
//                    ceiling = getCeiling(set.getYMin(), lastPrice);
//                }
//                float floor = getFloor(set.getYMin(), set.getYMax());
//                if (lastPrice < set.getYMax()){
//                    floor = getFloor(lastPrice, set.getYMax());
//                }
//                mChart.setYRange(floor, ceiling, true);
            }
        }

        mChart.invalidate();

    }


    @NotNull
    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "DataSet 1");
        set.setLineWidth(2.5f);
        set.setCircleSize(4.5f);
        set.setColor(Color.rgb(240, 99, 99));
        set.setCircleColor(Color.rgb(240, 99, 99));
        set.setHighLightColor(Color.rgb(190, 190, 190));

        return set;
    }

    public class MyMarkerView extends MarkerView {

        private TextView tvContentTime, tvContentPrice;

        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);

            tvContentTime = (TextView) findViewById(R.id.tvContentTime);
            tvContentPrice = (TextView) findViewById(R.id.tvContentPrice);

        }

        @Override
        public void refreshContent( @NotNull Entry e, int dataSetIndex) {

            tvContentTime.setText("时间: " + mChart.getData().getXVals().get(e.getXIndex()));
            tvContentPrice.setText("价格: " + Utils.formatNumber(e.getVal(), 0, false));
        }
    }

    @Override
    public void onValueSelected( @NotNull Entry e, int dataSetIndex) {
        // TODO Auto-generated method stub
        mChart.setxValToHighlight(mChart.getData().getXVals().get(e.getXIndex()));
    }

    @Override
    public void onNothingSelected() {
        // TODO Auto-generated method stub

    }

    private class MyValueFormatter implements ValueFormatter {

        /**
         * decimalformat for formatting
         */

        @NotNull
        private DecimalFormat mFormat = new DecimalFormat("#.##");

        @Override
        public String getFormattedValue(float value) {
            // avoid memory allocations here (for performance)
            return mFormat.format(value);
        }
    }

    private float getFloor(float min, float max){

        if (max == min) {
            return min * 0.9f;
        }

        float floor = min - (max - min) * 0.05f;
        if (floor < 0) return 0f;

        return floor;
    }

    private float getCeiling(float min, float max){

        if (max == min) {
            return max * 1.1f;
        }

        return max + (max - min) * 0.2f;
    }


}
