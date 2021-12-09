import java.util.ArrayList;
/**
 * User class represents an object containing user information such as restaurants reviewed, ratings, etc.
 *
 */
public class User
{
    private ArrayList<Review> reviews;
    public ArrayList<Review> commonReviews;
    public ArrayList<Review> possibleRecs;
    private String id;
    private double avgStars;
    
    
    public User(String id, double avgStars, ArrayList<Review> reviews)
    {
        this.id = id;
        this.reviews = reviews;
        this.avgStars = avgStars;
        this.commonReviews = new ArrayList<Review>();
        this.possibleRecs = new ArrayList<Review>();
    }
    
    public void addReview(Review rating)
    {
        reviews.add(rating);
    }
    
    public void setAllReviews(ArrayList<Review> reviews)
    {
    	this.reviews = reviews;
    }
    
    public Review getReview(int i)
    {
        return reviews.get(i);
    }
    
    public ArrayList<Review> getAllReviews() {
    	return reviews;
    }
    
    public String getId() {
    	return id;
    }
    
    public double getAvgStars() {
		return avgStars;
	}

	public void setAvgStars(double avgStars) {
		this.avgStars = avgStars;
	}
	
	public void setCommonAndPossReviews(ArrayList<Review> rev) {
		for (Review curUserRev : this.reviews) {
			boolean inCompareUserRevs = false;
			for (Review compareRev : rev) {
				if(curUserRev.getBusinessId().equals(compareRev.getBusinessId())) {
					inCompareUserRevs = true;
					this.commonReviews.add(curUserRev);
					break;
				}
			}
			
			if(!inCompareUserRevs) {
				this.possibleRecs.add(curUserRev);
			}
		}
	}
}