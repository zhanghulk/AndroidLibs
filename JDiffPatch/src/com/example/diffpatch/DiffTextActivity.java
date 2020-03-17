package com.example.diffpatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.difflib.Delta;
import com.difflib.DiffUtils;
import com.difflib.Patch;
import com.difflib.PatchFailedException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DiffTextActivity extends Activity {

    TextView list_info_tv, diff_info_tv, patch_info_tv;
    Button gnerate_diff_btn,apply_patch_btn;
    List<String> originalList = new ArrayList<String>();
    List<String> revisedList = new ArrayList<String>();
    
    List<String> targetList = new ArrayList<String>();

    
    Patch mPatch, mPatch2;
    List<String> mUnifiedDiff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff);
        list_info_tv = (TextView) findViewById(R.id.list_info_tv);
        diff_info_tv = (TextView) findViewById(R.id.diff_info_tv);
        patch_info_tv = (TextView) findViewById(R.id.patch_info_tv);
        
        gnerate_diff_btn = (Button) findViewById(R.id.gnerate_diff_btn);
        apply_patch_btn = (Button) findViewById(R.id.apply_patch_btn);
        
        
        initList();
        
        gnerate_diff_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                generateUnifiedDiff();
            }
        });
        
        apply_patch_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //applyPatch(mPatch);
                applyUnifiedDiff();
            }
        });
    }

    private void diff() {
        mPatch = DiffUtils.diff(originalList, revisedList);
        List<Delta> deltas = mPatch.getDeltas();
        StringBuffer buf = new StringBuffer();
        buf.append("diff patch Deltas\nOriginal:\n");
        for (Delta delta : deltas) {
            buf.append(delta.getOriginal().toString());
        }
        buf.append("\nRevised:\n");
        for (Delta delta : deltas) {
            buf.append(delta.getRevised().toString());
        }
        diff_info_tv.setText(buf.toString());
    }
    
    private void generateUnifiedDiff() {
        mPatch = DiffUtils.diff(originalList, revisedList);
        mUnifiedDiff = DiffUtils.generateUnifiedDiff("contact_permission.txt", "contact_permission.txt", originalList, mPatch, 100);
        StringBuffer buf = new StringBuffer("generateUnifiedDiff\n");
        buf.append("UnifiedDiff:" + Arrays.toString(mUnifiedDiff.toArray()));
        diff_info_tv.setText(buf.toString());
    }
    
    private void applyPatch(Patch patch) {
        StringBuffer buf = new StringBuffer("applyPatch:\n");
        if (patch != null) {
            try {
                //mPatch.applyTo(targetList);
                List<?> destList = DiffUtils.patch(targetList, patch);
                buf.append("destList:" + Arrays.toString(destList.toArray()));
            } catch (PatchFailedException e) {
                e.printStackTrace();
                buf.append(e.getMessage());
            }
        } else {
            buf.append("The patch object is null, please generate diff patch !! ");
        }
        patch_info_tv.setText(buf);
    }

    private void applyUnifiedDiff() {
        mPatch2 = DiffUtils.parseUnifiedDiff(mUnifiedDiff);
        applyPatch(mPatch2);
    }
    
    private void initList() {
        StringBuffer buf = new StringBuffer("initList:\n");
        originalList.clear();
        originalList.add("aaa");
        originalList.add("bbbb");
        originalList.add("cccc");
        originalList.add("dddd");
        originalList.add("eeeeee");
        buf.append("originalList:" + Arrays.toString(originalList.toArray()));
        
        revisedList.clear();
        revisedList.addAll(originalList);
        revisedList.remove(2);
        revisedList.set(1, "gggggg33");
        revisedList.add(2, "mmm222");
        revisedList.add("jjjjjjj");
        revisedList.add("kkkkkkkkk");
        buf.append("\nrevisedList:" + Arrays.toString(revisedList.toArray()));
        
        targetList.clear();
        targetList.addAll(originalList);
        //targetList.set(3, "lll");
        buf.append("\ntargetList:" + Arrays.toString(targetList.toArray()));
        
        list_info_tv.setText(buf);
    }
}
