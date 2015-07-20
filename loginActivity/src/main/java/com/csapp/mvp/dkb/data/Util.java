package com.csapp.mvp.dkb.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.csapp.mvp.dkb.Login.CSApplication;
import com.csapp.mvp.dkb.Login.RegisterListener;
import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.details.TabbedListActivity;
import com.csapp.mvp.dkb.main.MainActivity;
import com.csapp.mvp.dkb.network.StringPostRequest;
import com.devspark.appmsg.AppMsg;

import org.apache.http.conn.util.InetAddressUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@SuppressLint("DefaultLocale") public class Util {

    public static final String TAG_TX_FRAGMENT = "TAG_TX_FRAGMENT";
    public static final String TAG_LIST_FRAGMENT = "TAG_LIST_FRAGMENT";
    public static final String TAG_ABOUT_FRAGMENT = "TAG_ABOUT_FRAGMENT";
    public static final String serverAddress = "http://" + CSApplication.getContext().getResources().getString(R.string.server_address);
    public static final String companyId = CSApplication.getContext().getResources().getString(R.string.companyId);
    public static long lastTickUpdateTime = System.currentTimeMillis();


    public static String getId() {
        String id = android.provider.Settings.System.getString(CSApplication.getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        return id;
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param ipv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */

    @NotNull
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }


    public static String convertStockCode(@NotNull String input) {

        if (input.startsWith("IF")) {
            return input.replace("IF", "股指");
        } else {
            String str = input.replaceAll("\\D+", "");
            return DataHolder.getInstance().getInsInfoMap().get(input).getInstrumentName() + str;
        }

    }

    //"RequstOrderInsert/{accountID}/{instrumentid}/{direction}/{price}/{volume}/{stopprofit}/{stoploss}/{sIP}/{sDeviceID}"
    public static void sendRequest(@NotNull final String instrumentId,
                                   final int direction, final double price, final int volume, final double stopProfit,
                                   final double stopLoss, @NotNull final Activity mActivity) {

        String operation = null;
        final String encodedUsername = DataHolder.getInstance().getEncodedUsername();
        final String encodedPassword = DataHolder.getInstance().getEncodedPassword();

        switch (direction) {
            case 1:
                operation = "多开";
                break;
            case 2:
                operation = "多平";
                break;
            case 3:
                operation = "空开";
                break;
            case 4:
                operation = "空平";
                break;
        }

        final String op = operation;

        AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
        b.setTitle("交易信息: " + operation);
        String message;
        if (direction == 2 || direction == 4) {
            message = convertStockCode(instrumentId);
        } else {
            message = convertStockCode(instrumentId) + "\n价格:" + ((price == 0) ? "市价" : price) + "\n数量:" + volume;
            if (stopProfit != 0)
                message = message + "\n止盈:" + stopProfit + "\n止损:" + stopLoss;
        }

        b.setMessage(message);
        b.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(@NotNull DialogInterface dialog, int which) {
                dialog.dismiss();
                String requestString = "RequstOrderInsert/" + companyId + "/" +
                        encodedUsername + "/" + encodedPassword + "/" +
                        instrumentId + "/" + direction + "/" + price + "/" + volume + "/" + stopProfit + "/" + stopLoss + "/"
                        + getIPAddress(true) + "/" + getId();

                new RequestTask().execute(requestString);

            }

        });
        b.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(@NotNull DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        b.show();

    }

    public static void sendCancelRequest(int orderIndex, @NotNull final Activity mActivity) {

        AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
        b.setTitle("撤销交易: ");
        final String instrumentId = DataHolder.getInstance().getUnfinishedOrderList().get(orderIndex).getInstrumentID();
        final String orderSysId = DataHolder.getInstance().getUnfinishedOrderList().get(orderIndex).getOrderSysID();
        final String encodedUsername = DataHolder.getInstance().getEncodedUsername();
        final String encodedPassword = DataHolder.getInstance().getEncodedPassword();
        String message;
        message = convertStockCode(instrumentId) + "\n序号:" + (orderIndex + 1);

        b.setMessage(message);
        b.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(@NotNull DialogInterface dialog, int which) {
                dialog.dismiss();

                String requestString = "RequstOrderCancel/" +
                        encodedUsername + "/" + encodedPassword + "/" +
                        instrumentId + "/" + orderSysId;

                new RequestTask().execute(requestString);

            }

        });
        b.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(@NotNull DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        b.show();

    }

    public static void showProgressMessage(String message, final Activity mActivity, int time) {
        final AppMsg.Style style;
        style = new AppMsg.Style(time, AppMsg.STYLE_INFO.getBackground());
        AppMsg.makeText(mActivity, message, style).show();
    }

    public static void showWarning(String message, final Activity mActivity, int time) {
        final AppMsg.Style style;
        style = new AppMsg.Style(time, AppMsg.STYLE_ALERT.getBackground());
        AppMsg.makeText(mActivity, message, style).show();
    }

    public static String formatNumber(double number) {
        return String.format("%.2f", number);
    }

    public static double formatDouble(double number) {
        return Double.parseDouble(String.format("%.2f", number));
    }

    public static long dateStringToLong(String dateString) {

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        try {
//			System.out.println("date string: " + dateString);
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (date != null)
            return date.getTime();
        else
            return 0;

    }

    public static int binarySearch(@NotNull long[] a, long key) {

        if (a.length == 0) {
            return -1;
        }
        int low = 0;
        int high = a.length - 1;

        while (low <= high) {
            int middle = (low + high) / 2;
            if (key > a[middle]) {
                low = middle + 1;
            } else if (key < a[middle]) {
                high = middle - 1;
            } else { // The element has been found
                return middle;
            }
        }
        return -1;
    }

    @NotNull
    public static String getStringFromInputStream(@NotNull InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    private static class RequestTask extends AsyncTask<String, String, String> {

        @Nullable
        protected String responseString = null;
        protected boolean taskCancel = false;
        public int type;
        public static final int INITTASK = 1;
        public static final int LOADMORETASK = 2;
        public boolean isException = false;
        protected String username;
        private int timeout = 20000;

        // protected View contentView;
        // protected View loadingView;
        // protected View retryView;
        // protected Button mRetryButton;

        @Override
        protected void onPreExecute() {
//			Util.showProgressMessage("正在登录", mActivity);
        }

        @Nullable
        @Override
        protected String doInBackground(String... uri) {

            HttpURLConnection conn = null; // } else
            try {

                String insertUri = serverAddress
                        + "/spservice/spservice.svc/" + uri[0];
//                System.out.println(insertUri);
                URL url = new URL(insertUri);
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

                responseString = getStringFromInputStream(is);
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
            showWarning("发送报单失败，请稍后再试", (Activity) CSApplication.getContext(), 2000);
        }

        @Override
        protected void onPostExecute(@Nullable String result) {
            // Do anything with response..
            // System.out.println(result);

            if (!taskCancel && result != null) {
                // Do anything with response..

                if (result.equals("\"\"")) {

                    Log.i("Request", "send success");
                    // login success
//					new JSONTask(listener).execute(JSONUri);

                } else {

//					listener.onUsernameMismatchError();
                    if (result.startsWith("\"") && result.endsWith("\"")) {
                        String message = result.replaceAll("\"", "");
                        Util.showWarning(message, (Activity) CSApplication.getContext(), 2000);
                    }

                }

            } else {

                showWarning("发送报单失败，请稍后再试", (Activity) CSApplication.getContext(), 2000);
//				listener.onNetworkError();

            }

        }

    }

    public static void updateOrderRequest(String instrumentId, String orderSysId) {

        final String encodedUsername = DataHolder.getInstance().getEncodedUsername();
        final String encodedPassword = DataHolder.getInstance().getEncodedPassword();

        String updateQueryString = companyId + "/" + encodedUsername + "/" + encodedPassword + "/" + orderSysId + "/" + instrumentId;
        new updateOrderTask().execute(updateQueryString);
    }

    private static class updateOrderTask extends AsyncTask<String, String, String> {

        @Nullable
        protected String responseString = null;
        protected boolean taskCancel = false;
        public int type;
        public static final int INITTASK = 1;
        public static final int LOADMORETASK = 2;
        public boolean isException = false;
        protected String username;
        private int timeout = 20000;

        // protected View contentView;
        // protected View loadingView;
        // protected View retryView;
        // protected Button mRetryButton;

        @Override
        protected void onPreExecute() {
//			Util.showProgressMessage("正在登录", mActivity);
        }

        @Nullable
        @Override
        protected String doInBackground(String... uri) {

            HttpURLConnection conn = null; // } else
            try {

                String insertUri = serverAddress
                        + "/spservice/spservice.svc/RefreshRunTimeDataAfterOrderChange/" + uri[0];
//				System.out.println(insertUri);
                URL url = new URL(insertUri);
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

                responseString = getStringFromInputStream(is);
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
            showWarning("获取报单失败，请稍后再试", (Activity) CSApplication.getContext(), 2000);
        }

        @Override
        protected void onPostExecute(@Nullable String result) {
            // Do anything with response..
            // System.out.println(result);

            if (!taskCancel && result != null) {
                // Do anything with response..

                JSONObject orderJSON = null;
                try {
                    orderJSON = new JSONObject(result);
//					System.out.println(orderJSON.toString());
                    DataHolder.getInstance().updateOrderChange(orderJSON);

                    requestUpdate();


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            } else {

//				listener.onNetworkError();
                showWarning("获取报单失败，请稍后再试", (Activity) CSApplication.getContext(), 2000);

            }

        }

    }

    public static class JSONTask extends AsyncTask<String, String, String> {

        @Nullable
        protected String responseString = null;
        @NotNull
        protected String JSONUri = serverAddress
                + "/spservice/spservice.svc/RefreshData/" + companyId + "/";
        private int timeout = 100000;
        private String encodedUsername, encodedPassword, username;

        protected boolean taskCancel = false;
        public int type;
        public boolean isException = false;
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(CSApplication.getContext(), "请稍后",
                    "正在刷新数据", true);

        }


            @Nullable
        @Override
        protected String doInBackground(String... uri) {

            HttpURLConnection conn = null; // } else
            try {

                encodedUsername = DataHolder.getInstance().getEncodedUsername();
                encodedPassword = DataHolder.getInstance().getEncodedPassword();
                username = DataHolder.getInstance().getAccount();

                JSONUri = JSONUri + encodedUsername + "/" + encodedPassword;

//                if (isDebug)
//                    System.out.println(JSONUri);

                URL url = new URL(JSONUri);
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
            progress.dismiss();
            showWarning("网路错误", (Activity)CSApplication.getContext(), 1500);
        }

        @Override
        protected void onPostExecute(@Nullable String result) {

            if (!taskCancel && result != null) {
                // Do anything with response..

                JSONObject GeneralDataJSON = null;
                try {
                    GeneralDataJSON = new JSONObject(result);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    cancel(true);
                }

                String frontIP = null;
                try {
                    JSONObject mObj = null;
                    JSONArray mArray = null;

                    mArray = GeneralDataJSON.getJSONArray("ColMarketDataServerAddress");
                    if (mArray != null) {
                        DataHolder.getInstance().initFronIPArray(mArray);
                    } else {
                        progress.dismiss();
                        showWarning("网路错误", (Activity)CSApplication.getContext(), 1500);
                        return;
                    }

                    mObj = GeneralDataJSON.getJSONObject("AMI");
                    if (mObj != null) {
                        DataHolder.getInstance().initAMI(mObj);
                    } else {
                        progress.dismiss();
                        showWarning("网路错误", (Activity)CSApplication.getContext(), 1500);
                        return;
                    }

                    mArray = GeneralDataJSON.getJSONArray("ColInsInfo");
                    if (mArray != null) {
                        DataHolder.getInstance().initInsInfo(mArray);
                    } else {
                        progress.dismiss();
                        showWarning("网路错误", (Activity)CSApplication.getContext(), 1500);
                        return;
                    }

                    mArray = GeneralDataJSON.getJSONArray("ColOrder");
                    if (mArray != null) {
                        DataHolder.getInstance().initOrderList(mArray);
                    } else {
                        progress.dismiss();
                        showWarning("网路错误", (Activity)CSApplication.getContext(), 1500);
                        return;
                    }

                    mArray = GeneralDataJSON.getJSONArray("ColPos");
                    if (mArray != null) {
                        DataHolder.getInstance().initPostList(mArray);
                    } else {
                        progress.dismiss();
                        showWarning("网路错误", (Activity)CSApplication.getContext(), 1500);
                        return;
                    }

                    boolean isRealStock = GeneralDataJSON.getBoolean("IsRealOrSim");
                    DataHolder.getInstance().setRealStock(isRealStock);


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    cancel(true);
                }

                progress.dismiss();
                showProgressMessage("数据已更新", (Activity)CSApplication.getContext(), 1500);

            } else {

                progress.dismiss();
                showWarning("网路错误", (Activity)CSApplication.getContext(), 1500);
            }

        }
    }

        public static void tickUpdate(boolean isMinChanged) {

            if (System.currentTimeMillis() - lastTickUpdateTime < 1000) return; //mini interval
            lastTickUpdateTime = System.currentTimeMillis();

            if (CSApplication.getContext() instanceof MainActivity) {
                MainActivity mActivity = (MainActivity) CSApplication.getContext();
                if (mActivity != null)
                    mActivity.tickUpdate(isMinChanged);
            } else if (CSApplication.getContext() instanceof TabbedListActivity) {
                TabbedListActivity mActivity = (TabbedListActivity) CSApplication.getContext();
                if (mActivity != null)
                    mActivity.tickUpdate();
            }
        }

        public static void requestUpdate() {
            if (CSApplication.getContext() instanceof MainActivity) {
                MainActivity mActivity = (MainActivity) CSApplication.getContext();
                if (mActivity != null)
                    mActivity.requestUpdate();
            } else if (CSApplication.getContext() instanceof TabbedListActivity) {
                TabbedListActivity mActivity = (TabbedListActivity) CSApplication.getContext();
                if (mActivity != null)
                    mActivity.requestUpdate();
            }
        }

        public static void unbindDrawables(@NotNull View view) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        }

        public static double formatPrice(double price) {
            double formatted = price;
            for (int i = 0; i <= 4; i++) {
                if (Math.abs(Math.round((price * Math.pow(10, i))) / Math.pow(10, i) - price) < (0.0001)) {
                    formatted = Math.round((price * Math.pow(10, i))) / Math.pow(10, i);
                    return formatted;
                }
            }
            return formatted;
        }

        public static void registerUser(final RegisterListener listener, String realName, String password, String tel){

            JSONObject postJSON = new JSONObject();

            try {
                postJSON.put("companyid", companyId);
                postJSON.put("username", realName);
                postJSON.put("password", password);
                postJSON.put("phonenumber", tel);
                postJSON.put("moneyratio", DataHolder.getInstance().getmCompanyInfo().getMoneyRatio().get(0));
//                postJSON.put("moneyratio", ratio);
//                postJSON.put("withdrawchannel", channel);
//                postJSON.put("withdrawaccount", withdrawAccount);
            }catch(JSONException e){
                e.printStackTrace();
            }

//            System.out.println(postJSON.toString());

            String registerURL = serverAddress
                    + "/spservice/spservice.svc/RegisterUser";

            //uid=shfyxx&pwd=123123&rev=18621387502&msg=12345&sdt=&snd=

            StringPostRequest registerReq = new StringPostRequest(Method.POST,
                    registerURL, postJSON,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                String result = response.getString("value");
                                if (result.isEmpty())
                                    listener.onRegisterSuccess();
                                else
                                    listener.onRegisterFailure(result);
                            }catch (JSONException e){
                                e.printStackTrace();
                                listener.onRegisterFailure("用户注册失败,请检查网络");
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
//                  返回空字符串表示成功
//                    if (error.getMessage().equals("")){
//                        listener.onRegisterSuccess();
//                    }else {
                        VolleyLog.e("Error: " + error.getMessage());
                        listener.onRegisterFailure("用户注册失败,请检查网络");
//                    }

                }
            });

            CSApplication.getmRequestQueue().add(registerReq);
        }
    public static void sendVerificationRequest(String tel, int verificationCode){

        String msg = null;
        try {
            msg = URLEncoder.encode("尊敬的用户：您的验证码：" + verificationCode + "，工作人员不会索取，请勿泄露。", "gb2312");
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String query = "uid=shfydkb&pwd=123123&rev=" + tel + ",&msg=" + msg + "&sdt=&snd=";
        String verificationURL = "http://sms.eloone.com/ylSend.do?" + query;

        Log.i("Network", "URL: " + verificationURL);


        StringRequest registerReq = new StringRequest(verificationURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Network", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: " + error.getMessage());
                    }
                }
        );

        CSApplication.getmRequestQueue().add(registerReq);
    }

}
