package com.quartzdev.bukkitgit.push.gitevent;

import java.util.ArrayList;

import com.quartzdev.bukkitgit.push.webserver.WebRequest;

public class GitEvent extends WebRequest {
	
	private String masterBranch;
	private String defaultBranch;
	private String repositoryFullName;
	private String repositoryName;
	private String compressionType;
	private String compareLink;
	private String commiter;
	private String commitMessage;
	
	public GitEvent(String type, ArrayList<String> headers, String content, String masterBranch, String defaultBranch, String repositoryFullName, String repositoryName, String compressionType, String compareLink, String commiter, String commitMessage) {
		super(type, headers, content);
		this.masterBranch = masterBranch;
		this.defaultBranch = defaultBranch;
		this.repositoryFullName = repositoryFullName;
		this.repositoryName = repositoryName;
		this.compressionType = compressionType;
		this.compareLink = compareLink;
		this.commiter = commiter;
		this.commitMessage = commitMessage;
	}
	
	public String getMasterBranch() {
		return masterBranch;
	}
	
	public String getDefaultBranch() {
		return defaultBranch;
	}
	
	public String getRepositoryFullName() {
		return repositoryFullName;
	}
	
	public String getRepositoryName() {
		return repositoryName;
	}
	
	public String getCompressionType() {
		return compressionType;
	}
	
	public String getCompareLink() {
		return compareLink;
	}
	
	public String getCommiter() {
		return commiter;
	}
	
	public String getCommitMessage() {
		return commitMessage;
	}
	
}
