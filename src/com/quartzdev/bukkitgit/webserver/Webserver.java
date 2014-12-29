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
import javax.xml.bind.DatatypeConverter;

import org.bukkit.Bukkit;

import com.quartzdev.bukkitgit.gitevent.GitEventParser;

public class Webserver implements Runnable {
	
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	private int port;
	private String secret;
	ServerSocket serverSocket;
	
	public Webserver(int port, String secret) {
		this.port = port;
		serverSocket = null;
		this.secret = secret;
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
				String line = input.readLine();
				RequestType rt = setRequestType(line);
				
				// logMessage("First line: " + line);
				
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
					if (request.getHeaders().indexOf("X-GitHub-Event: push") >= 0) {
						
						boolean secure = false;
						for (String header : request.getHeaders()) {
							if (header.startsWith("X-Hub-Signature:")) {
								String recievedDigest = header.split("sha1=")[1];
								String ourDigest = hmac(request.getContent(), secret);
								Bukkit.broadcastMessage("Theirs: " + recievedDigest);
								Bukkit.broadcastMessage("Ours: " + ourDigest);
								if (recievedDigest.equals(ourDigest)) {
									secure = true;
								}
							}
						}
						
						if (secure) {
							sendHeader(output, "HTTP/1.0 200 OK");
							worked = true;
							GitEventParser.createNewGitEvent(request);
						} else {
							sendHeader(output, "HTTP/1.0 400 Bad Request");
						}
						
						// logMessage("It worked");
					} else if (request.getHeaders().indexOf("X-GitHub-Event: ping") >= 0) {
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
					sendHeader(output, "Request Acknowledged.");
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
	
	private static String hmac(String data, String key) throws java.security.SignatureException {
		String result;
		try {
			
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
			
			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			
			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());
			
			// base64-encode the hmac
			result = DatatypeConverter.printBase64Binary(rawHmac);
			
		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
		return result;
	}
	
	private RequestType setRequestType(String type) {
		if (type.startsWith("GET")) {
			return RequestType.GET;
		} else if (type.startsWith("POST")) {
			return RequestType.POST;
		}
		return null;
	}
	
}
