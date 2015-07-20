package com.csapp.mvp.dkb.data;

import android.util.Log;

import com.csapp.mvp.dkb.network.LoginNetworkInteraction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class DataHolder {

    //可能有的bug: 添加tick data没有lock 如果stocklist在更新以后有所变动 可能会有潜在bug

//    @Nullable
//    private HashMap<String, ArrayList<StockData>> instantStock;
    @Nullable
    private HashMap<String, ArrayList<StockData>> longtermStock;
    private AccountInfo AMI;
    private HashMap<String, InsInfo> insInfoMap;
    private ArrayList<OrderData> finishedOrderList, unfinishedOrderList;
    private ArrayList<PosData> posList;
    private ArrayList<String> insNameList;
    private ArrayList<String> frontIPArray;
    private HashMap<String, Integer> sGUIDMap = new HashMap<String, Integer>();
    private boolean isRealStock = true;

    private CompanyInfo mCompanyInfo;

    private String account;
    private String password;
    private String encodedUsername;
    private String encodedPassword;

    private Object AMILock = new Object();
    private Object InsInfoLock = new Object();
    private Object OrderListLock = new Object();
    private Object PosListLock = new Object();

    private static int instantLimit = 60, longtermLimit = 500;

    private static final DataHolder holder = new DataHolder();

    public String getEncodedUsername() {
        return encodedUsername;
    }

    public void setEncodedUsername(String encodedUsername) {
        this.encodedUsername = encodedUsername;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() { return password; }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void purge() {
//        instantStock = null;
        longtermStock = null;
        setAMI(null);
        setInsInfoMap(null);
        setFinishedOrderList(null);
        setUnfinishedOrderList(null);
        setPosList(null);
        setInsNameList(null);
        setFrontIPArray(null);
        setRealStock(true);
    }

    @NotNull
    public static DataHolder getInstance() {
        return holder;
    }

    public void initAMI(JSONObject AMIObject) {
        synchronized (AMILock) {
            AMI = new AccountInfo(AMIObject);
        }
    }

    public void initCompanyInfo(JSONObject companyObject){
        mCompanyInfo = new CompanyInfo(companyObject);
    }

    public void initInsHistoryData(String instrumentId, LoginNetworkInteraction.TaskType taskType,
                                   @NotNull JSONArray historyArray) {

//        if (instantStock == null) {
//            instantStock = new HashMap<String, ArrayList<StockData>>();
//        }
        if (longtermStock == null) {
            longtermStock = new HashMap<String, ArrayList<StockData>>();
        }

        ArrayList<StockData> tickList = new ArrayList<StockData>();
        for (int i = 0; i < historyArray.length(); i++) {
            JSONObject tickJSON;
            StockData tickData = null;
            try {
                tickJSON = historyArray.getJSONObject(i);
                double askPrice = tickJSON.getDouble("AskPrice1");
                double bidPrice = tickJSON.getDouble("BidPrice1");
                double lastPirce = tickJSON.getDouble("LastPrice");
                String time = tickJSON.getString("Time");
                if (time != null && !time.equals("null")) {
                    tickData = new StockData(time, askPrice, bidPrice, lastPirce, -1);
                } else {
                    continue;
//                    tickData = new StockData(null, askPrice, bidPrice, lastPirce, -1);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            tickList.add(tickData);
        }

//        if (taskType == TaskType.TICK) {
//            System.out.println(instrumentId + " inited "
//                    + historyArray.length() + " tick data");
//
//            instantStock.put(instrumentId, tickList);
//
//        } else {

            System.out.println(instrumentId + " inited "
                    + historyArray.length() + " min data");

            longtermStock.put(instrumentId, tickList);

//        }
        if (tickList.size() != 0) {
            StockData lastData = tickList.get(tickList.size() - 1);
            getInsInfoMap().get(instrumentId).setLastData(lastData);
        }

    }

    public void initInsInfo(@NotNull JSONArray insInfoArray) {

        synchronized (InsInfoLock) {

            insInfoMap = new HashMap<String, InsInfo>();
            insNameList = new ArrayList<String>();

            for (int i = 0; i < insInfoArray.length(); i++) {
                InsInfo mInsInfo = null;
                try {
                    mInsInfo = new InsInfo(insInfoArray.getJSONObject(i));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

			/* bug handling */
                if (mInsInfo != null) {
                    if (insInfoMap.get(mInsInfo.getInstrumentID()) != null)
                        continue;
                    insInfoMap.put(mInsInfo.getInstrumentID(), mInsInfo);
                    insNameList.add(mInsInfo.getInstrumentID());
                }
            }
        }
    }

    public synchronized void initOrderList(@NotNull JSONArray orderArray) {

        synchronized (OrderListLock) {

            finishedOrderList = new ArrayList<OrderData>();
            unfinishedOrderList = new ArrayList<OrderData>();

            for (int i = 0; i < orderArray.length(); i++) {
                OrderData orderData = null;
                try {
                    orderData = new OrderData(orderArray.getJSONObject(i));

                    if (orderData.getStatus() == 0 || orderData.getStatus() == 1 || orderData.getStatus() == 2 || orderData.getStatus() == 5)
                        continue;

                    if (orderData.getTradedVolume() > 0) {
                        finishedOrderList.add(orderData);
                    }
                    if (orderData.getTradedVolume() < orderData.getOriginVolume()) {
                        unfinishedOrderList.add(orderData);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }

            Collections.reverse(finishedOrderList);
            Collections.reverse(unfinishedOrderList);
        }
    }

    public void insertPosData(@NotNull PosData mPosData) {

        synchronized (PosListLock) {

            if (posList == null) return;

            boolean isFound = false;
            for (PosData posData : posList) {
                if (posData.getInstrumentID().equals(
                        mPosData.getInstrumentID())) {
                    if (posData.isLongOrShort() == mPosData.isLongOrShort()) {
                        posData.setOpenPrice((posData.getOpenPrice()
                                * posData.getVolume() + mPosData
                                .getOpenPrice() * mPosData.getVolume())
                                / (posData.getVolume() + mPosData
                                .getVolume()));
                        posData.setVolume(posData.getVolume()
                                + mPosData.getVolume());
                        isFound = true;
                        Log.d("push", "found " + mPosData.getInstrumentID());
                        break;
                    }
                }
            }
            if (!isFound) {
                Log.d("push", "added " + mPosData.getInstrumentID());
                posList.add(mPosData);
            }
        }
    }

    public synchronized void initPostList(@NotNull JSONArray posDataArray) {

        synchronized (PosListLock) {

            posList = new ArrayList<PosData>();

            for (int i = 0; i < posDataArray.length(); i++) {
                PosData mPosData = null;
                try {
                    mPosData = new PosData(posDataArray.getJSONObject(i));
                    insertPosData(mPosData);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<String> getInsNameList() {

        return insNameList;
    }

//    public boolean addTickData(String key, @NotNull StockData data) {
//
//        ArrayList<StockData> instantEntry = instantStock.get(key);
//
//        if (instantEntry == null) return false;
//
//        boolean isDataChanged = false;
//
//        if (instantEntry.size() == 0) {
//            instantStock.get(key).add(data);
//            isDataChanged = true;
//        } else {
//
//            if (data.getSeconds()
//                    - instantEntry.get(instantEntry.size() - 1).getSeconds() >= 1) {
//                if (instantEntry.size() == instantLimit) {
//                    instantStock.get(key).remove(0);
//                }
//                instantStock.get(key).add(data);
//                isDataChanged = true;
////                Log.d("MPChart", "second: " + data.getSeconds());
//            }
//        }
//
//        return isDataChanged;
//
//    }

    public boolean addMinData(String key, @NotNull StockData data) {

        boolean isDataChanged = false;
        ArrayList<StockData> longtermEntry = longtermStock.get(key);

        if (longtermEntry.size() == 0) {
            longtermStock.get(key).add(data);
            isDataChanged = true;
        } else {
            if (data.getMinutes()
                    - longtermEntry.get(longtermEntry.size() - 1).getMinutes() >= 1) {
                if (longtermEntry.size() == longtermLimit) {
                    longtermStock.get(key).remove(0);
                }
                longtermStock.get(key).add(data);
                isDataChanged = true;
                Log.d("MPChart", "minute added");
            }
        }

        return isDataChanged;
    }

    public AccountInfo getAMI() {
        synchronized (AMILock) {
            return AMI;
        }
    }

    public void setAMI(AccountInfo aMI) {
        synchronized (AMILock) {
            AMI = aMI;
        }
    }

    public HashMap<String, InsInfo> getInsInfoMap() {
        synchronized (InsInfoLock) {
            return insInfoMap;
        }
    }

    public void setInsInfoMap(HashMap<String, InsInfo> insInfoMap) {

        synchronized (InsInfoLock) {
            this.insInfoMap = insInfoMap;
        }
    }

    public ArrayList<OrderData> getFinishedOrderList() {
        synchronized (OrderListLock) {
            return finishedOrderList;
        }
    }

    public void setFinishedOrderList(ArrayList<OrderData> finishedOrderList) {
        synchronized (OrderListLock) {
            this.finishedOrderList = finishedOrderList;
        }
    }

    public ArrayList<OrderData> getUnfinishedOrderList() {
        synchronized (OrderListLock) {
            return unfinishedOrderList;
        }
    }

    public void setUnfinishedOrderList(ArrayList<OrderData> unfinishedOrderList) {
        synchronized (OrderListLock) {
            this.unfinishedOrderList = unfinishedOrderList;
        }
    }

    public ArrayList<PosData> getPosList() {
        synchronized (PosListLock) {
            return posList;
        }
    }

    public void setPosList(ArrayList<PosData> posList) {
        synchronized (PosListLock) {
            this.posList = posList;
        }
    }

//    @Nullable
//    public HashMap<String, ArrayList<StockData>> getInstantStock() {
//        return instantStock;
//    }

    @Nullable
    public HashMap<String, ArrayList<StockData>> getLongtermStock() {
        return longtermStock;
    }

    public void setInsNameList(ArrayList<String> insNameList) {
        synchronized (InsInfoLock) {
            this.insNameList = insNameList;
        }
    }

    @Nullable
    public PosData getPosData(boolean isLong, String instrumentId) {

        synchronized (PosListLock) {

            for (PosData posData : posList) {
                // System.out.println("pos data:" + posData.getInstrumentID() + "=="
                // + instrumentId);
                if (posData.getInstrumentID().equals(instrumentId)) {
                    // System.out.println("pos data: match");
                    if (posData.isLongOrShort() == isLong) {
                        return posData;
                    }

                } else {
//				System.out.println(posData.getInstrumentID() + " not match " + instrumentId);
                }
            }

            return null;
        }
    }

    public double getTotalFloating() {
        synchronized (PosListLock) {
            if (posList == null) return 0;
            double totalFloating = 0.0;
            for (PosData posData : posList) {
                double floating;
                if (posData.isLongOrShort()) {
                    floating = DataHolder.getInstance().getFloating(
                            posData.getInstrumentID(), true);
                } else {
                    floating = DataHolder.getInstance().getFloating(
                            posData.getInstrumentID(), false);
                }
                totalFloating += floating;
            }
            return totalFloating;
        }
    }

    public double getFloating(String instrumentId, boolean isLong) {
        InsInfo mInfo = getInsInfoMap().get(instrumentId);
        if (mInfo == null)
            return 0.0;
        StockData lastData = mInfo.getLastData();
        if (lastData == null)
            return 0.0;
        PosData longPos = getPosData(true, instrumentId);
        PosData shortPos = getPosData(false, instrumentId);
        if (isLong) {
            if (longPos == null)
                return  0.0;
            double longFloating = (lastData.getLastPrice() - longPos
                    .getOpenPrice())
                    * longPos.getVolume()
                    * getInsInfoMap().get(instrumentId).getMultiplier();
            return longFloating;
        } else {
            if (shortPos == null)
                return 0.0;
            double shortFloating = (shortPos.getOpenPrice() - lastData
                    .getLastPrice())
                    * shortPos.getVolume()
                    * getInsInfoMap().get(instrumentId).getMultiplier();
            return shortFloating;
        }
    }

    public void deletePosById(String instrumentId) {

        synchronized (PosListLock) {

            if (posList != null) {

//			for (PosData posData : posList) {
//				if (posData.getInstrumentID().equals(instrumentId)) {
//					posList.remove(posData);
//					Log.d("push", "deleted" + posData.getInstrumentID());
//				}
//			}
                for (Iterator<PosData> iterator = posList.iterator(); iterator.hasNext(); ) {
                    PosData nextPos = iterator.next();
                    if (nextPos.getInstrumentID().equals(instrumentId)) {
                        // Remove the current element from the iterator and the list.
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void updateOrder(@NotNull OrderData orderData) {

        synchronized (OrderListLock) {

            int unfinishedIndex = -1;
            int finishedIndex = -1;

            for (int i = 0; i < finishedOrderList.size(); i++) {
                OrderData finOrder = finishedOrderList.get(i);
                if (finOrder.getOrderSysID().equals(orderData.getOrderSysID())) {
                    finishedIndex = i;
                    break;
                }
            }

            for (int i = 0; i < unfinishedOrderList.size(); i++) {
                OrderData unfOrder = unfinishedOrderList.get(i);
                if (unfOrder.getOrderSysID().equals(orderData.getOrderSysID())) {
                    unfinishedIndex = i;
                    break;
                }
            }

            if (orderData.getStatus() == 0 || orderData.getStatus() == 1 || orderData.getStatus() == 2 || orderData.getStatus() == 5) {
                //删除
                if (unfinishedIndex >= 0) {
                    unfinishedOrderList.remove(unfinishedIndex);
                }

                if (finishedIndex >= 0) {
                    if (orderData.getTradedVolume() != finishedOrderList.get(finishedIndex).getTradedVolume()) {
                        finishedOrderList.remove(finishedIndex);
                        finishedOrderList.set(0, orderData);
                    }
                }

            } else {
                if (orderData.getTradedVolume() > 0) {
                    //完成
                    if (finishedIndex >= 0) {
                        finishedOrderList.set(finishedIndex, orderData);
                    } else {
                        finishedOrderList.add(0, orderData);
                    }
                }

                //未完成
                if (orderData.getTradedVolume() < orderData.getOriginVolume()) {
                    if (unfinishedIndex >= 0) {
                        unfinishedOrderList.set(unfinishedIndex, orderData);
                    } else {
                        unfinishedOrderList.add(0, orderData);
                    }
                }//完成
                else {
                    if (unfinishedIndex >= 0) {
                        unfinishedOrderList.remove(unfinishedIndex);
                    }
                }
            }
        }
    }

    public void updateOrderChange(@NotNull JSONObject changeObject) {
        try {
            initAMI(changeObject.getJSONObject("AMI"));
            OrderData orderData = new OrderData(changeObject.getJSONObject("OI"));
            updateOrder(orderData);
            deletePosById(orderData.getInstrumentID());
            JSONArray posDataArray = changeObject.getJSONArray("ColPos");
            for (int i = 0; i < posDataArray.length(); i++) {
                PosData mPosData = null;
                try {
                    mPosData = new PosData(posDataArray.getJSONObject(i));
                    insertPosData(mPosData);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public synchronized void initFronIPArray(@NotNull JSONArray mArray) {
        frontIPArray = new ArrayList<String>();
        for (int i = 0; i < mArray.length(); i++) {
            try {
                frontIPArray.add(mArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setFrontIPArray(ArrayList<String> frontIPArray) {
        this.frontIPArray = frontIPArray;
    }

    public ArrayList<String> getFrontIPArray() {
        return frontIPArray;
    }

    public HashMap<String, Integer> getsGUIDMap() {
        return sGUIDMap;
    }

    public void setsGUIDMap(HashMap<String, Integer> sGUIDMap) {
        this.sGUIDMap = sGUIDMap;
    }

    public boolean isRealStock() {
        return isRealStock;
    }

    public void setRealStock(boolean isRealStock) {
        this.isRealStock = isRealStock;
    }

    public CompanyInfo getmCompanyInfo() {
        return mCompanyInfo;
    }
}