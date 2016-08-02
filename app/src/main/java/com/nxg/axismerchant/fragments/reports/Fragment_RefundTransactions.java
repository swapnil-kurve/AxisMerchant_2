package com.nxg.axismerchant.fragments.reports;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.nxg.axismerchant.R;
import com.nxg.axismerchant.classes.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_RefundTransactions extends Fragment {

    public static final String ARG_OBJECT = "object";
    ViewPager viewPager;
    private String[] tabs ;
    String MID, MOBILE;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refund_transactions, container, false);

        tabs = getResources().getStringArray(R.array.all_xn_report_mis);
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Futura_LightBold.ttf");
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getChildFragmentManager()));

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        tabsStrip.setTypeface(typeFace,0);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);
        
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());
    }

    public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

        public SampleFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public Fragment getItem(int position) {
            PageFragment_for_refundXn refundXn = new PageFragment_for_refundXn();
            Bundle bundle = new Bundle();
            bundle.putInt(PageFragment_for_refundXn.ARG_OBJECT, position);
            refundXn.setArguments(bundle);
            return refundXn;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabs[position];
        }
    }


}
