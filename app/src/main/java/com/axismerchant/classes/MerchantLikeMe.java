package com.axismerchant.classes;

/**
 * Created by Dell on 02-07-2016.
 */
public class MerchantLikeMe {
    String noOfTxn,txnVol,avgTicketSize,mer_id,mHead;

    public MerchantLikeMe(String noOfTxn, String txnVol, String avgTicketSize, String mer_id, String mHead) {
        this.noOfTxn = noOfTxn;
        this.txnVol = txnVol;
        this.avgTicketSize = avgTicketSize;
        this.mer_id = mer_id;
        this.mHead = mHead;
    }

    public String getNoOfTxn() {
        return noOfTxn;
    }

    public String getTxnVol() {
        return txnVol;
    }

    public String getAvgTicketSize() {
        return avgTicketSize;
    }

    public String getMer_id() {
        return mer_id;
    }

    public String getmHead() {
        return mHead;
    }

}
