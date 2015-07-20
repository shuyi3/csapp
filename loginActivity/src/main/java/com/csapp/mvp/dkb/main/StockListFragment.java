package com.csapp.mvp.dkb.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cengalabs.flatui.views.FlatButton;
import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.buy.TransactionFragment;
import com.csapp.mvp.dkb.data.DataHolder;
import com.csapp.mvp.dkb.data.StockData;
import com.csapp.mvp.dkb.data.Util;

import org.jetbrains.annotations.NotNull;


public class StockListFragment extends Fragment {

    public ListView stockListView;
    public CustomAdapter adapter;
    private Activity mActivity;
    private RelativeLayout progressBar;

    public StockListFragment() {

    }

    @SuppressLint("ValidFragment")
    public StockListFragment(Activity activity) {
        mActivity = activity;
    }

    @Override
    public View onCreateView( @NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.stock_list, container, false);
        super.onCreate(savedInstanceState);

        stockListView = (ListView) v.findViewById(R.id.listView1);
        progressBar = (RelativeLayout) v.findViewById(R.id.progress);

        stockListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        adapter = new CustomAdapter(mActivity, stockListView);
        stockListView.setAdapter(adapter);

        return v;
    }

    public View getProgressBar() {
        return progressBar;
    }

    public ListView getStockListView() {
        return stockListView;
    }

    public class CustomAdapter extends BaseAdapter {

        MainActivity sActivity;
        private ListView parentView;

        public CustomAdapter(final Activity mActivity, ListView parentView) {
            this.sActivity = (MainActivity) mActivity;
            this.parentView = parentView;
        }

        @Override
        public int getCount() {
            /*
			 * Length of our listView
			 */
            int count = DataHolder.getInstance().getInsNameList().size();
            return count;
        }

        @Override
        public Object getItem(int position) {

			/*
			 * Current Item
			 */
            return position;
        }

        @Override
        public long getItemId(int position) {
			/*
			 * Current Item's ID
			 */
            return position;
        }

        public void updateRowStats() {
            for (int i = 0; i < getCount(); i++) {
                View view = parentView.getChildAt(i);
                if (view != null) {
                    final Button priceButton = (Button) view.findViewById(R.id.price);
                    final FlatButton mButton = (FlatButton) view.findViewById(R.id.txbutton);
                    if (mButton.getTag() == null) {
//                        Log.d("INS","no tag");
                        continue;
                    }
//                    Log.d("INS", "has tag");
                    int pos = (Integer) mButton.getTag();
                    String instrumentId = DataHolder.getInstance().getInsNameList()
                            .get(pos);

                    StockData lastData = DataHolder.getInstance().getInsInfoMap()
                            .get(instrumentId).getLastData();

                    if (lastData == null) {
                        priceButton.setText("--");
                    } else {
                        priceButton.setText(String.valueOf(lastData.getLastPrice()));

//                        System.out.println(instrumentId + " last: " + lastData.getLastPrice() + "settle: " + lastData.getPreSettlementPrice());

                        if (lastData.getLastPrice() - lastData.getPreSettlementPrice() < 0
                                && lastData.getPreSettlementPrice() != -1) {
                            int sdk = android.os.Build.VERSION.SDK_INT;
                            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                priceButton.setBackgroundDrawable(getResources()
                                        .getDrawable(R.drawable.rect_normal_green));
                            } else {
                                priceButton.setBackground(getResources().getDrawable(
                                        R.drawable.rect_normal_green));
                            }
                        }
                    }
                }
            }
        }

        @SuppressWarnings("deprecation")
        @SuppressLint("NewApi")
        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {

//            Log.d("INS","getView called pos = " + position);

//            View mView = convertView;
            if (convertView == null) {
				/*
				 * LayoutInflater
				 */
                final LayoutInflater sInflater = (LayoutInflater) sActivity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				/*
				 * Inflate Custom List View
				 */
                convertView = sInflater
                        .inflate(R.layout.stock_list_item, null, false);

            }

            String instrumentId = DataHolder.getInstance().getInsNameList()
                    .get(position);

            double margin = DataHolder.getInstance().getInsInfoMap()
                    .get(instrumentId).getMargin();


            final TextView sTV1 = (TextView) convertView.findViewById(R.id.textView1);
            final Button priceButton = (Button) convertView.findViewById(R.id.price);
            final Button marginButton = (Button) convertView.findViewById(R.id.margin);
            final FlatButton mButton = (FlatButton) convertView.findViewById(R.id.txbutton);
            mButton.setTag(position);

            sTV1.setSelected(true);

            if (margin > 1) {
                marginButton.setText(String.valueOf(margin));
            }else{
                marginButton.setVisibility(View.GONE);
            }

            StockData lastData = DataHolder.getInstance().getInsInfoMap()
                    .get(instrumentId).getLastData();
            if (lastData == null) {
                priceButton.setText("--");
            } else {
                priceButton.setText(String.valueOf(lastData.getLastPrice()));

                if (lastData.getLastPrice() - lastData.getPreSettlementPrice() < 0
                        && lastData.getPreSettlementPrice() != -1) {
                    int sdk = android.os.Build.VERSION.SDK_INT;
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        priceButton.setBackgroundDrawable(getResources()
                                .getDrawable(R.drawable.rect_normal_green));
                    } else {
                        priceButton.setBackground(getResources().getDrawable(
                                R.drawable.rect_normal_green));
                    }
                }
            }


            if (mButton.getListener() == null) {
                mButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub

                        if (((MainActivity) mActivity).getCurrentFragment().equals(Util.TAG_TX_FRAGMENT)) {
                            return;
                        }

                        int pos = (Integer) mButton.getTag();
//                        Log.d("INS", "listener null, pos = " + pos);
                        Log.d("INS", "click pos = " + pos);

                        stockListView.setSoundEffectsEnabled(false);
                        stockListView.performItemClick(stockListView.getAdapter()
                                        .getView(pos, null, null), pos,
                                stockListView.getAdapter().getItemId(pos));
                        stockListView.setSoundEffectsEnabled(true);

                        FragmentTransaction ft = getFragmentManager()
                                .beginTransaction();
//                        ft.setCustomAnimations(R.anim.fragment_slide_bot_enter,
//                                R.anim.fragment_slide_bot_exit,
//                                R.anim.fragment_slide_top_enter,
//                                R.anim.fragment_slide_top_exit);
                        ft.add(R.id.frag_holder, TransactionFragment.newInstance(DataHolder
                                        .getInstance().getInsNameList().get(pos),
                                mActivity), Util.TAG_TX_FRAGMENT);

                        ft.addToBackStack(null);
                        ft.commit();
                        ((MainActivity) mActivity)
                                .addFragmentToStack(Util.TAG_TX_FRAGMENT);

                    }

                });
            }else{
//                Log.d("INS", "listener not null");
            }

            String convertedName = Util.convertStockCode(instrumentId);

            sTV1.setText(convertedName);
            int pos = (Integer) mButton.getTag();
            Log.d("INS", "pos = " + pos + " position = " + position);

            if (((MainActivity) mActivity).getCurrentInstrument() == pos) {
                convertView.setBackgroundColor(getResources().getColor(
                        R.color.list_row_selected_bg));
            }
            else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            return convertView;
        }


    }

    //应该改为点对点update
    public void tickUpdate(){
        adapter.updateRowStats();
    }

    public void refreshContent(){
        adapter.updateRowStats();
    }

}