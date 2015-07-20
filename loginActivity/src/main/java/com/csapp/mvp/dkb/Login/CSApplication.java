package com.csapp.mvp.dkb.Login;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.frontia.FrontiaApplication;
import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.Util;
import com.csapp.mvp.dkb.details.TabbedListActivity;
import com.csapp.mvp.dkb.main.MainActivity;
import com.csapp.mvp.dkb.pay.Pingpp;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

@ReportsCrashes(
        formKey = "",
        formUri = "http://ec2-54-169-236-92.ap-southeast-1.compute.amazonaws.com:5984/acra-csapp/_design/acra-storage/_update/report",
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        reportType = org.acra.sender.HttpSender.Type.JSON,
        formUriBasicAuthLogin = "csapp",
        formUriBasicAuthPassword = "csapp",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_text)
//        resDialogTitle = )
//,
//        Your usual ACRA configuration
//        mode = ReportingInteractionMode.TOAST,
//        )
public class CSApplication extends FrontiaApplication {

    @Nullable
    private static Context mContext = null;
    private static RequestQueue mRequestQueue;

    private static WebSocketClient mWebSocketClient;

    private boolean isDebug = false;

    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        super.onCreate();
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        ACRA.init(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            try {
                ViewConfiguration config = ViewConfiguration.get(this);
                Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
                if (menuKeyField != null) {
                    menuKeyField.setAccessible(true);
                    menuKeyField.setBoolean(config, true);
                }
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    @Nullable
    public static Context getContext() {
        return mContext;
    }

    public static RequestQueue getmRequestQueue(){
        return mRequestQueue;
    }

    public static void setContext(Context context) {
        mContext = context;
    }


    public static WebSocketClient getWebSocketClient(){
        return  mWebSocketClient;
    }

    private static final int TIMER_RATE = 10000;
    private static final int TIMER_DELAY = 0;
    private static Timer timer;

    private static int pingSent;
    private static int pongReceived;

    private static void startTimer() {
        cancelTimer();
        scheduleTimer();
    }

    private static void scheduleTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendPing();
            }
        }, TIMER_DELAY, TIMER_RATE);
    }

    private static void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public static void sendPing()
    {
        if (pingSent - pongReceived >= 2){//connection lost
            Log.i("Websocket", "connection lost, try reconnect");
            closeWebSocket();
            connectWebSocket();
            return;
        }
        if (mWebSocketClient != null) {
            FramedataImpl1 frame = new FramedataImpl1(Framedata.Opcode.PING);
            frame.setFin(true);
            mWebSocketClient.getConnection().sendFrame(frame);
            pingSent++;
            //Log.i("Websocket", "sent ping");
        }
    }

    public static void closeWebSocket() {
        if (mWebSocketClient != null) {
            //cancelTimer();
            mWebSocketClient.close();
        }
    }

    public static void connectWebSocket() {

        URI uri;
        try {
            uri = new URI("ws://" + getContext().getResources().getString(R.string.server_address) + ":9990");
            pingSent = 0;
            pongReceived = 0;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("companyid=" + getContext().getResources().getString(R.string.companyId) + "&subaccountid=" + DataHolder.getInstance().getAccount() + "&devicetype=3");
                startTimer();
            }

            @Override
            public void onMessage(String message) {
                String messageString = "Websocket消息 message=\"" + message;
                Log.d("Websocket", messageString);

                if (DataHolder.getInstance().getAMI() == null || CSApplication.getContext() instanceof LoginActivity)
                {
                    return;
                }

                JSONObject messageJson = null;

                try {
                    messageJson = new JSONObject(message);
                    String sGUID = messageJson.getString("sGUID");
                    if (DataHolder.getInstance().getsGUIDMap().get(sGUID) != null){
                        Log.d("Websocket", "相同push，不作处理");
                        return;
                    }
                    DataHolder.getInstance().getsGUIDMap().put(sGUID, 1);
                    Log.d("Websocket", "加入map");

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }

                if (message.contains("下线通知")){
                    if (CSApplication.getContext() instanceof MainActivity){
                        MainActivity mainActivity = (MainActivity) CSApplication.getContext();
                        mainActivity.navigateToLogin();
                    }else if (CSApplication.getContext() instanceof TabbedListActivity){
                        TabbedListActivity tabbedListActivity = (TabbedListActivity) CSApplication.getContext();
                        tabbedListActivity.navigateToLogin();
                    }else if (CSApplication.getContext() instanceof Pingpp) {
                        Pingpp pingppActivity = (Pingpp) CSApplication.getContext();
                        pingppActivity.navigateToLogin();
                    }
                    Toast.makeText(CSApplication.getContext(), "当前账号已在其他设备登陆，账号自动登出",
                            Toast.LENGTH_SHORT).show();
                }
                // 自定义内容获取方式，mykey和myvalue对应透传消息推送时自定义内容中设置的键和值
                else if (!TextUtils.isEmpty(message)) {
                    JSONObject customJson = null;
                    try {
                        String description = messageJson.getString("description");
                        String title = messageJson.getString("title");
                        if (title.equals("错误回报")){
                            //error handling
                            Util.showWarning(description, (Activity) CSApplication.getContext(), 2000);
                        }else if (description.contains("充值") || description.contains("提现")){//充值 or 提现
                            Util.showProgressMessage(description, (Activity) CSApplication.getContext(), 2000);
                            new Util.JSONTask().execute("");
                        }else {//交易明细更新
                            customJson = messageJson.getJSONObject("custom_content");
                            String instrumentId = customJson.getString("InstrumentID");
                            String orderSysId = customJson.getString("OrderSysID").trim();
                            Util.updateOrderRequest(instrumentId, orderSysId);
                            if (!description.contains("已提交"))
                                Util.showProgressMessage(description, (Activity) CSApplication.getContext(), 2000);
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }

            @Override
            public void onWebsocketPong( WebSocket conn, Framedata f ) {
                //Log.i("Websocket", "receive pong");
                pongReceived++;
            }
        };

        mWebSocketClient.connect();
    }


}