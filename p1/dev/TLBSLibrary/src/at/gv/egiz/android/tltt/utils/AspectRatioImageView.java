package at.gv.egiz.android.tltt.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 
 * @author 
 *         http://stackoverflow.com/questions/4677269/how-to-stretch-three-images
 *         -across-the-screen-preserving-aspect-ratio
 * 
 */
public class AspectRatioImageView extends ImageView {

	public AspectRatioImageView(Context context) {
		super(context);
	}

	public AspectRatioImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AspectRatioImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (getDrawable() != null) {
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = width * getDrawable().getIntrinsicHeight()
					/ getDrawable().getIntrinsicWidth();
			setMeasuredDimension(width, height);
		}// else the image is not yet set, so do nothing specific
		else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
