package com.quartzdev.bukkitgit.webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import org.bukkit.Bukkit;

import com.quartzdev.bukkitgit.gitevent.GitEventParser;

public class Webserver implements IWebserver, Runnable {
	
	private int port;
	ServerSocket serverSocket;
	
	public Webserver(int port) {
		this.port = port;
		serverSocket = null;
	}
	
	@Override
	public void run() {
		logMessage("Starting server...");
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			// TODO disable plugin.
			e.printStackTrace();
			return;
		}
		do {
			try {
				Socket connectionsocket = serverSocket.accept();
				// TODO Make message logs more useful.
				BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
				RequestType rt = null;
				String line = input.readLine();
				// logMessage("First line: " + line);
				if (line.startsWith("GET")) {
					rt = RequestType.GET;
				} else if (line.startsWith("POST")) {
					rt = RequestType.POST;
				}
				
				boolean hasBody = false;
				int contentLength = 0;
				ArrayList<String> headers = new ArrayList<String>();
				while (!(line = input.readLine()).equals("")) {
					// logMessage("Headers: " + line);
					if (line.startsWith("Content-Length:")) {
						hasBody = true;
						contentLength = Integer.parseInt(line.split(": ")[1]);
					}
					headers.add(line);
				}
				
				String body = "";
				if (hasBody) {
					int i;
					while (contentLength > 0) {
						i = input.read();
						char c = (char) i;
						body += c;
						contentLength--;
					}
				}
				
				WebRequest request = createWebRequest(rt, headers, body);
				
				boolean worked = false;
				DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());
				if (request.getType() == RequestType.POST) {
					int headerLoc;
					if ((headerLoc = request.getHeaders().indexOf("X-GitHub-Event: push")) > 0) {
						sendHeader(output, "HTTP/1.0 200 OK");
						worked = true;
						
						// logMessage("It worked");
						GitEventParser.createNewGitEvent(request);
					} else if ((headerLoc = request.getHeaders().indexOf("X-GitHub-Event: ping")) > 0) {
						sendHeader(output, "HTTP/1.0 200 OK");
						worked = true;
						
						// TODO logMessage("Ping received.");
					} else {
						sendHeader(output, "HTTP/1.0 400 Bad Request");
						// TODO log message that unneccessary thing was called
					}
					// logMessage("headerLoc: " + headerLoc);
				} else {
					sendHeader(output, "HTTP/1.0 405 Method Not Allowed");
				}
				String[] responseHeaders = { "Connection: close", "Server: Bukkit-Git Auto-Updater", "Content-Type: text/plain", "Allow: POST", "" };
				sendHeaders(output, responseHeaders);
				
				if (worked) {
					sendHeader(output, "Plugin updated.");
				}
				
				input.close();
				output.close();
				// logMessage("Output closed");
				
			} catch (SocketException e) {
				// TODO? ignore
			} catch (Exception e) {
				// TODO
				e.printStackTrace();
				closeServer();
			}
		} while (!serverSocket.isClosed());
		closeServer();
		
	}
	
	@Override
	public void onShutdown() {
		// TODO Auto-generated method stub
		closeServer();
		
	}
	
	public void logMessage(Object contents) {
		java.util.logging.Logger log = Bukkit.getLogger();
		log.info("Stuff from Web: " + contents);
	}
	
	private void closeServer() {
		logMessage("Stopping server");
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendHeaders(DataOutputStream output, String[] headers) {
		for (String header : headers) {
			try {
				output.writeBytes(header + "\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendHeader(DataOutputStream output, String header) {
		try {
			output.writeBytes(header + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private WebRequest createWebRequest(RequestType rt, ArrayList<String> headers, String body) {
		return new WebRequest(rt, headers, body);
	}
	
}
