package com.nxg.axismerchant.classes;

/**
 * Created by user on 5/8/16.
 */
public class QRTransactions {

    String id,onDate, ref_no, mvisa_merchant_id, txn_amount;

    public QRTransactions(String id,String onDate, String ref_no, String mvisa_merchant_id, String txn_amount) {
        this.id = id;
        this.onDate = onDate;
        this.ref_no = ref_no;
        this.mvisa_merchant_id = mvisa_merchant_id;
        this.txn_amount = txn_amount;
    }

    public String getOnDate() {
        return onDate;
    }

    public String getRef_no() {
        return ref_no;
    }

    public String getMvisa_merchant_id() {
        return mvisa_merchant_id;
    }

    public String getTxn_amount() {
        return txn_amount;
    }

    public String getId() {
        return id;
    }
}
