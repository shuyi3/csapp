package com.csapp.mvp.dkb.Login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.Util;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by shuyi on 15/3/4.
 */
public class RegisterActivity extends Activity {

    private EditText realnameEdit, passwordEdit, retypeEdit, telEdit, verificationEdit;
    //    private Spinner ratioSpinner;
//            channelSpinner;
    private Button registerButton, verificationButton;
    private ProgressDialog registerProgress;
    private TextView countDownTextView;
    private Activity mActivity = this;
    private int verificationNumber;
    private final timerStatus ts = new timerStatus();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        realnameEdit = (EditText) findViewById(R.id.realname);
        passwordEdit = (EditText) findViewById(R.id.password);
        retypeEdit = (EditText) findViewById(R.id.retype);
        telEdit = (EditText) findViewById(R.id.tel);
        verificationEdit = (EditText) findViewById(R.id.verification);
        registerButton = (Button) findViewById(R.id.register_button);
        verificationButton = (Button) findViewById(R.id.verification_button);
        countDownTextView = (TextView) findViewById(R.id.countdown_text);

        final ArrayList<String> channelList = DataHolder.getInstance().getmCompanyInfo().getPaymentChannel();

        final CountDownTimer timer = new CountDownTimer(90000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                countDownTextView.setText("还剩" + millisUntilFinished / 1000 + "秒可再发送一次");
            }

            @Override
            public void onFinish() {
                countDownTextView.setText("");
                ts.setStatus(false);
                verificationButton.setEnabled(true);
            }
        };

        verificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //start timer once when button first click
                if (!ts.getStatus()){
                    timer.start();
                    ts.setStatus(true);
                    Random rng = new Random();
                    verificationNumber = 10000 + rng.nextInt(90000);
                    String tel = telEdit.getText().toString();
                    Util.sendVerificationRequest(tel, verificationNumber);
                    verificationButton.setEnabled(false);
                }

            }

        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String realName = realnameEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                String retype = retypeEdit.getText().toString();
                String tel = telEdit.getText().toString();
                String verification = verificationEdit.getText().toString();
//                String withdrawAccount = withdrawEdit.getText().toString();

//                String ratio = ratioSpinner.getSelectedItem().toString();
//                String channel = channelSpinner.getSelectedItem().toString();

                if (formValidate(realName, password, retype, tel, verification)) {

                    registerProgress = ProgressDialog.show(mActivity, "请稍后",
                            "正在注册用户", true);

                    final RegisterListener mRegisterListener = new RegisterListener() {
                        @Override
                        public void onRegisterSuccess() {
                            registerProgress.dismiss();
                            Util.showProgressMessage("用户注册成功", (Activity) CSApplication.getContext(), 2000);
                            finish();
                        }

                        @Override
                        public void onRegisterFailure(String message) {
                            registerProgress.dismiss();
                            Util.showWarning(message, mActivity, 2000);
                        }
                    };

                    Util.registerUser(mRegisterListener, realName, password, tel);
                }


            }
        });
    }

    public boolean formValidate(String realName, String password, String retype, String tel, String verification) {

        if (TextUtils.isEmpty(realName)) {
            realnameEdit.setError("姓名不能为空");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEdit.setError("密码不能为空");
            return false;
        }

        if (!retype.equals(password)) {
            retypeEdit.setError("两次输入密码不一致");
            return false;
        }

        if (TextUtils.isEmpty(tel)) {
            telEdit.setError("电话不能为空");
            return false;
        }

        if (!verification.equals(String.valueOf(verificationNumber))){
            verificationEdit.setError("验证码不正确");
            return false;
        }

        return true;
    }

    private class timerStatus{

        private boolean timerStatus;

        public timerStatus(){
            timerStatus = false;
        }

        public void setStatus(boolean s){
            timerStatus = s;
        }

        public boolean getStatus(){
            return  timerStatus;
        }
    }
}
