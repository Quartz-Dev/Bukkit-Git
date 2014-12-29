package com.quartzdev.bukkitgit.gitevent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
			File unzippedFolder = new File("plugins" + File.separator + "Bukkit-Git" + File.separator + "downloads" + File.separator + fileName + "unzip");
			File newJar = new File("plugins" + File.separator + "Bukkit-Git" + File.separator + "downloads" + File.separator + "jar_" + fileName + ".jar");
			
			FileUtils.copyURLToFile(website, dest);
			
			if (!unzippedFolder.exists()) {
				unzippedFolder.mkdir();
			}
			
			ZipInputStream zis = new ZipInputStream(new FileInputStream(dest));
			ZipEntry ze = zis.getNextEntry();
			
			while (ze != null) {
				
				String entryName = ze.getName();
				File newFile = new File(unzippedFolder + File.separator + entryName);
				
				System.out.println("file unzip : " + newFile.getAbsoluteFile());
				
				new File(newFile.getParent()).mkdirs();
				
				FileOutputStream fos = new FileOutputStream(newFile);
				
				int len;
				byte[] buffer = new byte[1024];
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				
				fos.close();
				ze = zis.getNextEntry();
			}
			
			zis.closeEntry();
			zis.close();
			
			for (File file : unzippedFolder.listFiles()) {
				if (Pattern.matches("*.java", file.getName())) {
					// Test
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
