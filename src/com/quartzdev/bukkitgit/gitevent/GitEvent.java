package com.quartzdev.bukkitgit.gitevent;

import java.util.ArrayList;

import com.quartzdev.bukkitgit.webserver.RequestType;
import com.quartzdev.bukkitgit.webserver.WebRequest;

public class GitEvent extends WebRequest {
	
	protected GitEvent(RequestType type, ArrayList<String> headers, ArrayList<String> content) {
		super(type, headers, content);
		// TODO Auto-generated constructor stub
	}
	
	private EventType eventType;
	
}
