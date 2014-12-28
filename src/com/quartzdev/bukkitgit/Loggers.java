package com.quartzdev.bukkitgit;

import java.util.logging.Logger;

import org.bukkit.Bukkit;

import com.quartzdev.bukkitgit.gitevent.GitEvent;

public class Loggers {
	
	public static void logGitEvent(GitEvent event) {
		Logger l = Bukkit.getLogger();
		String color = "§9";
		l.info(color + "=============");
		l.info(color + "New Github push on " + event.getRepositoryFullName() + " by " + event.getCommiter());
		l.info(color + event.getCommitMessage());
		l.info(color + "Compare: " + event.getCompareLink());
		l.info(color + "=============");
		
	}
	
}
