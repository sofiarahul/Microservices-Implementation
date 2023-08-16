package com.csc301.songmicroservice;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.csc301.songmicroservice.DbQueryExecResult;;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		// TODO Auto-generated method stub
	
		MongoCollection<Document> collection= db.getCollection("songs");
		
		DbQueryStatus queryStatus = null; 
		
		Document doc = Document.parse(songToAdd.toString());
		doc.remove("id");
		collection.insertOne(doc);
		songToAdd.setId((ObjectId)doc.get("_id"));

		queryStatus = new DbQueryStatus("Song added", DbQueryExecResult.QUERY_OK);
		queryStatus.setData(songToAdd.getJsonRepresentation().toMap());
		System.out.println(queryStatus.getData());
		
		return queryStatus;
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		// TODO Auto-generated method stub
		MongoCollection<Document> collection = db.getCollection("songs");
		DbQueryStatus queryStatus = null; 
		MongoCursor<Document> queryResult = null;
		Document doc=null;
		Song song=null;
		
		try {
			queryResult = collection.find(eq("_id",new ObjectId(songId))).iterator();
		}catch(Exception  e) {
			queryStatus = new DbQueryStatus("Not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
			return queryStatus;
		}
		
		
		if(queryResult.hasNext()) {
			doc = queryResult.next();
			
			song = new Song((String)doc.get("songName"), (String)doc.get("songArtistFullName"), (String)doc.get("songAlbum") );
			song.setId((ObjectId)doc.get("_id"));
			song.setSongAmountFavourites(Integer.parseInt((String)doc.get("songAmountFavourites")));
			
			queryStatus = new DbQueryStatus("Found", DbQueryExecResult.QUERY_OK);
			queryStatus.setData(song.getJsonRepresentation().toMap());
//			queryStatus.setData(doc);
		}else {
			queryStatus = new DbQueryStatus("Not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}
		
		
		return queryStatus;
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		// TODO Auto-generated method stub
		MongoCollection<Document> collection = db.getCollection("songs");
		DbQueryStatus queryStatus = null; 
		MongoCursor<Document> queryResult = null;
		Document doc=null;
		
		
		try {
			queryResult = collection.find(eq("_id",new ObjectId(songId))).iterator();
		}catch(Exception  e) {
			queryStatus = new DbQueryStatus("Not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
			return queryStatus;
		}
		
		
		if(queryResult.hasNext()) {
			doc = queryResult.next();
			
			queryStatus = new DbQueryStatus("Found", DbQueryExecResult.QUERY_OK);
			queryStatus.setData(doc.get("songName"));
//			queryStatus.setData(doc);
		}else {
			queryStatus = new DbQueryStatus("Not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}
		return queryStatus;
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		// TODO Auto-generated method stub
		MongoCollection<Document> collection = db.getCollection("songs");
		DbQueryStatus queryStatus = null; 
		DeleteResult queryResult = null;
		Document doc=null;
		
		
		try {
			queryResult = collection.deleteOne(eq("_id",new ObjectId(songId)));
		}catch(Exception  e) {
			queryStatus = new DbQueryStatus("Not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
			return queryStatus;
		}
		
		if(queryResult.getDeletedCount()!=0) {
//			URL url;
//			try {
			
			
//				url = new URL("http://localhost:3001/deleteAllSongsFromDb/"+songId);
//				HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
//				httpCon.setDoOutput(true);
//				httpCon.setRequestProperty(
//				    "Content-Type", "application/x-www-form-urlencoded" );
//				httpCon.setRequestMethod("DELETE");
//				httpCon.connect();
			
			
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			
			queryStatus = new DbQueryStatus("Deleted", DbQueryExecResult.QUERY_OK);
			// HAVE TO DELETE SONG FROM ALL PLAYLIST ON NEO4J
			
		}else {
			queryStatus = new DbQueryStatus("Not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}
		return queryStatus;
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// TODO Auto-generated method stub
		MongoCollection<Document> collection = db.getCollection("songs");
		DbQueryStatus queryStatus = null; 
		UpdateResult queryResult;
		
		System.out.println("songID "+ songId + " shouldDecrement "+ shouldDecrement);
		
		Document query = new Document();
		query.append("_id",new ObjectId(songId));
		
		Document change = new Document();
		Document inc = new Document();

		
		
//		try {
//			collection.updateOne(query, inc);
////			collection.find(query).iterator();
//
//		}catch(Exception  e) {
//			queryStatus = new DbQueryStatus("error happened", DbQueryExecResult.QUERY_ERROR_GENERIC);
//			return queryStatus;
//		}
		
		MongoCursor<Document> findResult = null;
		Document doc=null;
		try {
			findResult = collection.find(eq("_id",new ObjectId(songId))).iterator();
			if(findResult.hasNext()) {
				doc = findResult.next();
			}else {
				queryStatus = new DbQueryStatus("Not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				return queryStatus;
			}
			
			//KEEP SCORE AS STRING IN DB
			String favNum = (String) doc.get("songAmountFavourites");
			
			if(Integer.parseInt(favNum) == 0 && shouldDecrement) {
				queryStatus = new DbQueryStatus("cannnot decrement", DbQueryExecResult.QUERY_ERROR_GENERIC);
				return queryStatus;
			}
			
			if(shouldDecrement) {
				favNum = Integer.toString(Integer.parseInt(favNum) - 1);
			}else {
				favNum = Integer.toString(Integer.parseInt(favNum) + 1);
			}
			
			change.append("songAmountFavourites", favNum);
			inc.append("$set", change);	
			
			
			
			System.out.println(doc.get("songAmountFavourites"));
			
			queryResult = collection.updateOne(query, inc);
		}catch(Exception  e) {
			queryStatus = new DbQueryStatus("error happened", DbQueryExecResult.QUERY_ERROR_GENERIC);
			return queryStatus;
		}
		
		if(queryResult.getModifiedCount() !=0) {
			queryStatus = new DbQueryStatus("Updated ", DbQueryExecResult.QUERY_OK);
		}else {
			queryStatus = new DbQueryStatus("Not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}
		
//		queryStatus = new DbQueryStatus("default", DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		return queryStatus;
	}
}