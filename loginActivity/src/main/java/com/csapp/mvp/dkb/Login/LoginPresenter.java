

package com.csapp.mvp.dkb.Login;

public interface LoginPresenter {
    void validateCredentials(String username, String password, boolean isRemember, String pushUserId, String pushChannelId, String geTuiClientId);
    void registerPushService();
    void register();
}
