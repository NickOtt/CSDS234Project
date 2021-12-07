
public class Review {
	private String id;
	private String businessId;
	private Integer stars;
	
	public Review(String id, String bId, Integer stars) {
		this.id = id;
		this.businessId = bId;
		this.stars = stars;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	public Integer getStars() {
		return stars;
	}

	public void setStars(Integer stars) {
		this.stars = stars;
	}
}