package com.quartzdev.bukkitgit.webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.SignatureException;
import java.util.ArrayList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import net.minecraft.util.org.apache.commons.codec.binary.Hex;

import org.bukkit.plugin.Plugin;

import com.quartzdev.bukkitgit.Loggers;
import com.quartzdev.bukkitgit.gitevent.GitEventParser;

public class Webserver implements Runnable {
	
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	private static final int maxTimesPingRecieved = 3;
	
	private int port;
	private String secret;
	private int timesPingRecieved;
	ServerSocket serverSocket;
	Plugin plugin;
	
	public Webserver(int port, String secret, Plugin plugin) {
		this.port = port;
		serverSocket = null;
		this.secret = secret;
		timesPingRecieved = 0;
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		Loggers.logMessage("Starting webhook server...");
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			Loggers.logMessage("The webhook server could not be started.");
			Loggers.logMessage("You are no longer receiving automatic updates");
			Loggers.logMessage("Try reloading the plugin, if the problem persits please notify the authors");
			return;
		}
		
		do {
			try {
				Socket connectionsocket = serverSocket.accept();
				BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
				String line = input.readLine();
				String rt = line.split(" ")[0];
				
				boolean hasBody = false;
				int contentLength = 0;
				ArrayList<String> headers = new ArrayList<String>();
				while (!(line = input.readLine()).equals("")) {
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
				if (request.getType().equals("POST")) {
					if (request.getHeaders().indexOf("X-GitHub-Event: push") >= 0) {
						
						boolean secure = false;
						for (String header : request.getHeaders()) {
							if (header.startsWith("X-Hub-Signature:")) {
								String recievedDigest = header.split("sha1=")[1];
								String ourDigest = hmac(request.getContent(), secret);
								if (recievedDigest.equals(ourDigest)) {
									secure = true;
								}
							}
						}
						
						if (secure) {
							sendHeader(output, "HTTP/1.0 200 OK");
							worked = true;
							GitEventParser.createNewGitEvent(request, plugin);
						} else {
							sendHeader(output, "HTTP/1.0 400 Bad Request");
						}
						
					} else if (request.getHeaders().indexOf("X-GitHub-Event: ping") >= 0) {
						sendHeader(output, "HTTP/1.0 200 OK");
						worked = true;
						
						if (timesPingRecieved < maxTimesPingRecieved) {
							Loggers.logMessage("Ping received from GitHub.");
							timesPingRecieved++;
						}
					} else {
						sendHeader(output, "HTTP/1.0 501 Not Implemented");
						if (timesPingRecieved < maxTimesPingRecieved) {
							Loggers.logMessage("An unnecessary GitHub event was recieved. The only events required are the push event.");
							timesPingRecieved++;
						}
					}
				} else {
					sendHeader(output, "HTTP/1.0 405 Method Not Allowed");
				}
				String[] responseHeaders = { "Connection: close", "Server: Bukkit-Git Auto-Updater", "Content-Type: text/plain", "Allow: POST", "" };
				sendHeaders(output, responseHeaders);
				
				if (worked) {
					sendHeader(output, "Request Acknowledged.");
				}
				
				input.close();
				output.close();
				
			} catch (SocketException e) {
				// TODO? ignore
			} catch (Exception e) {
				Loggers.logMessage("An exception occurred whilst running the webhook server");
				e.printStackTrace();
				Loggers.logMessage("The webhook server will now shut down.");
				closeServer();
			}
		} while (!serverSocket.isClosed());
		
	}
	
	public void onShutdown() {
		closeServer();
	}
	
	private void closeServer() {
		Loggers.logMessage("Webhook server stopping...");
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
	
	private WebRequest createWebRequest(String requestType, ArrayList<String> headers, String body) {
		return new WebRequest(requestType, headers, body);
	}
	
	private static String hmac(String data, String key) throws java.security.SignatureException {
		String result;
		try {
			
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(data.getBytes());
			result = Hex.encodeHexString(rawHmac);
			
		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
		return result;
	}
	
}
