package com.example.areademo;

import java.util.ArrayList;
import java.util.List;

import com.daohelper.db.apis.IArea;
import com.daohelper.db.entry.Area;
import com.widget.wheelview.WheelListAdapter;
import com.widget.wheelview.WheelView;
import com.widget.wheelview.WheelView.OnWheelChangedListener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;


public class MainActivity extends Activity {
    protected static final String TAG = "MainActivity";
    WheelView mWheelView;
    IArea mAreaDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initContentView();
        findViewById(R.id.load_btn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        showPiker();
			}
		});
    }

    private void showPiker() {
    	AreaPicker picker = new AreaPicker(this, new AreaPicker.Callback() {
			
			@Override
			public void onPickArea(Area provice, Area city, final String selectedValue) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(MainActivity.this, selectedValue, Toast.LENGTH_LONG).show();
					}
				});
			}
		});
    	picker.show();
	}

    private void initContentView() {
        
        mWheelView = (WheelView) findViewById(R.id.wheel_view);
        mWheelView.setVisibility(View.VISIBLE);
        mWheelView.setVisibleItems(7);
        
    	mAreaDao = Dao.getAreaDao(this);
        showProvices();
        mWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldId, int newId, String currentText) {
                Area prov = mAreaDao.getArea(currentText);
                Log.i(TAG, "onChanged: oldId= " + oldId + ", newId= " + newId + ",currentText=" + currentText);
            }
        });
	}

    private void showProvices() {
        List<Area> provices = mAreaDao.getProvices();
        if(provices != null) {
            List<String> provData = new ArrayList<String>();
            if(provices != null) {
                for (Area pro : provices) {
                    provData.add(pro.getName());
                }
            }
            WheelListAdapter<String> provAdapter = new WheelListAdapter<String>(provData);
            mWheelView.setAdapter(provAdapter);
            mWheelView.setVisibleItems(7);
            int selProvId = (int) provices.get(0).getId();
            showCity(selProvId);
        }
    }

    private void showCity(int proviceId) {
        List<Area> cities = mAreaDao.getCities(proviceId);
        if(cities != null) {
            List<String> cityData = new ArrayList<String>();
            for (Area area : cities) {
                cityData.add(area.getName());
            }
            int city = (int) cities.get(0).getId();
            showDistricts(city);
        }
    }

    private void showDistricts(int cityId) {
        List<Area> districts = mAreaDao.getDistrcts(cityId);
        if(districts != null) {
            List<String> distData = new ArrayList<String>();
            for (Area area : districts) {
                distData.add(area.getName());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
