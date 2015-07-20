package com.csapp.mvp.dkb.details;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * This class implements the main functionalities of the TableAdapter in
 * Mutuactivos.
 * 
 * 
 * @author Brais Gab�n
 */
public abstract class SampleTableAdapter extends BaseTableAdapter {
    @NotNull
    private final Context context;
    @NotNull
    protected final LayoutInflater inflater;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The current context.
	 */
	public SampleTableAdapter( @NotNull Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	/**
	 * Returns the context associated with this array adapter. The context is
	 * used to create views from the resource passed to the constructor.
	 * 
	 * @return The Context associated with this adapter.
	 */
    @NotNull
    public Context getContext() {
		return context;
	}

	/**
	 * Quick access to the LayoutInflater instance that this Adapter retreived
	 * from its Context.
	 * 
	 * @return The shared LayoutInflater.
	 */
    @NotNull
    public LayoutInflater getInflater() {
		return inflater;
	}

	@Nullable
    @Override
	public View getView(final int row, int column, View converView, ViewGroup parent) {
//		if (converView == null) {
//			converView = inflater.inflate(getLayoutResource(row, column), parent, false);
//		}
//		setText(converView, getCellString(row, column));
//		converView.setOnClickListener(new OnClickListener() {
//
//		        @Override
//		        public void onClick(View v) {
//		            Toast.makeText(context, "cell in row " + row, Toast.LENGTH_SHORT).show();
//		        }
//		});
		return converView;
	}

	/**
	 * Sets the text to the view.
	 * 
	 * @param view
	 * @param text
	 */
	protected void setText( @NotNull View view, String text) {
		((TextView) view.findViewById(android.R.id.text1)).setGravity(Gravity.CENTER);
		((TextView) view.findViewById(android.R.id.text1)).setText(text);
	}

    protected void setTextColor( @NotNull View view, int color) {
		((TextView) view.findViewById(android.R.id.text1)).setTextColor(color);
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	/**
	 * @param row
	 *            the title of the row of this header. If the column is -1
	 *            returns the title of the row header.
	 * @param column
	 *            the title of the column of this header. If the column is -1
	 *            returns the title of the column header.
	 * @return the string for the cell [row, column]
	 */
	public abstract String getCellString(int row, int column);

	public abstract int getLayoutResource(int row, int column);
}
