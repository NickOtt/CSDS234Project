
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Executor {

	public static void main(String[] args) {
		
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );

		MongoDatabase database = mongoClient.getDatabase( "data" );
		
		MongoCollection<Document> collection = database.getCollection("user");
		
		System.out.println(collection.find().first().toJson());
		
		//To close connection
		mongoClient.close();
		
	}

}
