package com.csc301.profilemicroservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.spi.DirStateFactory.Result;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;


@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			}
			session.close();
		}
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		
		String message = "";
		DbQueryStatus result = new DbQueryStatus(message, DbQueryExecResult.QUERY_OK);
		DbQueryStatus result2 = new DbQueryStatus(message, DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		try(Session session = ProfileMicroserviceApplication.driver.session())
		{
			try(Transaction trans = session.beginTransaction())
			{
				StatementResult checker = trans.run("MATCH(n:profile) WHERE EXISTS(n.userName) AND n.userName=$x RETURN n", 
						Values.parameters("x", userName));
				if(checker.hasNext())
				{
					return result2;
				}
			}
		}
		
		try(Session session = driver.session())
		{
			String playListName = userName+"-favouritesplaylist";
			//System.out.println(playListName);
			session.run("CREATE (a:profile {userName:$x,fullName:$y,password:$z})", 
					Values.parameters( "x", userName, "y", fullName, "z", password));
			session.run("CREATE (n:playlist{plName:$x})", Values.parameters("x", playListName));
			session.run("MATCH (a:profile), (b:playlist) Where a.userName = $x AND b.plName = $y CREATE (a)-[r:created]->(b)", 
					Values.parameters("x", userName, "y", playListName));
			session.close();
			return result;
		}
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		
		String message = "";
		DbQueryStatus result = new DbQueryStatus(message, DbQueryExecResult.QUERY_OK);
		DbQueryStatus result2 = new DbQueryStatus(message, DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		
		try(Session session = ProfileMicroserviceApplication.driver.session())
		{
			try(Transaction trans = session.beginTransaction())
			{
				StatementResult checker = trans.run("MATCH(n:profile) WHERE EXISTS(n.userName) AND n.userName=$x RETURN n", Values.parameters("x", userName));
				if(checker.hasNext() == false)
				{
					return result2;
				}
				
				checker = trans.run("MATCH(n:profile) WHERE EXISTS(n.userName) AND n.userName=$x RETURN n", Values.parameters("x", frndUserName));
				if(checker.hasNext()==false)
				{
					return result2;
				}
			}
		}
		
		try(Session session = driver.session())
		{
			session.run("MATCH (a:profile), (b:profile) Where a.userName = $x AND b.userName = $y CREATE (a)-[r:follows]->(b)", Values.parameters("x", userName, "y", frndUserName));
			session.close();
			return result;
		}
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		
		String message = "";
		DbQueryStatus result = new DbQueryStatus(message, DbQueryExecResult.QUERY_OK);
		DbQueryStatus result2 = new DbQueryStatus(message, DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		try(Session session = ProfileMicroserviceApplication.driver.session())
		{
			try(Transaction trans = session.beginTransaction())
			{
				StatementResult checker = trans.run("MATCH(n:profile) WHERE EXISTS(n.userName) AND n.userName=$x RETURN n", Values.parameters("x", userName));
				if(checker.hasNext() == false)
				{
					return result2;
				}
				
				checker = trans.run("MATCH(n:profile) WHERE EXISTS(n.userName) AND n.userName=$x RETURN n", Values.parameters("x", frndUserName));
				if(checker.hasNext()==false)
				{
					return result2;
				}
			}
		}
		
		try(Session session = driver.session())
		{
			session.run("MATCH(:profile{userName:$x}) - [r:follows] -> (:profile{userName:$y}) DELETE r", Values.parameters("x", userName, "y", frndUserName));
			session.close();
			return result;
		}
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
			
		String message = "";
		DbQueryStatus result = new DbQueryStatus(message, DbQueryExecResult.QUERY_OK);
		DbQueryStatus result2 = new DbQueryStatus(message, DbQueryExecResult.QUERY_ERROR_GENERIC);
		
		try(Session session = ProfileMicroserviceApplication.driver.session())
		{
			try(Transaction trans = session.beginTransaction())
			{
				StatementResult checker = trans.run("MATCH(n:profile) WHERE EXISTS(n.userName) AND n.userName=$x RETURN n", Values.parameters("x", userName));
				if(checker.hasNext() == false)
				{
					return result2;
				}
				
				checker = trans.run("MATCH(n:profile) WHERE EXISTS(n.userName) AND n.userName=$x RETURN n", Values.parameters("x", userName));
				if(checker.hasNext()==false)
				{
					return result2;
				}
			}
		}
		
		try(Session session = driver.session())
		{
			
			session.close();
			return result;
		}
	}
}
