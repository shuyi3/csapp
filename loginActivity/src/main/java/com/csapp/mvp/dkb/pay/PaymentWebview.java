package com.csapp.mvp.dkb.pay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.csapp.mvp.dkb.Login.CSApplication;
import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.data.DataHolder;

/**
 * Created by shuyi on 15/6/21.
 */
public class PaymentWebview extends Activity {

    private WebView mWebView;
    private String action;
    public static final String serverAddress = "http://" + CSApplication.getContext().getResources().getString(R.string.server_address);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_activity);

        CSApplication.setContext(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            action = extras.getString("action");
        }

        mWebView = (WebView) findViewById(R.id.webview);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                // Handle the error
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebViewJavaScriptInterface(), "app");

        if (action.equals("pay")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("充值的银行卡和取现的银行卡需保持一致，不可修改，请谨慎填写！")
                    .setTitle("注意")
                    .setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String url = serverAddress + ":8024/MobilePay.aspx?companyid=" + getResources().getString(R.string.companyId) +
                                    "&subaccount=" + DataHolder.getInstance().getAccount() +
                                    "&devicetype=3" + "&moneyratio=" + DataHolder.getInstance().getmCompanyInfo().getMoneyRatio().get(0);
                            Log.d("Payment", url);
                            mWebView.loadUrl(url);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }else {
            int amount = extras.getInt("amount");
            final String url = serverAddress + ":8024/MobileWithdraw.aspx?companyid=" + getResources().getString(R.string.companyId) + "&" +
                    "subaccount=" + DataHolder.getInstance().getAccount() +"&password=" + DataHolder.getInstance().getPassword() +
                    "&devicetype=3&maxwithdrawmoney=" + amount;
            Log.d("Payment", url);
            mWebView.loadUrl(url);

        }

    }

    public class WebViewJavaScriptInterface{

        /*
         * Need a reference to the context in order to sent a post message
         */
        /*
         * This method can be called from Android. @JavascriptInterface
         * required after SDK version 17.
         */
        @JavascriptInterface
        public void exit() {
            finish();
        }
    }

    public void navigateToLogin(){
        setResult(2);
        finish();
        System.out.println("purged to main");
    }

    @Override
    protected void onResume(){
        CSApplication.setContext(this);
        super.onResume();
    }
}
