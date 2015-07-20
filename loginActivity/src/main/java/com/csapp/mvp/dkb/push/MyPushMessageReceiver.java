package com.csapp.mvp.dkb.push;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.frontia.api.FrontiaPushMessageReceiver;
import com.csapp.mvp.dkb.details.TabbedListActivity;
import com.csapp.mvp.dkb.Login.CSApplication;
import com.csapp.mvp.dkb.Login.LoginActivity;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.Util;
import com.csapp.mvp.dkb.main.MainActivity;
import com.csapp.mvp.dkb.pay.Pingpp;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Push消息处理receiver。请编写您需要的回调函数， 一般来说： onBind是必须的，用来处理startWork返回值；
 * onMessage用来接收透传消息； onSetTags、onDelTags、onListTags是tag相关操作的回调；
 * onNotificationClicked在通知被点击时回调； onUnbind是stopWork接口的返回值回调
 * 
 * 返回值中的errorCode，解释如下： 
 *  0 - Success
 *  10001 - Network Problem
 *  30600 - Internal Server Error
 *  30601 - Method Not Allowed 
 *  30602 - Request Params Not Valid
 *  30603 - Authentication Failed 
 *  30604 - Quota Use Up Payment Required 
 *  30605 - Data Required Not Found 
 *  30606 - Request Time Expires Timeout 
 *  30607 - Channel Token Timeout 
 *  30608 - Bind Relation Not Found 
 *  30609 - Bind Number Too Many
 * 
 * 当您遇到以上返回错误时，如果解释不了您的问题，请用同一请求的返回值requestId和errorCode联系我们追查问题。
 * 
 */
public class MyPushMessageReceiver extends FrontiaPushMessageReceiver {
    /** TAG to Log */
    public static final String TAG = MyPushMessageReceiver.class
            .getSimpleName();

    /**
     * 调用PushManager.startWork后，sdk将对push
     * server发起绑定请求，这个过程是异步的。绑定请求的结果通过onBind返回。 如果您需要用单播推送，需要把这里获取的channel
     * id和user id上传到应用server中，再调用server接口用channel id和user id给单个手机或者用户推送。
     * 
     * @param context
     *            BroadcastReceiver的执行Context
     * @param errorCode
     *            绑定接口返回值，0 - 成功
     * @param appid
     *            应用id。errorCode非0时为null
     * @param userId
     *            应用user id。errorCode非0时为null
     * @param channelId
     *            应用channel id。errorCode非0时为null
     * @param requestId
     *            向服务端发起的请求id。在追查问题时有用；
     * @return none
     */
    @Override
    public void onBind( @NotNull Context context, int errorCode, String appid,
            String userId, String channelId, String requestId) {
        String responseString = "onBind errorCode=" + errorCode + " appid="
                + appid + " userId=" + userId + " channelId=" + channelId
                + " requestId=" + requestId;
        Log.d(TAG, responseString);

        // 绑定成功，设置已绑定flag，可以有效的减少不必要的绑定请求
        if (errorCode == 0) {
            Utils.setBind(context, true);
            // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
            if (CSApplication.getContext() instanceof LoginActivity){
                LoginActivity loginActivity = (LoginActivity)CSApplication.getContext();
                loginActivity.setPushUserId(userId);
                loginActivity.setPushChannelId(channelId);
//                if (loginActivity.getGeTuiClientId() != null) {
                loginActivity.dismissProgressDialog();
                Log.d("推送", "优先获取baidu");
//                }
            }
        }else{
            Toast.makeText(CSApplication.getContext(), "推送服务设置失败，请联网再试",
                    Toast.LENGTH_SHORT).show();
            ((LoginActivity)CSApplication.getContext()).finish();
        }

        
        //updatecontent(context, responseString);
    }

    /**
     * 接收透传消息的函数。
     * 
     * @param context
     *            上下文
     * @param message
     *            推送的消息
     * @param customContentString
     *            自定义内容,为空或者json字符串
     */
    @Override
    public void onMessage( @NotNull Context context,  @NotNull String message,
            String customContentString) {
        String messageString = "Baidu消息 message=\"" + message
                + "\" customContentString=" + customContentString;
        Log.d(TAG, messageString);

        if (DataHolder.getInstance().getAMI() == null || CSApplication.getContext() instanceof LoginActivity)
        {
            return;
        }

        JSONObject messageJson = null;

        try {
            messageJson = new JSONObject(message);
            String sGUID = messageJson.getString("sGUID");
            if (DataHolder.getInstance().getsGUIDMap().get(sGUID) != null){
                Log.d(TAG, "相同push，不作处理");
                return;
            }
            DataHolder.getInstance().getsGUIDMap().put(sGUID, 1);
            Log.d(TAG, "加入map");

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

    /**
     * 接收通知点击的函数。注：推送通知被用户点击前，应用无法通过接口获取通知的内容。
     * 
     * @param context
     *            上下文
     * @param title
     *            推送的通知的标题
     * @param description
     *            推送的通知的描述
     * @param customContentString
     *            自定义内容，为空或者json字符串
     */
    @Override
    public void onNotificationClicked( @NotNull Context context, String title,
            String description, String customContentString) {
        String notifyString = "通知点击 title=\"" + title + "\" description=\""
                + description + "\" customContent=" + customContentString;
        Log.d(TAG, notifyString);

        // 自定义内容获取方式，mykey和myvalue对应通知推送时自定义内容中设置的键和值
        if (!TextUtils.isEmpty(customContentString)) {
            JSONObject customJson = null;
            try {
                customJson = new JSONObject(customContentString);
                String myvalue = null;
                if (!customJson.isNull("OrderSysID")) {
                    myvalue = customJson.getString("OrderSysID");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
//        //updatecontent(context, notifyString);
    }

    /**
     * setTags() 的回调函数。
     * 
     * @param context
     *            上下文
     * @param errorCode
     *            错误码。0表示某些tag已经设置成功；非0表示所有tag的设置均失败。
     * @param successTags
     *            设置成功的tag
     * @param failTags
     *            设置失败的tag
     * @param requestId
     *            分配给对云推送的请求的id
     */
    @Override
    public void onSetTags( @NotNull Context context, int errorCode,
            List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onSetTags errorCode=" + errorCode
                + " sucessTags=" + sucessTags + " failTags=" + failTags
                + " requestId=" + requestId;
        Log.d(TAG, responseString);

        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        //updatecontent(context, responseString);
    }

    /**
     * delTags() 的回调函数。
     * 
     * @param context
     *            上下文
     * @param errorCode
     *            错误码。0表示某些tag已经删除成功；非0表示所有tag均删除失败。
     * @param successTags
     *            成功删除的tag
     * @param failTags
     *            删除失败的tag
     * @param requestId
     *            分配给对云推送的请求的id
     */
    @Override
    public void onDelTags( @NotNull Context context, int errorCode,
            List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onDelTags errorCode=" + errorCode
                + " sucessTags=" + sucessTags + " failTags=" + failTags
                + " requestId=" + requestId;
        Log.d(TAG, responseString);

        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        //updatecontent(context, responseString);
    }

    /**
     * listTags() 的回调函数。
     * 
     * @param context
     *            上下文
     * @param errorCode
     *            错误码。0表示列举tag成功；非0表示失败。
     * @param tags
     *            当前应用设置的所有tag。
     * @param requestId
     *            分配给对云推送的请求的id
     */
    @Override
    public void onListTags( @NotNull Context context, int errorCode, List<String> tags,
            String requestId) {
        String responseString = "onListTags errorCode=" + errorCode + " tags="
                + tags;
        Log.d(TAG, responseString);

        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        //updatecontent(context, responseString);
    }

    /**
     * PushManager.stopWork() 的回调函数。
     * 
     * @param context
     *            上下文
     * @param errorCode
     *            错误码。0表示从云推送解绑定成功；非0表示失败。
     * @param requestId
     *            分配给对云推送的请求的id
     */
    @Override
    public void onUnbind( @NotNull Context context, int errorCode, String requestId) {
        String responseString = "onUnbind errorCode=" + errorCode
                + " requestId = " + requestId;
        Log.d(TAG, responseString);

        // 解绑定成功，设置未绑定flag，
        if (errorCode == 0) {
            Utils.setBind(context, false);
        }
        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        //updatecontent(context, responseString);
    }

//    @SuppressLint("SimpleDateFormat") private void ////updatecontent( @NotNull Context context, String content) {
//        Log.d(TAG, "////updatecontent");
//        String logText = "" + Utils.logStringCache;
//
//        if (!logText.equals("")) {
//            logText += "\n";
//        }
//
//        SimpleDateFormat sDateFormat = new SimpleDateFormat("HH-mm-ss");
//        logText += sDateFormat.format(new Date()) + ": ";
//        logText += content;
//        logText += " " + context.toString();
//
//        Utils.logStringCache = logText;
//
////        Intent intent = new Intent();
////        intent.setClass(context.getApplicationContext(), PushDemoActivity.class);
////        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        context.getApplicationContext().startActivity(intent);
////        Util.showProgressMessage(logText, (Activity) CSApplication.getContext(), 5000);
//    }
    
}
