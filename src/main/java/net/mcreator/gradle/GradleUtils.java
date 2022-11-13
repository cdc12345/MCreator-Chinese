/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2020 Pylo and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.mcreator.gradle;

import net.mcreator.io.FileIO;
import net.mcreator.minecraft.api.ModAPIImplementation;
import net.mcreator.minecraft.api.ModAPIManager;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.workspace.Workspace;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.annotations.NonNull;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.ProjectConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradleUtils {

	private static final Logger LOG = LogManager.getLogger(GradleUtils.class);

	private static ProjectConnection getGradleProjectConnection(Workspace workspace) {
		updateMCreatorBuildFile(workspace);
		return workspace.getGenerator().getGradleProjectConnection();
	}

	public static BuildLauncher getGradleTaskLauncher(Workspace workspace, String... tasks) {
		BuildLauncher retval = getGradleProjectConnection(workspace).newBuild().forTasks(tasks)
				.setJvmArguments("-Xms" + PreferencesManager.PREFERENCES.gradle.xms + "m",
						"-Xmx" + PreferencesManager.PREFERENCES.gradle.xmx + "m");

		String workSpaceJavaHome = workspace.getWorkspaceSettings().getJavaHome().getPath();
		String java_home = ("".equals(workSpaceJavaHome))? getJavaHome():workSpaceJavaHome;
		if (java_home != null) {
			retval = retval.setJavaHome(new File(java_home));
		}

		Map<String, String> environment = new HashMap<>(System.getenv());

		// avoid global overrides
		cleanupEnvironment(environment);

		if (java_home != null)
			environment.put("JAVA_HOME", java_home);

		// use custom set of environment variables to prevent system overrides
		retval.setEnvironmentVariables(environment);

		if (java_home != null)
			retval.withArguments(Arrays.asList("-Porg.gradle.java.installations.auto-detect=false",
					"-Porg.gradle.java.installations.paths=" + java_home.replace('\\', '/')));

		return retval;
	}

	public static String getJavaHome() {
		if (PreferencesManager.PREFERENCES.gradle.java_home != null) {
			return PreferencesManager.PREFERENCES.gradle.java_home.getPath();
		}
		return getDefaultJavaHome();
	}

	public static String getJavaHome(@NonNull String generatorName){
		if (generatorName.contains("1.16.5")){
			return getJavaHome(8);
		} else {
			return getJavaHome(17);
		}
	}

	public static String getJavaHome(int version){
		String java = getJavaHome();
		if (getJavaVersion(java)==version){
			return java;
		} else {
			return PreferencesManager.PREFERENCES.hidden.javaHomes.stream()
					.filter(a->getJavaVersion(a)==version).findFirst().orElse(java);
		}
	}

	public static String getDefaultJavaHome(){
		// if we have bundled JDK, we set JAVA_HOME to bundled JDK
		if (new File("./jdk/bin/javac.exe").isFile() || new File("./jdk/bin/javac").isFile()) {
			return FilenameUtils.normalize(new File("./jdk/").getAbsolutePath());
		}
		// otherwise, we try to set JAVA_HOME to the same Java as MCreator is launched with
		String current_java_home = System.getProperty("java.home");
		// only set it if it is jdk, not jre
		if (current_java_home != null && isJDK(current_java_home)){
			PreferencesManager.PREFERENCES.gradle.java_home = new File(current_java_home);
			return current_java_home;
		}

		LOG.error("系统找不到任何可用JDK，如果系统环境存在JAVA_HOME，则使用系统默认值");

		// if we can not get a better match, use system default JAVA_HOME variable
		// THIS ONE CAN BE null!!!, so handle this with care where used
		return System.getenv("JAVA_HOME");
	}

	public static boolean isJDK(String path){
		return new File(path, "bin/java.exe").exists() && new File(path,
				"bin/javac.exe").exists();
	}

	public static void updateMCreatorBuildFile(Workspace workspace) {
		if (workspace != null) {
			StringBuilder mcreatorGradleConfBuilder = new StringBuilder();

			if (workspace.getWorkspaceSettings() != null
					&& workspace.getWorkspaceSettings().getMCreatorDependencies() != null) {
				for (String dep : workspace.getWorkspaceSettings().getMCreatorDependencies()) {
					ModAPIImplementation implementation = ModAPIManager.getModAPIForNameAndGenerator(dep,
							workspace.getGenerator().getGeneratorName());
					if (implementation != null) {
						mcreatorGradleConfBuilder.append(implementation.gradle()).append("\n\n");
					}
				}
			}

			FileIO.writeStringToFile(mcreatorGradleConfBuilder.toString(),
					new File(workspace.getWorkspaceFolder(), "mcreator.gradle"));
		}
	}
	private static final HashMap<String,Integer> cache_javaVersion = new HashMap<>();
	private static final Thread javaFresh = new Thread(()->{
		while (true){
			try {
				Thread.sleep(1000L * 6);
				for (String javaHome:cache_javaVersion.keySet()){
					int result = getJavaVersion0(javaHome);
					if (result == 0){
						cache_javaVersion.remove(javaHome);
						PreferencesManager.PREFERENCES.hidden.javaHomes.remove(javaHome);
						continue;
					}
					cache_javaVersion.put(javaHome,result);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception ignore){}
		}
	});
	public static Integer getJavaVersion(String javaHome)  {
		if (javaFresh.getState() == Thread.State.NEW){
			javaFresh.setDaemon(true);
			javaFresh.start();
		}
		if (cache_javaVersion.containsKey(javaHome)){
			return cache_javaVersion.get(javaHome);
		}
		int result = getJavaVersion0(javaHome);
		cache_javaVersion.put(javaHome,result);
		return result;
	}

	public static Integer getJavaVersion0(String javaHome){
		if (!isJDK(javaHome)){
			return 0;
		}
		try {
			String javaExeString = javaHome + "\\bin\\java.exe";
			Process process = Runtime.getRuntime().exec(javaExeString + " -version");
			process.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String firstLineString = reader.readLine();
			Matcher matcher = Pattern.compile("version \"(?<version>.+)\"").matcher(firstLineString);
			matcher.find();
			String versionString = matcher.group("version");
			if (versionString.startsWith("1.")) {
				return Integer.parseInt(versionString.split("\\.")[1]);
			} else if (versionString.contains(".")) {
				return Integer.parseInt(versionString.split("\\.")[0]);
			} else {
				return Integer.parseInt(versionString);
			}
		} catch (Exception ignore){
			return 0;
		}

	}

	public static void cleanupEnvironment(Map<String, String> environment) {
		environment.remove("_JAVA_OPTIONS");
		environment.remove("GRADLE_USER_HOME");
		environment.remove("GRADLE_OPTS");
		environment.remove("JAVA_HOME");
		environment.remove("JDK_HOME");
		environment.remove("JRE_HOME");
		environment.remove("CLASSPATH");
		environment.remove("JAVA_TOOL_OPTIONS");
	}

}
