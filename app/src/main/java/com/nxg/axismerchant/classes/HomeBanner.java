package com.nxg.axismerchant.classes;

/**
 * Created by user on 12/8/16.
 */
public class HomeBanner {
    String pImg,stype,pID,sPriority;

    public HomeBanner(String pImg, String stype, String pID, String sPriority) {
        this.pImg = pImg;
        this.stype = stype;
        this.pID = pID;
        this.sPriority = sPriority;
    }

    public String getpImg() {
        return pImg;
    }

    public String getStype() {
        return stype;
    }

    public String getpID() {
        return pID;
    }

    public String getsPriority() {
        return sPriority;
    }
}
