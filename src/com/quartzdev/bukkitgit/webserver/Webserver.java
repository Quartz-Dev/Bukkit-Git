package com.quartzdev.bukkitgit.webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import org.bukkit.Bukkit;

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
				InetAddress client = connectionsocket.getInetAddress();
				// logMessage("Connection from: " + client.getHostAddress());
				// TODO Make message logs more useful.
				BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
				RequestType rt = null;
				String line = input.readLine();
				logMessage("First line: " + line);
				if (line.startsWith("GET")) {
					rt = RequestType.GET;
				} else if (line.startsWith("POST")) {
					rt = RequestType.POST;
				}
				
				ArrayList<String> headers = new ArrayList<String>();
				while (!(line = input.readLine()).equals("")) {
					logMessage("Headers: " + line);
					headers.add(line);
				}
				
				ArrayList<String> body = new ArrayList<String>();
				logMessage("Made it to body");
				if (input.read() != -1) {
					logMessage("Body is not null!");
					while ((line = input.readLine()) != null) {
						logMessage("Body: " + line);
						body.add(line);
					}
				} else {
					logMessage("Body is null");
				}
				
				logMessage("Passed body");
				
				WebRequest request = createWebRequest(rt, headers, body);
				
				DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());
				if (request.getType() == RequestType.POST) {
					int headerLoc;
					if ((headerLoc = request.getHeaders().indexOf("X-Github-Event:")) > 0) {
						sendHeader(output, "HTTP/1.0 200 OK");
						
						String event = request.getHeaders().get(headerLoc).split(":")[1];
						if (!event.toUpperCase().equals("PUSH")) {
							logMessage("Webhook unneccisarilly sends all requests. Blah fix me");
							// TODO Make message more humane
						}
					} else {
						sendHeader(output, "HTTP/1.0 400 Bad Request");
					}
				} else {
					sendHeader(output, "HTTP/1.0 405 Method Not Allowed");
				}
				String[] responseHeaders = { "Connection: close", "Server: Bukkit-Git Auto-Updater", "Content-Type: text/plain", "Allow: POST", "" };
				sendHeaders(output, responseHeaders);
				
				sendHeader(output, "Plugin updated.");
				
				input.close();
				output.close();
				// logMessage("Output closed");
				
			} catch (SocketException e) {
				logMessage("Server not running!");
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
	
	private WebRequest createWebRequest(RequestType rt, ArrayList<String> headers, ArrayList<String> body) {
		return new WebRequest(rt, headers, body);
	}
	
}
