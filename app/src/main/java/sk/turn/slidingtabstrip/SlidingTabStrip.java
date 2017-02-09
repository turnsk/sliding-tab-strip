package sk.turn.slidingtabstrip;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class SlidingTabStrip extends LinearLayout {

	private static final int FLAG_STRIP_POSITION = 1;

	private Paint mPaint;
	private int mSliderHeight;
	private int mSliderColor;

	private int mLastSelectedViewIndex = -1;
	private int mSelectedPosition;
	private float mSelectionOffset;

	private final int[] mStripPosition = new int[2];

	private int flags = 0;

	public SlidingTabStrip(Context context) {
		this(context, null);
	}

	public SlidingTabStrip(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.slidingTabStripStyle);
	}

	public SlidingTabStrip(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initialize(context, attrs, defStyleAttr, R.style.SlidingTabStrip);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SlidingTabStrip(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initialize(context, attrs, defStyleAttr, defStyleRes);
	}

	private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		setWillNotDraw(false);
		mPaint = new Paint();

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabStrip, defStyleAttr, defStyleRes);
		setSliderHeight(a.getDimensionPixelSize(R.styleable.SlidingTabStrip_sliderHeight, 0));
		setSliderColor(a.getColor(R.styleable.SlidingTabStrip_sliderColor, 0));
		a.recycle();
	}

	public void setSliderHeight(int height) {
		mSliderHeight = height;
		invalidate();
	}

	public void setSliderColor(int color) {
		mSliderColor = color;
		invalidate();
	}

	public void getItemPosition(int position, int[] itemPosition) {
		if (itemPosition == null || itemPosition.length < 4) {
			throw new IllegalArgumentException("stripPosition must be array of four");
		}
		int prev = 0, current = 0, next = 0, nextEnd = 0;

		View selectedView = getChildAt(position);
		if (selectedView != null) {
			prev = current = selectedView.getLeft();
			next = nextEnd = selectedView.getRight();

			View prevView = getChildAt(position - 1);
			if (prevView != null) {
				prev = prevView.getLeft();
			}

			View nextView = getChildAt(position + 1);
			if (nextView != null) {
				nextEnd = nextView.getRight();
			}
		}

		itemPosition[0] = prev;
		itemPosition[1] = current;
		itemPosition[2] = next;
		itemPosition[3] = nextEnd;
	}

	/* package */ void onViewPagerPageChanged(int position, float positionOffset) {
		mSelectedPosition = position;
		mSelectionOffset = positionOffset;
		clearFlags(FLAG_STRIP_POSITION);
		invalidate();
	}

	/* package */ void onViewPagerPageSelected(int position) {
		if (mLastSelectedViewIndex != position) {
			if (mLastSelectedViewIndex >= 0) {
				getChildAt(mLastSelectedViewIndex).setSelected(false);
			}
			mLastSelectedViewIndex = position;
			if (mLastSelectedViewIndex >= 0) {
				getChildAt(mLastSelectedViewIndex).setSelected(true);
			}
		}
	}

	private void updatePosition() {
		if (isFlag(FLAG_STRIP_POSITION)) {
			return;
		}
		int left = 0, right = 0;
		View selectedView = getChildAt(mSelectedPosition);
		if (selectedView != null) {
			left = selectedView.getLeft();
			right = selectedView.getRight();
			View nextView = getChildAt(mSelectedPosition + 1);
			if (nextView != null) {
				left = (int) (mSelectionOffset * nextView.getLeft() + (1.0f - mSelectionOffset) * left);
				right = (int) (mSelectionOffset * nextView.getRight() + (1.0f - mSelectionOffset) * right);
			}
		}
		mStripPosition[0] = left;
		mStripPosition[1] = right;
		if (left != 0 || right != 0) {
			setFlag(FLAG_STRIP_POSITION);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		updatePosition();
		if (mStripPosition[0] == 0 && mStripPosition[1] == 0) {
			return;
		}
		final int height = getHeight();
		mPaint.setColor(mSliderColor);
		canvas.drawRect(mStripPosition[0], height - mSliderHeight, mStripPosition[1], height, mPaint);
	}

	private boolean isFlag(int flag) {
		return (flags & flag) != 0;
	}

	private void setFlag(int flag) {
		flags |= flag;
	}

	private void clearFlags(int flag) {
		flags &= ~flag;
	}

}
