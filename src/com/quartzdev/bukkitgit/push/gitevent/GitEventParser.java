package com.quartzdev.bukkitgit.push.gitevent;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.json.JSONObject;

import com.quartzdev.bukkitgit.Loggers;
import com.quartzdev.bukkitgit.push.webserver.WebRequest;

public class GitEventParser {
	private static final String compType = "zipball";
	
	// Maybe make this customizable in the future if it doesn't work on all
	// Operating Systems.
	
	private PluginManager pm;
	
	public GitEventParser(PluginManager pluginManager) {
		// TODO Auto-generated constructor stub
		this.pm = pluginManager;
	}
	
	public void createNewGitEvent(WebRequest wr, Plugin plugin) {
		JSONObject json = new JSONObject(wr.getContent());
		
		String masterBranch = json.getJSONObject("repository").getString("master_branch");
		String defaultBranch = json.getJSONObject("repository").getString("default_branch");
		String repositoryFullName = json.getJSONObject("repository").getString("full_name");
		String repositoryName = json.getJSONObject("repository").getString("name");
		String compressionType = compType;
		String compareLink = json.getString("compare");
		String commiter = json.getJSONObject("head_commit").getJSONObject("committer").getString("username");
		String commitMessage = json.getJSONObject("head_commit").getString("message");
		
		GitEvent event = new GitEvent(wr.getType(), wr.getHeaders(), wr.getContent(), masterBranch, defaultBranch, repositoryFullName, repositoryName, compressionType, compareLink, commiter, commitMessage);
		Loggers.logGitEvent(event);
		
		GitDownloader downloader = new GitDownloader(pm);
		downloader.download();
		
	}
	
}
