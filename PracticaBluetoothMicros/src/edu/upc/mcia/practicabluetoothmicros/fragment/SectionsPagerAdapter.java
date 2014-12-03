package edu.upc.mcia.practicabluetoothmicros.fragment;

import java.util.Locale;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

	// Constant
	private static final int NUM_FRAGMENTS = 2;

	// Fragment cache
	private Fragment[] fragmentCache;

	public SectionsPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
		fragmentCache = new Fragment[NUM_FRAGMENTS];
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			if (fragmentCache[0] == null) {
				fragmentCache[0] = LedsFragment.newInstance();
			}
			return fragmentCache[0];
		case 1:
			if (fragmentCache[1] == null) {
				fragmentCache[1] = LedsFragment.newInstance();
			}
			return fragmentCache[1];
		}
		return null;
	}

	@Override
	public int getCount() {
		return NUM_FRAGMENTS;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale loc = Locale.ENGLISH;
		switch (position) {
		case 0:
			return "Bits"; // getString(R.string.title_section1).toUpperCase(loc);
		case 1:
			return "Bytes"; // getString(R.string.title_section2).toUpperCase(loc);
		}
		return null;
	}

}
