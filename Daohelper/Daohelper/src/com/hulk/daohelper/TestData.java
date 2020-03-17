package com.hulk.daohelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.daohelper.db.entry.Area;
import com.daohelper.db.entry.College;
import com.daohelper.db.pairs.SavedResult;
import com.daohelper.factories.SimpleDao;

public class TestData {
    
    public static final String TAG = "DaoTestData";
    public static final int NUM = 1000;

    Context mContext;
    public TestData(Context context) {
        mContext = context;
    }

    public void addData() {
        Log.i(TAG, "Add data start time: " + new Date().toLocaleString());
        for (int i = 0; i < NUM; i++) {
            addCollege(i);
        }
        Log.i(TAG, "Show data end time: " + new Date().toLocaleString());
    }

    public int insertBatchColleges() {
        Log.i(TAG, "insertBatchColleges start time: " + new Date().toLocaleString());
        List<College> list = new ArrayList<College>();
        for (int i = 0; i < NUM; i++) {
            list.add(getCollege(i));
        }
        int delAllCount = SimpleDao.getCollegeDao(mContext).deleteAll();
        Log.i(TAG, "delAllCount delAllCount= " + delAllCount + "delAllCount time: " + new Date().toLocaleString());
        int count = SimpleDao.getCollegeDao(mContext).insertBatch(list);
        Log.i(TAG, "insertBatchColleges DONE count: " + count + ",Time=" + new Date().toLocaleString() );
        return count;
    }

    public College getCollegeInfo(String collegeId) {
        College cl = SimpleDao.getCollegeDao(mContext).getCollege(collegeId);
        return cl;
    }

    public String getAreaInfo(String proviceName) {
        StringBuffer buf = new StringBuffer();
        Area prov = SimpleDao.getAreaDao(mContext).getProvice(proviceName);
        if(prov == null) return null;
        int proviceId = (int) prov.getId();
        buf.append("PROVICE: " + proviceName + ", Id= " + proviceId).append("\n");
        List<Area> cityList = SimpleDao.getAreaDao(mContext).getChildArea(proviceId);
        if(cityList != null) {
            for (Area area : cityList) {
                buf.append("\n\t\tCITY: ").append(area.getId() + ", ").append(area.getName() + ",  ")
                .append(area.getIdxChar() + ",  ").append(area.getPid()).append("\n");
                int cityId = (int) area.getId();
                buf.append(getDistrictsInfo(cityId));
            }
        }
        return buf.toString();
    }

    public String getDistrictsInfo(int cityId) {
        StringBuffer buf = new StringBuffer();
        List<Area> distList = SimpleDao.getAreaDao(mContext).getChildArea(cityId);
        if(distList != null) {
            for (Area dist : distList) {
                buf.append("\t\t\t\tDIST: ").append(dist.getId() + ", ").append(dist.getName() + ",  ")
                .append(dist.getIdxChar() + ",  ").append(dist.getPid()).append("\n");
            } 
        }
        return buf.toString();
    }
    public void addCollege(int i) {
        College cl = getCollege(i);
        SavedResult res = SimpleDao.getCollegeDao(mContext).save(cl);
        Log.i(TAG, "save college SavedResult= " + res);
    }

    public College getCollege(int i) {
        College cl = new College();
        cl.setCollegeId("" + i);
        cl.setCollegeName("College_name_" + i);
        return cl;
    }

    public StringBuffer getCollegeInfo() {
        StringBuffer info = new StringBuffer();
        info.append("\n colleges: \n");
        List<College> colleges = SimpleDao.getCollegeDao(mContext).getAll();
        if(colleges != null) {
            for (int i = 0; i < colleges.size(); i++) {
                College c = colleges.get(i);
                info.append(c.getCollegeId())
                .append("   ").append(c.getCollegeName()).append("\n");
            }
        }
        return info;
    }
}
