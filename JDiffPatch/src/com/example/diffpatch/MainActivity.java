
package com.example.diffpatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

    private static final String TAG = "Diffpatch";
    Button diff_common_text_btn, diff_file_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        diff_common_text_btn = (Button) findViewById(R.id.diff_common_text_btn);
        diff_file_btn = (Button) findViewById(R.id.diff_file_text_btn);
        
        diff_common_text_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DiffTextActivity.class));
            }
        });
        diff_file_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DiffFileActivity.class));
            }
        });
    }
}
