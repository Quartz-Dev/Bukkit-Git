package com.quartzdev.bukkitgit;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.quartzdev.bukkitgit.gitevent.GitEvent;

public class Loggers {
	
	public static void logGitEvent(GitEvent event) {
		Logger l = Bukkit.getLogger();
		l.info(ChatColor.BLUE + "=============");
		l.info(ChatColor.BLUE + "New Github push on " + event.getRepositoryFullName() + " by " + event.getCommiter());
		l.info(ChatColor.BLUE + event.getCommitMessage());
		l.info(ChatColor.BLUE + "Compare: " + event.getCompareLink());
		l.info(ChatColor.BLUE + "=============");
		
	}
	
}
