
public class Recommendation {

	private String businessId;
	private double expectedRatingP;
	private double expectedRatingC;

	public Recommendation(String busId, double pearsonRating, double cosineRating) {
		this.businessId = busId;
		this.expectedRatingP = pearsonRating;
		this.expectedRatingC = cosineRating;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	public double getExpectedRatingP() {
		return expectedRatingP;
	}

	public void setExpectedRatingP(double pearsonK) {
		this.expectedRatingP = pearsonK;
	}

	public double getExpectedRatingC() {
		return expectedRatingC;
	}

	public void setExpectedRatingC(double cosineK) {
		this.expectedRatingC = cosineK;
	}
}
