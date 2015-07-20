package com.csapp.mvp.dkb.details;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import com.cengalabs.flatui.views.FlatButton;
import com.csapp.mvp.dkb.Login.CSApplication;
import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.OrderData;
import com.csapp.mvp.dkb.data.PosData;
import com.csapp.mvp.dkb.data.Util;
import com.csapp.mvp.dkb.main.MainActivity;
import com.csapp.mvp.dkb.network.DetailNetworkInteraction;
import com.csapp.mvp.dkb.pay.PaymentWebview;
import com.inqbarna.tablefixheaders.TableFixHeaders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * This activity allows you to have multiple views (in this case two
 * {@link ListView}s) in one tab activity. The advantages over separate
 * activities is that you can maintain tab state much easier and you don't have
 * to constantly re-create each tab activity when the tab is selected.
 */
//public class TabbedListFragment extends Fragment implements
//        TabHost.OnTabChangeListener, RadioGroup.OnCheckedChangeListener {

public class TabbedListActivity extends Activity implements
        TabHost.OnTabChangeListener, RadioGroup.OnCheckedChangeListener {

    private DetailNetworkInteraction networkInteraction;

    private static final String LIST1_TAB_TAG = "自动单";
    private static final String LIST2_TAB_TAG = "已成交";
    private static final String LIST3_TAB_TAG = "持仓详情";
    private static final String LIST4_TAB_TAG = "资金详情";

    @Nullable
    private ArrayList<TXRecord> unfinishedRecordList = null;
    @Nullable
    private ArrayList<TXRecord> finishedRecordList = null;

    private static final int NAVIGATION_MODE_STANDARD = 0;

    // The two views in our tabbed example
    private TableFixHeaders listView1;
    private TableFixHeaders listView2;
    @Nullable
    private ActionBar actionBar;
    private Menu mOptionsMenu;

    private TabHost tabHost;

    private RelativeLayout progressBar;
    private ListView listView3;
    private View infoView;
    private TextView AccountText;
    private TextView profitlossText;
//    private TextView commissionText;
    private TextView currentTotalText;
    private TextView curTotalText;
    private Button payButton, withdrawButton;
    private boolean isNavigateToLogin = false;

//    public MenuItem statsItem;
//    public TextView statsTextTotal;
//    public TextView statsTextProfitLoss;


    public TabbedListActivity() {
    }

    private class TXType {
        private final String name;

        @NotNull
        private final List<TXRecord> list;

        TXType(String name) {
            this.name = name;
            list = new ArrayList<TXRecord>();
        }

        public int size() {
            return list.size();
        }

        public TXRecord get(int i) {
            return list.get(i);
        }
    }

    private class TXRecord {

        @NotNull
        private final String[] data;

        private TXRecord(String name, String company, String version,
                         String api, String storage, String inches, String ram) {
            data = new String[]{name, company, version, api, storage, inches,
                    ram};
        }

        private TXRecord(String name, String orderSysId, String company, String version,
                         String api, String storage, String inches, String ram) {
            data = new String[]{name, orderSysId, company, version, api, storage, inches,
                    ram};
        }
    }

    //    @Override
//    public void onCreate() {
//        super.onCreate(savedInstanceState);
////        setHasOptionsMenu(true);
//    }
    @Override
    protected void onResume() {
        CSApplication.setContext(this);
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.detail_tabhost);
//        View view = inflater.inflate(R.layout.detail_tabhost, container, false);
        super.onCreate(savedInstanceState);

        CSApplication.setContext(this);

        networkInteraction = new DetailNetworkInteraction();

        actionBar = ((Activity) CSApplication.getContext()).getActionBar();
        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);

            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);

//            actionBar.setTitle("交易明细");

            ViewGroup v = (ViewGroup) LayoutInflater.from(this)
                    .inflate(R.layout.actionbar_top_tabbed, null);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM); //show it
            actionBar.setCustomView(v,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER));

            ImageView refresh = (ImageView) findViewById(R.id.refresh_image);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Util.JSONTask().execute("");
                }
            });

//            statsTextTotal = (TextView) findViewById(R.id.account_total);
//            statsTextProfitLoss = (TextView) findViewById(R.id.account_profitloss);
        }
//        updateStatsView();

        tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setOnTabChangedListener(this);

        infoView = findViewById(R.id.detail_layout);

        listView2 = (TableFixHeaders)
                findViewById(R.id.fixed_header_list2);

        listView1 = (TableFixHeaders)
                findViewById(R.id.fixed_header_list1);

        listView3 = (ListView) findViewById(R.id.detail_list);

        AccountText = (TextView) findViewById(R.id.account_info);

        profitlossText = (TextView)
                findViewById(R.id.profitloss_info);

//        commissionText = (TextView)
//                findViewById(R.id.commission_info);

        currentTotalText = (TextView)
                findViewById(R.id.total_info);

        curTotalText = (TextView)
                findViewById(R.id.curtotal_info);

        payButton = (Button)
                findViewById(R.id.pay_button);

        withdrawButton = (Button) findViewById(R.id.withdraw_button);

        LinearLayout moneyOperation = (LinearLayout) findViewById(R.id.moneyOpration);
        if (!DataHolder.getInstance().isRealStock()){
            moneyOperation.setVisibility(View.GONE);
        }

        payButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                payAction();
            }
        });

        withdrawButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                double currentTotal = DataHolder.getInstance().getAMI()
                        .getCurrentTotal() - DataHolder.getInstance().getAMI().getMargin() - DataHolder.getInstance().getAMI().getPosProfit();

                int amount = (int) currentTotal;
                withdrawAction(amount);
            }
        });


        updateList2();
        updateList1();
        updateList3();
        updateAccountInfo();

        listView3.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long id) {

                PosData posData = DataHolder.getInstance().getPosList()
                        .get(position);
                int direction;
                if (posData.isLongOrShort()) {
                    direction = 2;
                } else {
                    direction = 4;
                }
                Util.sendRequest(
                        posData.getInstrumentID(), direction, 0, 0, 0, 0,
                        (MainActivity) CSApplication.getContext());

            }
        });

        tabHost.setup();

        // add views to tab host

        tabHost.addTab(tabHost.newTabSpec(LIST4_TAB_TAG)
                .setIndicator(LIST4_TAB_TAG)
                .setContent(new TabContentFactory() {
                    public View createTabContent(String arg0) {
                        return infoView;
                    }
                }));

        tabHost.addTab(tabHost.newTabSpec(LIST3_TAB_TAG)
                .setIndicator(LIST3_TAB_TAG)
                .setContent(new TabContentFactory() {
                    public View createTabContent(String arg0) {
                        return listView3;
                    }
                }));

        tabHost.addTab(tabHost.newTabSpec(LIST1_TAB_TAG)
                .setIndicator(LIST1_TAB_TAG)
                .setContent(new TabContentFactory() {
                    public View createTabContent(String arg0) {
                        return listView1;
                    }
                }));

        tabHost.addTab(tabHost.newTabSpec(LIST2_TAB_TAG)
                .setIndicator(LIST2_TAB_TAG)
                .setContent(new TabContentFactory() {
                    public View createTabContent(String arg0) {
                        return listView2;
                    }
                }));

        SegmentedGroup segmentedControl = (SegmentedGroup)
                findViewById(R.id.segmented2);
        segmentedControl.setOnCheckedChangeListener(this);
        segmentedControl.check(R.id.button21);
        // tabHost.setCurrentTabByTag(LIST1_TAB_TAG);
        checkTab(LIST4_TAB_TAG);

//        return view;

    }

    public void payAction() {
        Intent intent = new Intent(this, PaymentWebview.class);
        intent.putExtra("action", "pay");
        startActivityForResult(intent, 1);
    }

    public void withdrawAction(int amount) {
        Intent intent = new Intent(this, PaymentWebview.class);
        intent.putExtra("action", "withdraw");
        intent.putExtra("amount", amount);
        startActivityForResult(intent, 1);
    }

    public void updateList1() {

        ArrayList<OrderData> unfinishedData = DataHolder.getInstance()
                .getUnfinishedOrderList();

        if (unfinishedData == null) return;

        if (unfinishedRecordList == null) {
            unfinishedRecordList = new ArrayList<TXRecord>();
        } else {
            unfinishedRecordList.clear();
        }

        int i = 0;
        for (OrderData tempItem : unfinishedData) {
            i++;
            // close & buy = 空平
            // new & buy = 多开
            // new & sell = 空开
            // close & sell = 多平
            unfinishedRecordList.add(new TXRecord(String.valueOf(i), Util
                    .convertStockCode(tempItem.getInstrumentID()), ((tempItem
                    .isBuyOrSell() && tempItem.isNewOrClose() || (!tempItem
                    .isBuyOrSell() && !tempItem.isNewOrClose()))) ? "多" : "空",
                    tempItem.isNewOrClose() ? "开" : "平", (tempItem
                    .getOriginPrice() == 0) ? "市价" : String
                    .valueOf(tempItem.getOriginPrice()), String
                    .valueOf(tempItem.getOriginVolume() - tempItem.getTradedVolume()), (tempItem
                    .getOriginTime().equals("null") ? "N/A" : tempItem
                    .getOriginTime())));
        }

        String unfinHeaders[] = {"序号", "合约名称", "多空", "开平", "委托价", "挂单量", "挂单时间",};
        int unfinWidthArray[] = {50, 75, 50, 50, 70, 50, 70};

        if (listView1.getAdapter() == null) {
            listView1.setAdapter(new MyAdapter(CSApplication.getContext(), unfinHeaders, unfinishedRecordList,
                    false, unfinWidthArray));
        } else {

            ((Activity) CSApplication.getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    ((MyAdapter) listView1.getAdapter()).notifyDataSetChanged();
                }
            });
        }

    }

    public void updateList2() {

        ArrayList<OrderData> finishedData = DataHolder.getInstance()
                .getFinishedOrderList();

        if (finishedData == null) return;
        int i = 0;
        if (finishedRecordList == null) {
            finishedRecordList = new ArrayList<TXRecord>();
        } else {
            finishedRecordList.clear();
        }

        String finHeaders[] = {"序号", "合约名称", "多空", "开平", "成交价", "成交量", "成交时间", "成交号"};
        int finWidthArray[] = {50, 75, 50, 50, 70, 50, 70, 70};

        for (OrderData tempItem : finishedData) {
            i++;
            finishedRecordList.add(new TXRecord(String.valueOf(i), Util
                    .convertStockCode(tempItem.getInstrumentID()), ((tempItem
                    .isBuyOrSell() && tempItem.isNewOrClose() || (!tempItem
                    .isBuyOrSell() && !tempItem.isNewOrClose()))) ? "多" : "空",
                    tempItem.isNewOrClose() ? "开" : "平", String
                    .valueOf(tempItem.getTradedPrice()), String
                    .valueOf(tempItem.getTradedVolume()), (tempItem
                    .getTradedTime().equals("null") ? "N/A" : tempItem
                    .getTradedTime()), tempItem.getOrderSysID()));
        }

        if (listView2.getAdapter() == null) {
            listView2.setAdapter(new MyAdapter(CSApplication.getContext(), finHeaders, finishedRecordList, true, finWidthArray));
        } else {

            ((Activity) CSApplication.getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    ((MyAdapter) listView2.getAdapter()).notifyDataSetChanged();
                }
            });
        }
    }

    public void updateList3() {

        if (listView3.getAdapter() == null) {
            DetailAdapter adapter = new DetailAdapter((Activity) CSApplication.getContext(), listView3);
            listView3.setAdapter(adapter);
        } else {

            ((Activity) CSApplication.getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    ((BaseAdapter) listView3.getAdapter()).notifyDataSetChanged();
                }
            });
        }

    }

    public void updateAccountInfo() {

        if (DataHolder.getInstance().getAMI() != null) {
            AccountText.setText(DataHolder.getInstance().getAccount());

            double profitloss = DataHolder.getInstance().getAMI()
                    .getClosePosProfit()
                    - DataHolder.getInstance().getAMI().getCommission() + DataHolder.getInstance().getTotalFloating();
            profitlossText.setText(Util.formatNumber(profitloss));

            double currentTotal = DataHolder.getInstance().getAMI()
                    .getCurrentTotal() - DataHolder.getInstance().getAMI().getMargin() - DataHolder.getInstance().getAMI().getPosProfit();
            currentTotalText.setText(Util.formatNumber(currentTotal));
            curTotalText.setText(Util.formatNumber(DataHolder.getInstance().getAMI()
                    .getCurrentTotal()));
        }
    }


    public void refreshContent() {
        if (tabHost.getCurrentTab() == 0) {
            updateList3();
        } else if (tabHost.getCurrentTab() == 1) {
            updateList1();
        } else if (tabHost.getCurrentTab() == 2) {
            updateList2();
        } else {
            updateAccountInfo();
        }
    }

    public void tickUpdate() {
        if (tabHost.getCurrentTab() == 0) {
            updateList3();
        } else if (tabHost.getCurrentTab() == 3) {
            updateAccountInfo();
        }
//        updateStatsView();
    }

    public void requestUpdate() {

        if (tabHost.getCurrentTab() == 1) {
            updateList1();
        } else if (tabHost.getCurrentTab() == 2) {
            updateList2();
        }
//        updateStatsView();
    }


//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // TODO Add your menu entries here
//        MenuItem item = menu.findItem(R.id.action_details);
//        item.setVisible(false);
//        mOptionsMenu = menu;
//        super.onCreateOptionsMenu(menu, inflater);
//    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Implement logic here when a tab is selected
     */
    public void onTabChanged(@NotNull String tabName) {
        if (tabName.equals(LIST2_TAB_TAG)) {
            // do something
        } else if (tabName.equals(LIST1_TAB_TAG)) {
            // do something
        }
    }

    public class MyAdapter extends SampleTableAdapter {

        private final int height;
        private final boolean isFinished;
        private ArrayList<TXRecord> inputList;
        private String[] headers;
        int[] widthArray;

        public MyAdapter(@NotNull Context context, String[] headers, ArrayList<TXRecord> inputList,
                         boolean isFinished, int[] widthArray) {
            super(context);

            this.inputList = inputList;
            Resources resources = context.getResources();

            this.headers = headers;
            this.isFinished = isFinished;
            height = resources.getDimensionPixelSize(R.dimen.table_height);
            this.widthArray = widthArray;

        }

        @Override
        public int getRowCount() {
            return inputList.size();
        }

        @Override
        public int getColumnCount() {
            return headers.length - 1;
        }

        @Override
        public int getWidth(int column) {
            final float scale = getContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (widthArray[column + 1] * scale + 0.5f);
            return pixels;
        }

        @Override
        public int getHeight(int row) {
            return height;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public String getCellString(int row, int column) {
            if (row < 0) {
                return headers[column + 1];
            }
            return inputList.get(row).data[column + 1];
        }

        @Override
        public int getLayoutResource(int row, int column) {
            final int layoutResource;
            switch (getItemViewType(row, column)) {
                case 0:
                    layoutResource = R.layout.item_table1_header;
                    break;
                case 1:
                    layoutResource = R.layout.item_table1;
                    break;
                default:
                    throw new RuntimeException("wtf?");
            }
            return layoutResource;
        }

        @Override
        public int getItemViewType(int row, int column) {
            if (row < 0) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Nullable
        @Override
        public View getView(final int row, int column, @Nullable View converView,
                            ViewGroup parent) {
            if (converView == null) {
                converView = inflater.inflate(getLayoutResource(row, column),
                        parent, false);
            }
            setText(converView, getCellString(row, column));
            setTextColor(converView, Color.BLACK);
            if (!this.isFinished)
                converView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (row == -1)
                            return;
                        Util.sendCancelRequest(row, (Activity) CSApplication.getContext());
                    }
                });
            return converView;
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.button21:
                tabHost.setCurrentTabByTag(LIST4_TAB_TAG);
                refreshContent();
                return;
            case R.id.button22:
                tabHost.setCurrentTabByTag(LIST3_TAB_TAG);
                refreshContent();
                return;
            case R.id.button33:
                tabHost.setCurrentTabByTag(LIST1_TAB_TAG);
                refreshContent();
                return;
            case R.id.button44:
                tabHost.setCurrentTabByTag(LIST2_TAB_TAG);
                refreshContent();
                return;
        }
    }

    public void checkTab(@NotNull String tag) {

        if (tag.equals(LIST4_TAB_TAG)){
            listView1.setVisibility(View.GONE);
            listView2.setVisibility(View.GONE);
            listView3.setVisibility(View.GONE);
        }
        else if (tag.equals(LIST3_TAB_TAG)) {
            listView1.setVisibility(View.GONE);
            listView2.setVisibility(View.GONE);
            infoView.setVisibility(View.GONE);
        }
        else if (tag.equals(LIST1_TAB_TAG)) {
            listView2.setVisibility(View.GONE);
            listView3.setVisibility(View.GONE);
            infoView.setVisibility(View.GONE);
        } else {
            listView1.setVisibility(View.GONE);
            listView3.setVisibility(View.GONE);
            infoView.setVisibility(View.GONE);
        }
        tabHost.setCurrentTabByTag(tag);

    }

    public class DetailAdapter extends BaseAdapter {

        Activity sActivity;
        private FlatButton closeoutButton;
        private ListView parentView;

        public DetailAdapter(final Activity mActivity, ListView listView) {
            this.sActivity = mActivity;
            this.parentView = listView;
        }

        @Override
        public int getCount() {
            /*
             * Length of our listView
			 */
            return DataHolder.getInstance().getPosList().size();
        }

        @Override
        public Object getItem(int position) {

			/*
			 * Current Item
			 */
            return position;
        }

        @Override
        public long getItemId(int position) {
			/*
			 * Current Item's ID
			 */
            return position;
        }

        @SuppressLint("NewApi")
        @SuppressWarnings("deprecation")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View mView = convertView;
            if (mView == null) {
				/*
				 * LayoutInflater
				 */
                final LayoutInflater sInflater = (LayoutInflater) sActivity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				/*
				 * Inflate Custom List View
				 */
                mView = sInflater.inflate(R.layout.stock_detail_item, null,
                        false);

            }

            final TextView sTV1 = (TextView) mView.findViewById(R.id.textView1);
            final TextView floatingText = (TextView) mView
                    .findViewById(R.id.float_value);
            final Button volume = (Button) mView.findViewById(R.id.volume);

            final PosData posData = DataHolder.getInstance().getPosList()
                    .get(position);

            String convertedName = Util.convertStockCode(posData
                    .getInstrumentID());
            sTV1.setText(convertedName);

            // set floating
            if (posData.isLongOrShort()) {
                double longFloating = DataHolder.getInstance().getFloating(
                        posData.getInstrumentID(), true);
                floatingText.setText(Util.formatNumber(longFloating));
            } else {
                double shortFloating = DataHolder.getInstance().getFloating(
                        posData.getInstrumentID(), false);
                floatingText.setText(Util.formatNumber(shortFloating));

                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    volume.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.rect_normal_green));
                } else {
                    volume.setBackground(getResources().getDrawable(
                            R.drawable.rect_normal_green));
                }

            }

            volume.setText(String.valueOf(posData.getVolume()));

            closeoutButton = (FlatButton) mView
                    .findViewById(R.id.close_out_button);
//            if (closeoutButton.getListener() == null) {
            closeoutButton.setOnTouchListener(new View.OnTouchListener() {
                //
                @Override
                public boolean onTouch(View v, @NotNull MotionEvent motion) {

                    if (motion.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        int direction;
                        if (posData.isLongOrShort()) {
                            direction = 2;
                        } else {
                            direction = 4;
                        }
                        Util.sendRequest(posData.getInstrumentID(),
                                direction, 0, 0, 0, 0, (Activity) CSApplication.getContext());
                    }

                    return true;
                }

            });

            return mView;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tabbed_activity_ations, menu);
//        statsItem = menu.getItem(0);
        return super.onCreateOptionsMenu(menu);

    }

//    public void updateStatsView() {
//        if (statsTextTotal != null) {
//            double available = DataHolder.getInstance().getAMI().getCurrentTotal() - DataHolder.getInstance().getAMI().getMargin();
//            statsTextTotal.setText("可用资金: "
//                    + Util.formatNumber(available));
//        }
//        if (statsTextProfitLoss != null){
//            double profitLoss = DataHolder.getInstance().getAMI().getClosePosProfit()
//                    - DataHolder.getInstance().getAMI().getCommission() + DataHolder.getInstance().getTotalFloating();
//            statsTextProfitLoss.setText("总盈亏: " + Util.formatNumber(profitLoss));
//        }
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 2){
            navigateToLogin();
        }
    }

    public void navigateToLogin() {
        setResult(2);
        finish();
        System.out.println("purged to main");
    }
}