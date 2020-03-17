package com.hulk.imageloader.ui;

import java.util.ArrayList;
import java.util.List;

import com.hulk.imageloader.ImageLoader;
import com.hulk.imageloader.R;
import com.hulk.imageloader.ui.ImageGridAdpater.ImageItemInfo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ImageView;


public class MainActivity extends Activity {

    private static final int NUM = 1000;
    ImageLoader mLoader;
    ImageView imageview;
    GridView gridview;
    private Bitmap placeHolderBitmap;
    
    ImageGridAdpater mAdapter;
    List<ImageGridAdpater.ImageItemInfo> dataSet;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageview = (ImageView) findViewById(R.id.img);
        gridview = (GridView) findViewById(R.id.gridview);
        
        mLoader = new ImageLoader();
        placeHolderBitmap = ImageLoader.getBitmap(this, R.drawable.loading_img);
        mLoader.loadResBitmap(imageview, R.drawable.img, placeHolderBitmap);
        loadResImgs();
    }

    private void loadResImgs() {
        mAdapter = new ImageGridAdpater(this);
        gridview.setAdapter(mAdapter);
        dataSet = new ArrayList<ImageGridAdpater.ImageItemInfo>();
        for (int i = 0; i < NUM; i++) {
            ImageItemInfo item = new ImageItemInfo();
            item.resId = R.drawable.ic_launcher;
            dataSet.add(item);
        }
        mAdapter.updateDataSet(dataSet);
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
