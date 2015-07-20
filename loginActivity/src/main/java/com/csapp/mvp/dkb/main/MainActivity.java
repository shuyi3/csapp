package com.csapp.mvp.dkb.main;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.csapp.mvp.dkb.Login.CSApplication;
import com.csapp.mvp.dkb.Login.LoginActivity;
import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.buy.TransactionFragment;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.StockData;
import com.csapp.mvp.dkb.data.Util;
import com.csapp.mvp.dkb.details.TabbedListActivity;
import com.sfit.ctp.thostmduserapi.CThostFtdcDepthMarketDataField;
import com.sfit.ctp.thostmduserapi.CThostFtdcMdApi;
import com.sfit.ctp.thostmduserapi.CThostFtdcMdSpi;
import com.sfit.ctp.thostmduserapi.CThostFtdcReqUserLoginField;
import com.sfit.ctp.thostmduserapi.CThostFtdcRspInfoField;
import com.sfit.ctp.thostmduserapi.CThostFtdcRspUserLoginField;
import com.sfit.ctp.thostmduserapi.CThostFtdcSpecificInstrumentField;
import com.sfit.ctp.thostmduserapi.CThostFtdcUserLogoutField;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Stack;

import info.hoang8f.android.segmented.SegmentedGroup;

public class MainActivity extends FragmentActivity implements MainView,
        RadioGroup.OnCheckedChangeListener {

    private static final int MSG_FRONT_SUCCESS = 0;// 连接前置成功
    private static final int MSG_FRONT_DISCONNECT = 1;// 连接前置断开

    private static final int MSG_LOGIN_SUCCESS = 2;// 登入成功
    private static final int MSG_LOGIN_FAILURE = 3;// 登入失败

    private static final int MSG_DEPTHDATA_RETURNED = 4;

    private static final int MSG_ERROR = 14;

    private ArrayList<String> frontIPArray;

    private static final String BrokerId = "1007";
    private static final String UserId = "8000_admin";
    private boolean doubleBackToExitPressedOnce = false;
    private String[] ppInstrumentId;

    @Nullable
    CThostFtdcMdApi api = null;
    @Nullable
    TestMdspi mSpi = null;
    // private ListView listView;
    private FigureFragment f1, f2;
    private StockListFragment stockFragment;
    private ViewPager pager;
    @Nullable
    private ActionBar actionBar;
    // private String[] stockArray = { InstrumentId };

    private int currentInstrument;
//    public MenuItem statsItem;
    private SegmentedGroup segmentedControl;
    private MenuItem backItem;

    public TextView statsTextTotal;
    public TextView statsTextProfitLoss;

    CandleStickWebViewFragment webViewFragment;



    // public String[] getStockArray() {
    // return stockArray;
    // }

    @Override
    public Resources getResources() {
        //force split menu
        return new ResourceFix(super.getResources());
    }

    private class ResourceFix extends Resources {
        private int targetId = 0;

        ResourceFix(Resources resources) {
            super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
            targetId = Resources.getSystem().getIdentifier("split_action_bar_is_narrow", "bool", "android");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean getBoolean(int id) throws Resources.NotFoundException {
            return targetId == id || super.getBoolean(id);
        }
    }


    @NotNull
    private Stack<String> FragmentTagStack = new Stack<String>();

    public void addFragmentToStack(String currentFragment) {
        FragmentTagStack.add(currentFragment);
    }

    public boolean popFragmentTagStack() {
        if (FragmentTagStack.size() == 1) return false;
        FragmentTagStack.pop();
        return true;
    }

    public String getCurrentFragment() {
        return FragmentTagStack.get(FragmentTagStack.size() - 1);
    }

    public int getCurrentInstrument() {
        return currentInstrument;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CSApplication.setContext(this);

        frontIPArray = DataHolder.getInstance().getFrontIPArray();

        currentInstrument = 0;

        ppInstrumentId = DataHolder.getInstance().getInsNameList().toArray(new String[DataHolder.getInstance().getInsNameList().size()]);

        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();

        stockFragment = new StockListFragment(this);
        fragmentTransaction.add(R.id.frag_holder, stockFragment, Util.TAG_LIST_FRAGMENT);
        fragmentTransaction.commit();
        addFragmentToStack(Util.TAG_LIST_FRAGMENT);

        actionBar = getActionBar();

        if (actionBar != null) {
//            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//            actionBar.setTitle(R.string.title_activity_main);

            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);

            ViewGroup v = (ViewGroup) LayoutInflater.from(this)
                    .inflate(R.layout.actionbar_top, null);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM); //show it
            actionBar.setCustomView(v,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER));

            statsTextTotal = (TextView) findViewById(R.id.account_total);
            statsTextProfitLoss = (TextView) findViewById(R.id.account_profitloss);

            ImageView refresh = (ImageView) findViewById(R.id.refresh_image);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Util.JSONTask().execute("");
                }
            });

//            actionBar.setDisplayShowCustomEnabled(true);

        }
        updateStatsView();

        f1 = FigureFragment.newInstance(ppInstrumentId[currentInstrument],
                FigureFragment.GraphType.MINUTE_GRAPH, this);
//        f2 = FigureFragment.newInstance(ppInstrumentId[currentInstrument],
//                GraphType.TICK_GRAPH, this);
        webViewFragment = CandleStickWebViewFragment.initWithInsInfo(ppInstrumentId[currentInstrument]);

        pager = (ViewPager) findViewById(R.id.graphpager);
        pager.setOffscreenPageLimit(1);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    //主动
                    case 0:
                        if (segmentedControl.getCheckedRadioButtonId() != R.id.minute_button)
                            pageSelectLogic(position);
                        segmentedControl.check(R.id.minute_button);
                        break;
                    case 1:
                        if (segmentedControl.getCheckedRadioButtonId() != R.id.tick_graph_button)
                            pageSelectLogic(position);
                        segmentedControl.check(R.id.tick_graph_button);
                        break;
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

        PageAdapter a = new PageAdapter(getFragmentManager());
        pager.setAdapter(a);

        segmentedControl = (SegmentedGroup) findViewById(R.id.graph_segmented);
        segmentedControl.setOnCheckedChangeListener(this);
        segmentedControl.check(R.id.minute_button);

        apiThread mAPI = new apiThread();
        mAPI.start();

    }

    protected void pageSelectLogic(int position) {
        switch (position) {
            //主动
            case 0:
                webViewFragment.loadWithInsInfo(ppInstrumentId[currentInstrument]);
                break;
            case 1:
                if (!f1.getInstrumentID().equals(ppInstrumentId[currentInstrument])) {
                    f1.refreshData(ppInstrumentId[currentInstrument], false);
                }
                break;
//                if (!f2.getInstrumentID().equals(ppInstrumentId[currentInstrument])) {
//                    f2.refreshData(ppInstrumentId[currentInstrument], false);
//                }

        }
    }

    public void updateStatsView() {
        if (statsTextTotal != null) {
            double available = DataHolder.getInstance().getAMI().getCurrentTotal() - DataHolder.getInstance().getAMI().getMargin();
            statsTextTotal.setText("可取资金: "
                    + Util.formatNumber(available));
        }
        if (statsTextProfitLoss != null){
            double profitLoss = DataHolder.getInstance().getAMI().getClosePosProfit()
                    - DataHolder.getInstance().getAMI().getCommission() + DataHolder.getInstance().getTotalFloating();
            statsTextProfitLoss.setText("总盈亏: " + Util.formatNumber(profitLoss));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CSApplication.setContext(this);
        showProgress();
        setItems();
        hideProgress();
    }

    @Override
    public boolean onCreateOptionsMenu( @NotNull Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.main_activity_actions, menu);

        backItem = menu.findItem(R.id.action_back);
        backItem.setVisible(false);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected( @NotNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_details) {

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Intent intent=new Intent(this, TabbedListActivity.class);
            startActivityForResult(intent, 1);
        }
        if (item.getItemId() == R.id.menu_settings) {
            // Start setting activity
            showAbout();
        }
        if (item.getItemId() == R.id.change_account) {

            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("提醒");
            String message = "确定要切换账户吗";

            b.setMessage(message);
            b.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick( @NotNull DialogInterface dialog, int which) {
                    dialog.dismiss();
                    navigateToLogin();
                }

            });
            b.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick( @NotNull DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });
            b.show();

        }
        if (item.getItemId() == R.id.action_back){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void navigateToLogin(){
        Intent intent = new Intent(CSApplication.getContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showProgress() {
        stockFragment.getProgressBar().setVisibility(View.VISIBLE);
        stockFragment.getStockListView().setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideProgress() {
        stockFragment.getProgressBar().setVisibility(View.INVISIBLE);
        stockFragment.getStockListView().setVisibility(View.VISIBLE);
    }

    public void setItems() {
        // final CustomAdapter adapter = new CustomAdapter(this);
        // listView.setAdapter(adapter);
        final StockListFragment.CustomAdapter mAdapter = (StockListFragment.CustomAdapter) stockFragment.getStockListView().getAdapter();
        stockFragment.getStockListView().setOnItemClickListener(
                new OnItemClickListener() {

                    @Override
                    public void onItemClick( @NotNull AdapterView parent,  @NotNull View view,
                                            int position, long id) {

                        chooseFigure(position);
                        mAdapter.notifyDataSetChanged();

                    }
                });
    }

    public void chooseFigure(int position) {
        if (position != currentInstrument) {
            currentInstrument = position;
            refreshGraph();
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private class PageAdapter extends FragmentPagerAdapter {

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Nullable
        @Override
        public Fragment getItem(int pos) {
            Fragment f = null;

            switch (pos) {
                case 0:
                    f = webViewFragment;
                    break;
                case 1:
                    f = f1;
                    break;
            }

            return f;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

    @Override
    public void onBackPressed() {

        getFragmentManager().popBackStack();

        boolean isPopDone = popFragmentTagStack();
        if (getCurrentFragment().equals(Util.TAG_LIST_FRAGMENT)) {
            if (doubleBackToExitPressedOnce && !isPopDone) {
                super.onBackPressed();
                return;
            }

            if (!isPopDone) {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }else {
//                actionBar.setDisplayHomeAsUpEnabled(false);
//                actionBar.setTitle(R.string.title_activity_main);
                backItem.setVisible(false);
                refreshContent();
            }
        } else {
            actionBar.setTitle(Util.convertStockCode(ppInstrumentId[currentInstrument]));
            refreshContent();
        }
        invalidateOptionsMenu();

    }

    class apiThread extends Thread {
        public void run() {
            String apppath = getApplicationContext().getFilesDir()
                    .getAbsolutePath();
            apppath += "/";
            try {
                api = CThostFtdcMdApi.CreateFtdcMdApi(apppath);
            } catch (Exception e) {
                Log.e("createError", "exception");
                e.printStackTrace();
            }

            mSpi = new TestMdspi();

            mSpi.initspi(api);

            api.RegisterSpi(mSpi);
            for (String frontIP : frontIPArray) {
                System.out.println("register: " + frontIP);
                api.RegisterFront("tcp://" + frontIP);
            }

//            String simulationFrontIP =  "tcp://27.115.78.150:26213";

//            api.RegisterFront(simulationFrontIP);

            Log.i("tryInit", "beforeInit");

//			api.SubscribeMarketData(ppInstrumentId, 2);
            api.Init();
            api.Join();
        }
    }

    class SpiHandler extends Handler {
        public SpiHandler( @NotNull Looper looper) {
            super(looper);
        }

        public void handleMessage( @NotNull Message msg) {
            switch (msg.what) {
                case MSG_FRONT_SUCCESS:
                    // outputText.append("前置已经连接，发送登录\n");

                    Log.i("tryLogin", "beforeLogin");

                    CThostFtdcReqUserLoginField loginfld = new CThostFtdcReqUserLoginField();

                    loginfld.setBrokerID(BrokerId);
                    loginfld.setUserID(UserId);
                    loginfld.setPassword("12345");
                    loginfld.setUserProductInfo("android");

                    api.ReqUserLogin(loginfld, 0);
                    break;

                case MSG_FRONT_DISCONNECT:
                    // outputText.append("前置断开\n");
                    // Connect.setEnabled(true);

                    break;

                case MSG_LOGIN_SUCCESS:

                    api.SubscribeMarketData(ppInstrumentId, ppInstrumentId.length);

                    break;

                case MSG_DEPTHDATA_RETURNED:

                    //Data changed
                    //Release
                    if (DataHolder.getInstance().getAMI() == null || CSApplication.getContext() instanceof LoginActivity) break;

                    CThostFtdcDepthMarketDataField dataObj = (CThostFtdcDepthMarketDataField) msg.obj;

                    String key = dataObj.getInstrumentID();

                    if (!isKeyValid(key)){
                        System.out.println("invalid key: " + key);
                        break;
                    }

                    String tradingDay = dataObj.getTradingDay();
                    final double askPrice = dataObj.getAskPrice1();
                    final double bidPrice = dataObj.getBidPrice1();
                    final double lastPrice = dataObj.getLastPrice();
                    final double preSettlementPrice = dataObj.getPreSettlementPrice();

                    String tradingTime = dataObj.getTradingDay() + " " + dataObj.getUpdateTime();

                    StockData mData = new StockData(tradingTime, askPrice, bidPrice, lastPrice, preSettlementPrice);

//                    boolean isTickChanged = DataHolder.getInstance().addTickData(key, mData);
                    boolean isMinChanged = DataHolder.getInstance().addMinData(key, mData);

                    DataHolder.getInstance().getInsInfoMap().get(key).setLastData(mData);

                    Util.tickUpdate(isMinChanged);

                case MSG_ERROR:
                    // outputText.append(msg.toString()+"\n");
                    break;

            }
        }
    }

    public void tickUpdate(boolean isMinChanged){
        updateGraph(isMinChanged);
        updateStatsView();
        if (getCurrentFragment().equals(Util.TAG_TX_FRAGMENT)){
            TransactionFragment txFragment = (TransactionFragment) getFragmentManager().findFragmentByTag(Util.TAG_TX_FRAGMENT);
            if (txFragment != null)
                txFragment.tickUpdate();
        }else{
            if (stockFragment != null)
                stockFragment.tickUpdate();
        }
    }

    public void requestUpdate(){
        updateStatsView();
        if (getCurrentFragment().equals(Util.TAG_TX_FRAGMENT)){
            TransactionFragment txFragment = (TransactionFragment) getFragmentManager().findFragmentByTag(Util.TAG_TX_FRAGMENT);
            if (txFragment != null)
                txFragment.requestUpdate();
        }
    }

    public void refreshContent(){
        refreshGraph();
        updateStatsView();
        if (getCurrentFragment().equals(Util.TAG_TX_FRAGMENT)){
            TransactionFragment txFragment = (TransactionFragment) getFragmentManager().findFragmentByTag(Util.TAG_TX_FRAGMENT);
            if (txFragment != null)
                txFragment.refreshContent();
        }else{
            if (stockFragment != null)
                stockFragment.refreshContent();
        }
    }

    public void refreshGraph(){
        if (pager.getCurrentItem() == 0) {
            webViewFragment.loadWithInsInfo(ppInstrumentId[currentInstrument]);
        } else if (pager.getCurrentItem() == 1) {
//            f2.refreshData(ppInstrumentId[currentInstrument], false);
            f1.refreshData(ppInstrumentId[currentInstrument], false);

        }
    }

    public void updateGraph(final boolean isMinChanged){

//        System.out.println("int update graph func");

        if (CSApplication.getContext() instanceof MainActivity) {

//            System.out.println("update graph");

            if (pager.getCurrentItem() == 1)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        f1.refreshData(ppInstrumentId[currentInstrument],
                                isMinChanged);
                    }
                });
            else{
//                Log.d("webview", "webview tick updated");
                webViewFragment.loadLastTick();
            }
        }
    }

    class TestMdspi extends CThostFtdcMdSpi {
        @Nullable
        Looper looper = null;
        @Nullable
        CThostFtdcMdApi theapi = null;
        @Nullable
        SpiHandler myhandler = null;

        public void initspi(CThostFtdcMdApi api) {
            theapi = api;
            // 主线程的Looper对象
//            Looper.prepare();
            looper = Looper.getMainLooper();
//            Looper.loop();
            // 这里以主线程的Looper对象创建了handler，
            // 所以，这个handler发送的Message会被传递给主线程的MessageQueue。
            myhandler = new SpiHandler(looper);
        }

        @Override
        public void OnFrontConnected() {
            System.out.println("front connected");
            // 构建Message对象
            // 第一个参数：是自己指定的message代号，方便在handler选择性地接收
            // 第二三个参数没有什么意义
            // 第四个参数需要封装的对象
            Message msg = myhandler.obtainMessage(MSG_FRONT_SUCCESS, 1, 1,
                    "前置已经连接\n");
            myhandler.sendMessage(msg); // 发送消息
        }

        @Override
        public void OnFrontDisconnected(int nReason) {
            System.out.println("NReason:" + nReason);
            Message msg = myhandler.obtainMessage(MSG_FRONT_DISCONNECT, 1, 1,
                    nReason);
            myhandler.sendMessage(msg); // 发送消息
        }

        @Override
        public void OnHeartBeatWarning(int nTimeLapse) {
            Log.i("HeartBeat", String.valueOf(nTimeLapse));
        }

        @Override
        public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin,
                                    @NotNull CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
            if (pRspInfo.getErrorID() == 0) {
                Message msg = myhandler.obtainMessage(MSG_LOGIN_SUCCESS, 1, 1,
                        "登入成功");
                myhandler.sendMessage(msg); // 发送消息
            } else {
                Message msg = myhandler.obtainMessage(MSG_LOGIN_FAILURE, 1, 1,
                        "登入失败\n");
                myhandler.sendMessage(msg); // 发送消息
            }
        }

        @Override
        public void OnRspError(CThostFtdcRspInfoField pRspInfo, int nRequestID,
                               boolean bIsLast) {

        }

        @Override
        public void OnRspSubMarketData(
                CThostFtdcSpecificInstrumentField pSpecificInstrument,
                CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {

        }

        @Override
        public void OnRspUnSubMarketData(
                CThostFtdcSpecificInstrumentField pSpecificInstrument,
                CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {

        }

        @Override
        public void OnRtnDepthMarketData(
                CThostFtdcDepthMarketDataField pDepthMarketData) {
            Message msg = myhandler.obtainMessage(MSG_DEPTHDATA_RETURNED, 1, 1,
                    pDepthMarketData);
            myhandler.sendMessage(msg); // 发送消息
        }

    }

    static {
        try {
            System.loadLibrary("thostmduserapi");
            System.loadLibrary("thostmduserapi_wrap");

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.minute_button:
                if (pager.getCurrentItem() != 0) {
                    pageSelectLogic(0);
                    pager.setCurrentItem(0, true);
                }
                return;
            case R.id.tick_graph_button:
                if (pager.getCurrentItem() != 1) {
                    pageSelectLogic(1);
                    pager.setCurrentItem(1, true);
                }
                return;
        }
    }

    protected void showAbout() {
        // Inflate the about message contents
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

        // When linking text, force to always use default color. This works
        // around a pressed color state bug.
        TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);

        TextView versionText = (TextView) messageView.findViewById(R.id.about_version);
        versionText.setText("当前软件版本号: " + getResources().getString(R.string.server_version));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    public void updateStockListView() {
        runOnUiThread(new Runnable() {
                          public void run() {
                              stockFragment.adapter.updateRowStats();
                          }
                      }

        );
    }

    protected void exitCTP(){
        CThostFtdcUserLogoutField logoutField = new CThostFtdcUserLogoutField();
        logoutField.setBrokerID(BrokerId);
        logoutField.setUserID(UserId);

        if (api != null) {
            api.ReqUserLogout(logoutField, 0);
            api.UnSubscribeMarketData(ppInstrumentId, ppInstrumentId.length);
            api.Release();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitCTP();
        Util.unbindDrawables(findViewById(R.id.main_holder));
        DataHolder.getInstance().purge();
        System.out.println("purged");
        System.gc();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 2){
            navigateToLogin();
        }
    }

    public boolean isKeyValid(String key){
        for (String instrumentId : ppInstrumentId){
            if (key.equals(instrumentId)) return true;
        }
        return false;
    }
}
