package com.hulk.imageloader.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * adapter is abstract, contains all common functions of BaseAdapter and other
 * ourselves. but the subclass must define getView(), in order to achieve
 * specific UI requirement
 * 
 * @author Hulk
 * 
 * @param <T>
 *            Item data type
 */
public abstract class AbsAdapter<T> extends BaseAdapter {

	protected Context mContext = null;
	protected List<T> mList = null;
	protected LayoutInflater mInflater;
	protected int mHeaderCount = 0;
	protected boolean mArrowVisible = true, mClickable = true;

	public AbsAdapter(Context context) {
		this(context, new ArrayList<T>());
	}

	public AbsAdapter(Context context, List<T> list) {
		this(context, list, true, true);
	}

	public AbsAdapter(Context context, boolean arrowVisible, boolean clickable) {
		this(context, null, arrowVisible, clickable);
	}

	public AbsAdapter(Context context, List<T> list, boolean arrowVisible,
			boolean clickable) {
		mContext = context;
		mList = list;
		mInflater = LayoutInflater.from(getContext());
		mArrowVisible = arrowVisible;
		mClickable = clickable;
	}

	public List<T> getDataSet() {
		return mList;
	}
	
	public void add(T data) {
		if (data != null) {
			mList.add(data);
			notifyDataSetChanged();
		}
	}

	public void update(T data, int index) {
		if (index < 0 || index > getCount() - 1 || data == null)
			return;
		mList.set(index, data);
		notifyDataSetChanged();
	}

	public void delete(int index) {
		if (index < 0 || index > getCount() - 1)
			return;
		mList.remove(index);
		notifyDataSetChanged();
	}

	public void setDataSet(List<T> list) {
		mList = list;
	}

	public void updateDataSet(List<T> list) {
		setDataSet(list);
		notifyDataSetChanged();
	}
	
	public void updateDataSet(T[] array) {
		List<T> list = new ArrayList<T>();
		if(array != null) {
			for (T t : array) {
				list.add(t);
			}
		}
		mList = list;
		setDataSet(list);
		notifyDataSetChanged();
	}

	public void addData(List<T> list) {
		if (list == null)
			return;
		mList.addAll(list);
		notifyDataSetChanged();
	}

	public int getCount() {
		if (mList == null)
			return 0;
		return mList.size();

	}

	public int getHeaderCount() {
		return mHeaderCount;
	}

	public void setHeaderCount(int headerCount) {
		this.mHeaderCount = headerCount;
	}

	public T getItem(int position) {
		return mList == null || position < 0 || mList.isEmpty() ? null : mList
				.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	/**
	 * @return the mContext
	 */
	public Context getContext() {
		return mContext;
	}

	public boolean isArrowVisible() {
		return mArrowVisible;
	}

	public void setArrowVisible(boolean mArrowVisible) {
		this.mArrowVisible = mArrowVisible;
	}

	 @Override 
	 public boolean isEnabled(int position) {
		 return mClickable; 
		 }

	public boolean isClickable() {
		return mClickable;
	}

	public void setClickable(boolean clickable) {
		this.mClickable = clickable;
	}

	public void clear() {
		if (mList != null) {
			mList.clear();
			notifyDataSetChanged();
		}
	}
	
	public String getString(int resId) {
		if(mContext != null) {
			return mContext.getString(resId);
		}
		return null;
	}

	public int getColor(int resId) {
		if(mContext != null) {
			return mContext.getResources().getColor(resId);
		}
		return 0;
	}
	
	public String getString(int resId, Object...formatArgs) {
		if(mContext != null) {
			return mContext.getString(resId, formatArgs);
		}
		return null;
	}
	
	protected View getLayoutView(int layoutId) {
		return LayoutInflater.from(mContext).inflate(layoutId, null);
	}

	abstract public View getView(int position, View view, ViewGroup parent);
}
