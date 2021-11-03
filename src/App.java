

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;

public class App 
{
    public static void main( String[] args )
    {
    	//Test stuff
    	MongoClient mongoClient = new MongoClient( "localhost" , 27017 );

		MongoDatabase database = mongoClient.getDatabase( "data" );
		
		MongoCollection<Document> collection = database.getCollection("user");
		
		System.out.println(collection.find().first().toJson());
		
		JSONObject firstDoc = new JSONObject(collection.find().first().toJson());
		System.out.println(firstDoc.get("name").toString());
		
		//Find all user id
		List<Document> userIdList = collection.find().projection(Projections.fields(Projections.include("user_id"))).into(new ArrayList<Document>());
		for (int i = 0; i < 20; i++) {
			Document userId = userIdList.get(i);
            System.out.println(userId.get("user_id"));
        }
		
		ArrayList<User> userList = new ArrayList<User>();
		//for() {
			//User newUser = new User(userId);
		//}
		
		//for(User user: userList) {
			
		//}
		
		//To close connection
		mongoClient.close();
    }
}
