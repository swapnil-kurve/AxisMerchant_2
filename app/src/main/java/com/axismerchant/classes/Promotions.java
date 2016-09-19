package com.axismerchant.classes;

import java.io.Serializable;

/**
 * Created by Dell on 03-03-2016.
 */
public class Promotions implements Serializable{

    String mTitle;
    String mMessage;
    String mSubTitle;
    String mImgPath;
    String mPromotionType;
    String mPromotionID;
    String mWithOption ;
    String mStatus;
    String mUid;
    String mReadStatus;
    String mOnDate;

    public Promotions(String mUid, String mTitle, String mMessage, String mSubTitle, String mImgPath, String mPromotionType, String mPromotionID, String mWithOption, String mStatus, String mReadStatus, String mOnDate) {
        this.mUid = mUid;
        this.mTitle = mTitle;
        this.mMessage = mMessage;
        this.mSubTitle = mSubTitle;
        this.mImgPath = mImgPath;
        this.mPromotionType = mPromotionType;
        this.mPromotionID = mPromotionID;
        this.mWithOption = mWithOption;
        this.mStatus = mStatus;
        this.mReadStatus = mReadStatus;
        this.mOnDate = mOnDate;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmMessage() {
        return mMessage;
    }

    public String getmSubTitle() {
        return mSubTitle;
    }

    public String getmImgPath() {
        return mImgPath;
    }

    public String getmPromotionType() {
        return mPromotionType;
    }

    public String getmPromotionID() {
        return mPromotionID;
    }

    public String getmWithOption() {
        return mWithOption;
    }

    public String getmStatus() {
        return mStatus;
    }

    public String getmUid() {
        return mUid;
    }

    public String getmReadStatus() {
        return mReadStatus;
    }

    public String getmOnDate() {
        return mOnDate;
    }
}
