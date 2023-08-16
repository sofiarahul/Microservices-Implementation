package com.csc301.profilemicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.csc301.profilemicroservice.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class ProfileController {
	public static final String KEY_USER_NAME = "userName";
	public static final String KEY_USER_FULLNAME = "fullName";
	public static final String KEY_USER_PASSWORD = "password";

	@Autowired
	private final ProfileDriverImpl profileDriver;

	@Autowired
	private final PlaylistDriverImpl playlistDriver;

	OkHttpClient client = new OkHttpClient();

	public ProfileController(ProfileDriverImpl profileDriver, PlaylistDriverImpl playlistDriver) {
		this.profileDriver = profileDriver;
		this.playlistDriver = playlistDriver;
	}

	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addProfile(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		DbQueryStatus result;
		
		String path = String.format("http://localhost:3002/profile");
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		
		//System.out.println(params.toString());
		
		if(params.containsKey("userName") && params.containsKey("fullName") && params.containsKey("password"))
		{
			//System.out.println("Correct body format");
			result = profileDriver.createUserProfile(params.get("userName"), 
					params.get("fullName"), params.get("password"));
		}
		else 
		{
			//System.out.print("Params missing");
			result = new DbQueryStatus("Params missing", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}

		response = Utils.setResponseStatus(response, result.getdbQueryExecResult(), result.getData());
		return response;
	}

	@RequestMapping(value = "/followFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> followFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		DbQueryStatus result;
		
		//System.out.println(userName);
		//System.out.println(friendUserName);
		//System.out.println(request.toString());
		
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		if(userName != null && friendUserName != null)
		{
			result = profileDriver.followFriend(userName, friendUserName);
		}
		else
		{
			result = new DbQueryStatus("Parameters Missing", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		response = Utils.setResponseStatus(response, result.getdbQueryExecResult(), result.getData());
		return response;
	}

	@RequestMapping(value = "/getAllFriendFavouriteSongTitles/{userName}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAllFriendFavouriteSongTitles(@PathVariable("userName") String userName,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		DbQueryStatus result;
		
		response.put("path", String.format("GET %s", Utils.getUrl(request)));
		
		if(userName != null)
		{
			result = profileDriver.getAllSongFriendsLike(userName);
		}
		else
		{
			result = new DbQueryStatus("Parameters Missing", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		response = Utils.setResponseStatus(response, result.getdbQueryExecResult(), result.getData());
		return response;
	}


	@RequestMapping(value = "/unfollowFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unfollowFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		DbQueryStatus result;
		
		//System.out.println(userName);
		//System.out.println(friendUserName);
		//System.out.println(request.toString());
		
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		if(userName != null && friendUserName != null)
		{
			result = profileDriver.unfollowFriend(userName, friendUserName);
		}
		else
		{
			result = new DbQueryStatus("Parameters Missing", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		response = Utils.setResponseStatus(response, result.getdbQueryExecResult(), result.getData());
		return response;
	}

	@RequestMapping(value = "/likeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> likeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		DbQueryStatus result;
		
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		if(userName != null && songId != null)
		{
			result = playlistDriver.likeSong(userName, songId);
		}
		else
		{
			result = new DbQueryStatus("Parameters Missing", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		response = Utils.setResponseStatus(response, result.getdbQueryExecResult(), result.getData());
		return response;
	}

	@RequestMapping(value = "/unlikeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unlikeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		DbQueryStatus result;
		
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		if(userName != null && songId != null)
		{
			result = playlistDriver.unlikeSong(userName, songId);
		}
		else
		{
			result = new DbQueryStatus("Parameters Missing", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		response = Utils.setResponseStatus(response, result.getdbQueryExecResult(), result.getData());
		return response;
	}

	@RequestMapping(value = "/deleteSongFromDb/{songId}", method = RequestMethod.DELETE)
	public @ResponseBody Map<String, Object> deleteSongFromDb(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		DbQueryStatus result;
		
		response.put("path", String.format("DELETE %s", Utils.getUrl(request)));
		
		if(songId != null)
		{
			result = playlistDriver.deleteSongFromDb(songId);
		}
		else
		{
			result = new DbQueryStatus("Parameters Missing", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		response = Utils.setResponseStatus(response, result.getdbQueryExecResult(), result.getData());
		return response;
	}
}