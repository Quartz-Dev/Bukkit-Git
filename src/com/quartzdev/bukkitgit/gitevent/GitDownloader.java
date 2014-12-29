package com.quartzdev.bukkitgit.gitevent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
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
	private String zipLoc;
	
	public GitDownloader(GitEvent event) {
		this.event = event;
		zipLoc = null;
	}
	
	@Override
	public void run() {
		
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
			ArrayList<File> filesToCompile = new ArrayList<File>();
			
			FileUtils.copyURLToFile(website, dest);
			
			Unzipper uz = new Unzipper();
			uz.unzip(dest.getAbsolutePath(), unzippedFolder.getAbsolutePath());
			
			File insideFolder = unzippedFolder.listFiles()[0];
			
			Collection<File> files = FileUtils.listFiles(insideFolder, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
			for (File file : files) {
				if (Pattern.matches("([^\\s]+(\\.(?i)(java))$)", file.getName())) {
					filesToCompile.add(file);
				}
			}
			compileJava(filesToCompile);
			
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			JarOutputStream target = new JarOutputStream(new FileOutputStream(newJar.getAbsoluteFile()), manifest);
			Bukkit.broadcastMessage("Inside folder: " + insideFolder.getPath());
			zipLoc = insideFolder.getPath();
			moveFilesIntoJar(insideFolder, target);
			target.close();
			
		} catch (IOException e) {
			// TODO Make it do something useful
			e.printStackTrace();
		}
		
	}
	
	private void compileJava(ArrayList<File> files) throws IOException {
		System.setProperty("java.home", "C:\\Program Files\\Java\\jdk1.7.0_51");
		// TODO [SEVERE] Not everyone will have the same version
		// TODO warn user of change, also detect which version the user has
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			// TODO show error for bad version, yeah
			// https://www.java.net/node/688208
			Bukkit.broadcastMessage("Compiler is null!");
			return;
		}
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
		boolean success = task.call();
		Bukkit.broadcastMessage("Success: " + success);
		fileManager.close();
	}
	
	private void moveFilesIntoJar(File source, JarOutputStream target) throws IOException {
		BufferedInputStream in = null;
		try {
			if (source.isDirectory()) {
				String name = source.getPath().replace("\\", "/").replace(zipLoc, "");
				Bukkit.broadcastMessage("Name: " + name);
				if (!name.isEmpty()) {
					if (!name.endsWith("/"))
						name += "/";
					JarEntry entry = new JarEntry(name);
					entry.setTime(source.lastModified());
					target.putNextEntry(entry);
					target.closeEntry();
				}
				for (File nestedFile : source.listFiles())
					moveFilesIntoJar(nestedFile, target);
				return;
			}
			
			JarEntry entry = new JarEntry(source.getPath().replace("\\", "/").replace(zipLoc, ""));
			Bukkit.broadcastMessage("Entry: " + source.getPath().replace("\\", "/").replace(zipLoc, ""));
			entry.setTime(source.lastModified());
			target.putNextEntry(entry);
			in = new BufferedInputStream(new FileInputStream(source));
			
			byte[] buffer = new byte[1024];
			while (true) {
				int count = in.read(buffer);
				if (count == -1)
					break;
				target.write(buffer, 0, count);
			}
			target.closeEntry();
		} finally {
			if (in != null)
				in.close();
		}
	}
	
}
