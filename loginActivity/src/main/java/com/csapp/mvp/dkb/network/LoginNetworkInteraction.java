package com.csapp.mvp.dkb.network;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.csapp.mvp.dkb.Login.CSApplication;
import com.csapp.mvp.dkb.Login.DesUtils;
import com.csapp.mvp.dkb.Login.OnLoginFinishedListener;
import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.Util;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by shuyi on 15/3/8.
 */
public class LoginNetworkInteraction {

    private boolean isDebug = false;
    private String key = "FengyunT";
    public static final String TAG = "Login";
    private int timeout = 100000;

    private OnLoginFinishedListener listener;
    private Activity mActivity = (Activity) CSApplication.getContext();
    private RequestQueue mRequestQueue = CSApplication.getmRequestQueue();

    private String serverAddress = "http://" + CSApplication.getContext().getResources().getString(R.string.server_address);
    private String companyId = CSApplication.getContext().getResources().getString(R.string.companyId);
    private int finishedTask = 0, totalTask = 0;

    private String username, password, encodedUsername, encodedPassword, pushChannelId, pushUserId, geTuiClientId;

    public enum TaskType {
        TICK, MINUTE
    }

    public void register(){

        if (DataHolder.getInstance().getmCompanyInfo() == null) {
            new CompanyInfoTask(listener, 1).execute();
        }
        else {
            listener.onSuccess();
        }

    }

    public void DoLoginTask(String username, String password, String pushChannelId, String pushUserId, String geTuiClientId) {

        boolean error = false;
        if (TextUtils.isEmpty(username)) {
            listener.onUsernameError();
            error = true;
        }
        if (TextUtils.isEmpty(password)) {
            listener.onPasswordError();
            error = true;
        }
        if (!error) {

            try {
                encodedUsername = DesUtils.encode(key, username);
                encodedPassword = DesUtils.encode(key, password);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            this.username = username;
            this.password = password;
            this.pushChannelId = pushChannelId;
            this.pushUserId = pushUserId;
            this.geTuiClientId = geTuiClientId;

            //开始执行网络请求
            executeCompanyReq(true);

        }
    }

    public void executeCompanyReq(boolean isLogin){

        final String companyInfoURL = serverAddress
                + "/spservice/spservice.svc/GetCompanyInfo/"
                + companyId;

        if (isDebug)
            Log.i("Login", "companyUrl: " + companyInfoURL);

        JsonObjectRequest req = new JsonObjectRequest(companyInfoURL, null,
                            new CompanyInfoListener(isLogin), new ErrorListener());

        req.setTag(TAG);

        mRequestQueue.add(req);
    }

    public void executeLoginReq(){

        final String loginURL = Util.serverAddress + "/spservice/spservice.svc/Login/" + Util.companyId + "/" + encodedUsername + "/" + encodedPassword + "/"
                + pushChannelId + "/" + pushUserId + "/3" + "/" + mActivity.getResources().getString(R.string.server_version);

        if (isDebug)
            Log.i("Login", "LoginURL: " + loginURL);

        StringRequest req = new StringRequest(loginURL,
                new LoginListener(), new ErrorListener());

        req.setTag(TAG);

        mRequestQueue.add(req);

        }

    public void executeRefreshReq(){

        final String refreshUrl = Util.serverAddress + "/spservice/spservice.svc/RefreshData/" + Util.companyId + "/" + encodedUsername + "/" + encodedPassword;

        if (isDebug)
            Log.i("Login", "refreshUrl: " + refreshUrl);

        JsonObjectRequest req = new JsonObjectRequest(refreshUrl, null,
                new RefreshListener(), new ErrorListener());

        req.setTag(TAG);

        mRequestQueue.add(req);
    }

    public void executeInsReq(String instrumentId){

        String minURL = serverAddress + "/spservice/spservice.svc/RequstLastMinPriceData/" + companyId + "/" + encodedUsername + "/" + encodedPassword + "/"
                    + instrumentId + "/500";

        if (isDebug){
            Log.i("Login", "insMinUrl: " + minURL);
        }

        JsonArrayRequest minReq = new JsonArrayRequest(minURL,
                new InsListener(instrumentId, LoginNetworkInteraction.TaskType.MINUTE), new ErrorListener());

        minReq.setTag(TAG);

        mRequestQueue.add(minReq);

    }

    public LoginNetworkInteraction(OnLoginFinishedListener listener) {
        this.listener = listener;
    }

    private class InsListener implements Response.Listener<JSONArray> {

        private String instrumentId;
        private TaskType taskType;

        public InsListener(String instrumentId, TaskType taskType){
            this.instrumentId = instrumentId;
            this.taskType = taskType;
        }

        @Override
        public void onResponse(JSONArray response) {

            DataHolder.getInstance().initInsHistoryData(instrumentId,taskType,
                    response);

            synchronized (this) {
                finishedTask++;
            }

            Log.i("Login", "task finished:" + finishedTask);

            if (finishedTask == totalTask){
                listener.onSuccess(username, true);
            }

            //TODO: exception 应该要 throw， 这里要改
        }
    }

    private class LoginListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {

            if (response.equals("\"\"")) {

                if (isDebug)
                    Log.i("Login", "login success");
                // login success
                executeRefreshReq();

            } else {

                listener.onUsernameMismatchError();

                //下载最新版本

                if (response.startsWith("\"") && response.endsWith("\"")) {
                    String message = response.replaceAll("\"", "");
                    if (message.contains("版本")) {
                        listener.updateVersionDialog();
                    } else {
//                        if (provided != null)
//                            provided.cancel();
                        Util.showWarning(message, mActivity, 3000);
                    }
                }
            }
        }
    }



    private class CompanyInfoListener implements Response.Listener<JSONObject> {

        private boolean isLogin;

        CompanyInfoListener(boolean isLogin){
            this.isLogin = isLogin;
        }

        @Override
        public void onResponse(JSONObject response) {
            DataHolder.getInstance().initCompanyInfo(response);
            if (isDebug)
                Log.i("Login", response.toString());
            if (isLogin){
                Log.i("Login", "isLogin");
                executeLoginReq();
            }else{
                //TODO: 注册
            }
            //TODO: exception 应该要 throw， 这里要改
        }
    }

    private class RefreshListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {

            try {

                if (isDebug)
                    Log.i("Login", response.toString());
//                System.out.println("Response:" + response.toString(4));

                JSONObject mObj = null;
                JSONArray mArray = null;

                mArray = response.getJSONArray("ColMarketDataServerAddress");
                if (mArray != null) {
                    DataHolder.getInstance().initFronIPArray(mArray);
                } else {
                    listener.onNetworkError();
                    return;
                }

                mObj = response.getJSONObject("AMI");
                if (mObj != null) {
                    DataHolder.getInstance().initAMI(mObj);
                } else {
                    listener.onNetworkError();
                    return;
                }

                mArray = response.getJSONArray("ColInsInfo");
                if (mArray != null) {
                    DataHolder.getInstance().initInsInfo(mArray);
                } else {
                    listener.onNetworkError();
                    return;
                }

                mArray = response.getJSONArray("ColOrder");
                if (mArray != null) {
                    DataHolder.getInstance().initOrderList(mArray);
                } else {
                    listener.onNetworkError();
                    return;
                }

                mArray = response.getJSONArray("ColPos");
                if (mArray != null) {
                    DataHolder.getInstance().initPostList(mArray);
                } else {
                    listener.onNetworkError();
                    return;
                }

                boolean isRealStock = response.getBoolean("IsRealOrSim");
                DataHolder.getInstance().setRealStock(isRealStock);


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                listener.onNetworkError();
            }

            DataHolder.getInstance().setAccount(username);
            DataHolder.getInstance().setPassword(password);
            DataHolder.getInstance().setEncodedUsername(encodedUsername);
            DataHolder.getInstance().setEncodedPassword(encodedPassword);

            CSApplication.connectWebSocket(); //connect here because username is inited

            Log.i("Login", username + ": refresh finished");

            ArrayList<String> nameList = DataHolder.getInstance().getInsNameList();
            totalTask = nameList.size();

            for (String instrumentId : nameList) {
                executeInsReq(instrumentId);
            }

            Util.showProgressMessage("正在获取历史数据", mActivity, 3000);


        }


    }

    private class ErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (isDebug)
                Log.i("LoginError", error.getMessage());
            listener.onNetworkError();
            mRequestQueue.cancelAll(TAG);
//            executeLoginReq();
        }
    }

    public class CompanyInfoTask extends AsyncTask<Void, String, String> {

        @Nullable
        protected String responseString = null;
        protected boolean taskCancel = false;
        public int type;
        public boolean isException = false;
        public OnLoginFinishedListener listener;

        public CompanyInfoTask(final OnLoginFinishedListener _listener, int _type) {
            this.listener = _listener;
            this.type = _type;
        }

        @Nullable
        @Override
        protected String doInBackground(Void... params) {

            HttpURLConnection conn = null; // } else
            try {

                String companyInfoUrl = serverAddress
                        + "/spservice/spservice.svc/GetCompanyInfo/"
                        + companyId;

                if (isDebug)
                    System.out.println(companyInfoUrl);

                URL url = new URL(companyInfoUrl);
                conn = (HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);

                InputStream is = conn.getInputStream();
                int http_status = conn.getResponseCode();

                // better check it first
                if (http_status / 100 != 2) {

                    isException = true;
                    cancel(true);
                    taskCancel = true;

                }

                responseString = Util.getStringFromInputStream(is);
                is.close();

            } catch (Exception e) {
                e.printStackTrace();
                isException = true;
                cancel(true);
                taskCancel = true;

            } finally {

                if (conn != null)
                    conn.disconnect();

            }
            return responseString;
        }

        @Override
        protected void onCancelled() {
            listener.onNetworkError();
        }

        @Override
        protected void onPostExecute(@Nullable String result) {

            if (!taskCancel && result != null) {
                // Do anything with response..
                if (isDebug)
                    System.out.println(result);

                JSONObject GeneralDataJSON = null;
                try {
                    GeneralDataJSON = new JSONObject(result);
                    DataHolder.getInstance().initCompanyInfo(GeneralDataJSON);

                    //success
                    if (type == 0) {//login
                        listener.onSuccess(username, true);
                    }
                    else{//register
                        listener.onSuccess();
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    cancel(true);
                }

            } else {

                listener.onNetworkError();

            }

        }

    }


}

