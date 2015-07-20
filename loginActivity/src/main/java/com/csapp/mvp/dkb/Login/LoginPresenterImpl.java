

package com.csapp.mvp.dkb.Login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.baidu.android.pushservice.PushConstants;
import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.network.LoginNetworkInteraction;
import com.csapp.mvp.dkb.push.Utils;

import org.jetbrains.annotations.NotNull;

public class LoginPresenterImpl implements LoginPresenter,
        OnLoginFinishedListener {

    private LoginView loginView;
    private LoginNetworkInteraction networkInteraction;
    private Activity mActivity;
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String key = "FengyunT";
    private RequestQueue mRequestQueue;
    private String encodedUsername, encodedPassword;

    private boolean isDebug = false;

    public LoginPresenterImpl(LoginView loginView) {
        this.loginView = loginView;
        this.mActivity = (Activity) loginView;
        this.networkInteraction = new LoginNetworkInteraction(this);
    }

    @Override
    public void validateCredentials(final String username, String password,
                                    boolean isRemember, String pushUserId, String pushChannelId, String geTuiClientId) {
        loginView.showProgress();
        networkInteraction.DoLoginTask(username, password, pushChannelId, pushUserId, geTuiClientId);
    }

    @Override
    public void registerPushService() {
//		loginView.showProgress();
//        Util.showProgressMessage("正在注册通知服务", mActivity, 3000);
        com.baidu.android.pushservice.PushManager.startWork(mActivity.getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(mActivity, "api_key"));

//        PushManager.getInstance().initialize(mActivity.getApplicationContext());
//        String cid = PushManager.getInstance().getClientid(mActivity);
        String cid = "FakeGetuiId";
        if (cid != null) {
            if (CSApplication.getContext() instanceof LoginActivity) {
                LoginActivity loginActivity = (LoginActivity) CSApplication.getContext();
                loginActivity.setGeTuiClientId(cid);
//                if (loginActivity.getPushUserId() != null || loginActivity.getPushChannelId() != null) {
//                ((LoginActivity) CSApplication.getContext()).dismissProgressDialog();
//                Log.d("推送","优先获取个推");
//                }
            }
        }
        Log.d("GetuiSdkDemo", "cid:" + cid);
    }


    @Override
    public void register() {
        networkInteraction.register();
    }

    @Override
    public void onUsernameError() {
        loginView.setUsernameError();
        loginView.hideProgress();
    }

    @Override
    public void onPasswordError() {
        loginView.setPasswordError();
        loginView.hideProgress();
    }

    @Override
    public void onSuccess() {
        loginView.navigateToRegister();
    }

    @Override
    public void onSuccess(String username, boolean isRemember) {
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("account", username);
        editor.putBoolean("isRemember", isRemember);

        // Commit the edits!
        editor.apply();
        loginView.navigateToHome();
    }

    @Override
    public void onNetworkError() {
        loginView.networkError();
        loginView.hideProgress();
    }

    @Override
    public void onUsernameMismatchError() {
        // TODO Auto-generated method stub
        loginView.usernameMismatchError();
        loginView.hideProgress();
    }

    @Override public void updateVersionDialog(){
            AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
            b.setTitle("提示");

            b.setCancelable(false);
            b.setMessage("当前客户端版本过低，是否下载最新版本?");
            b.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick( @NotNull DialogInterface dialog, int which) {
                    dialog.dismiss();
                    String url = mActivity.getResources().getString(R.string.download_url);
                    Intent downloadIntent = new Intent(Intent.ACTION_VIEW);
                    downloadIntent.setData(Uri.parse(url));
                    mActivity.startActivity(downloadIntent);
                }

            });
            b.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick( @NotNull DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });
            b.show();
    }
}
