package com.quartzdev.bukkitgit.gitevent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import net.minecraft.util.org.apache.commons.io.FileUtils;

import org.bukkit.Bukkit;

public class GitDownloader implements Runnable {
	
	private GitEvent event;
	
	public GitDownloader(GitEvent event) {
		this.event = event;
	}
	
	@Override
	public void run() {
		try {
			// TODO allow choice for master branch or default branch maybe
			URL website = new URL("https://api.github.com/repos/" + event.getRepositoryFullName() + "/" + event.getCompressionType() + "/" + event.getDefaultBranch());
			String fileName = System.currentTimeMillis() + "";
			File dest = new File("plugins" + File.separator + "Bukkit-Git" + File.separator + "downloads" + File.separator + fileName + ".zip");
			File newJar = new File("plugins" + File.separator + "Bukkit-Git" + File.separator + "downloads" + File.separator + "jar_" + fileName + ".jar");
			
			FileUtils.copyURLToFile(website, dest);
			
			for (File file : dest.listFiles()) {
				if (Pattern.matches("*.java", file.getName())) {
					Bukkit.broadcastMessage("Java file: Path: " + file.getPath() + ", File: " + file.getName());
					JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
					DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
					StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
					Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList("YouFileToCompile.java"));
					JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
					boolean success = task.call();
					fileManager.close();
				} else {
					// TODO Simply move over files
				}
			}
			
		} catch (IOException e) {
			// TODO Make it do something useful
			e.printStackTrace();
		}
		
	}
}
