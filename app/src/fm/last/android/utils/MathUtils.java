package fm.last.android.utils;

/**
 * Math equations
 *  
 * @author Lukasz Wisniewski
 */
public class MathUtils {
	/**
	 * Calculates great-circle distance between two points on earth
	 * using the Haversine formula
	 * 
	 * @param lat1 (in degrees)
	 * @param lon1 (in degrees)
	 * @param lat2 (in degrees)
	 * @param lon2 (in degrees)
	 * @return distance in kilometers
	 */
	public static double distance(double lat1, double lon1, double lat2, double lon2){
		
		double dlat = Math.toRadians(lat2 - lat1);
		double dlon = Math.toRadians(lon2 - lon1);
		
		double a = Math.pow(Math.sin(dlat/2),2) 
			+ Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.pow(Math.sin(dlon/2),2);
		
		double c = 2 * Math.asin( 1 < Math.sqrt(a) ? 1 : Math.sqrt(a));
		
		double d = 6367 * c;
		return d;
	}
}