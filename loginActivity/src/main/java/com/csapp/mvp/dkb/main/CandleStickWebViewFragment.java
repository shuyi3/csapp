package com.csapp.mvp.dkb.main;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.StockData;
import com.csapp.mvp.dkb.data.Util;

/**
 * Created by shuyi on 15/3/10.
 */
public class CandleStickWebViewFragment extends Fragment {
    private String mURL;
    boolean loadingFinished = true;
    private float xInPx, yInPx;
    private String title, instrumentId;

    private WebView mWebView;
    private boolean mIsWebViewAvailable;
    private ProgressBar mProgressBar;

    public CandleStickWebViewFragment() {
    }

    public static CandleStickWebViewFragment initWithInsInfo(String instrumentId) {
        CandleStickWebViewFragment f = new CandleStickWebViewFragment();
        f.instrumentId = instrumentId;
        f.title = Util.convertStockCode(instrumentId);
        return f;
    }

    private static String filterInstrumentId(String instrumentId){
        if (DataHolder.getInstance().getInsInfoMap().get(instrumentId).getExchangeName().equals("郑州")) {
            return instrumentId.replaceAll("(\\d+)", "1$1");
        }
        return instrumentId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                    Bundle savedInstanceState){

        View v = inflater.inflate(R.layout.fragment_webview, container, false);

        mWebView = (WebView) v.findViewById(R.id.webView);
        mIsWebViewAvailable = true;

        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        if (Build.VERSION.SDK_INT >= 11){
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "Android");
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                loadingFinished = false;
                showProgress();
                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
                Log.i("webview", "start");
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);
                Log.i("webview", "finish");
                loadingFinished = true;
                int x = mWebView.getWidth();
                int y = mWebView.getHeight();

                Display display = getActivity().getWindowManager().getDefaultDisplay();
                DisplayMetrics outMetrics = new DisplayMetrics ();
                display.getMetrics(outMetrics);

                float density  = getResources().getDisplayMetrics().density;
                xInPx = x / density;
                yInPx  = y / density;
                getWebView().loadUrl("javascript:initialTable(" + xInPx + "," + yInPx + "," + "'" + title + "'" + "," + "'" + filterInstrumentId(instrumentId) + "')");
            }
        });

        mWebView.loadUrl("file:///android_asset/stock/examples/candlestick-and-volume/index.htm");

        return v;

    }

    public void loadWithInsInfo(String instrumentId){
        if (loadingFinished && !instrumentId.equals(this.instrumentId)){
            this.instrumentId = instrumentId;
            this.title = Util.convertStockCode(instrumentId);
            getWebView().loadUrl("javascript:initialTable(" + xInPx + "," + yInPx + "," + "'" + title + "'" + "," + "'" + filterInstrumentId(instrumentId) + "')");
        }
    }

    public void loadLastTick(){
        StockData lastData = null;
        lastData = DataHolder.getInstance().getInsInfoMap().get(instrumentId).getLastData();
        if (lastData != null) {
//            Log.d("webview", instrumentId + ": last data != null");
            int x = 1; //fake x
            float y = (float) lastData.getLastPrice();
            getWebView().loadUrl("javascript:updateLastData(" + x + "," + y + ")");
        }else{
//            Log.d("webview", instrumentId + ": last data == null");
        }
    }

    public void loadUrl(String url){
        getWebView().loadUrl(url);
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }
    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    @Override
    public void onResume() {
        mWebView.onResume();
        super.onResume();
    }
    /**
     * Called when the WebView has been detached from the fragment.
     * The WebView is no longer available after this time.
     */
    @Override
    public void onDestroyView() {
        mIsWebViewAvailable = false;
        super.onDestroyView();
    }
    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }
    /**
     * Gets the WebView.
     */
    public WebView getWebView() {
        return mIsWebViewAvailable ? mWebView : null;
    }

    public void showProgress(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress(){
        mProgressBar.setVisibility(View.GONE);
    }

    public class JavaScriptInterface {

        public JavaScriptInterface(){}

        @JavascriptInterface
        public void onStartLoading(){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgress();
                }
            });
        }

        @JavascriptInterface
        public void onLoadingFinished(){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgress();
                }
            });
        }
    }
}
