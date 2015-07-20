package com.csapp.mvp.dkb.Login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.data.Util;
import com.csapp.mvp.dkb.main.MainActivity;

import org.jetbrains.annotations.NotNull;


public class LoginActivity extends Activity implements LoginView, View.OnClickListener {

    private ProgressBar progressBar;
    private EditText username;
    private EditText password;
    private LoginPresenter presenter;
    private CheckBox rememberCheck;
    private Button loginButton;
    private Button registerButton;
    private ProgressDialog progress;

    public static final String PREFS_NAME = "MyPrefsFile";

    private String pushUserId, pushChannelId, geTuiClientId;

    public String getPushUserId() {
        return pushUserId;
    }

    public void setPushUserId(String pushUserId) {
        this.pushUserId = pushUserId;
    }

    public String getPushChannelId() {
        return pushChannelId;
    }

    public void setPushChannelId(String pushChannelId) {
        this.pushChannelId = pushChannelId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_login);

        CSApplication.setContext(this);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        rememberCheck = (CheckBox) findViewById(R.id.checkbox_checked_enabled);

        loginButton = (Button)findViewById(R.id.button);
        loginButton.setOnClickListener(this);

        registerButton = (Button)findViewById(R.id.register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.register();
            }
        });



        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        String Account = settings.getString("account", "");
        boolean isRemember = settings.getBoolean("isRemember", false);
        username.setText(Account);
        rememberCheck.setChecked(isRemember);
        
        presenter = new LoginPresenterImpl(this);

        if (settings.getBoolean("firstRun", true)){
            showAgreement();
        }else {
            registerPushService();
        }
//        Util.saveLogcatToFile(this);
    }

    public void showAgreement(){

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("多空宝手机交易系统风险提示书");
        String message;

        b.setCancelable(false);
        b.setMessage(" 敬请投资者仔细阅读以下内容，以便正确、全面地了解手机终端期货委托系统进行交易的风险。\n" +
                "      1、 由于互联网和移动通讯网络数据传输等原因，交易指令可能会出现中断、停顿、延迟、数据错误等情况；\n" +
                "      2、 由于互联网和移动通讯网络上存在黑客恶意攻击的可能性，网络服务器可能会出现故障及其他不可预测的因素，行情信息及其他期货信息可能会出现错误或延迟；\n" +
                "      3、 投资者的手机终端设备及软件系统可能会受到非法攻击或病毒感染，导致无法下达委托或委托失败；\n" +
                "      4、 投资者的手机终端设备及软件系统与所提供的手机期货交易系统不兼容，无法下达委托或委托失败；\n" +
                "      5、 如投资者缺乏手机终端委托经验，可能因操作不当造成委托失败或委托失误；\n" +
                "      6、 由于网络故障，投资者通过手机期货交易系统进行期货交易时，投资者手机终端设备已显示委托成功，而期货交易系统服务器未接到其委托指令，从而存在投资者不能买卖和平仓的风险；投资者手机终端设备对其委托未显示成功，于是投资者再次发出委托指令，而期货交易系统服务器已收到投资者两次委托指令，并按其指令进行了交易，使投资者由此产生重复买卖的风险。 上述风险可能会导致投资者发生损失。\n" +
                " \n" +
                "在此郑重提醒，手机终端期货委托有风险。如果投资者不了解或不能承受手机终端期货委托的风险，建议投资者不要使用手机终端期货委托系统进行交易。如果投资者申请或已申请使用手机终端期货委托系统，将认为投资者已经完全了解手机终端期货委托的风险，准备承受手机终端期货委托风险，并愿意承担由此带来的损失。\n");
        b.setPositiveButton("接受", new DialogInterface.OnClickListener() {

            @Override
            public void onClick( @NotNull DialogInterface dialog, int which) {
                dialog.dismiss();
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("firstRun", false).apply();

                registerPushService();
            }

        });
        b.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }

        });
        b.show();


    }



    @Override public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);
    }

    @Override public void hideProgress() {
        progressBar.setVisibility(View.GONE);
        loginButton.setEnabled(true);
        registerButton.setEnabled(true);
    }

    @Override public void setUsernameError() {
        username.setError(getString(R.string.username_error));
    }

    @Override public void setPasswordError() {
        password.setError(getString(R.string.password_error));
    }
    
    @Override public void networkError(){
    	Toast.makeText(getApplicationContext(), R.string.network_error,
    		     Toast.LENGTH_SHORT).show();
    }
    
	@Override
	public void usernameMismatchError() {
		// TODO Auto-generated method stub
//		Toast.makeText(getApplicationContext(), R.string.username_mismatch,
//   		     Toast.LENGTH_SHORT).show();
	}


    @Override public void navigateToHome() {
    	
    	Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override public void navigateToRegister() {

        Intent intent=new Intent(this, RegisterActivity.class);
        startActivity(intent);
//        finish();
    }

    @Override
    public synchronized void dismissProgressDialog() {
        progress.dismiss();
    }

    @Override
    public void registerPushService() {
        progress = ProgressDialog.show(CSApplication.getContext(), "请稍后",
                "正在注册通知服务", true);
        presenter.registerPushService();

    }

    @Override public void onClick(View v) {
    	loginWithPushService(pushUserId, pushChannelId, geTuiClientId);
    }

    public void loginWithPushService(String pushUserId, String pushChannelId, String geTuiClientId){
        presenter.validateCredentials(username.getText().toString(), password.getText().toString(), rememberCheck.isChecked(), pushUserId, pushChannelId, geTuiClientId);
    }

    @Override
    protected void onResume(){
        CSApplication.setContext(this);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Util.unbindDrawables(findViewById(R.id.RootView));
        System.gc();
    }

    public void setGeTuiClientId(String geTuiClientId) {
        this.geTuiClientId = geTuiClientId;
    }

    public String getGeTuiClientId() {
        return geTuiClientId;
    }

}
