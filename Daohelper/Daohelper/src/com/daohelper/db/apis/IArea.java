
package com.daohelper.db.apis;

import java.util.List;

import com.daohelper.db.entry.Area;

public interface IArea extends IFace<Area> {
    Area getArea(long id);
    
    Area getArea(String name);

    /**The pid is 0 of province.*/
    List<Area> getProvices();

    Area getParentArea(long id);
    
    List<Area> getChildArea(long id);

    Area getProvice(String proviceName);

    List<Area> getCities(long provinceId);
    
    List<Area> getCities(long cityId, boolean isGetMunicipalityDistrcts);

    Area getCity(String proviceName, String cityName);

    List<Area> getDistrcts(long cityId);
    
    List<Area> getMunicipalityDistrcts(long provinceId);

    Area getDistrct(String proviceName, String cityName, String distrctName);

    List<Area> matchName(String keywords);

    List<Area> matchIdxChar(String idx_char);

    /**
     * The demo municipality province ids : {1,2,9, 27}.
     * <p>if your database is different , please override this function.
     * @param provinceId
     * @return true if provinceId is Municipality, or false.
     */
    boolean isMunicipality(long provinceId);
}
