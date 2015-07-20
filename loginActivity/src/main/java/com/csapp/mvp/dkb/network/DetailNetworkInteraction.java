package com.csapp.mvp.dkb.network;

import android.app.Activity;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.csapp.mvp.dkb.Login.CSApplication;
import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shuyi on 15/3/11.
 */
public class DetailNetworkInteraction {

    private boolean isDebug = false;

    private Activity mActivity = (Activity) CSApplication.getContext();
    private RequestQueue mRequestQueue = CSApplication.getmRequestQueue();

    private String serverAddress = "http://" + CSApplication.getContext().getResources().getString(R.string.server_address);
    private String companyId = CSApplication.getContext().getResources().getString(R.string.companyId);
    private String encodedUsername = DataHolder.getInstance().getEncodedUsername();
    private String encodedPassword = DataHolder.getInstance().getEncodedPassword();

    //UriTemplate = "RequestWithdrawMoney/{companyid}/{encryptuserid}/{encryptpassword}/{amount}",

    public void executeWithdrawReq(int amount){
        final String withdrawUrl = serverAddress + ":8024/MobileWithdraw.aspx?companyid=" + companyId + "&" +
                "subaccount=" + DataHolder.getInstance().getAccount() +"&password=" + DataHolder.getInstance().getPassword() + "&devicetype=3&maxwithdrawmoney=" + amount;

//                + companyId + "/" + encodedUsername + "/" + encodedPassword + "/" + amount + "/" + channel + "/" + account;

        if (isDebug) {
            Log.i("Main", "withdrawUrl: " + withdrawUrl);
        }

        StringRequest withdrawReq = new StringRequest(withdrawUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Main", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: " + error.getMessage());
                    }
                }
        );

        mRequestQueue.add(withdrawReq);

    }

    private class ReqListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            if (isDebug)
                Log.i("Main", response.toString());

            try{
                String result = response.getString("value");
                if (result.contains("成功")) {
                    Util.showProgressMessage(result, mActivity, 2000);
                    new Util.JSONTask().execute("");
                }
                else {
                    Util.showWarning(result, (Activity) CSApplication.getContext(), 2000);
                }
            }catch (JSONException e){
                e.printStackTrace();
                Util.showWarning("请求失败，请稍后再试", (Activity) CSApplication.getContext(), 2000);
            }
        }
    }

    private class ErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (isDebug)
                Log.i("LoginError", error.getMessage().toString());
            Util.showWarning("请求失败，请稍后再试", (Activity) CSApplication.getContext(), 2000);
        }
    }
}
