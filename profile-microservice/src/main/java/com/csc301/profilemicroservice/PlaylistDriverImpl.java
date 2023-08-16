package com.csc301.profilemicroservice;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}

	
	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
		String message = "";
		DbQueryStatus result = new DbQueryStatus(message, DbQueryExecResult.QUERY_OK);
		DbQueryStatus result2 = new DbQueryStatus(message, DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url("http://localhost:3001/getSongById/"+songId).build();
		
		try {
			Response response = client.newCall(request).execute();
			JSONObject deserialized = new JSONObject(response.body().string());
			response.close();
			
			System.out.println(deserialized);
			
			if(!deserialized.getString("status").equals("OK"))
			{
				System.out.println("before returning error");
				return result2;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try(Session session = ProfileMicroserviceApplication.driver.session())
		{
			try(Transaction trans = session.beginTransaction())
			{
				System.out.println("before checking for user node");
				StatementResult checker = trans.run("MATCH(n:Profile) WHERE EXISTS(n.userName) AND n.userName=$x RETURN n",
						Values.parameters("x", userName));
				if(checker.hasNext() == false)
				{
					return result2;
				}
				checker = trans.run("MATCH(n:Song) WHERE EXISTS(n.songId) AND n.songId=$x RETURN n", Values.parameters("x", songId));
				if(checker.hasNext() == false)
				{
					System.out.println("creating node");
					trans.run("CREATE (a:Song {songId:$x})", Values.parameters("x", songId));
				}

			}
		}
		
		try(Session session = driver.session())
		{
			String playListName = userName+"-favouritesplaylist";
			System.out.println("before creating relationship");
			session.run("MATCH (a:Playlist{plName:$x}) MATCH (b:Song{songId:$y}) MERGE (a)-[r:INCLUDES]->(b)",
					Values.parameters("x", playListName, "y", songId));
			session.close();
			return result;
		}
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		
		//check to make sure node has other neighbours, if it doesn't delete it?
		String message = "";
		DbQueryStatus result = new DbQueryStatus(message, DbQueryExecResult.QUERY_OK);
		DbQueryStatus result2 = new DbQueryStatus(message, DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		
		try(Session session = ProfileMicroserviceApplication.driver.session())
		{
			try(Transaction trans = session.beginTransaction())
			{
				StatementResult checker = trans.run("MATCH(n:Profile) WHERE EXISTS(n.userName) AND n.userName=$x RETURN n", Values.parameters("x", userName));
				if(checker.hasNext() == false)
				{
					return result2;
				}

			}
		}
		
		try(Session session = driver.session())
		{
			String playListName = userName+"-favouritesplaylist";
			session.run("CREATE (a:Song {songId:$x})", Values.parameters("x", songId));
			session.run("MATCH (:Playlist {plName: $x})-[r:INCLUDES]-(:Song {songId: $y}) DELETE r",
					Values.parameters("x", playListName, "y", songId));
			session.close();
			return result;
		}
		
		/*
		 * session.
		 * run("MATCH (:Playlist {plName: $x})-[r:INCLUDES]-(:Song {songId: $y}) DELETE r"
		 * , Values.parameters("x", userName+"-favouritesplaylist", "y", "songId"));
		 */
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
		
		String message = "";
		DbQueryStatus result = new DbQueryStatus(message, DbQueryExecResult.QUERY_OK);
		DbQueryStatus result2 = new DbQueryStatus(message, DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		try(Session session = ProfileMicroserviceApplication.driver.session())
		{
			try(Transaction trans = session.beginTransaction())
			{
				StatementResult checker = trans.run("MATCH(n:Song) WHERE EXISTS(n.songId) AND n.songId=$x RETURN n",
						Values.parameters("x", songId));
				if(checker.hasNext() == false)
				{
					return result2;
				}
			}
		}
		
		try(Session session = driver.session())
		{
			session.run("MATCH(a:Song{songId:$x}) DETACH DELETE a", Values.parameters("x", songId));
			session.close();
			return result;
		}
		
	}
}
