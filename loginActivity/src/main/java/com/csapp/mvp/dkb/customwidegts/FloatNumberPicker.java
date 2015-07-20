package com.csapp.mvp.dkb.customwidegts;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.csapp.mvp.dkb.R;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public class FloatNumberPicker extends LinearLayout {
	
    private double quantity;
    public EditText number;
    private Button minusButton;
    private Button plusButton;
    @NotNull
    private BigDecimal step = new BigDecimal("0.2");
    private double max = 99999, min = 0;

    public interface onNumberChangeListener{
		void onNumberChanged();
	}

    public void setStep(double step){
        this.step = new BigDecimal(step);
    }

    @Nullable
    private onNumberChangeListener mOnNumberChangeListener = null;

	public void setOnNumberChangeListener(
			onNumberChangeListener mOnNumberChangeListener) {
		this.mOnNumberChangeListener = mOnNumberChangeListener;
	}
    
    public FloatNumberPicker(@NotNull Context context, AttributeSet attrs) {
        super(context, attrs);
 
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

				BigDecimal quantityDecimal = new BigDecimal(String.valueOf(quantity));
				quantityDecimal = quantityDecimal.subtract(step);
                if (quantityDecimal.doubleValue() < min) return;
				quantity = quantityDecimal.doubleValue();
				changeText();
				onNumberChanged();
			}	
        	
        });

        plusButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				BigDecimal quantityDecimal = new BigDecimal(String.valueOf(quantity));
				quantityDecimal = quantityDecimal.add(step);
                if (quantityDecimal.doubleValue() > max) return;
				quantity = quantityDecimal.doubleValue();
				changeText();
				onNumberChanged();
			}	
        	
        });
        
        number.addTextChangedListener(new TextWatcher()
        {
        	boolean isBlocked = false;
                public void  onTextChanged  ( @NotNull CharSequence s, int start, int before,
                        int count) 
                { 
                	if (!isBlocked){
                			try {
                                double value = Double.parseDouble(s.toString());
                                if (value >= min && value <= max){
                                    quantity = value;
                                }
                                onNumberChanged();
                			}
                			catch(NumberFormatException ex) {
                				//exception
                			}

                	  }
                }

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub

				}

				@Override
				public void beforeTextChanged( @NotNull CharSequence s, int arg1,
						int arg2, int arg3) {
					// TODO Auto-generated method stub
					isBlocked = s.length() == 0;
				}
        });

        
        
    }

	public double getQuantity() {
		return quantity;
	}
	
	public void onNumberChanged(){
//		System.out.println("number changed");
		if (this.mOnNumberChangeListener != null)
			this.mOnNumberChangeListener.onNumberChanged();
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
//		System.out.println("quantity: " + quantity);
		changeText();
	}
	
	public void changeText()
	{
		number.setText(Double.toString(quantity));
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