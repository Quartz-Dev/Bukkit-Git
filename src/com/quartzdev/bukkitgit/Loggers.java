package com.quartzdev.bukkitgit;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.fusesource.jansi.Ansi;

import com.quartzdev.bukkitgit.push.gitevent.GitEvent;

public class Loggers {
	
	private static final String color = Ansi.ansi().fg(Ansi.Color.CYAN) + "";
	private static final String endColor = Ansi.ansi().fg(Ansi.Color.DEFAULT) + "";
	
	public static void logGitEvent(GitEvent event) {
		Logger l = Bukkit.getLogger();
		
		l.info(color + "=============" + endColor);
		l.info(color + "New Github push to " + event.getRepositoryFullName() + " by " + event.getCommiter() + endColor);
		for (String s : event.getCommitMessage().split("\\n")) {
			l.info(color + "Message: " + s + endColor);
		}
		l.info(color + "Compare: " + event.getCompareLink() + endColor);
		l.info(color + "The plugin will now download and install." + endColor);
	}
	
	public static void logFinishedInstalling() {
		Logger l = Bukkit.getLogger();
		l.info(color + "The plugin is now done installing." + endColor);
		l.info(color + "=============" + endColor);
		
	}
	
	public static void logMessage(String message) {
		Logger l = Bukkit.getLogger();
		l.info("[Bukkit-Git] " + message);
	}
	
	public static void logError(String message) {
		Logger l = Bukkit.getLogger();
		l.severe("[Bukkit-Git] " + message);
	}
	
}
