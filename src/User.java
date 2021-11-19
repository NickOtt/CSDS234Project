import java.util.ArrayList;
/**
 * User class represents an object containing user information such as restaurants reviewed, ratings, etc.
 *
 */
public class User
{
    private ArrayList<Review> reviews;
    private String id;
    
    public User(String id, ArrayList<Review> reviews)
    {
        this.id = id;
        this.reviews = reviews;
    }
    
    public void addReview(Review rating)
    {
        reviews.add(rating);
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
    
}
