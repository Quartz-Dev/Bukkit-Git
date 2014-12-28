package com.quartzdev.bukkitgit.webserver;

import java.util.ArrayList;

public class WebRequest {
	
	public RequestType type;
	public ArrayList<String> headers;
	public ArrayList<String> content;
	
	protected WebRequest(RequestType type, ArrayList<String> headers, ArrayList<String> content) {
		this.type = type;
		this.headers = headers;
		this.content = content;
	}
	
	protected RequestType getType() {
		return type;
	}
	
	protected ArrayList<String> getHeaders() {
		return headers;
	}
	
	protected ArrayList<String> getContent() {
		return content;
	}
	
}
