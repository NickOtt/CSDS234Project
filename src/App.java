

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class App 
{
    public static void main( String[] args )
    {
    	MongoClient mongoClient = new MongoClient( "localhost" , 27017 );

		MongoDatabase database = mongoClient.getDatabase( "data" );
		
		MongoCollection<Document> collection = database.getCollection("user");
		
		System.out.println(collection.find().first().toJson());
		
		JSONObject firstDoc = new JSONObject(collection.find().first().toJson());
		System.out.println(firstDoc.get("name").toString());
		
		//To close connection
		mongoClient.close();
    }
}
