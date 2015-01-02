package com.quartzdev.bukkitgit.push.webserver;

import java.util.ArrayList;

public class WebRequest {
	
	private String type;
	private ArrayList<String> headers;
	private String content;
	
	public WebRequest(String type, ArrayList<String> headers, String content) {
		this.type = type;
		this.headers = headers;
		this.content = content;
	}
	
	public String getType() {
		return type;
	}
	
	public ArrayList<String> getHeaders() {
		return headers;
	}
	
	public String getContent() {
		return content;
	}
	
}
