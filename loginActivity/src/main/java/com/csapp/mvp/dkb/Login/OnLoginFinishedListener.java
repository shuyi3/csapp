

package com.csapp.mvp.dkb.Login;

public interface OnLoginFinishedListener {

    void onUsernameError();

    void onPasswordError();

    void onSuccess(String username, boolean isRemember);

    void onSuccess();

    void onNetworkError();

	void onUsernameMismatchError();

    void updateVersionDialog();

}
