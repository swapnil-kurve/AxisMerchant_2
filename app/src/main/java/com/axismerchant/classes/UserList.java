package com.axismerchant.classes;

/**
 * Created by Dell on 12-03-2016.
 */
public class UserList {

    String regUsersID,merchantId,mobileNo,userName, addedDate,emailid,isRegistered,assignedMVisaID;

    public UserList(String regUsersID, String merchantId, String mobileNo, String userName, String addedDate, String emailid, String isRegistered, String assignedMVisaID) {
        this.regUsersID = regUsersID;
        this.merchantId = merchantId;
        this.mobileNo = mobileNo;
        this.userName = userName;
        this.addedDate = addedDate;
        this.emailid = emailid;
        this.isRegistered = isRegistered;
        this.assignedMVisaID = assignedMVisaID;
    }

    public String getRegUsersID() {
        return regUsersID;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public String getUserName() {
        return userName;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public String getEmailid() {
        return emailid;
    }

    public String getIsRegistered() {
        return isRegistered;
    }

    public String getAssignedMVisaID() {
        return assignedMVisaID;
    }
}
