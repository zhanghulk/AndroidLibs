package com.daohelper.db.impls;

import java.util.List;

import com.daohelper.db.IDbFileHelper;
import com.daohelper.db.apis.IArea;
import com.daohelper.db.entry.Area;
import com.daohelper.factories.DaoConatants.AreaColumns;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
public class AreaDao extends CommonImpl<Area> implements IArea, AreaColumns {

    private static final String TAG = null;

	public AreaDao(IDbFileHelper helper, String table) {
		super(helper, table);
	}
	
	@Override
	protected Area parseData(Cursor c) {
		Area area = new Area();
		area.setId(getInt(c, ID));
		area.setPid(getInt(c, PID));
		area.setName(getString(c, NAME));
		area.setIdxChar(getString(c, IDX_CHAR));
		return area;
	}

	@Override
	protected ContentValues getContentValues(Area data) {
		ContentValues cv = new ContentValues();
		cv.put(IDX_CHAR, data.getIdxChar());
		cv.put(NAME, data.getName());
		cv.put(PID, data.getPid());
		return cv;
	}

    @Override
    public Area getArea(long id) {
        return get(id);
    }

    @Override
    public Area getArea(String name) {
        return getOneData(NAME + "= ? ", new String[]{name}, null);
    }

    @Override
    public Area getParentArea(long id) {
      //the pid of child is id of parent.
        Area area = getArea(id);
        if(area != null) {
            getArea(area.getPid());
        }
        return null;
    }

    @Override
    public List<Area> getChildArea(long id) {
        return get(PID + " = " + id);
    }

    /**
     * The pid is 0 of province.
     */
    @Override
    public List<Area> getProvices() {
        return get(PID + " = 0 ");
    }

    @Override
    public Area getProvice(String proviceName) {
        String selection = PID + " = 0  AND " + NAME + " = ? ";
        String[] selectionArgs = {proviceName};
        return getOneData(selection, selectionArgs, null);
    }

    @Override
    public List<Area> getCities(long provinceId) {
        return getCities(provinceId, false);
    }

    @Override
    public List<Area> getCities(long provinceId, boolean isGetMunicipalityDistrcts) {
    	if (provinceId <= 0) {
    		Log.e(TAG, "The provinceId nust be > 0, invalid provinceId=" + provinceId);
			return null;
		}
    	if (isGetMunicipalityDistrcts && isMunicipality(provinceId)) {
			return getMunicipalityDistrcts(provinceId);
		} else {
			return get(PID + " = " + provinceId);
		}
    }

    @Override
    public Area getCity(String proviceName, String cityName) {
        Area prov = getProvice(proviceName);
        if(prov != null) {
            String selection = PID + "=" + prov .getId() + " AND " + NAME + "= ? ";
            String[] selectionArgs = {cityName};
            return getOneData(selection, selectionArgs, null);
        }
        return null;
    }

    @Override
    public List<Area> getDistrcts(long cityId) {
    	if (cityId <= 0) {
    		Log.e(TAG, "The cityId nust be > 0, invalid cityId=" + cityId);
			return null;
		}
        return get(PID + " = " + cityId);
    }

    @Override
    public Area getDistrct(String proviceName, String cityName, String distrctName) {
        Area city = getCity(proviceName, cityName);
        if(city != null) {
            String selection = PID + "=" + city .getId() + " AND " + NAME + "= ? ";
            String[] selectionArgs = {distrctName};
            return getOneData(selection, selectionArgs, null);
        }
        return null;
    }

    @Override
    public List<Area> getMunicipalityDistrcts(long provinceId) {
    	List<Area> ciries = getCities(provinceId);
    	int cityId = -1;
    	if (ciries != null && !ciries.isEmpty()) {
    		cityId = (int) ciries.get(0).getId();
		}
		return getDistrcts(cityId);
    }

    @Override
    public List<Area> matchName(String keywords) {
        return get("LIKE " + NAME + "= ?% ", new String[]{keywords});
    }

    @Override
    public List<Area> matchIdxChar(String idx_char) {
        return get("LIKE " + IDX_CHAR + "= ?% ", new String[]{idx_char});
    }

    /**
     * The demo municipality province ids : {1,2,9, 27}.
     * <p>if your database is different , please override this function.
     * @param provinceId
     * @return true if provinceId is Municipality, or false.
     */
    @Override
    public boolean isMunicipality(long provinceId) {
		int[] muns = {1,2,9, 27}; 
		for (int i = 0; i < muns.length; i++) {
			if (provinceId == muns[i]) {
				return true;
			}
		}
		return false;
	}
}
