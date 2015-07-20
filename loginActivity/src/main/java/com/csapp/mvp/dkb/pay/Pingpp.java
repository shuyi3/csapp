package com.csapp.mvp.dkb.pay;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.Util;
import com.csapp.mvp.dkb.Login.CSApplication;
import com.csapp.mvp.dkb.R;
import com.google.gson.Gson;
import com.pingplusplus.android.PaymentActivity;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class Pingpp extends Activity implements View.OnClickListener{

    private static final String URL = "http://121.40.131.144/pingpp/ping/example/pay.php";
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final String CHANNEL_UPMP = "upmp";
    private static final String CHANNEL_WECHAT = "wx";
    private static final String CHANNEL_ALIPAY = "alipay";
    private static final String CHANNEL_BFB = "bfb";

    private EditText amountEditText;
    private Button wechatButton;
    private Button alipayButton;
    private Button upmpButton;
    private Button bfbButton;
    
    private String currentAmount = "";

    private TextView depositText;
    private Switch depositSwitch;

    private String userId = DataHolder.getInstance().getAccount();
    private boolean isDebug = false;
    private int parsed = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay);

        ActionBar mActionBar = getActionBar();
        if (mActionBar != null){
            mActionBar.setTitle("充值界面");
        }

        CSApplication.setContext(this);

        amountEditText = (EditText) findViewById(R.id.amountEditText);
        wechatButton = (Button) findViewById(R.id.wechatButton);
        alipayButton = (Button) findViewById(R.id.alipayButton);
        upmpButton = (Button) findViewById(R.id.upmpButton);
        bfbButton = (Button) findViewById(R.id.bfbButton);
        wechatButton.setOnClickListener(Pingpp.this);
        alipayButton.setOnClickListener(Pingpp.this);
        upmpButton.setOnClickListener(Pingpp.this);
        bfbButton.setOnClickListener(Pingpp.this);

        depositText = (TextView) findViewById(R.id.deposit_text);
        depositSwitch = (Switch) findViewById(R.id.deposit_switch);

        depositSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    depositText.setEnabled(true);
                    setDepositText();
                }else{
                    depositText.setEnabled(false);
                }
            }
        });
    
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(currentAmount)) {
                    amountEditText.removeTextChangedListener(this);
                    String replaceable = String.format("[%s, \\s.]", NumberFormat.getCurrencyInstance(Locale.CHINA).getCurrency().getSymbol(Locale.CHINA));
                    String cleanString = s.toString().replaceAll(replaceable, "");

                    if (cleanString == "" || new BigDecimal(cleanString).toString().equals("0")) {
                        amountEditText.setText(null);
                    } else {
                        parsed = Integer.parseInt(cleanString);
                        String formatted = NumberFormat.getCurrencyInstance(Locale.CHINA).getCurrency().getSymbol(Locale.CHINA) + parsed;
                        currentAmount = formatted;
                        amountEditText.setText(formatted);
                        amountEditText.setSelection(formatted.length());
                        //textview
                        if (depositText.isEnabled()) {
                            setDepositText();
                        }
                    }
                    amountEditText.addTextChangedListener(this);
                }
            }
        });

        depositSwitch.setChecked(true);

        Log.d("PingppSdkVersion", PaymentActivity.getVersion());
    }

    public void setDepositText(){
        String ratio[] = DataHolder.getInstance().getAMI().getMoneyRatio().split(":");
        if (isDebug)
            Log.d("Ping++", "money ratio: " + ratio[0] + ":" + ratio[1]);
        double depositRatio = Double.parseDouble(ratio[1]) / Double.parseDouble(ratio[0]);
        depositText.setText("资方配资金额: " + String.valueOf(Util.formatDouble(parsed * depositRatio)));
    }

    public void onClick(View view) {
        String amountText = amountEditText.getText().toString();
        if (amountText.equals("")) {
            return;
        }

        String replaceable = String.format("[%s, \\s.]", NumberFormat.getCurrencyInstance(Locale.CHINA).getCurrency().getSymbol(Locale.CHINA));
        String cleanString = amountText.toString().replaceAll(replaceable, "");
        int amount = Integer.valueOf(new BigDecimal(cleanString).toString());

        if (amount > DataHolder.getInstance().getmCompanyInfo().getMaxDepositMoney() || amount < DataHolder.getInstance().getmCompanyInfo().getMinDepositMoney()){
            //out of range

            if (isDebug)
                Log.d("Ping++","amout=" + amount + "range: " + DataHolder.getInstance().getmCompanyInfo().getMinDepositMoney() + "-" + DataHolder.getInstance().getmCompanyInfo().getMaxDepositMoney());
            amountEditText.setError("输入金额超出有效范围");
            return;
        }

        String moneyRatio = "0:0";
        if (depositSwitch.isChecked()){
            moneyRatio = DataHolder.getInstance().getAMI().getMoneyRatio();
        }

        if (isDebug)
            Log.d("Ping++", "money ratio: " + moneyRatio);

        // 支付宝，微信支付，银联，百度钱包 按键的点击响应处理
        if (view.getId() == R.id.upmpButton) {
            new PaymentTask().execute(new PaymentRequest(CHANNEL_UPMP, amount, moneyRatio));
        } else if (view.getId() == R.id.alipayButton) {
            new PaymentTask().execute(new PaymentRequest(CHANNEL_ALIPAY, amount, moneyRatio));
        } else if (view.getId() == R.id.wechatButton) {
            new PaymentTask().execute(new PaymentRequest(CHANNEL_WECHAT, amount, moneyRatio));
        } else if (view.getId() == R.id.bfbButton) {
        	new PaymentTask().execute(new PaymentRequest(CHANNEL_BFB, amount, moneyRatio));
        }
    }

    class PaymentTask extends AsyncTask<PaymentRequest, Void, String> {

        @Override
        protected void onPreExecute() {

            //按键点击之后的禁用，防止重复点击
            wechatButton.setOnClickListener(null);
            alipayButton.setOnClickListener(null);
            upmpButton.setOnClickListener(null);
            bfbButton.setOnClickListener(null);
        }

        @Override
        protected String doInBackground(PaymentRequest... pr) {

            PaymentRequest paymentRequest = pr[0];
            String data = null;
            String json = new Gson().toJson(paymentRequest);
            if (isDebug)
                System.out.println(json);
            try {
                //向Your Ping++ Server SDK请求数据
                data = postJson(URL, json);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String data) {
            if (isDebug)
        	    Log.d("charge", data);
            if (data.equals("失败")){
                Util.showWarning("配比资金不足",(Activity)CSApplication.getContext(),2000);
                wechatButton.setOnClickListener(Pingpp.this);
                alipayButton.setOnClickListener(Pingpp.this);
                upmpButton.setOnClickListener(Pingpp.this);
                bfbButton.setOnClickListener(Pingpp.this);
            }else{
                Intent intent = new Intent();
                String packageName = getPackageName();
                ComponentName componentName = new ComponentName(packageName, packageName + ".wxapi.WXPayEntryActivity");
                intent.setComponent(componentName);
                intent.putExtra(PaymentActivity.EXTRA_CHARGE, data);
                startActivityForResult(intent, REQUEST_CODE_PAYMENT);
            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        wechatButton.setOnClickListener(Pingpp.this);
        alipayButton.setOnClickListener(Pingpp.this);
        upmpButton.setOnClickListener(Pingpp.this);
        bfbButton.setOnClickListener(Pingpp.this);

        //支付页面返回处理
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getExtras().getString("pay_result");
                /* 处理返回值
                 * "success" - payment succeed
                 * "fail"    - payment failed
                 * "cancel"  - user canceld
                 * "invalid" - payment plugin not installed
                 */
                 if (result.equals("success"))
                     Util.showProgressMessage("充值成功",(Activity)CSApplication.getContext(),2000);
                 else if (result.equals("success"))
                     Util.showWarning("充值失败,原因:" + data.getExtras().getString("error_msg"),(Activity)CSApplication.getContext(),2000);
                 else if (result.equals("cancel"))
                     Util.showWarning("充值失败,原因: 用户取消" ,(Activity)CSApplication.getContext(),2000);
                 else if (result.equals("invalid"))
                     Util.showWarning("充值失败,原因: 充值软件没有安装" ,(Activity)CSApplication.getContext(),2000);
//                String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
//                String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息
//                showMsg(result, errorMsg, extraMsg);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Util.showWarning("充值失败,原因: 用户取消" ,(Activity)CSApplication.getContext(),2000);
//                showMsg("User canceled", "", "");
            }
        }
    }
    
    public void showMsg(String title, String msg1, String msg2) {
    	String str = title;
    	if (msg1.length() != 0) {
    		str += "\n" + msg1;
    	}
    	if (msg2.length() != 0) {
    		str += "\n" + msg2;
    	}
    	Builder builder = new Builder(Pingpp.this);
    	builder.setMessage(str);
    	builder.setTitle("提示");
    	builder.setPositiveButton("OK", null);
    	builder.create().show();
    }

    private static String postJson(String url, String json) throws IOException {
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(type, json);
        Request request = new Request.Builder().url(url).post(body).build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        return response.body().string();
    }

    class PaymentRequest {
        String channel;
        int amount;
        Metadata metadata;

        public PaymentRequest(String channel, int amount, String moneyratio) {
            this.channel = channel;
            this.amount = amount;
            this.metadata = new Metadata(Util.companyId, userId, moneyratio);
        }

        class Metadata{
            String companyid;
            String userid;
            String moneyratio;
            public Metadata(String companyid, String userid, String moneyratio){
                this.companyid = companyid;
                this.userid = userid;
                this.moneyratio = moneyratio;
            }
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
