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
			
			if (!unzippedFolder.exists()) {
				unzippedFolder.mkdir();
			}
			
			ZipInputStream zis = new ZipInputStream(new FileInputStream(dest));
			ZipEntry ze = zis.getNextEntry();
			
			while (ze != null) {
				
				String entryName = ze.getName();
				File newFile = new File(unzippedFolderPath + File.separator + entryName);
				
				System.out.println("file unzip : " + newFile.getAbsoluteFile());
				
				try {
					new File(newFile.getParent()).mkdirs();
					
					newFile.createNewFile();
					FileOutputStream fos = new FileOutputStream(newFile);
					
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				ze = zis.getNextEntry();
			}
			
			zis.closeEntry();
			zis.close();
			
			for (File file : unzippedFolder.listFiles()) {
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
