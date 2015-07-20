

package com.csapp.mvp.dkb.Login;

public interface LoginView {
    void showProgress();

    void hideProgress();

    void setUsernameError();

    void setPasswordError();
    
    void networkError();
    
    void usernameMismatchError();

    void navigateToHome();

    void navigateToRegister();

    void dismissProgressDialog();

    void registerPushService();
}
