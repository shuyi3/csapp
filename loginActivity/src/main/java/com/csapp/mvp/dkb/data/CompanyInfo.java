package com.csapp.mvp.dkb.data;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by shuyi on 15/3/3.
 */
public class CompanyInfo {

    private double MaxDepositMoney;
    private double MinDepositMoney;
    private ArrayList<String> paymentChannel;
    private ArrayList<String> moneyRatio;

    public CompanyInfo( @NotNull JSONObject CompanyObject){
        paymentChannel = new ArrayList<String>();
        moneyRatio = new ArrayList<String>();
        try {
            MaxDepositMoney = Util.formatDouble(CompanyObject.getDouble("MaxDepositMoney"));
            MinDepositMoney = Util.formatDouble(CompanyObject.getDouble("MinDepositMoney"));
            JSONArray tempArray = CompanyObject.getJSONArray("ColAllowWithdrawChannel");
            for(int i = 0; i < tempArray.length(); i++){
                paymentChannel.add(tempArray.getString(i));
            }
            tempArray = CompanyObject.getJSONArray("colAllowMoneyRatio");
            for(int i = 0; i < tempArray.length(); i++){
                moneyRatio.add(tempArray.getString(i));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public double getMaxDepositMoney() {
        return MaxDepositMoney;
    }

    public double getMinDepositMoney() {
        return MinDepositMoney;
    }

    public ArrayList<String> getPaymentChannel(){
        return paymentChannel;
    }

    public ArrayList<String> getMoneyRatio(){
        return moneyRatio;
    }
}
