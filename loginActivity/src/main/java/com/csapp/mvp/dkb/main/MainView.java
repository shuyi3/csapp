

package com.csapp.mvp.dkb.main;

public interface MainView {

    void showProgress();

    void hideProgress();

    void setItems();

    void showMessage(String message);
}
