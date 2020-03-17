
package com.common.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.common.utils.R;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class SaveViewImageActivity extends Activity {

    private ImageView m_ivMain;
    private Button m_btnSave;
    private TextView m_tvPath;
    private EditText m_etFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_view_image);

        String strPath = getSDPath();
        m_ivMain = (ImageView) findViewById(R.id.ID_ivMain);
        m_ivMain.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        m_btnSave = (Button) findViewById(R.id.ID_btnSave);
        m_tvPath = (TextView) findViewById(R.id.ID_tvPath);
        m_etFileName = (EditText) findViewById(R.id.ID_etFileName);

        m_tvPath.setText(strPath);

        m_btnSave.setOnClickListener(new Button.OnClickListener() {// 创建监听
                    public void onClick(View v)
                    {
                        String strFileName = m_etFileName.getText().toString().trim();
                        saveImage(strFileName);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public String getSDPath()
    {
        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSDCard)
        {
            return Environment.getExternalStorageDirectory().toString() + "/pics/";
        }
        else
            return getFilesDir() + "/pics/";
    }

    public static Bitmap convertViewToBitmap(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

    // first SDCard is in the device, if yes, the pic will be stored in the
    // SDCard, folder "HaHa_Picture"
    // second if SDCard not exist, the picture will be stored in
    // /data/data/HaHa_Picture
    // file will be named by the customer
    public void saveImage(String strFileName)
    {
        Bitmap bitmap = convertViewToBitmap(m_ivMain);
        String strPath = getSDPath();

        try
        {
            File destDir = new File(strPath);
            if (!destDir.exists())
            {
                Log.d("MagicMirror", "Dir not exist create it " + strPath);
                destDir.mkdirs();
                Log.d("MagicMirror", "Make dir success: " + strPath);
            }
            if (TextUtils.isEmpty(strFileName)) {
                strFileName = "view_pic.jpg";
            }

            File imageFile = new File(strPath + "/" + strFileName);
            imageFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(CompressFormat.JPEG, 50, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
