package com.common.ui;

import java.io.File;

import com.common.utils.CommonFileUtil;
import com.common.utils.R;
import com.image.utils.ImagePicker.CallBack;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class ImagePickActivity extends  Activity implements CallBack {
	
	ImagePickDialog mImagePickDialog;
	ImageView original_img, compressed_img;
	TextView original_info_tv, compressed_info_tv;
	EditText compress_size_et, compress_persentage_et;
	Button compress_btn;

	int compWidth, compHeight;
	int compSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_compress);
        mImagePickDialog = new ImagePickDialog(this, this);
        original_img = (ImageView) findViewById(R.id.original_img);
        compressed_img = (ImageView) findViewById(R.id.compressed_img);
        original_info_tv = (TextView) findViewById(R.id.original_info_tv);
        compressed_info_tv = (TextView) findViewById(R.id.compressed_info_tv);
        compress_size_et = (EditText) findViewById(R.id.compress_size_et);
        compress_persentage_et = (EditText) findViewById(R.id.compress_persentage_et);
        compress_btn = (Button) findViewById(R.id.compress_btn);
        original_img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	mImagePickDialog.showMenuWindow();
            }
        });
        compress_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	//to compress image and show
            }
        });
    }

    private void initCompInfo() {
    	String sizeText = compress_size_et.getText().toString();
    	if (!TextUtils.isEmpty(sizeText)) {
    		try {
    			String[] sizes = sizeText.split("x");
        		compWidth = Integer.valueOf(sizes[0]);
        		compHeight = Integer.valueOf(sizes[1]);
        		compressed_info_tv.setTextColor(Color.BLACK);
			} catch (Exception e) {
				e.printStackTrace();
				compressed_info_tv.setText(e.getMessage());
				compressed_info_tv.setTextColor(Color.RED);
			}
		}
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	mImagePickDialog.onActivityResult(requestCode, resultCode, data);
    	super.onActivityResult(requestCode, resultCode, data);
    }

	@Override
	public void onReturnBitmap(boolean isSuccess, Bitmap bitmap, Uri uri, String filePath) {
		original_img.setImageBitmap(bitmap);
		setImageInfo(original_info_tv, bitmap, filePath);
	}
	
	private void setImageInfo(TextView infoTv, Bitmap bitmap, String filePath) {
		if(bitmap == null) return;
		File file = new File(filePath);
		int fileSize = CommonFileUtil.getFileSize(file);
		compWidth = bitmap.getWidth();
		compHeight = bitmap.getHeight();
		compSize = fileSize;
		infoTv.setText(fileSize / 1024 + "kb, " + bitmap.getWidth() + " x " + bitmap.getHeight());
	}

	
}
