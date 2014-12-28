package com.quartzdev.bukkitgit;

import org.bukkit.plugin.java.JavaPlugin;

import com.quartzdev.bukkitgit.webserver.Webserver;

public class MainClass extends JavaPlugin {
	
	private int port;
	private Webserver webserver;
	
	public void onEnable() {
		port = 8022;
		
		webserver = new Webserver(port);
		Thread thread = new Thread(webserver);
		thread.start();
	}
	
	public void onDisable() {
		webserver.onShutdown();
	}
	
}
