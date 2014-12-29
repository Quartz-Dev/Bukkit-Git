package com.quartzdev.bukkitgit.gitevent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import net.minecraft.util.org.apache.commons.io.FileUtils;
import net.minecraft.util.org.apache.commons.io.filefilter.DirectoryFileFilter;
import net.minecraft.util.org.apache.commons.io.filefilter.RegexFileFilter;

import org.bukkit.Bukkit;

public class GitDownloader implements Runnable {
	
	private GitEvent event;
	
	public GitDownloader(GitEvent event) {
		this.event = event;
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		
		try {
			// TODO allow choice for master branch or default branch maybe
			URL website = new URL("https://api.github.com/repos/" + event.getRepositoryFullName() + "/" + event.getCompressionType() + "/" + event.getDefaultBranch());
			String fileName = System.currentTimeMillis() + "";
			String unzippedFolderPath = "plugins" + File.separator + "Bukkit-Git" + File.separator + "downloads" + File.separator + fileName + "unzip";
			String destPath = "plugins" + File.separator + "Bukkit-Git" + File.separator + "downloads" + File.separator + fileName + ".zip";
			String newJarPath = "plugins" + File.separator + "Bukkit-Git" + File.separator + "downloads" + File.separator + "jar_" + fileName + ".jar";
			
			File unzippedFolder = new File(unzippedFolderPath);
			File dest = new File(destPath);
			File newJar = new File(newJarPath);
			
			FileUtils.copyURLToFile(website, dest);
			
			Unzipper uz = new Unzipper();
			uz.unzip(dest.getAbsolutePath(), unzippedFolder.getAbsolutePath());
			
			File insideFolder = unzippedFolder.listFiles()[0];
			
			Collection<File> files = FileUtils.listFiles(insideFolder, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
			for (File file : files) {
				if (Pattern.matches("([^\\s]+(\\.(?i)(java))$)", file.getName())) {
					Bukkit.broadcastMessage("Java file: Path: " + file.getPath() + ", File: " + file.getName());
					Bukkit.broadcastMessage("Java home: " + System.getProperty("java.home"));
					
					JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
					if (compiler == null) {
						// TODO show error for bad version, yeah
						// https://www.java.net/node/688208
						Bukkit.broadcastMessage("Compiler is null!");
						return;
					}
					DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
					StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
					Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(file.getPath()));
					JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
					boolean success = task.call();
					Bukkit.broadcastMessage("Success: " + success);
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
