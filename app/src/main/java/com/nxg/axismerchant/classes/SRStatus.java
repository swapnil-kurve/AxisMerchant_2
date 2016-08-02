package com.nxg.axismerchant.classes;

/**
 * Created by hp on 7/28/2016.
 */
public class SRStatus {

    String merchantId,serviceID,merMobileNo,tid,serviceType,probDetails,offDays,visitTiming,contactNo,rollsRequired,
            serviceRequestNumber,serviceStatus,requestDate,problemSubCode;

    public SRStatus(String merchantId, String serviceID, String merMobileNo, String tid, String serviceType, String probDetails, String offDays, String visitTiming, String contactNo, String rollsRequired, String serviceRequestNumber, String serviceStatus, String requestDate, String problemSubCode) {
        this.merchantId = merchantId;
        this.serviceID = serviceID;
        this.merMobileNo = merMobileNo;
        this.tid = tid;
        this.serviceType = serviceType;
        this.probDetails = probDetails;
        this.offDays = offDays;
        this.visitTiming = visitTiming;
        this.contactNo = contactNo;
        this.rollsRequired = rollsRequired;
        this.serviceRequestNumber = serviceRequestNumber;
        this.serviceStatus = serviceStatus;
        this.requestDate = requestDate;
        this.problemSubCode = problemSubCode;
    }


    public String getMerchantId() {
        return merchantId;
    }

    public String getServiceID() {
        return serviceID;
    }

    public String getMerMobileNo() {
        return merMobileNo;
    }

    public String getTid() {
        return tid;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getProbDetails() {
        return probDetails;
    }

    public String getOffDays() {
        return offDays;
    }

    public String getVisitTiming() {
        return visitTiming;
    }

    public String getContactNo() {
        return contactNo;
    }

    public String getRollsRequired() {
        return rollsRequired;
    }

    public String getServiceRequestNumber() {
        return serviceRequestNumber;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public String getProblemSubCode() {
        return problemSubCode;
    }
}
