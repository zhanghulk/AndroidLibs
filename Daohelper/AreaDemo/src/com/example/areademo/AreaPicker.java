package com.example.areademo;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.daohelper.db.apis.IArea;
import com.daohelper.db.entry.Area;
import com.widget.wheelview.WheelListAdapter;
import com.widget.wheelview.WheelView;
import com.widget.wheelview.WheelView.OnWheelChangedListener;

public class AreaPicker implements OnWheelChangedListener {

    public enum WheelWhich {
        WHEEL_PROV, WHEEL_CITY, WHEEL_DIST
    }
	public interface Callback {
		void onPickArea(Area provice, Area city, String selectedValue);
	}
	private static final int VISIBLE_ITEMS = 7;
	private static final String TAG = "AreaPicker";
	private static final boolean DBG = true;
	IArea mAreaDao;
	Context mContext;
	List<Area> proviceList = null;
	List<String> provData = null;
	List<Area> cityList = null;
	List<String> cityData = null;
	//selected provice and city
	private Area selProvice = null;
	private Area selCity = null;
	
	AlertDialog.Builder builder;
	TextView mTitleTv;
	WheelView mLeftWheel, mRightWheel;
	WheelListAdapter<String> provAdapter;
	int visibleItems = VISIBLE_ITEMS;
	
	Callback mCallback;
	
	
	public AreaPicker(Context context, Callback callback) {
		mAreaDao = Dao.getAreaDao(context.getApplicationContext());
		mContext = context;
		mCallback = callback;
		initProv();
		initView();
	}
	
	public void initView() {
		builder = new AlertDialog.Builder(mContext);
		View view = LayoutInflater.from(mContext).inflate(R.layout.pick_area_dialog, null);
		mTitleTv = (TextView) view.findViewById(R.id.wheel_title_tv);
		mLeftWheel = (WheelView) view.findViewById(R.id.left_wheel);
		mRightWheel = (WheelView) view.findViewById(R.id.right_wheel);
		mLeftWheel.setVisibleItems(visibleItems);
		mRightWheel.setVisibleItems(visibleItems);
		mLeftWheel.addChangingListener(this);
		mRightWheel.addChangingListener(this);
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(mCallback != null) {
					mCallback.onPickArea(selProvice, selCity, getFormatText());
				}
				dialog.dismiss();
			}
		});
		builder.setView(view);
		provAdapter = new WheelListAdapter<String>(provData);
        mLeftWheel.setAdapter(provAdapter);
        //set default value
        selProvice = proviceList.get(0);
        refreshCity(selProvice.getId());
        updateTitletext();
	}

	public int getVisibleItems() {
        return visibleItems;
    }

    public void setVisibleItems(int visibleItems) {
        this.visibleItems = visibleItems;
    }

    public void show() {
		builder.create().show();
	}

	private void initProv() {
		proviceList = mAreaDao.getProvices();
		if(proviceList != null) {
            provData = new ArrayList<String>();
            if(proviceList != null) {
                for (Area pro : proviceList) {
                    provData.add(pro.getName());
                }
            }
        }
	}

    public Area refreshCity(long proviceId) {
    	cityList = mAreaDao.getCities(proviceId, true);
        if(cityList != null) {
            List<String> cityData = new ArrayList<String>();
            for (Area area : cityList) {
                cityData.add(area.getName());
            }
            
            WheelListAdapter<String> cityAdapter = new WheelListAdapter<String>(cityData);
            mRightWheel.setAdapter(cityAdapter);
            mRightWheel.setCurrentItem(0);
            selCity = cityList.get(0);
            updateTitletext();
            //showDistricts(city);
        }
        return selCity;
    }

    public boolean isMunicipality(int provinceId) {
		int[] muns = {1,2,9, 27}; 
		for (int i = 0; i < muns.length; i++) {
			if (provinceId == muns[i]) {
				return true;
			}
		}
		return false;
	}

    public Area refreshDistricts(int cityId) {
        List<Area> districts = mAreaDao.getDistrcts(cityId);
        if(districts != null) {
            List<String> distData = new ArrayList<String>();
            for (Area area : districts) {
                distData.add(area.getName());
            }
            //WheelListAdapter<String> distAdapter = new WheelListAdapter<String>(distData);
            //mLeftWheel.setRightAdapter(distAdapter);
            return districts.get(0);
        }
        return null;
    }

    @Override
    public void onChanged(WheelView wheelView, int oldId, int newId, String currentValue) {
        WheelWhich which = WheelWhich.WHEEL_PROV;
        switch (wheelView.getId()) {
            case R.id.left_wheel:
                which = WheelWhich.WHEEL_PROV;
                selProvice = proviceList.get(newId);
                refreshCity(selProvice.getId());
                break;
            case R.id.right_wheel:
                which = WheelWhich.WHEEL_CITY;
                selCity = cityList.get(newId);
                //showDistricts(selCity);
                break;
            default:
                break;
            }
       if(DBG)Log.i(TAG, "onChanged : WheelWhich=" + which + ", oldId=" + oldId + ",newId=" + newId
               + ",provValue=" + selProvice.getName() + ",cityValue=" + selCity.getName());
       updateTitletext();
    }

    public String getFormatText() {
        StringBuffer formatText = new StringBuffer();
        if(selProvice != null) {
            formatText.append(selProvice.getName());
        }
        if(selCity != null) {
            formatText.append("|").append(selCity.getName());
        }
        return formatText.toString();
    }

    private void updateTitletext() {
        mTitleTv.setText(getFormatText());
    }
}
