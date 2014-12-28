package com.quartzdev.bukkitgit.webserver;

import java.util.ArrayList;

public class WebRequest {
	
	private RequestType type;
	private ArrayList<String> headers;
	private String content;
	
	public WebRequest(RequestType type, ArrayList<String> headers, String content) {
		this.type = type;
		this.headers = headers;
		this.content = content;
	}
	
	public RequestType getType() {
		return type;
	}
	
	public ArrayList<String> getHeaders() {
		return headers;
	}
	
	public String getContent() {
		return content;
	}
	
}
