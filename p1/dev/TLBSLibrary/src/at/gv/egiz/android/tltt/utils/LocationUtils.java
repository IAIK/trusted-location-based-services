package at.gv.egiz.android.tltt.utils;

import com.google.android.maps.MapView;

public class LocationUtils {

	public static int metersToRadius(float meters, MapView map, double latitude) {
		return (int) (map.getProjection().metersToEquatorPixels(meters) * (1 / Math
				.cos(Math.toRadians(latitude))));
	}

}
