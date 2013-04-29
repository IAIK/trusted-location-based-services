package at.gv.egiz.android.tltt;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import at.gv.egiz.android.tltt.utils.LocationUtils;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * Class that draws an overlay on top of the maps. In our case a black point,
 * with a surrounding red circle is drawn.
 * 
 */
public class CircleOverlay extends Overlay {
	GeoPoint geoPoint;
	double accuracy;

	public CircleOverlay(GeoPoint geoPoint, double accuracy) {
		this.geoPoint = geoPoint;
		this.accuracy = accuracy;
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {

		Paint circlePaint;
		circlePaint = new Paint();
		circlePaint.setColor(Color.RED);
		circlePaint.setAntiAlias(true);
		circlePaint.setStyle(Style.FILL_AND_STROKE);
		circlePaint.setAlpha(30);

		Paint pointPaint;
		pointPaint = new Paint();
		pointPaint.setColor(Color.BLACK);
		pointPaint.setAntiAlias(true);
		pointPaint.setStyle(Style.FILL_AND_STROKE);
		pointPaint.setAlpha(100);

		int radiusInPixels = LocationUtils.metersToRadius((float) accuracy,
				mapView, (double) geoPoint.getLatitudeE6() / 1000000.0);

		Point pixels = mapView.getProjection().toPixels(geoPoint, null);

		canvas.drawCircle(pixels.x, pixels.y, radiusInPixels, circlePaint);
		canvas.drawCircle(pixels.x, pixels.y, 5, pointPaint);
		mapView.invalidate();
		return true;

	}

}
