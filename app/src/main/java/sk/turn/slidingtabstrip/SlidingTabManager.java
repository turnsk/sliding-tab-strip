package sk.turn.slidingtabstrip;

import android.animation.ObjectAnimator;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;

public class SlidingTabManager {

	public static SlidingTabManager setup(ViewPager viewPager, SlidingTabStrip tabStrip) {
		return new SlidingTabManager(viewPager, tabStrip);
	}

	private static final Interpolator SCROLL_ANIM_INTERPOLATOR = new DecelerateInterpolator();
	private static final int SCROLL_ANIM_DURATION = 300;

	private final ViewPager mViewPager;
	private final SlidingTabStrip mTabStrip;
	private final ViewPager.OnPageChangeListener listener;
	private final HorizontalScrollView mTabStripScroll;

	private SlidingTabManager(ViewPager viewPager, SlidingTabStrip tabStrip) {
		mViewPager = viewPager;
		mTabStrip = tabStrip;
		mTabStripScroll = tabStrip.getParent() instanceof HorizontalScrollView ? (HorizontalScrollView) tabStrip.getParent() : null;
		validateTabStrip();
		mTabStrip.onViewPagerPageSelected(mViewPager.getCurrentItem());
		mTabStrip.onViewPagerPageChanged(mViewPager.getCurrentItem(), 0);
		listener = new InternalViewPagerListener();
		mViewPager.addOnPageChangeListener(listener);
	}

	public void unload() {
		mViewPager.removeOnPageChangeListener(listener);
		mTabStrip.onViewPagerPageSelected(-1);
	}

	private void validateTabStrip() {
		final PagerAdapter adapter = mViewPager.getAdapter();
		final int itemCount = adapter.getCount();
		final int tabCount = mTabStrip.getChildCount();
		if (itemCount != tabCount) {
			throw new IllegalStateException("ViewPagerAdapter and SlidingTabStrip child count mismatch");
		}
		final OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem((Integer)v.getTag());
			}
		};
		for (int i=0; i<tabCount; i++) {
			View v = mTabStrip.getChildAt(i);
			v.setTag(i);
			v.setOnClickListener(listener);
		}
	}

	private final int[] itemPosition = new int[4];
	private void ensureParentScroll(int position) {
		if (mTabStripScroll == null) {
			return;
		}
		mTabStrip.getItemPosition(position, itemPosition);
		final int size = mTabStripScroll.getWidth();
		final int scroll = mTabStripScroll.getScrollX();
		if (itemPosition[0] < scroll) {
			scrollTo(itemPosition[0]);
		} else if (itemPosition[3] > scroll + size) {
			scrollTo(itemPosition[3] - size);
		}
	}

	private void scrollTo(int scrollTo) {
		ObjectAnimator animator = ObjectAnimator.ofInt(mTabStripScroll, "scrollX", scrollTo);
		animator.setInterpolator(SCROLL_ANIM_INTERPOLATOR);
		animator.setDuration(SCROLL_ANIM_DURATION);
		animator.start();
	}

	private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
		private int mScrollState = ViewPager.SCROLL_STATE_IDLE;
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			int tabStripChildCount = mTabStrip.getChildCount();
			if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
				return;
			}
			mTabStrip.onViewPagerPageChanged(position, positionOffset);
		}
		@Override
		public void onPageScrollStateChanged(int state) {
			mScrollState = state;
		}
		@Override
		public void onPageSelected(int position) {
			mTabStrip.onViewPagerPageSelected(position);
			if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
				mTabStrip.onViewPagerPageChanged(position, 0f);
			}
			ensureParentScroll(position);
		}
	}

}
