package com.quartzdev.bukkitgit.gitevent;

import org.bukkit.Bukkit;

public class GitDownloader implements Runnable {
	
	private GitEvent event;
	
	public GitDownloader(GitEvent event) {
		this.event = event;
	}
	
	@Override
	public void run() {
		// TODO allow choice for master branch or default branch maybe
		
		String downloadURL = "https://api.github.com/repos/" + event.getRepositoryFullName() + "/" + event.getCompressionType() + "/" + event.getDefaultBranch();
		Bukkit.broadcastMessage("Download URL: " + downloadURL);
	}
}
