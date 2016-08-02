package com.nxg.axismerchant.classes;

/**
 * Created by Dell on 28-06-2016.
 */
public class MIS_MPR {
    String Transactions,AvgTicketSize,TxnVolume,transDate,tDate;

    public MIS_MPR(String transactions, String avgTicketSize, String txnVolume, String transDate, String tDate) {
        Transactions = transactions;
        AvgTicketSize = avgTicketSize;
        TxnVolume = txnVolume;
        this.transDate = transDate;
        this.tDate = tDate;
    }

    public String getTransactions() {
        return Transactions;
    }

    public String getAvgTicketSize() {
        return AvgTicketSize;
    }

    public String getTxnVolume() {
        return TxnVolume;
    }

    public String getTransDate() {
        return transDate;
    }

    public String gettDate() {
        return tDate;
    }
}
