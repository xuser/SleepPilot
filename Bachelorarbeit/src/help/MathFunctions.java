package help;

/**
 * Collection of useful mathematical functions.
 * 
 * @author Nils Finke
 */
public class MathFunctions {
	
	static double log2 = Math.log(2.0);
	
	/**
	 * @param x		value
	 * @return 		logarithm of value x with base 2.
	 */
	public static double lb(double x) {
		
	  return (Math.log(x) / log2);
	}
}
