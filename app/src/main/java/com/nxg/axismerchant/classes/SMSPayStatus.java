package com.nxg.axismerchant.classes;

import java.io.Serializable;

/**
 * Created by Dell on 21-03-2016.
 */
public class SMSPayStatus implements Serializable {

    String custMobile, amount, invoiceNum, status, remark, uid, isRefund, transDate;

    public SMSPayStatus(String uid, String custMobile, String amount, String invoiceNum, String status, String remark, String isRefund, String transDate) {
        this.uid = uid;
        this.custMobile = custMobile;
        this.amount = amount;
        this.invoiceNum = invoiceNum;
        this.status = status;
        this.remark = remark;
        this.isRefund = isRefund;
        this.transDate = transDate;
    }

    public String getUid() {
        return uid;
    }



    public String getCustMobile() {
        return custMobile;
    }

    public String getAmount() {
        return amount;
    }

    public String getInvoiceNum() {
        return invoiceNum;
    }

    public String getStatus() {
        return status;
    }

    public String getRemark() {
        return remark;
    }

    public String getIsRefund() {
        return isRefund;
    }

    public String getTransDate() {
        return transDate;
    }
}
