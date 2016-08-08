package com.nxg.axismerchant.activity.mis_reports;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.nxg.axismerchant.R;

public class Activity_FilterMIS extends AppCompatActivity implements View.OnClickListener {

    RadioButton rdTransactions, rdTransactionVolume, rdTicketSize, rdDaily, rdWeekly, rdMonthly, rdAll, rdDomesticCredit, rdDomesticDebit,rdInternationalCredit, rdInternationalDebit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_mis);

        ImageView imgCloseFilter = (ImageView) findViewById(R.id.imgBack);
        TextView txtClearFilter = (TextView) findViewById(R.id.txtClear);
        TextView txtApplyFilter = (TextView) findViewById(R.id.txtApply);

        rdTransactions = (RadioButton) findViewById(R.id.rdTransactions);
        rdTransactionVolume = (RadioButton) findViewById(R.id.rdTransactionVolume);
        rdTicketSize = (RadioButton) findViewById(R.id.rdTicketSize);
        rdDaily = (RadioButton) findViewById(R.id.rdDaily);
        rdWeekly = (RadioButton) findViewById(R.id.rdWeekly);
        rdMonthly = (RadioButton) findViewById(R.id.rdMonthly);
        rdAll = (RadioButton) findViewById(R.id.rdAll);
        rdDomesticCredit = (RadioButton) findViewById(R.id.rdDomesticCredit);
        rdDomesticDebit = (RadioButton) findViewById(R.id.rdDomesticDebit);
        rdInternationalCredit = (RadioButton) findViewById(R.id.rdInternationalCredit);
        rdInternationalDebit = (RadioButton) findViewById(R.id.rdInternationalDebit);

        imgCloseFilter.setOnClickListener(this);
        txtClearFilter.setOnClickListener(this);
        txtApplyFilter.setOnClickListener(this);
        rdTransactionVolume.setOnClickListener(this);
        rdTransactions.setOnClickListener(this);
        rdTicketSize.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.txtClear:
                uncheckAll();
                break;

            case R.id.txtApply:
                getData();
                break;

            case R.id.rdTransactions:
                rdTransactions.setChecked(true);
                rdTransactionVolume.setChecked(false);
                rdTicketSize.setChecked(false);
                break;

            case R.id.rdTransactionVolume:
                rdTransactions.setChecked(false);
                rdTransactionVolume.setChecked(true);
                rdTicketSize.setChecked(false);
                break;

            case R.id.rdTicketSize:
                rdTransactions.setChecked(false);
                rdTransactionVolume.setChecked(false);
                rdTicketSize.setChecked(true);
                break;
        }
    }

    private void getData() {
        String reportType = "", duration = "", reportCriteria = "";

        if(rdTransactions.isChecked())
            reportType = "Transactions";
        else if(rdTransactionVolume.isChecked())
            reportType = "Transaction Volume";
        else if(rdTicketSize.isChecked())
            reportType = "Average Ticket Size";

        if(rdDaily.isChecked())
            duration = "Daily";
        else if(rdWeekly.isChecked())
            duration = "Weekly";
        else if(rdMonthly.isChecked())
            duration = "Monthly";

        if(rdAll.isChecked())
            reportCriteria = "All";
        else if(rdDomesticCredit.isChecked())
            reportCriteria = "domcredit";
        else if(rdDomesticDebit.isChecked())
            reportCriteria = "domdebit";
        else if(rdInternationalCredit.isChecked())
            reportCriteria = "intlcredit";
        else if(rdInternationalDebit.isChecked())
            reportCriteria = "intldebit";

        Intent intent=new Intent();
        intent.putExtra("ReportType",reportType);
        intent.putExtra("Duration",duration);
        intent.putExtra("Criteria",reportCriteria);
        setResult(2,intent);
        finish();

    }

    private void uncheckAll() {
        rdTransactions.setChecked(true);
        rdTransactionVolume.setChecked(false);
        rdTicketSize.setChecked(false);
        rdDaily.setChecked(false);
        rdWeekly.setChecked(true);
        rdMonthly.setChecked(false);
        rdAll.setChecked(true);
        rdDomesticCredit.setChecked(false);
        rdDomesticDebit.setChecked(false);
        rdInternationalCredit.setChecked(false);
        rdInternationalDebit.setChecked(false);
    }

}
