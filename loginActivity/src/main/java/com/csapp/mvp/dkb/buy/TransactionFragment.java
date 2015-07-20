package com.csapp.mvp.dkb.buy;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.customwidegts.FloatNumberPicker;
import com.csapp.mvp.dkb.customwidegts.IntegerNumberPicker;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.PosData;
import com.csapp.mvp.dkb.data.StockData;
import com.csapp.mvp.dkb.data.Util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.grantland.widget.AutofitHelper;

public class TransactionFragment extends Fragment {

    @Nullable
    private ActionBar actionBar;
    @Nullable
    private PopupWindow mDropdown = null;
    private LayoutInflater mInflater;
    private IntegerNumberPicker volumePicker;
    private Button upButton, downButton, longButton, shortButton;
    private TextView longFloatingText, shortFloatingText;
    private Switch profitLossSwitch;
    private double step;
    double askPrice = 0.0;
    double bidPrice = 0.0;
    double lastPrice = 0.0;
    ToggleButton stopProfitToggle, stopLossToggle;
    RelativeLayout multiCloseButton, emptyCloseButton;
    int direction;
    int volume;
    @Nullable
    PosData longPos = null, shortPos = null;
    double longFloating, shortFloating;
    boolean isQuotePrice;
    String instrumentId;
    // direction: 1-多开，2-多平，3-空开，4-空平，5-多平今，6-空平今
    private double stopProfit = 1.0, stopLoss = 1.0;


    @NotNull
    String[] profitData;

    //	String[] data;
    private Activity mActivity;

    public TransactionFragment() {
    }


    @NotNull
    public static TransactionFragment newInstance(String instrumentId, Activity activity) {

        TransactionFragment myFragment = new TransactionFragment();

        myFragment.instrumentId = instrumentId;
        myFragment.mActivity = activity;

        System.out.println("ID: " + instrumentId);

        return myFragment;

    }

    public void initVariables() {
        StockData lastData = DataHolder.getInstance().getInsInfoMap().get(instrumentId).getLastData();
        if (lastData != null) {
            if (lastData.getAskPrice() < Double.MAX_VALUE && lastData.getAskPrice() != 0)
                askPrice = lastData.getAskPrice();
            if (lastData.getBidPrice() < Double.MAX_VALUE && lastData.getBidPrice() != 0)
                bidPrice = lastData.getBidPrice();
            if (lastData.getLastPrice() < Double.MAX_VALUE && lastData.getLastPrice() != 0)
                lastPrice = lastData.getLastPrice();
        }

        volume = 1;
        isQuotePrice = false;
        step = DataHolder.getInstance().getInsInfoMap()
                .get(instrumentId).getPriceStep();

        int stopProfitIndex = (int)DataHolder.getInstance().getInsInfoMap()
                .get(instrumentId).getStopProfit();
        int stopLossIndex = (int)DataHolder.getInstance().getInsInfoMap()
                .get(instrumentId).getStopLoss();

        int profitLength = stopLossIndex * 2 - stopProfitIndex + 1;

        profitData = new String[profitLength];

        for (int i = 0; i < profitLength; i++){
            profitData[i] = Double.toString(Util.formatDouble((stopProfitIndex + i) * step));
        }

        stopProfit = Util.formatDouble(Double.parseDouble(profitData[0]));
        stopLoss = Util.formatDouble(stopLossIndex * step);

        longPos = DataHolder.getInstance().getPosData(true, instrumentId);
        shortPos = DataHolder.getInstance().getPosData(false, instrumentId);

        //多仓：（最新价-开仓价）*数量*合约乘数
        //空仓：（开仓价-最新价）*数量*合约乘数
        if (longPos != null)
            longFloating = DataHolder.getInstance().getFloating(instrumentId, true);
        if (shortPos != null)
            shortFloating = DataHolder.getInstance().getFloating(instrumentId, false);

    }

    @Override
    public View onCreateView( @NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_buy, container, false);

        initVariables();

//        actionBar = mActivity.getActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setTitle(Util.convertStockCode(instrumentId));
//        }

        setHasOptionsMenu(true);

        upButton = (Button) view.findViewById(R.id.upButton);
        downButton = (Button) view.findViewById(R.id.downButton);

        longButton = (Button) view.findViewById(R.id.open_pos_button);
        shortButton = (Button) view.findViewById(R.id.close_position_button);

        longFloatingText = (TextView) view.findViewById(R.id.image_button_1_text);
        shortFloatingText = (TextView) view.findViewById(R.id.image_button_2_text);


//		if (longPos != null){
//			longButton.setText(String.valueOf(longPos.getVolume()));
//			longFloatingText.setText(Util.formatNumber(longFloating));
//		}
//		if (shortPos != null){
//			shortButton.setText(String.valueOf(shortPos.getVolume()));
//			shortFloatingText.setText(Util.formatNumber(shortFloating));
//		}

//		upButton.setText("   涨:" + askPrice);
//		downButton.setText("   跌:" + bidPrice);

        multiCloseButton = (RelativeLayout) view
                .findViewById(R.id.image_button_1);
        emptyCloseButton = (RelativeLayout) view
                .findViewById(R.id.image_button_2);

//        multiCloseButton.setEnabled(false);
//        emptyCloseButton.setEnabled(false);

        volumePicker = (IntegerNumberPicker) view.findViewById(R.id.quantityPicker);
//		volumePicker.setQuantity(volume);

        stopProfitToggle = (ToggleButton) view.findViewById(R.id.stopProfit);
//		stopProfitToggle.setTextOn("止盈:" + stopProfit);
//		stopProfitToggle.setTextOff("止盈:" + stopProfit);
        stopLossToggle = (ToggleButton) view.findViewById(R.id.stopLoss);

        volumePicker.setQuantity(volume);

        stopProfitToggle.setTextOn("止盈:" + stopProfit + "点");
        stopProfitToggle.setTextOff("止盈:" + stopProfit + "点");
        stopProfitToggle.setText("止盈:" + stopProfit + "点");

        stopLossToggle.setTextOn("止损:" + stopLoss + "点");
        stopLossToggle.setTextOff("止损:" + stopLoss + "点");
        stopLossToggle.setText("止损:" + stopLoss + "点");

        stopLossToggle.setEnabled(false);

        TextView imageTextView1 = (TextView) view.findViewById(R.id.image_textview1);
        TextView imageTextView2 = (TextView) view.findViewById(R.id.image_textview2);

        profitLossSwitch = (Switch) view.findViewById(R.id.switch1);

        //auto fit settings
        AutofitHelper.create(stopProfitToggle);
        AutofitHelper.create(stopLossToggle);
        AutofitHelper.create(longFloatingText);
        AutofitHelper.create(shortFloatingText);
        AutofitHelper.create(imageTextView1);
        AutofitHelper.create(imageTextView2);

        updatePriceData();

        stopProfitToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick( @NotNull View v) {
                initiatePopupWindow(v, true);
            }
        });

//		stopLossToggle.setTextOn("止损:" + stopLoss);
//		stopLossToggle.setTextOff("止损:" + stopLoss);
        stopLossToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick( @NotNull View v) {
                initiatePopupWindow(v, false);
            }
        });

        volumePicker.setOnNumberChangeListener(new FloatNumberPicker.onNumberChangeListener() {
            @Override
            public void onNumberChanged() {
                volume = volumePicker.getQuantity();
            }
        });

        profitLossSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    if (!stopProfitToggle.isEnabled()) {
                        stopProfitToggle.setEnabled(true);
                        stopProfitToggle.setTextOn("止盈:" + stopProfit + "点");
                        stopProfitToggle.setTextOff("止盈:" + stopProfit + "点");
                        stopProfitToggle.setText("止盈:" + stopProfit + "点");
                    }
//                    if (!stopLossToggle.isEnabled()) {
//                        stopLossToggle.setEnabled(true);
//                        stopLossToggle.setTextOn("止损:" + stopLoss + "点");
//                        stopLossToggle.setTextOff("止损:" + stopLoss + "点");
//                        stopLossToggle.setText("止损:" + stopLoss + "点");
//                    }
                }else{
                    if (stopProfitToggle.isEnabled()) {
                        stopProfitToggle.setEnabled(false);
                    }
//                    if (stopLossToggle.isEnabled()) {
//                        stopLossToggle.setEnabled(false);
//                    }
                }
            }
        });

        profitLossSwitch.setChecked(true);

        multiCloseButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 多平
                direction = 2;
                if (isQuotePrice) {
                    Util.sendRequest(
                            instrumentId, direction, 0, 0,
                            0, 0, mActivity);
                } else {
                    Util.sendRequest(
                            instrumentId, direction, 0, 0,
                            0, 0, mActivity);
                }
            }

        });

        emptyCloseButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 空平
                direction = 4;
                if (isQuotePrice) {
                    Util.sendRequest(
                            instrumentId, direction, 0, 0,
                            0, 0, mActivity);
                } else {
                    Util.sendRequest(
                            instrumentId, direction, 0, 0,
                            0, 0, mActivity);
                }
            }

        });

        upButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 多开
                direction = 1;
                if (isQuotePrice) {
//                    if (profitLossSwitch.isChecked()) {
//                        Util.sendRequest(
//                                instrumentId, direction, quotePrice, volume,
//                                stopProfit, stopLoss, mActivity);
//                    }else {
//                        Util.sendRequest(
//                                instrumentId, direction, quotePrice, volume,
//                                0, 0, mActivity);
//                    }
                } else {
                    if (profitLossSwitch.isChecked()) {
                        Util.sendRequest(
                                instrumentId, direction, 0, volume,
                                stopProfit, stopLoss, mActivity);
                    }else {
                        Util.sendRequest(
                                instrumentId, direction, 0, volume,
                                0, 0, mActivity);
                    }
                }
            }
        });

        downButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 空开
                direction = 3;
                if (isQuotePrice) {
//                    if (profitLossSwitch.isChecked()) {
//                        Util.sendRequest(
//                                instrumentId, direction, quotePrice, volume,
//                                stopProfit, stopLoss, mActivity);
//                    }else{
//                        Util.sendRequest(
//                                instrumentId, direction, quotePrice, volume,
//                                0, 0, mActivity);
//                    }
                } else {
                    if (profitLossSwitch.isChecked()) {
                        Util.sendRequest(
                                instrumentId, direction, 0, volume,
                                stopProfit, stopLoss, mActivity);
                    }else{
                        Util.sendRequest(
                                instrumentId, direction, 0, volume,
                                0, 0, mActivity);
                    }
                }
            }
        });

        return view;

        // ////Button myButton = (Button) findViewById(R.id.my_button); //Told
        // in tutorial to put this in onCreate
    }

//	public void sendRequest(String accountId, String instrumentId,
//			int direction, double price, int volume, double stopProfit,
//			double stopLoss) {
//
//		String operation = null;
//
//		switch (direction) {
//		case 1:
//			operation = "多开";
//			break;
//		case 2:
//			operation = "多平";
//			break;
//		case 3:
//			operation = "空开";
//			break;
//		case 4:
//			operation = "空平";
//			break;
//		}
//		
//		final String op = operation;
//
//		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
//		b.setTitle("交易信息: " + operation);
//		b.setMessage(instrumentId + "\n价格:" + price + "\n数量:" + volume
//				+ "\n止盈:" + stopProfit + "\n止损:" + stopLoss);
//		b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
//				String message = "交易: " + op + "请求已成功发送。";
//				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
//			}
//
//		});
//		b.show();
//
//	}

    public void updatePriceData() {
        StockData lastData = DataHolder.getInstance().getInsInfoMap().get(instrumentId).getLastData();
        if (lastData != null) {
            if (lastData.getAskPrice() < Double.MAX_VALUE && lastData.getAskPrice() != 0) {
                askPrice = lastData.getAskPrice();
            }
            if (lastData.getBidPrice() < Double.MAX_VALUE && lastData.getBidPrice() != 0) {
                bidPrice = lastData.getBidPrice();
            }
            if (lastData.getLastPrice() < Double.MAX_VALUE && lastData.getLastPrice() != 0) {
                lastPrice = lastData.getLastPrice();
            }
//            if (!priceMode.isChecked()) {
            upButton.setText("   涨:" + askPrice);
            downButton.setText("   跌:" + bidPrice);
//            }
        }

        longPos = DataHolder.getInstance().getPosData(true, instrumentId);
        shortPos = DataHolder.getInstance().getPosData(false, instrumentId);

        //多仓：（最新价-开仓价）*数量*合约乘数
        //空仓：（开仓价-最新价）*数量*合约乘数
        if (longPos != null) {
            longFloating = DataHolder.getInstance().getFloating(instrumentId, true);
            longButton.setText(String.valueOf(longPos.getVolume()));
            longFloatingText.setText(Util.formatNumber(longFloating));
        } else {
            longFloating = 0.0;
            longButton.setText("0");
            longFloatingText.setText(Util.formatNumber(longFloating));
        }

        if (shortPos != null) {
            shortFloating = DataHolder.getInstance().getFloating(instrumentId, false);
            shortButton.setText(String.valueOf(shortPos.getVolume()));
            shortFloatingText.setText(Util.formatNumber(shortFloating));
        } else {
            shortFloating = 0.0;
            shortButton.setText("0");
            shortFloatingText.setText(Util.formatNumber(shortFloating));
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
	    MenuItem item=menu.findItem(R.id.action_back);
        item.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected( @NotNull MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Nullable
    private PopupWindow initiatePopupWindow( @NotNull View v, final boolean isProfit) {

        try {

            mInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View layout = mInflater.inflate(R.layout.row, null);

            // If you want to add any listeners to your textviews, these are two
            // //textviews.

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mDropdown = new PopupWindow(layout,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT, true);
            Drawable background = getResources().getDrawable(
                    android.R.drawable.editbox_dropdown_dark_frame);
            mDropdown.setBackgroundDrawable(background);

            mDropdown.setOnDismissListener(new PopupWindow.OnDismissListener() {

                @Override
                public void onDismiss() {
                    mDropdown.dismiss();
                    if (isProfit) {
                        stopProfitToggle.setChecked(false);
                    } else {
                        stopLossToggle.setChecked(false);
                    }
                    // end may TODO anything else
                }
            });

            ListView listView = (ListView) layout.findViewById(R.id.listView1);
            listView.setAdapter(new ArrayAdapter<String>(getActivity(),
                    R.layout.smalllist, profitData));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> arg0, View view,
                                        int position, long id) {
                    // TODO Auto-generated method stub
                    if (isProfit) {
                        stopProfit = Util.formatDouble(Double.parseDouble(profitData[position]));
                        stopProfitToggle.setTextOn("止盈:" + stopProfit + "点");
                        stopProfitToggle.setTextOff("止盈:" + stopProfit + "点");
                        stopProfitToggle.setText("止盈:" + stopProfit + "点");
                    } else {
                        stopLoss = Util.formatDouble(Double.parseDouble(profitData[position]));
                        stopLossToggle.setTextOn("止损:" + stopLoss + "点");
                        stopLossToggle.setTextOff("止损:" + stopLoss + "点");
                        stopLossToggle.setText("止损:" + stopLoss + "点");
                    }
                    mDropdown.dismiss();
                }
            });

            Rect location = locateView(v);

            mDropdown.showAtLocation(v, Gravity.BOTTOM | Gravity.LEFT,
                    location.left, location.top);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mDropdown;

    }

    @Nullable
    private Rect locateView(@Nullable View v) {
        int[] loc_int = new int[2];
        if (v == null)
            return null;
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe) {
            // Happens when the view doesn't exist on screen anymore.
            return null;
        }

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;

        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = height - loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top - v.getHeight();
        return location;
    }

    public void tickUpdate() {
        updatePriceData();
    }

    public void requestUpdate() {
        longPos = DataHolder.getInstance().getPosData(true, instrumentId);
        shortPos = DataHolder.getInstance().getPosData(false, instrumentId);

        //多仓：（最新价-开仓价）*数量*合约乘数
        //空仓：（开仓价-最新价）*数量*合约乘数
        if (longPos != null) {
            longFloating = DataHolder.getInstance().getFloating(instrumentId, true);
            longButton.setText(String.valueOf(longPos.getVolume()));
            longFloatingText.setText(Util.formatNumber(longFloating));
        } else {
            longFloating = 0.0;
            longButton.setText("0");
            longFloatingText.setText(Util.formatNumber(longFloating));
        }

        if (shortPos != null) {
            shortFloating = DataHolder.getInstance().getFloating(instrumentId, false);
            shortButton.setText(String.valueOf(shortPos.getVolume()));
            shortFloatingText.setText(Util.formatNumber(shortFloating));
        } else {
            shortFloating = 0.0;
            shortButton.setText("0");
            shortFloatingText.setText(Util.formatNumber(shortFloating));
        }
    }

    public void refreshContent() {
        updatePriceData();
    }

}