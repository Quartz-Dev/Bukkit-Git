package com.quartzdev.bukkitgit;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.fusesource.jansi.Ansi;

import com.quartzdev.bukkitgit.gitevent.GitEvent;

public class Loggers {
	
	public static void logGitEvent(GitEvent event) {
		Logger l = Bukkit.getLogger();
		String color = Ansi.ansi().fg(Ansi.Color.BLUE) + "";
		String endColor = Ansi.ansi().fg(Ansi.Color.DEFAULT) + "";
		
		l.info(color + "=============" + endColor);
		l.info(color + "New Github push on " + event.getRepositoryFullName() + " by " + event.getCommiter() + endColor);
		l.info(color + event.getCommitMessage() + endColor);
		l.info(color + "Compare: " + event.getCompareLink() + endColor);
		l.info(color + "=============" + endColor);
		
	}
	
}
