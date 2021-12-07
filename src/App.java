

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.lang.Math;

/*import org.json.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
*/
public class App 
{
    public static void main(String[] args )
    {
    	//Test stuff
    	/*MongoClient mongoClient = new MongoClient( "localhost" , 27017 );

	MongoDatabase database = mongoClient.getDatabase( "data" );
	
	MongoCollection<Document> userTable = database.getCollection("user");
	MongoCollection<Document> reviewTable = database.getCollection("review");
	
	//System.out.println(userTable.find().first().toJson());
	
	//JSONObject firstDoc = new JSONObject(userTable.find().first().toJson());
	//System.out.println(firstDoc.get("name").toString());
	
	//Find all user id and the first review for them (slow currently)
	List<Document> userIdList = userTable.find().projection(Projections.fields(Projections.include("user_id"))).into(new ArrayList<Document>());
	for (int i = 0; i < 20; i++) {
            Document userId = userIdList.get(i);
            List<Document> userReviewList = reviewTable.find(Filters.eq("user_id",userId.get("user_id").toString())).projection(Projections.fields(Projections.include("review_id","user_id","business_id","stars"))).into(new ArrayList<Document>());
            System.out.println(userId.get("user_id"));
            if(userReviewList.size() > 0) {
		Document userReview = userReviewList.get(0);
	        System.out.println(userReview.get("stars"));
            }
        }*/
		
	ArrayList<User> userList = new ArrayList<User>();
            //for() {
		//User newUser = new User(userId);
	//}
	
	//for(User user: userList) {
		
	//}
	
	for (User x: userList){
	    if (!(x.equals(args))){
	        //Sort into common reviews and possible businesses for recommendation (review in y but not in x)
        	for (Review r1: x.getAllReviews()){
        	    String biz1 = r1.getBusinessId();
	            /*for (Review r2: args.getAllReviews()){ //modify when we figure out I/O
	              String biz2 = r2.getBusinessId();
	              if (biz1.equals(biz2)){
	                  x.commonReviews.add(r1);
                      }
                      else{
                          x.possibleRecs.add(r1);
                      }
                    }*/
                }
                for (Review rev: x.possibleRecs){
                    double expRatP = expRatP(x, userList, rev);
                    double expRatC = expRatC(x, userList, rev);
                }
            }
        }
        
	//To close connection
	//mongoClient.close();
    }
    
    public static double pearsonCorrelation(User x, User y) {
    	List<Review> xUserReviews = x.getAllReviews();
    	List<Review> yUserReviews = y.getAllReviews();
    	
    	//Calculate average ratings for users x and y across all ratings
    	double avgRatingUserX = avgRating(x);
    	double avgRatingUserY = avgRating(y);
    	
    	double similNumerator = 0;
    	double similDenomX = 0;
    	double similDenomY = 0;
    	
    	//Find reviews for the same business
    	for(Review revX : xUserReviews) {
            for(Review revY : yUserReviews) {
		if (revX.getBusinessId().equals(revY.getBusinessId())) { //Can change this to utilize commonReviews list
                    similNumerator += (revX.getStars()-avgRatingUserX)*(revY.getStars()-avgRatingUserY);
                    similDenomX += Math.pow((revX.getStars()-avgRatingUserX),2);
                    similDenomY += Math.pow((revY.getStars()-avgRatingUserY),2);
		}
            }
    	}
    	
    	//Return the Pearson correlation similarity of users x and y
    	return similNumerator/(Math.sqrt(similDenomX)*Math.sqrt(similDenomY));
    }
    
    public static double cosineCorrelation(User x, User y) {
    	List<Review> xUserReviews = x.getAllReviews();
    	List<Review> yUserReviews = y.getAllReviews();
    	
    	//Calculate average ratings for users x and y across all ratings
    	double avgRatingUserX = avgRating(x);
    	double avgRatingUserY = avgRating(y);
    	
    	double similNumerator = 0;
    	double similDenomX = 0;
    	double similDenomY = 0;
    	
    	//Sum of the squared ratings of all reviews of user x
    	for(Review revX : xUserReviews) {
            similDenomX += Math.pow(revX.getStars(),2);
    	}

    	//Sum of the squared ratings of all reviews of user y
    	for(Review revY : yUserReviews) {
            similDenomY += Math.pow(revY.getStars(),2);
    	}
    	
    	//Find reviews for the same business
    	for(Review revX : xUserReviews) {
            for(Review revY : yUserReviews) {
    		if (revX.getBusinessId().equals(revY.getBusinessId())) { //can change to utilize commonReviews list
                    similNumerator += revX.getStars()*revY.getStars();
		}
            }
    	}
    	
    	//Return the Pearson correlation similarity of users x and y
    	return similNumerator/(Math.sqrt(similDenomX)*Math.sqrt(similDenomY));
    }
    
    private static double avgRating(User user) {
    	List<Review> userReviews = user.getAllReviews();
    	double avgRating = 0;
    	int numRatings = userReviews.size();
    	
    	for(Review rev : userReviews) {
    		avgRating += rev.getStars();
    	}
    	
    	return avgRating/numRatings;
    }
    
    private static double cosineK(User x, List<User> userList){
        double k;
        double summation = 0;
        for (User y: userList){
            double simil = cosineCorrelation(x, y);
            simil = Math.abs(simil);
            summation += simil;
        }
        k = 1/summation;
        
        return k;
    }
    
    private static double pearsonK(User x, List<User> userList){
        double k;
        double summation = 0;
        for (User y: userList){
            double simil = pearsonCorrelation(x, y);
            simil = Math.abs(simil);
            summation += simil;
        }
        k = 1/summation;
        
        return k;
    }
    
    private static double expRatP(User x, List<User> userList, Review rev){
        double expRat;
        double k = pearsonK(x, userList);
        double summation = 0;
        for (User y: userList){
            double simil = pearsonCorrelation(x, y);
            double rating = rev.getStars();
            summation += (simil*rating);
        }
        expRat = (k*summation);
        return expRat;
    }
    
    public static double expRatC(User x, List<User> userList, Review rev){
        double expRat;
        double k = pearsonK(x, userList);
        double summation = 0;
        for (User y: userList){
            double simil = cosineCorrelation(x, y);
            double rating = rev.getStars();
            summation += (simil*rating);
        }
        expRat = (k*summation);
        return expRat;
    }
}