package com.csapp.mvp.dkb.push;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.csapp.mvp.dkb.Login.CSApplication;
import com.csapp.mvp.dkb.Login.LoginActivity;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.Util;
import com.csapp.mvp.dkb.details.TabbedListActivity;
import com.csapp.mvp.dkb.main.MainActivity;
import com.csapp.mvp.dkb.pay.Pingpp;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;

import org.json.JSONException;
import org.json.JSONObject;

public class PushDemoReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Log.d("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));
		switch (bundle.getInt(PushConsts.CMD_ACTION)) {

		case PushConsts.GET_MSG_DATA:
			// 获取透传数据
			// String appid = bundle.getString("appid");
			byte[] payload = bundle.getByteArray("payload");
			
			String taskid = bundle.getString("taskid");
			String messageid = bundle.getString("messageid");

			// smartPush第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
			boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
			System.out.println("第三方回执接口调用" + (result ? "成功" : "失败"));
			
			if (payload != null) {
				String message = new String(payload);

				Log.d("GetuiSdkDemo", "Getui:" + message);

                if (DataHolder.getInstance().getAMI() == null || CSApplication.getContext() instanceof LoginActivity)
                {
                    return;
                }

                JSONObject messageJson = null;

                try {
                    messageJson = new JSONObject(message);
                    String sGUID = messageJson.getString("sGUID");
                    if (DataHolder.getInstance().getsGUIDMap().get(sGUID) != null){
                        Log.d("GetuiSdkDemo", "相同push，不作处理");
                        return;
                    }
                    DataHolder.getInstance().getsGUIDMap().put(sGUID, 1);
                    Log.d("GetuiSdkDemo", "加入map");

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
			break;
		case PushConsts.GET_CLIENTID:
			// 获取ClientID(CID)
			// 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
//			String cid = bundle.getString("clientid");
            String cid = "FakeGetuiId";
            Log.d("GetuiSdkDemo", "cid:" + cid);
            if (CSApplication.getContext() instanceof LoginActivity){
                LoginActivity loginActivity = (LoginActivity) CSApplication.getContext();
                loginActivity.setGeTuiClientId(cid);
//                if (loginActivity.getPushUserId() != null || loginActivity.getPushChannelId() != null) {
//                ((LoginActivity) CSApplication.getContext()).dismissProgressDialog();
                Log.d("推送", "优先获取个推1");
//                }
            }

//			if (GetuiSdkDemoActivity.tView != null)
//				GetuiSdkDemoActivity.tView.setText(cid);
			break;
		case PushConsts.THIRDPART_FEEDBACK:
			/*String appid = bundle.getString("appid");
			String taskid = bundle.getString("taskid");
			String actionid = bundle.getString("actionid");
			String result = bundle.getString("result");
			long timestamp = bundle.getLong("timestamp");

			Log.d("GetuiSdkDemo", "appid = " + appid);
			Log.d("GetuiSdkDemo", "taskid = " + taskid);
			Log.d("GetuiSdkDemo", "actionid = " + actionid);
			Log.d("GetuiSdkDemo", "result = " + result);
			Log.d("GetuiSdkDemo", "timestamp = " + timestamp);*/
			break;
		default:
			break;
		}
	}
}
