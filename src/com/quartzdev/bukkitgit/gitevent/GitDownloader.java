package com.quartzdev.bukkitgit.gitevent;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.minecraft.util.org.apache.commons.io.FileUtils;

public class GitDownloader implements Runnable {
	
	private GitEvent event;
	
	public GitDownloader(GitEvent event) {
		this.event = event;
	}
	
	@Override
	public void run() {
		try {
			// TODO allow choice for master branch or default branch maybe
			
			URL website = new URL("https://api.github.com/repos/" + event.getRepositoryFullName() + "/" + event.getCompressionType() + "/" + event.getDefaultBranch());
			
			File dest = new File("plugins" + File.separator + "Bukkit-Git" + File.separator + "downloads" + File.separator + "info.zip");
			FileUtils.copyURLToFile(website, dest);
		} catch (IOException e) {
			// TODO Make it do something useful
			e.printStackTrace();
		}
		
	}
}
