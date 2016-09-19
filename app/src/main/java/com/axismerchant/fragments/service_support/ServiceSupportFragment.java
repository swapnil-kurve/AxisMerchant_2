package com.axismerchant.fragments.service_support;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.service_support.Activity_SubLinks;

/**
 * Created by vismita.jain on 7/1/16.
 */
public class ServiceSupportFragment extends Fragment implements View.OnClickListener
{
    ImageView btnExpandQuickLinks, btnExpandHardwareIssue, btnExpandApplicationIssue, btnExpandOther, btnExpandAccountMang;
    LinearLayout lyExpandQuickLinks, lyExpandHardwareIssue, lyExpandApplicationIssue, lyExpandOther, lyExpandAccountMang;
    TextView qlRollRequired, qlTrainningRequired;
    TextView hiAdaptorProblem, hiBaseProblem, hiBatteryProblem, hiCardReaderProblem, hiDisplayProblem, hiKeysNotWorking,
            hiPowerCardProblem, hiPrinterProblem;
    TextView aiSettlementProblem, aiTerminalSoftwareCorrupted;
    TextView othersCallIssuer, othersPaymentInquiry, othersPickUpCard, othersDeclineCard, othersCardSwipeError;
    TextView amAxisAccNo, amNeftRtgs, amDbaName, amLegalName, amAddressChange, amPhoneNumber, amNewLocation, amAssetSwapping, amDcc,
            amAdditionalDcc, amCashPos, amApply_mVisa, amMprStatementRequest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_servicesupport, container, false);

        btnExpandQuickLinks = (ImageView) view.findViewById(R.id.imgQuickLinks);
        btnExpandHardwareIssue = (ImageView) view.findViewById(R.id.imgHardwareIssue);
        btnExpandApplicationIssue = (ImageView) view.findViewById(R.id.imgApplicationIssue);
        btnExpandOther = (ImageView) view.findViewById(R.id.imgOther);
        btnExpandAccountMang = (ImageView) view.findViewById(R.id.imgAccountMang);

        lyExpandQuickLinks = (LinearLayout) view.findViewById(R.id.lyQuickLinks);
        lyExpandHardwareIssue = (LinearLayout) view.findViewById(R.id.lyHardwareIssue);
        lyExpandApplicationIssue = (LinearLayout) view.findViewById(R.id.lyApplicationIssue);
        lyExpandOther = (LinearLayout) view.findViewById(R.id.lyOther);
        lyExpandAccountMang = (LinearLayout) view.findViewById(R.id.lyAccountMang);

        qlRollRequired = (TextView) view.findViewById(R.id.txtrollrequired);
        qlTrainningRequired = (TextView) view.findViewById(R.id.txttrainningrequired);

        hiAdaptorProblem = (TextView) view.findViewById(R.id.txtAdaptorProblem);
        hiBaseProblem = (TextView) view.findViewById(R.id.txtBaseProblem);
        hiBatteryProblem = (TextView) view.findViewById(R.id.txtBatteryProblem);
        hiCardReaderProblem = (TextView) view.findViewById(R.id.txtCardReaderProblem);
        hiDisplayProblem = (TextView) view.findViewById(R.id.txtDisplayProblem);
        hiKeysNotWorking = (TextView) view.findViewById(R.id.txtKeysNotWorking);
        hiPowerCardProblem = (TextView) view.findViewById(R.id.txtPowerCardProblem);
        hiPrinterProblem = (TextView) view.findViewById(R.id.txtPrinterProblem);

        aiSettlementProblem = (TextView) view.findViewById(R.id.txtSettlementProblem);
        aiTerminalSoftwareCorrupted = (TextView) view.findViewById(R.id.txtTerminalSoftwareCorrupted);

        othersCallIssuer = (TextView) view.findViewById(R.id.txtCallIssuer);
        othersPaymentInquiry = (TextView) view.findViewById(R.id.txtPaymentInquiry);
        othersPickUpCard = (TextView) view.findViewById(R.id.txtPickUpCard);
        othersDeclineCard = (TextView) view.findViewById(R.id.txtDeclineCard);
        othersCardSwipeError = (TextView) view.findViewById(R.id.txtCardSwipeError);

        amAxisAccNo = (TextView) view.findViewById(R.id.txtAxisAccNo);
        amNeftRtgs = (TextView) view.findViewById(R.id.txtNeftRtgs);
        amDbaName = (TextView) view.findViewById(R.id.txtDbaName);
        amLegalName = (TextView) view.findViewById(R.id.txtLegalName);
        amAddressChange = (TextView) view.findViewById(R.id.txtAddressChange);
        amPhoneNumber = (TextView) view.findViewById(R.id.txtPhoneNo);
        amNewLocation = (TextView) view.findViewById(R.id.txtNewLocation);
        amAssetSwapping = (TextView) view.findViewById(R.id.txtAssetSwapping);
        amDcc = (TextView) view.findViewById(R.id.txtDcc);
        amAdditionalDcc = (TextView) view.findViewById(R.id.txtAdditionalDcc);
        amCashPos = (TextView) view.findViewById(R.id.txtCashPos);
        amApply_mVisa = (TextView) view.findViewById(R.id.txtApply_mVisa);
        amMprStatementRequest = (TextView) view.findViewById(R.id.txtMprStatmentRequest);


        btnExpandQuickLinks.setOnClickListener(this);
        btnExpandHardwareIssue.setOnClickListener(this);
        btnExpandApplicationIssue.setOnClickListener(this);
        btnExpandOther.setOnClickListener(this);
        btnExpandAccountMang.setOnClickListener(this);

        qlRollRequired.setOnClickListener(this);
        qlTrainningRequired.setOnClickListener(this);

        hiAdaptorProblem.setOnClickListener(this);
        hiBaseProblem.setOnClickListener(this);
        hiBatteryProblem.setOnClickListener(this);
        hiCardReaderProblem.setOnClickListener(this);
        hiDisplayProblem.setOnClickListener(this);
        hiKeysNotWorking.setOnClickListener(this);
        hiPowerCardProblem.setOnClickListener(this);
        hiPrinterProblem.setOnClickListener(this);

        aiSettlementProblem.setOnClickListener(this);
        aiTerminalSoftwareCorrupted.setOnClickListener(this);

        othersCallIssuer.setOnClickListener(this);
        othersPaymentInquiry.setOnClickListener(this);
        othersPickUpCard.setOnClickListener(this);
        othersDeclineCard.setOnClickListener(this);
        othersCardSwipeError.setOnClickListener(this);

        amAxisAccNo.setOnClickListener(this);
        amNeftRtgs.setOnClickListener(this);
        amDbaName.setOnClickListener(this);
        amLegalName.setOnClickListener(this);
        amAddressChange.setOnClickListener(this);
        amPhoneNumber.setOnClickListener(this);
        amNewLocation.setOnClickListener(this);
        amAssetSwapping.setOnClickListener(this);
        amDcc.setOnClickListener(this);
        amAdditionalDcc.setOnClickListener(this);
        amCashPos.setOnClickListener(this);
        amApply_mVisa.setOnClickListener(this);
        amMprStatementRequest.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.imgQuickLinks:
                if(lyExpandQuickLinks.getVisibility() == View.VISIBLE) {
                    lyExpandQuickLinks.setVisibility(View.GONE);
                    btnExpandQuickLinks.setImageResource(R.mipmap.plus_sr);
                }
                else {
                    lyExpandQuickLinks.setVisibility(View.VISIBLE);
                    btnExpandQuickLinks.setImageResource(R.mipmap.minus_sr);
                }
                break;

            case R.id.imgHardwareIssue:
                if(lyExpandHardwareIssue.getVisibility() == View.VISIBLE) {
                    lyExpandHardwareIssue.setVisibility(View.GONE);
                    btnExpandHardwareIssue.setImageResource(R.mipmap.plus_sr);
                }
                else {
                    lyExpandHardwareIssue.setVisibility(View.VISIBLE);
                    btnExpandHardwareIssue.setImageResource(R.mipmap.minus_sr);
                }
                break;

            case R.id.imgApplicationIssue:
                if(lyExpandApplicationIssue.getVisibility() == View.VISIBLE) {
                    lyExpandApplicationIssue.setVisibility(View.GONE);
                    btnExpandApplicationIssue.setImageResource(R.mipmap.plus_sr);
                }
                else {
                    lyExpandApplicationIssue.setVisibility(View.VISIBLE);
                    btnExpandApplicationIssue.setImageResource(R.mipmap.minus_sr);
                }
                break;

            case R.id.imgOther:
                if(lyExpandOther.getVisibility() == View.VISIBLE) {
                    lyExpandOther.setVisibility(View.GONE);
                    btnExpandOther.setImageResource(R.mipmap.plus_sr);
                }
                else {
                    lyExpandOther.setVisibility(View.VISIBLE);
                    btnExpandOther.setImageResource(R.mipmap.minus_sr);
                }
                break;

            case R.id.imgAccountMang:
                if(lyExpandAccountMang.getVisibility() == View.VISIBLE) {
                    lyExpandAccountMang.setVisibility(View.GONE);
                    btnExpandAccountMang.setImageResource(R.mipmap.plus_sr);
                }
                else {
                    lyExpandAccountMang.setVisibility(View.VISIBLE);
                    btnExpandAccountMang.setImageResource(R.mipmap.minus_sr);
                }
                break;

            case R.id.txtrollrequired:
                Intent inroll = new Intent(getActivity(), Activity_SubLinks.class);
                inroll.putExtra("Heading", "RollsRequired");
                startActivity(inroll);
                break;
            case R.id.txttrainningrequired:
                Intent intraining = new Intent(getActivity(), Activity_SubLinks.class);
                intraining.putExtra("Heading", "TrainingRequired");
                startActivity(intraining);
                break;

            case R.id.txtAdaptorProblem:
                Intent inAdaptorProblem = new Intent(getActivity(), Activity_SubLinks.class);
                inAdaptorProblem.putExtra("Heading", "AdaptorProblem");
                startActivity(inAdaptorProblem);
                break;
            case R.id.txtBaseProblem:
                Intent inBaseProblem = new Intent(getActivity(), Activity_SubLinks.class);
                inBaseProblem.putExtra("Heading", "BaseProblem");
                startActivity(inBaseProblem);
                break;
            case R.id.txtBatteryProblem:
                Intent inBatteryProblem = new Intent(getActivity(), Activity_SubLinks.class);
                inBatteryProblem.putExtra("Heading", "BatteryProblem");
                startActivity(inBatteryProblem);
                break;
            case R.id.txtCardReaderProblem:
                Intent inCardReaderProblem= new Intent(getActivity(), Activity_SubLinks.class);
                inCardReaderProblem.putExtra("Heading", "CardReaderProblem");
                startActivity(inCardReaderProblem);
                break;
            case R.id.txtDisplayProblem:
                Intent inDisplayProblem = new Intent(getActivity(), Activity_SubLinks.class);
                inDisplayProblem.putExtra("Heading", "DisplayProblem");
                startActivity(inDisplayProblem);
                break;
            case R.id.txtKeysNotWorking:
                Intent inKeysNotWorking = new Intent(getActivity(), Activity_SubLinks.class);
                inKeysNotWorking.putExtra("Heading", "KeysNotWorking");
                startActivity(inKeysNotWorking);
                break;
            case R.id.txtPowerCardProblem:
                Intent inPowerCardProblem = new Intent(getActivity(), Activity_SubLinks.class);
                inPowerCardProblem.putExtra("Heading", "PowerCardProblem");
                startActivity(inPowerCardProblem);
                break;
            case R.id.txtPrinterProblem:
                Intent inPrinterProblem = new Intent(getActivity(), Activity_SubLinks.class);
                inPrinterProblem.putExtra("Heading", "PrinterProblem");
                startActivity(inPrinterProblem);
                break;

            case R.id.txtSettlementProblem:
                Intent inSettlementProblem = new Intent(getActivity(), Activity_SubLinks.class);
                inSettlementProblem.putExtra("Heading", "SettlementProblem");
                startActivity(inSettlementProblem);
                break;
            case R.id.txtTerminalSoftwareCorrupted:
                Intent inTerminalSoftwareCorrupted = new Intent(getActivity(), Activity_SubLinks.class);
                inTerminalSoftwareCorrupted.putExtra("Heading", "TerminalSoftwareCorrupted");
                startActivity(inTerminalSoftwareCorrupted);
                break;

            case R.id.txtCallIssuer:
                Intent inCallIssuer = new Intent(getActivity(), Activity_SubLinks.class);
                inCallIssuer.putExtra("Heading", "CallIssues");
                startActivity(inCallIssuer);
                break;
            case R.id.txtPaymentInquiry:
                Intent inPaymentInquiry = new Intent(getActivity(), Activity_SubLinks.class);
                inPaymentInquiry.putExtra("Heading", "PaymentInquiry");
                startActivity(inPaymentInquiry);
                break;
            case R.id.txtPickUpCard:
                Intent inPickUpCard = new Intent(getActivity(), Activity_SubLinks.class);
                inPickUpCard.putExtra("Heading", "PickUpCard");
                startActivity(inPickUpCard);
                break;
            case R.id.txtDeclineCard:
                Intent inDeclineCard = new Intent(getActivity(), Activity_SubLinks.class);
                inDeclineCard.putExtra("Heading", "DeclineCard");
                startActivity(inDeclineCard);
                break;
            case R.id.txtCardSwipeError:
                Intent inCardSwipeError = new Intent(getActivity(), Activity_SubLinks.class);
                inCardSwipeError.putExtra("Heading", "CardSwipeError");
                startActivity(inCardSwipeError);
                break;

            case R.id.txtAxisAccNo:
                Intent inAxisAccNo = new Intent(getActivity(), Activity_SubLinks.class);
                inAxisAccNo.putExtra("Heading", "AxisAccNo");
                startActivity(inAxisAccNo);
                break;
            case R.id.txtNeftRtgs:
                Intent inNeftRtgs = new Intent(getActivity(), Activity_SubLinks.class);
                inNeftRtgs.putExtra("Heading", "NeftRtgs");
                startActivity(inNeftRtgs);
                break;
            case R.id.txtDbaName:
                Intent inDbaName = new Intent(getActivity(), Activity_SubLinks.class);
                inDbaName.putExtra("Heading", "DbaName");
                startActivity(inDbaName);
                break;
            case R.id.txtLegalName:
                Intent inLegalName = new Intent(getActivity(), Activity_SubLinks.class);
                inLegalName.putExtra("Heading", "LegalName");
                startActivity(inLegalName);
                break;
            case R.id.txtAddressChange:
                Intent inAddressChange = new Intent(getActivity(), Activity_SubLinks.class);
                inAddressChange.putExtra("Heading", "AddressChange");
                startActivity(inAddressChange);
                break;
            case R.id.txtPhoneNo:
                Intent inPhoneNo = new Intent(getActivity(), Activity_SubLinks.class);
                inPhoneNo.putExtra("Heading", "PhoneNo");
                startActivity(inPhoneNo);
                break;
            case R.id.txtNewLocation:
                Intent inNewLocation = new Intent(getActivity(), Activity_SubLinks.class);
                inNewLocation.putExtra("Heading", "NewLocation");
                startActivity(inNewLocation);
                break;
            case R.id.txtAssetSwapping:
                Intent inAssetSwapping = new Intent(getActivity(), Activity_SubLinks.class);
                inAssetSwapping.putExtra("Heading", "AssetSwapping");
                startActivity(inAssetSwapping);
                break;
            case R.id.txtDcc:
                Intent inDcc = new Intent(getActivity(), Activity_SubLinks.class);
                inDcc.putExtra("Heading", "Dcc");
                startActivity(inDcc);
                break;
            case R.id.txtAdditionalDcc:
                Intent inAdditionalDcc = new Intent(getActivity(), Activity_SubLinks.class);
                inAdditionalDcc.putExtra("Heading", "AdditionalDcc");
                startActivity(inAdditionalDcc);
                break;
            case R.id.txtCashPos:
                Intent inCashPos = new Intent(getActivity(), Activity_SubLinks.class);
                inCashPos.putExtra("Heading", "CashPos");
                startActivity(inCashPos);
                break;
            case R.id.txtApply_mVisa:
                Intent inApply_mVisa = new Intent(getActivity(), Activity_SubLinks.class);
                inApply_mVisa.putExtra("Heading", "Apply_mVisa");
                startActivity(inApply_mVisa);
                break;
            case R.id.txtMprStatmentRequest:
                Intent inMprStatmentRequest = new Intent(getActivity(), Activity_SubLinks.class);
                inMprStatmentRequest.putExtra("Heading", "MprStatmentRequest");
                startActivity(inMprStatmentRequest);
                break;
        }
    }
}
