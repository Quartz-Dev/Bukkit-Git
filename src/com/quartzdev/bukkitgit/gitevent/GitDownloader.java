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
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;

import com.quartzdev.bukkitgit.Loggers;

public class GitDownloader implements Runnable {
	
	private final static String JDK_ARTICLE = "https://github.com/Quartz-Dev/Bukkit-Git/wiki/Why-JDKs-are-important";
	
	private String repoFullName;
	private String defaultBranch;
	private String compressionType;
	private String repoName;
	
	private String zipLoc;
	private Plugin plugin;
	
	private File dest;
	private File unzippedFolder;
	
	public GitDownloader(GitEvent event, Plugin plugin) {
		repoFullName = event.getRepositoryFullName();
		repoName = event.getRepositoryName();
		defaultBranch = event.getDefaultBranch();
		compressionType = event.getCompressionType();
		zipLoc = null;
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		
		try {
			// TODO allow choice for master branch or default branch maybe
			URL website = new URL("https://api.github.com/repos/" + repoFullName + "/" + compressionType + "/" + defaultBranch);
			String fileName = System.currentTimeMillis() + "";
			String unzippedFolderPath = "plugins" + File.separator + "Bukkit-Git" + File.separator + "downloads" + File.separator + fileName + "unzip";
			String destPath = "plugins" + File.separator + "Bukkit-Git" + File.separator + "downloads" + File.separator + fileName + ".zip";
			String newJarPath = "plugins" + File.separator + "Bukkit-Git" + File.separator + "downloads" + File.separator + "jar_" + fileName + ".jar";
			unzippedFolder = new File(unzippedFolderPath);
			dest = new File(destPath);
			File newJar = new File(newJarPath);
			ArrayList<File> filesToCompile = new ArrayList<File>();
			File newPlugin = new File("plugins" + File.separator + repoName + ".jar");
			
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
			JarOutputStream target = new JarOutputStream(new FileOutputStream(newPlugin.getAbsoluteFile()), manifest);
			zipLoc = insideFolder.getPath();
			moveFilesIntoJar(insideFolder, target);
			target.close();
			
			deleteTempFiles();
			
			migrateJar(newJar, repoName);
			
		} catch (IOException e) {
			Loggers.logError("Something bad happened while downloading a plugin.");
			e.printStackTrace();
		}
		
	}
	
	private void deleteTempFiles() throws IOException {
		dest.delete();
		FileUtils.deleteDirectory(unzippedFolder);
	}
	
	private void compileJava(ArrayList<File> files) throws IOException {
		
		File home = new File(System.getProperty("java.home"));
		String oldHome = System.getProperty("java.home");
		boolean jdkChanged = false;
		if (!home.getName().startsWith("jdk")) {
			String jdk = findJDK(home);
			if (jdk != null) {
				jdkChanged = true;
				System.setProperty("java.home", jdk);
			} else {
				Loggers.logError("There is no JDK installed!");
				Loggers.logError("Please see the GitHub article about why a JDK is important for this plugin");
				Loggers.logError(JDK_ARTICLE);
				deleteTempFiles();
				Bukkit.getPluginManager().disablePlugin(plugin);
				return;
			}
		}
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
		task.call();
		fileManager.close();
		
		if (jdkChanged) {
			System.setProperty("java.home", oldHome);
		}
	}
	
	private void moveFilesIntoJar(File source, JarOutputStream target) throws IOException {
		BufferedInputStream in = null;
		try {
			if (source.isDirectory()) {
				String name = source.getPath().replace(zipLoc, "").replace("\\", "/").replaceFirst("src/", "").replaceFirst("/", "");
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
			
			JarEntry entry = new JarEntry(source.getPath().replace(zipLoc, "").replace("\\", "/").replaceFirst("src/", "").replaceFirst("/", ""));
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
	
	private void migrateJar(File jarFile, String pluginName) {
		File newPlugin = new File("plugins" + File.separator + pluginName + ".jar");
		
		try {
			Bukkit.getPluginManager().loadPlugin(newPlugin);
		} catch (UnknownDependencyException | InvalidDescriptionException e) {
			e.printStackTrace();
		} catch (InvalidPluginException e) {
			reloadPlugin(pluginName);
		}
		
	}
	
	private void reloadPlugin(String pluginName) {
		Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
		if (plugin != null) {
			Bukkit.getPluginManager().disablePlugin(plugin);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Bukkit.getPluginManager().enablePlugin(plugin);
		} else {
			Loggers.logMessage("The plugin " + pluginName + " wasn't reloaded.");
			Loggers.logMessage("Change the plugin.yml name to be the same as the repository name to allow for easier reloading.");
			// TODO Change this in the future.
		}
	}
	
	private String findJDK(File home) {
		File parent = home.getParentFile();
		
		for (File child : parent.listFiles()) {
			if (child.getName().startsWith("jdk")) {
				return child.getAbsolutePath();
			}
		}
		
		return null;
	}
}
