

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

public class App 
{
    public static void main( String[] args )
    {
    	//Test stuff
    	MongoClient mongoClient = new MongoClient( "localhost" , 27017 );

		MongoDatabase database = mongoClient.getDatabase( "data" );
		
		MongoCollection<Document> userTable = database.getCollection("user");
		MongoCollection<Document> reviewTable = database.getCollection("review");
		
		//System.out.println(userTable.find().first().toJson());
		
		//JSONObject firstDoc = new JSONObject(userTable.find().first().toJson());
		//System.out.println(firstDoc.get("name").toString());
		
		ArrayList<User> userList = new ArrayList<User>();
		
		//If needed, we could get user and their friends to compare
		//Or just get users who have all rated one business
		
		//Find all user id and the first review for them (slow currently)
		List<Document> userIdList = userTable.find().projection(Projections.fields(Projections.include("user_id","average_stars"))).into(new ArrayList<Document>());
		for (int user = 0; user < 20; user++) {
			Document userId = userIdList.get(user);
			List<Document> userReviewList = reviewTable.find(Filters.eq("user_id",userId.get("user_id").toString())).projection(Projections.fields(Projections.include("review_id","user_id","business_id","stars"))).into(new ArrayList<Document>());
            //System.out.println(userId.get("user_id"));
            ArrayList<Review> userReviews = new ArrayList<Review>();
            for (int userReview = 0; userReview < userReviewList.size(); userReview++) {
				Document newUserReview = userReviewList.get(userReview);
            	Review newReview = new Review(newUserReview.get("review_id").toString(),newUserReview.get("business_id").toString(), (int) newUserReview.get("stars"));
            	userReviews.add(newReview);
            }
            
            double userAvgStars = 0;
            try {
            	userAvgStars = (double) userId.get("average_stars");
            } catch (Exception e) {
            	e.printStackTrace();
            	userAvgStars = (int) userId.get("average_stars");
            }
            
            User newUser = new User(userId.get("user_id").toString(), userAvgStars, userReviews);
            userList.add(newUser);
            System.out.println("Added user " + user);
//            if(userReviewList.size() > 0) {
//				Document userReview = userReviewList.get(0);
//	            System.out.println(userReview.get("stars"));
//            }
        }
		
		//Testing Pearson correlation
		User user0 = userList.get(0);
		for (int user = 1; user < userList.size(); user++) {
			System.out.println("Pearson for 0 and " + user + ": " + pearsonCorrelation(user0, userList.get(user)));
		}
		
		//Testing Cosine similarity
		for (int user = 1; user < userList.size(); user++) {
			System.out.println("Cosine for 0 and " + user + ": " + cosineCorrelation(user0, userList.get(user)));
		}
		
		//Predicted rating for user 0 TODO: Weighted part needs finished
//		double absPearsonCorrSum = 0;
//		double weightedPearsonCorrSum = 0;
//		int predictionBusiness = 0;
//		for (int user = 2; user < userList.size(); user++) {
//			absPearsonCorrSum += Math.abs(pearsonCorrelation(userList.get(0),userList.get(user)));
//			//TODO: Figure out how to set business to predict for
//			weightedPearsonCorrSum += pearsonCorrelation(userList.get(0),userList.get(user)) * (userList.get(user).getReview(predictionBusiness).getStars()-userList.get(user).getAvgStars());
//		}
//		double normFactor = 1/absPearsonCorrSum;
//		
//		double predictedRatingForUser0 = userList.get(0).getAvgStars() + normFactor*weightedPearsonCorrSum;
//		System.out.println(predictedRatingForUser0);
		
		//To close connection
		mongoClient.close();
    }
    
    public static double pearsonCorrelation(User x, User y) {
    	List<Review> xUserReviews = x.getAllReviews();
    	List<Review> yUserReviews = y.getAllReviews();
    	
    	//Calculate average ratings for users x and y across all ratings
    	double avgRatingUserX = x.getAvgStars();
    	double avgRatingUserY = y.getAvgStars();
    	
    	double similNumerator = 0;
    	double similDenomX = 0;
    	double similDenomY = 0;
    	
    	//Find reviews for the same business
    	for(Review revX : xUserReviews) {
    		for(Review revY : yUserReviews) {
    			if (revX.getBusinessId().equals(revY.getBusinessId())) {
    				similNumerator += (revX.getStars()-avgRatingUserX)*(revY.getStars()-avgRatingUserY);
    				similDenomX += Math.pow((revX.getStars()-avgRatingUserX),2);
    				similDenomY += Math.pow((revY.getStars()-avgRatingUserY),2);
    			}
    		}
    	}
    	
    	if(similDenomX == 0 || similDenomY == 0) {
    		return 0;
    	}
    	
    	//Return the Pearson correlation similarity of users x and y
    	return similNumerator/(Math.sqrt(similDenomX)*Math.sqrt(similDenomY));
    	
    }
    
    public static double cosineCorrelation(User x, User y) {
    	List<Review> xUserReviews = x.getAllReviews();
    	List<Review> yUserReviews = y.getAllReviews();
    	
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
    			if (revX.getBusinessId().equals(revY.getBusinessId())) {
    				similNumerator += revX.getStars()*revY.getStars(); 
    			}
    		}
    	}
    	
    	//Return the cosine similarity similarity of users x and y
    	return similNumerator/(Math.sqrt(similDenomX)*Math.sqrt(similDenomY));
    	
    }
}
