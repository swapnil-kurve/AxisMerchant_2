package com.nxg.axismerchant.fragments.reports;


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

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_for_TransactionReport extends Fragment {
    public static final String ARG_OBJECT = "object";
    ViewPager viewPager;
    private String[] tabs ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions_status, container, false);

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

        return view; //fragment_page_for_transaction_report
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
            PageFragment_for_TransactionReport fragmentForTransaction = new PageFragment_for_TransactionReport();
            Bundle bundle = new Bundle();
            bundle.putInt(PageFragment_for_TransactionReport.ARG_OBJECT, position);
            fragmentForTransaction.setArguments(bundle);
            return fragmentForTransaction;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabs[position];
        }
    }


}
