

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.lang.Math;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

public class App 
{
    public static void main(String[] args )
    {
    	//Test stuff
    	MongoClient mongoClient = new MongoClient( "localhost" , 27017 );

		MongoDatabase database = mongoClient.getDatabase( "data" );
		
		MongoCollection<Document> userTable = database.getCollection("user");
		MongoCollection<Document> reviewTable = database.getCollection("review");
		
		String queriedUserId = "gSNq08XIdh_vvdm7USKunA"; //Should be set with parameter passed in
		
		//Put together queried user object
		List<Document> qUserInfo = userTable.find(Filters.eq("user_id",queriedUserId)).projection(Projections.fields(Projections.include("user_id","average_stars"))).into(new ArrayList<Document>());
		List<Document> qUserReviewsList = reviewTable.find(Filters.eq("user_id",queriedUserId)).projection(Projections.fields(Projections.include("review_id","user_id","business_id","stars"))).into(new ArrayList<Document>());
		ArrayList<Review> qUserReviews = new ArrayList<Review>();
		double qUserAvgStars = 0;
        try {
        	qUserAvgStars = (double) qUserInfo.get(0).get("average_stars");
        } catch (Exception e) {
        	e.printStackTrace();
        	qUserAvgStars = (int) qUserInfo.get(0).get("average_stars");
        }
        for (int userReview = 0; userReview < qUserReviewsList.size(); userReview++) {
			Document newUserReview = qUserReviewsList.get(userReview);
        	Review newReview = new Review(newUserReview.get("review_id").toString(),newUserReview.get("business_id").toString(), (int) newUserReview.get("stars"));
        	qUserReviews.add(newReview);
        }
		
		User queriedUser = new User(qUserInfo.get(0).getString("user_id"),qUserAvgStars,qUserReviews);
		
		ArrayList<User> relatedUserList = new ArrayList<User>();
		List<Document> queriedUserReviewList = reviewTable.find(Filters.eq("user_id",queriedUserId)).projection(Projections.fields(Projections.include("review_id","user_id","business_id","stars"))).into(new ArrayList<Document>());
		for (int rev = 0; rev < queriedUserReviewList.size(); rev++) {
			//This should be used as the list of users to compare to. Need to get the average stars for these though
			List<Document> relatedUserId =  reviewTable.find(Filters.eq("business_id",queriedUserReviewList.get(rev).get("business_id"))).projection(Projections.fields(Projections.include("user_id"))).into(new ArrayList<Document>());
			for(int relatedUser = 0; relatedUser < relatedUserId.size(); relatedUser++) {
				List<Document> userExists = userTable.find(Filters.eq("user_id",relatedUserId.get(relatedUser).get("user_id"))).projection(Projections.fields(Projections.include("user_id","average_stars"))).into(new ArrayList<Document>());
				if(userExists.size() > 0) {
		            double userAvgStars = 0;
		            try {
		            	userAvgStars = (double) userExists.get(0).get("average_stars");
		            } catch (Exception e) {
		            	e.printStackTrace();
		            	userAvgStars = (int) userExists.get(0).get("average_stars");
		            }
					relatedUserList.add(new User(relatedUserId.get(relatedUser).get("user_id").toString(), userAvgStars, null));
				}
			}
			System.out.println("Done with review " + rev);
		}
		
		
		ArrayList<User> userList = new ArrayList<User>();
		
		for (int user = 0; user < 20; user++) {
			User relatedUser = relatedUserList.get(user);
			
			List<Document> userReviewList = reviewTable.find(Filters.eq("user_id",relatedUser.getId())).projection(Projections.fields(Projections.include("review_id","user_id","business_id","stars"))).into(new ArrayList<Document>());
            
            ArrayList<Review> userReviews = new ArrayList<Review>();
            for (int userReview = 0; userReview < userReviewList.size(); userReview++) {
				Document newUserReview = userReviewList.get(userReview);
            	Review newReview = new Review(newUserReview.get("review_id").toString(),newUserReview.get("business_id").toString(), (int) newUserReview.get("stars"));
            	userReviews.add(newReview);
            }
            
            relatedUserList.get(user).setAllReviews(userReviews);
            userList.add(relatedUserList.get(user));
            System.out.println("Updated user " + user);
        }
		
		//ArrayList<User> userList = new ArrayList<User>();
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
		            for (Review r2: queriedUser.getAllReviews()){ //modify when we figure out I/O
		              String biz2 = r2.getBusinessId();
		              if (biz1.equals(biz2)){
		                  x.commonReviews.add(r1);
                      }
                      else{
                          x.possibleRecs.add(r1);
                      }
                    }
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