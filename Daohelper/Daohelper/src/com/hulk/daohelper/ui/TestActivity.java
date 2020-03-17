package com.hulk.daohelper.ui;

import com.daohelper.db.entry.College;
import com.hulk.daohelper.R;
import com.hulk.daohelper.TestData;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TestActivity extends Activity {

	TextView mInfoTv;
	Button action_btn;
	TestData mTestData;
	StringBuffer info = new StringBuffer();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		mTestData = new TestData(getApplicationContext());
		mInfoTv = (TextView) findViewById(R.id.info_tv);
		action_btn = (Button) findViewById(R.id.action_btn);
		//mTestData.insertBatchColleges();
		mTestData.addCollege(12);
		action_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        info.delete(0, info.length() - 1);
                        showTextInfo();
                    }
                }).start();
            }
        });
		//insert college data and show
		showTextInfo();
	}

	private void showTextInfo() {
	    String collegeId = "100";
	    info.append(" COLLEGE Id:\t " + collegeId);
        College col = mTestData.getCollegeInfo(collegeId);
        if(col != null) {
            info.append("\n" + col.getCollegeId() + " \t\t " + col.getCollegeName());
        }
        String proviceName = "河北省";
        String areaInfo = mTestData.getAreaInfo(proviceName);//beijing 
        info.append("\n\n AREA Info:" + proviceName).append("\n" + areaInfo);
        runOnUiThread(new Runnable() {
            public void run() {
                mInfoTv.setText(info);
            }
        });
    }
}
