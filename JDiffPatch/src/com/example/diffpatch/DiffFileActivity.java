package com.example.diffpatch;

import java.io.FileNotFoundException;
import java.util.List;

import com.difflib.DiffDataUtils;
import com.difflib.DiffUtils;
import com.difflib.Patch;
import com.difflib.PatchFailedException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DiffFileActivity extends Activity {
    private static String DIFF_DIR_NAME = "difffiles";
    private static String FILE_ORINGINAL = "Gemfile";
    private static String FILE_Gemfile2 = "Gemfile2";
    private static String FILE_DIFF_PATCH = "update.patch";

    private String diffDir;
    private String oringinalPath;
    private String revisedPath;
    private String update_patch_path;

    List<String> oringinalList = null;
    List<String> revisedList = null;
    List<String> List = null;
    List<String> updatedList = null;
    
    List<String> patchList = null;
    Patch mPatch = null;
    
    TextView oringinal_file_tv,diff_file_tv,revised_file_tv;
    Button apply_patch_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_file);
        oringinal_file_tv = (TextView) findViewById(R.id.oringinal_file_tv);
        diff_file_tv = (TextView) findViewById(R.id.diff_file_tv);
        revised_file_tv = (TextView) findViewById(R.id.revised_file_tv);
        apply_patch_btn = (Button) findViewById(R.id.apply_patch_btn);
        copyassetsFiles();
        readFiles();
        apply_patch_btn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applyDiff();
            }
        });
    }
    
    private void applyDiff() {
        mPatch = DiffUtils.parseUnifiedDiff(patchList);
        try {
            updatedList = DiffUtils.patchText(oringinalList, mPatch);
        } catch (PatchFailedException e) {
            e.printStackTrace();
        }
        StringBuffer reaultText = new StringBuffer();
        if (updatedList != null) {
            if(updatedList.equals(revisedList)) {
                reaultText.append("Diff patch apply SUCCESS !!, revised file: \n");
                DiffDataUtils.writeToFile(updatedList, oringinalPath);
            } else {
                reaultText.append("Diff patch apply FAILED !! \n");
            }
        }
        try {
            reaultText.append(DiffDataUtils.readFileText(oringinalPath));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        revised_file_tv.setText(reaultText);
    }

    private void readFiles() {
        try {
            oringinalList = DiffDataUtils.readFileLines(oringinalPath);
            revisedList = DiffDataUtils.readFileLines(revisedPath);
            //patchList = DiffDataUtils.readFileLines(update_patch_path);
            String patchText = DiffDataUtils.readFileText(update_patch_path);
            byte[] data = patchText.getBytes();
            String patchText2 = DiffDataUtils.decodeData(data);
            patchList = DiffDataUtils.decodeDataLines(data);
            oringinal_file_tv.setText("Original Gemfile:\n" + DiffDataUtils.readFileText(oringinalPath));
            diff_file_tv.setText("updated patch:\n" + patchText2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyassetsFiles() {
        diffDir = getDir(DIFF_DIR_NAME, MODE_PRIVATE).toString();
        oringinalPath = DiffDataUtils.copyAssetsFileToDir(getApplicationContext(), FILE_ORINGINAL, diffDir);
        revisedPath = DiffDataUtils.copyAssetsFileToDir(getApplicationContext(), FILE_Gemfile2, diffDir);
        update_patch_path = DiffDataUtils.copyAssetsFileToDir(getApplicationContext(), FILE_DIFF_PATCH, diffDir);
    }
}