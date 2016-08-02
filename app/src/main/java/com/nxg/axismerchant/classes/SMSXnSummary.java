package com.nxg.axismerchant.classes;

/**
 * Created by Dell on 03-06-2016.
 */
public class SMSXnSummary {
    String volume,ticketSize,noOfTrans,transdate,fd,fday,fmonth,fyear,fulldate,transstatus;

    public SMSXnSummary(String volume, String ticketSize, String noOfTrans, String transdate, String fd, String fday, String fmonth, String fyear, String fulldate, String transstatus) {
        this.volume = volume;
        this.ticketSize = ticketSize;
        this.noOfTrans = noOfTrans;
        this.transdate = transdate;
        this.fd = fd;
        this.fday = fday;
        this.fmonth = fmonth;
        this.fyear = fyear;
        this.fulldate = fulldate;
        this.transstatus = transstatus;
    }

    public String getVolume() {
        return volume;
    }

    public String getTicketSize() {
        return ticketSize;
    }

    public String getNoOfTrans() {
        return noOfTrans;
    }

    public String getTransdate() {
        return transdate;
    }

    public String getFd() {
        return fd;
    }

    public String getFday() {
        return fday;
    }

    public String getFmonth() {
        return fmonth;
    }

    public String getFyear() {
        return fyear;
    }

    public String getFulldate() {
        return fulldate;
    }

    public String getTransstatus() {
        return transstatus;
    }
}
