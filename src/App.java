
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.Math;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

public class App {
	public static void main(String[] args) {

		// Initialize general use variables
		Logger logger = Logger.getLogger("org.mongodb.driver");
		logger.setLevel(Level.SEVERE);

		MongoClient mongoClient = new MongoClient("localhost", 27017);

		MongoDatabase database = mongoClient.getDatabase("data");

		MongoCollection<Document> userTable = database.getCollection("user");
		MongoCollection<Document> reviewTable = database.getCollection("review");

		// User inputs user_id
		Scanner scanUserId = new Scanner(System.in);
		System.out.println("Enter User ID:");

		String queriedUserId = scanUserId.nextLine().trim(); // "yWd8xZQY5F6QXhVNQ5TxtA";//"jHEl6lbZ_ToShziNmO-16w";
																// //"gSNq08XIdh_vvdm7USKunA";

		scanUserId.close();
		
		long start = System.currentTimeMillis();

		long startBuildQueriedUser = System.currentTimeMillis();
		
		// Put together queried user object
		List<Document> qUserInfo = userTable.find(Filters.eq("user_id", queriedUserId))
				.projection(Projections.fields(Projections.include("user_id", "average_stars")))
				.into(new ArrayList<Document>());
		
		if(qUserInfo.size() == 0) {
			System.out.println("User " + queriedUserId + " does not exist. Try another user ID.");
			mongoClient.close();
			return;
		}
		
		List<Document> qUserReviewsList = reviewTable.find(Filters.eq("user_id", queriedUserId))
				.projection(Projections.fields(Projections.include("review_id", "user_id", "business_id", "stars")))
				.into(new ArrayList<Document>());
		ArrayList<Review> qUserReviews = new ArrayList<Review>();
		double qUserAvgStars = 0;
		try {
			qUserAvgStars = (double) qUserInfo.get(0).get("average_stars");
		} catch (Exception e) {
			qUserAvgStars = (int) qUserInfo.get(0).get("average_stars");
		}
		for (int userReview = 0; userReview < qUserReviewsList.size(); userReview++) {
			Document newUserReview = qUserReviewsList.get(userReview);
			Review newReview = new Review(newUserReview.get("review_id").toString(),
					newUserReview.get("business_id").toString(), (int) newUserReview.get("stars"));
			qUserReviews.add(newReview);
		}

		User queriedUser = new User(qUserInfo.get(0).getString("user_id"), qUserAvgStars, qUserReviews);
		
		long finishBuildQueriedUser = System.currentTimeMillis();
		System.out.println("Build User time: " + (finishBuildQueriedUser - startBuildQueriedUser)/1000);

		long startRelatedUserList = System.currentTimeMillis();
		
		// Build related users list based on shared reviewed businesses
		ArrayList<User> relatedUserList = new ArrayList<User>();
		List<Document> queriedUserReviewList = reviewTable.find(Filters.eq("user_id", queriedUserId))
				.projection(Projections.fields(Projections.include("review_id", "user_id", "business_id", "stars")))
				.into(new ArrayList<Document>());
		for (int rev = 0; rev < queriedUserReviewList.size(); rev++) {
			// This should be used as the list of users to compare to
			List<Document> relatedUserId = reviewTable
					.find(Filters.eq("business_id", queriedUserReviewList.get(rev).get("business_id")))
					.projection(Projections.fields(Projections.include("user_id"))).into(new ArrayList<Document>());
			for (int relatedUser = 0; relatedUser < relatedUserId.size(); relatedUser++) {
				List<Document> userExists = userTable
						.find(Filters.eq("user_id", relatedUserId.get(relatedUser).get("user_id")))
						.projection(Projections.fields(Projections.include("user_id", "average_stars")))
						.into(new ArrayList<Document>());
				if (userExists.size() > 0) {
					double userAvgStars = 0;
					try {
						userAvgStars = (double) userExists.get(0).get("average_stars");
					} catch (Exception e) {
						userAvgStars = (int) userExists.get(0).get("average_stars");
					}
					relatedUserList.add(
							new User(relatedUserId.get(relatedUser).get("user_id").toString(), userAvgStars, null));
				}
			}
			// System.out.println("Done with review " + rev); //Print used to show progress
			// of queries
		}
		
		long finishRelatedUserList = System.currentTimeMillis();
		System.out.println("Build Related User time: " + (finishRelatedUserList - startRelatedUserList)/1000);

		ArrayList<User> userList = new ArrayList<User>();
		
		long startRelatedUserReviews = System.currentTimeMillis();

		// Create related User instantiations
		for (int user = 0; user < 20; user++) {
			User relatedUser = relatedUserList.get(user);

			List<Document> userReviewList = reviewTable.find(Filters.eq("user_id", relatedUser.getId()))
					.projection(Projections.fields(Projections.include("review_id", "user_id", "business_id", "stars")))
					.into(new ArrayList<Document>());

			// Add all reviews to user
			ArrayList<Review> userReviews = new ArrayList<Review>();
			for (int userReview = 0; userReview < userReviewList.size(); userReview++) {
				Document newUserReview = userReviewList.get(userReview);
				Review newReview = new Review(newUserReview.get("review_id").toString(),
						newUserReview.get("business_id").toString(), (int) newUserReview.get("stars"));
				userReviews.add(newReview);
			}

			relatedUserList.get(user).setAllReviews(userReviews);
			if (!relatedUserList.get(user).getId().equals(queriedUser.getId())) {
				userList.add(relatedUserList.get(user));
			}
			// System.out.println("Updated user " + user); //Print used to show progress of
			// queries
		}
		

		long finishRelatedUserReviews = System.currentTimeMillis();
		System.out.println("Build Related User reviews time: " + (finishRelatedUserReviews - startRelatedUserReviews)/1000);

		// Get recommendations
		ArrayList<Recommendation> recommendationList = new ArrayList<Recommendation>();

		for (User x : userList) {
			x.setCommonAndPossReviews(queriedUser.getAllReviews());

			for (Review rev : x.possibleRecs) {
				double expRatP = expRatP(queriedUser, userList, rev);
				double expRatC = expRatC(queriedUser, userList, rev);
				recommendationList.add(new Recommendation(rev.getBusinessId(), expRatP, expRatC));
			}
		}

		// Sort by Pearson Rating
		Collections.sort(recommendationList, new Comparator<Recommendation>() {
			@Override
			public int compare(Recommendation rec1, Recommendation rec2) {
				if (rec1.getExpectedRatingP() < rec2.getExpectedRatingP())
					return 1;
				if (rec1.getExpectedRatingP() > rec2.getExpectedRatingP())
					return -1;
				return 0;
			}
		});

		for (int recNum = 0; recNum < 10; recNum++) {
			System.out.println("Recommending " + recommendationList.get(recNum).getBusinessId() + " with Pearson: "
					+ recommendationList.get(recNum).getExpectedRatingP());
		}

		// Sort by Cosine Rating
		Collections.sort(recommendationList, new Comparator<Recommendation>() {
			@Override
			public int compare(Recommendation rec1, Recommendation rec2) {
				if (rec1.getExpectedRatingC() < rec2.getExpectedRatingC())
					return 1;
				if (rec1.getExpectedRatingC() > rec2.getExpectedRatingC())
					return -1;
				return 0;
			}
		});

		for (int recNum = 0; recNum < 10; recNum++) {
			System.out.println("Recommending " + recommendationList.get(recNum).getBusinessId() + " with Cosine: "
					+ recommendationList.get(recNum).getExpectedRatingC());
		}
		
		long finish = System.currentTimeMillis();
		
		System.out.println("Total time: " + (finish-start)/1000);

		// To close connection
		mongoClient.close();
	}

	public static double pearsonCorrelation(User x, User y) {
		List<Review> xUserReviews = x.getAllReviews();
		List<Review> yUserReviews = y.getAllReviews();

		// Calculate average ratings for users x and y across all ratings
		double avgRatingUserX = avgRating(x);
		double avgRatingUserY = avgRating(y);

		double covariance = 0;
		double varianceX = 0;
		double varianceY = 0;

		// Find reviews for the same business
		for (Review revX : xUserReviews) {
			for (Review revY : yUserReviews) {
				if (revX.getBusinessId().equals(revY.getBusinessId())) { 
					covariance += Math.abs((revX.getStars() - avgRatingUserX) * (revY.getStars() - avgRatingUserY));
					varianceX += Math.pow((revX.getStars() - avgRatingUserX), 2);
					varianceY += Math.pow((revY.getStars() - avgRatingUserY), 2);
				}
			}
		}

		if (varianceX == 0 || varianceY == 0) {
			return 0;
		}

		// Return the Pearson correlation similarity of users x and y
		return covariance / (Math.sqrt(varianceX) * Math.sqrt(varianceY));
	}

	public static double cosineCorrelation(User x, User y) {
		List<Review> xUserReviews = x.getAllReviews();
		List<Review> yUserReviews = y.getAllReviews();

		double dotProd = 0;
		double normX = 0;
		double normY = 0;

		// Sum of the squared ratings of all reviews of user x
		for (Review revX : xUserReviews) {
			normX += Math.pow(revX.getStars(), 2);
		}

		// Sum of the squared ratings of all reviews of user y
		for (Review revY : yUserReviews) {
			normY += Math.pow(revY.getStars(), 2);
		}

		// Find reviews for the same business
		for (Review revX : xUserReviews) {
			for (Review revY : yUserReviews) {
				if (revX.getBusinessId().equals(revY.getBusinessId())) { 
					dotProd += revX.getStars() * revY.getStars();
				}
			}
		}

		// Return the Pearson correlation similarity of users x and y
		return dotProd / (Math.sqrt(normX) * Math.sqrt(normY));
	}

	private static double avgRating(User user) {
		List<Review> userReviews = user.getAllReviews();
		double avgRating = 0;
		int numRatings = userReviews.size();

		for (Review rev : userReviews) {
			avgRating += rev.getStars();
		}

		return avgRating / numRatings;
	}

	private static double cosineK(User x, List<User> userList) {
		double k;
		double summation = 0;
		for (User y : userList) {
			double simil = cosineCorrelation(x, y);
			simil = Math.abs(simil);
			summation += simil;
		}
		k = 1 / summation;

		return k;
	}

	private static double pearsonK(User x, List<User> userList) {
		double k;
		double summation = 0;
		for (User y : userList) {
			double simil = pearsonCorrelation(x, y);
			simil = Math.abs(simil);
			summation += simil;
		}
		k = 1 / summation;

		return k;
	}

	private static double expRatP(User x, List<User> userList, Review rev) {
		double expRat;
		double k = pearsonK(x, userList);
		double summation = 0;
		for (User y : userList) {
			double simil = pearsonCorrelation(x, y);
			double rating = rev.getStars();
			summation += (simil * rating);
		}
		expRat = (k * summation);
		return expRat;
	}

	public static double expRatC(User x, List<User> userList, Review rev) {
		double expRat;
		double k = cosineK(x, userList);
		double summation = 0;
		for (User y : userList) {
			double simil = cosineCorrelation(x, y);
			double rating = rev.getStars();
			summation += (simil * rating);
		}
		expRat = (k * summation);
		return expRat;
	}
}