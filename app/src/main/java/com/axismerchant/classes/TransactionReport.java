package com.axismerchant.classes;

import java.io.Serializable;

/**
 * Created by Dell on 29-06-2016.
 */
public class TransactionReport implements Serializable{
    String Totaltransaction,transDate,TxnVolume,avgTicketSize,tDate,tType;

    public TransactionReport(String totaltransaction, String transDate, String txnVolume, String avgTicketSize, String tDate, String tType) {
        Totaltransaction = totaltransaction;
        this.transDate = transDate;
        TxnVolume = txnVolume;
        this.avgTicketSize = avgTicketSize;
        this.tDate = tDate;
        this.tType = tType;
    }

    public String getTotaltransaction() {
        return Totaltransaction;
    }

    public String getTransDate() {
        return transDate;
    }

    public String getTxnVolume() {
        return TxnVolume;
    }

    public String getAvgTicketSize() {
        return avgTicketSize;
    }

    public String gettDate() {
        return tDate;
    }

    public String gettType() {
        return tType;
    }
}
