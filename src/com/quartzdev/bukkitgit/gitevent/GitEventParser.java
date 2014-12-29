package com.quartzdev.bukkitgit.gitevent;

import org.json.JSONObject;

import com.quartzdev.bukkitgit.Loggers;
import com.quartzdev.bukkitgit.webserver.WebRequest;

public class GitEventParser {
	
	public static void createNewGitEvent(WebRequest wr) {
		JSONObject json = new JSONObject(wr.getContent());
		
		String masterBranch = json.getJSONObject("repository").getString("master_branch");
		String defaultBranch = json.getJSONObject("repository").getString("default_branch");
		String repositoryFullName = json.getJSONObject("repository").getString("full_name");
		String repositoryName = json.getJSONObject("repository").getString("name");
		String compressionType = "zipball"; // TODO Customizable?
		String compareLink = json.getString("compare");
		String commiter = json.getJSONObject("head_commit").getJSONObject("committer").getString("username");
		String commitMessage = json.getJSONObject("head_commit").getString("message");
		
		GitEvent event = new GitEvent(wr.getType(), wr.getHeaders(), wr.getContent(), masterBranch, defaultBranch, repositoryFullName, repositoryName, compressionType, compareLink, commiter, commitMessage);
		Loggers.logGitEvent(event);
		
		GitDownloader downloader = new GitDownloader(event);
		Thread t = new Thread(downloader);
		t.start();
		
	}
	
}
