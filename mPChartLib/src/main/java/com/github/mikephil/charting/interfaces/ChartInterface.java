
package com.github.mikephil.charting.interfaces;

import android.graphics.RectF;
import android.view.View;

/**
 * Interface that provides everything there is to know about the dimensions,
 * bounds, and range of the chart.
 * 
 * @author Philipp Jahoda
 */
public interface ChartInterface {

    float getOffsetBottom();

    float getOffsetTop();

    float getOffsetLeft();

    float getOffsetRight();

    float getDeltaX();

    float getDeltaY();

    float getYChartMin();

    float getYChartMax();

    int getWidth();

    int getHeight();
    
    RectF getContentRect();
    
    View getChartView();
}
