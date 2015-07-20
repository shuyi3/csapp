package com.csapp.mvp.dkb.customwidegts;

import com.csapp.mvp.dkb.R;
import com.csapp.mvp.dkb.customwidegts.FloatNumberPicker.onNumberChangeListener;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntegerNumberPicker extends LinearLayout {
	
    private int quantity;
    private EditText number;
    private Button minusButton;
    private Button plusButton;
    private int max = 99999, min = 1;

	@Nullable
    private onNumberChangeListener mOnNumberChangeListener = null;

	public void setOnNumberChangeListener(
			onNumberChangeListener mOnNumberChangeListener) {
		this.mOnNumberChangeListener = mOnNumberChangeListener;
	}
    
    public IntegerNumberPicker( @NotNull Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.setClickable(true);
        
        LayoutInflater layoutInflater = (LayoutInflater)context
                  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.number_picker, this);
        
        minusButton = (Button) findViewById(R.id.btn_minus);
        plusButton = (Button) findViewById(R.id.btn_plus);
        number = (EditText) findViewById(R.id.quantity_text);
        
        number.setFocusableInTouchMode(true);
        number.clearFocus();

        minusButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
                if (quantity - 1 < min) return;
				quantity--;
				changeText();
				onNumberChanged();
			}	
        	
        });

        plusButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
                if (quantity + 1 > max) return;
				quantity++;
				changeText();
				onNumberChanged();
			}	
        	
        });
        
        number.addTextChangedListener(new TextWatcher()
        {
             
                public void  onTextChanged  ( @NotNull CharSequence s, int start, int before,
                        int count) 
                { 
                	
                		try { 
                			int value = Integer.parseInt(s.toString());
                            if (value >= min && value <= max){
                                quantity = value;
                            }
                    		onNumberChanged();
                	    } catch(NumberFormatException e) { 
                	    	//
                	    }
                }

				@Override
				public void afterTextChanged(Editable arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {
					// TODO Auto-generated method stub
					
				}
        });

       
    }

	public int getQuantity() {
		return quantity;
	}
	
	public void onNumberChanged(){
		if (this.mOnNumberChangeListener != null)
			this.mOnNumberChangeListener.onNumberChanged();
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
		changeText();
	}
	
	public void changeText()
	{
		number.setText(Integer.toString(quantity));
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		plusButton.setEnabled(enabled);
		minusButton.setEnabled(enabled);
		number.setEnabled(enabled);
		number.setText("");
		super.setEnabled(enabled);
	}
}