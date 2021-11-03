import java.util.ArrayList;
/**
 * User class represents an object containing user information such as restaurants reviewed, ratings, etc.
 *
 */
public class User
{
    private ArrayList<String> places;
    private ArrayList<Integer> ratings;
    public static String id;
    
    public User(String id)
    {
        this.id = id;
        places = new ArrayList<String>();
        ratings = new ArrayList<Integer>();
    }
    
    public void addReview(String place, Integer rating)
    {
        places.add(place);
        ratings.add(rating);
    }
    
    public String getPlace(int i)
    {
        return places.get(i);
    }
    
    public Integer getRating(int i)
    {
        return ratings.get(i);
    }
    
}
