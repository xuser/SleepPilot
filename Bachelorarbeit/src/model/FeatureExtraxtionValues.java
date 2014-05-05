package model;

public class FeatureExtraxtionValues {
	
	/**
	 * This matrix saves continuously for each epoch their permutation entropies.
	 * The first value (y-axis) represent the PE values for the different epochs
	 * and the second value (x-axis) represent the different channels.
	 */
	private float[][] featureValuesPE;

	/**
	 * @return the featureValuesPE
	 */
	public float[][] getFeatureValuesPE() {
		return featureValuesPE;
	}

	/**
	 * @param featureValuesPE the featureValuesPE to set
	 */
	public void setFeatureValuesPE(float[][] featureValuesPE) {
		this.featureValuesPE = featureValuesPE;
	}
	
	
}
