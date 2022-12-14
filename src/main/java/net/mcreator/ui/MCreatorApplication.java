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

package net.mcreator.ui;

import javafx.application.Platform;
import net.mcreator.Launcher;
import net.mcreator.blockly.data.BlocklyLoader;
import net.mcreator.element.ModElementTypeLoader;
import net.mcreator.generator.Generator;
import net.mcreator.generator.GeneratorConfiguration;
import net.mcreator.gradle.GradleUtils;
import net.mcreator.io.FileIO;
import net.mcreator.io.net.analytics.Analytics;
import net.mcreator.io.net.analytics.DeviceInfo;
import net.mcreator.io.net.api.D8WebAPI;
import net.mcreator.io.net.api.IWebAPI;
import net.mcreator.minecraft.DataListLoader;
import net.mcreator.minecraft.api.ModAPIManager;
import net.mcreator.plugin.MCREvent;
import net.mcreator.plugin.PluginLoader;
import net.mcreator.plugin.events.ApplicationLoadedEvent;
import net.mcreator.plugin.events.PreGeneratorsLoadingEvent;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.themes.ThemeLoader;
import net.mcreator.ui.action.impl.AboutAction;
import net.mcreator.ui.component.util.DiscordClient;
import net.mcreator.ui.dialogs.RulesDialog;
import net.mcreator.ui.dialogs.UpdateNotifyDialog;
import net.mcreator.ui.dialogs.UpdatePluginDialog;
import net.mcreator.ui.dialogs.preferences.PreferencesDialog;
import net.mcreator.ui.help.HelpLoader;
import net.mcreator.ui.init.*;
import net.mcreator.ui.laf.MCreatorLookAndFeel;
import net.mcreator.ui.traslatable.TranslatablePool;
import net.mcreator.ui.workspace.selector.RecentWorkspaceEntry;
import net.mcreator.ui.workspace.selector.WorkspaceSelector;
import net.mcreator.util.MCreatorVersionNumber;
import net.mcreator.util.SoundUtils;
import net.mcreator.util.locale.TranslatorUtils;
import net.mcreator.workspace.CorruptedWorkspaceFileException;
import net.mcreator.workspace.UnsupportedGeneratorException;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.VariableTypeLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public final class MCreatorApplication {

	private static MCreatorApplication application;

	public static void exit(boolean restart){
		application.closeApplication(restart);
	}

	private static final Logger LOG = LogManager.getLogger("Application");

	public static IWebAPI WEB_API = new D8WebAPI();
	public static final String SERVER_DOMAIN = "https://mcreator.net";
	public static boolean isInternet = true;

	private final Analytics analytics;
	private final DeviceInfo deviceInfo;
	private static boolean applicationStarted = false;
	private final WorkspaceSelector workspaceSelector;

	private final List<MCreator> openMCreators = new ArrayList<>();

	private final DiscordClient discordClient;

	private final TaskbarIntegration taskbarIntegration;

	private MCreatorApplication(List<String> launchArguments) {

		final SplashScreen splashScreen = new SplashScreen();
		splashScreen.setVisible(true);

		LOG.info("?????????????????????");
		isInternet = MCreatorApplication.WEB_API.initAPI();
		TranslatablePool.getPool();
		TranslatorUtils.initCopyTranslation();

		splashScreen.setProgress(5, "??????????????????");

		// Plugins are loaded before the Splash screen is visible, so every image can be changed
		PluginLoader.initInstance();

		MCREvent.event(new ApplicationLoadedEvent(this));

		splashScreen.setProgress(10, "????????????????????????");

		// We load UI themes now as theme plugins are loaded at this point
		ThemeLoader.initUIThemes();

		splashScreen.setProgress(15, "????????????UI??????");

		UIRES.preloadImages();

		try {
			UIManager.setLookAndFeel(new MCreatorLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			LOG.error("??????????????????: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		SoundUtils.initSoundSystem();

		taskbarIntegration = new TaskbarIntegration();

		splashScreen.setProgress(25, "??????????????????");

		// ?????????????????????
		HelpLoader.preloadCache();

		// load translations after plugins are loaded
		L10N.initTranslations();

		splashScreen.setProgress(35, "??????????????????");

		// load datalists and icons for them after plugins are loaded
		BlockItemIcons.init();
		DataListLoader.preloadCache();

		splashScreen.setProgress(45, "??????????????????");

		// load templates for image makers
		ImageMakerTexturesCache.init();
		ArmorMakerTexturesCache.init();

		splashScreen.setProgress(55, "??????????????????");

		// load apis defined by plugins after plugins are loaded
		ModAPIManager.initAPIs();

		// load variable elements
		VariableTypeLoader.loadVariableTypes();

		// load JS files for Blockly
		BlocklyJavaScriptsLoader.init();

		// load blockly blocks after plugins are loaded
		BlocklyLoader.init();

		// load entity animations for the Java Model animation editor
		EntityAnimationsLoader.init();

		// register mod element types
		ModElementTypeLoader.loadModElements();

		splashScreen.setProgress(60, "???????????????");
		TiledImageCache.loadAndTileImages();

		splashScreen.setProgress(70, "??????????????????");

		MCREvent.event(new PreGeneratorsLoadingEvent(this));

		Set<String> fileNamesUnordered = PluginLoader.INSTANCE.getResources(Pattern.compile("generator\\.yaml"));
		List<String> fileNames = new ArrayList<>(fileNamesUnordered);
		Collections.sort(fileNames);
		int i = 0;
		for (String generator : fileNames) {
			splashScreen.setProgress(70 + i * ((90 - 70) / fileNames.size()),
					"?????????????????????: " + generator.split("/")[0]);
			LOG.info("??????????????????: " + generator);
			generator = generator.replace("/generator.yaml", "");
			try {
				Generator.GENERATOR_CACHE.put(generator, new GeneratorConfiguration(generator));
			} catch (Exception e) {
				LOG.error("?????????????????????: " + generator, e);
			}
			i++;
		}

		splashScreen.setProgress(93, "????????????????????????");

		deviceInfo = new DeviceInfo();
		analytics = new Analytics(deviceInfo);

		// we do async login attempt
		LOG.info("???????????????????????????");
		UpdateNotifyDialog.showUpdateDialogIfUpdateExists(splashScreen, false);
		LOG.info("?????????????????????????????????");
		UpdatePluginDialog.showPluginUpdateDialogIfUpdatesExist(splashScreen);

		splashScreen.setProgress(100, "????????????MCreator??????");

		try {
			if (Desktop.getDesktop().isSupported(Desktop.Action.APP_ABOUT))
				Desktop.getDesktop().setAboutHandler(aboutEvent -> AboutAction.showDialog(null));

			if (Desktop.getDesktop().isSupported(Desktop.Action.APP_PREFERENCES))
				Desktop.getDesktop().setPreferencesHandler(preferencesEvent -> new PreferencesDialog(null, null));

			if (Desktop.getDesktop().isSupported(Desktop.Action.APP_QUIT_HANDLER))
				Desktop.getDesktop().setQuitHandler((e, response) -> MCreatorApplication.this.closeApplication(false));
		} catch (Exception e) {
			LOG.warn("????????????desktop handlers", e);
		}

		discordClient = new DiscordClient();

		if (Launcher.version.isSnapshot() && PreferencesManager.PREFERENCES.notifications.snapshotMessage) {
			JOptionPane.showMessageDialog(splashScreen, L10N.t("action.eap_loading.text"),
					L10N.t("action.eap_loading.title"), JOptionPane.WARNING_MESSAGE);
		}

		discordClient.updatePresence(L10N.t("dialog.discord_rpc.just_opened"),
				L10N.t("dialog.discord_rpc.version") + Launcher.version.getMajorString());

		workspaceSelector = new WorkspaceSelector(this::openWorkspaceInMCreator);

		boolean directLaunch = false;
		if (launchArguments.size() > 0) {
			String lastArg = launchArguments.get(launchArguments.size() - 1);
			if (lastArg.length() >= 2 && lastArg.charAt(0) == '"' && lastArg.charAt(lastArg.length() - 1) == '"')
				lastArg = lastArg.substring(1, lastArg.length() - 1);
			File passedFile = new File(lastArg);
			if (passedFile.isFile() && passedFile.getName().endsWith(".mcreator")) {
				splashScreen.setVisible(false);
				openWorkspaceInMCreator(passedFile,false);
				directLaunch = true;
			}
		}

		if (!directLaunch) {
			showWorkspaceSelector();
		}

		splashScreen.setVisible(false);

		//track after the setup is done
		analytics.async(analytics::trackMCreatorLaunch);


	}

	public Analytics getAnalytics() {
		return analytics;
	}

	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

	public static void createApplication(List<String> arguments) {
		if (!applicationStarted) {
			SwingUtilities.invokeLater(() ->
					application = new MCreatorApplication(arguments)
			);
			applicationStarted = true;
		}
	}

	public WorkspaceSelector getWorkspaceSelector() {
		return workspaceSelector;
	}

	public void openWorkspaceInMCreator(File work){
		openWorkspaceInMCreator(work,false);
	}

	public void openWorkspaceInMCreator(File workspaceFile,boolean compatibilityMode) {
		this.workspaceSelector.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		try {
			Workspace workspace = Workspace.readFromFS(workspaceFile, this.workspaceSelector);
			if (!compatibilityMode){
				if (workspace.getMCreatorVersion() > Launcher.version.versionlong && !MCreatorVersionNumber.isBuildNumberDevelopment(workspace.getMCreatorVersion())) {
					JOptionPane.showMessageDialog(workspaceSelector, L10N.t("dialog.workspace.open_failed_message"),
							L10N.t("dialog.workspace.open_failed_title"), JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if ((!GradleUtils.isJDK(workspace.getWorkspaceSettings().getJavaHome().getPath())) || compatibilityMode){
				workspace.getWorkspaceSettings().setJavaHome(PreferencesManager.PREFERENCES.gradle.java_home);
			}
			MCreator mcreator = new MCreator(this, workspace);
			this.workspaceSelector.setVisible(false);
			if (!this.openMCreators.contains(mcreator)) {
				this.openMCreators.add(mcreator);
				mcreator.setVisible(true);
				mcreator.requestFocusInWindow();
				mcreator.toFront();
			} else { // already open, just focus it
				LOG.warn("?????????????????????????????????????????????????????????????????????????????????.");
				for (MCreator openmcreator : openMCreators) {
					if (openmcreator.equals(mcreator)) {
						openmcreator.requestFocusInWindow();
						openmcreator.toFront();
					}
				}
			}
			this.workspaceSelector.addOrUpdateRecentWorkspace(
						new RecentWorkspaceEntry(mcreator.getWorkspace(), workspaceFile));
		} catch (CorruptedWorkspaceFileException corruptedWorkspaceFile) {
			LOG.fatal("????????????????????????!", corruptedWorkspaceFile);

			File backupsDir = new File(workspaceFile.getParentFile(), ".mcreator/workspaceBackups");
			if (backupsDir.isDirectory()) {
				String[] files = backupsDir.list();
				if (files != null) {
					String[] backups = Arrays.stream(files).filter(e -> e.contains(".mcreator-backup"))
							.sorted(Collections.reverseOrder()).toArray(String[]::new);
					String selected = (String) JOptionPane.showInputDialog(this.workspaceSelector,
							L10N.t("dialog.workspace.got_corrupted_message"),
							L10N.t("dialog.workspace.got_corrupted_title"), JOptionPane.QUESTION_MESSAGE, null, backups,
							"");
					if (selected != null) {
						File backup = new File(backupsDir, selected);
						FileIO.copyFile(backup, workspaceFile);
						openWorkspaceInMCreator(workspaceFile,false);
					} else {
						reportFailedWorkspaceOpen(
								new IOException("User canceled workspace backup restoration", corruptedWorkspaceFile));
					}
				}
			} else {
				reportFailedWorkspaceOpen(
						new IOException("Corrupted workspace file and no backups found", corruptedWorkspaceFile));
			}
		} catch (IOException | UnsupportedGeneratorException e) {
			reportFailedWorkspaceOpen(e);
		}
		this.workspaceSelector.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	private void reportFailedWorkspaceOpen(Exception e) {
		JOptionPane.showMessageDialog(this.workspaceSelector,
				L10N.t("dialog.workspace.is_not_valid_message") + e.getMessage(),
				L10N.t("dialog.workspace.is_not_valid_title"), JOptionPane.ERROR_MESSAGE);
	}

	public void closeApplication(boolean restart) {
		LOG.debug("?????????????????????MCreator??????");
		List<MCreator> mcreatorsTmp = new ArrayList<>(
				openMCreators); // create list copy so we don't modify the list we iterate
		for (MCreator mcreator : mcreatorsTmp) {
			LOG.info("??????????????????????????????MCreator??????: " + mcreator.getWorkspace());
			if (!mcreator.closeThisMCreator(false)) {
				return; // if we fail to close all windows, we cancel the application close
			}
		}

		LOG.debug("??????????????????");
		PreferencesManager.storePreferences(PreferencesManager.PREFERENCES); // store any potential preferences changes
		analytics.trackMCreatorClose(); // track app close in sync mode

		discordClient.close(); // close discord client

		SoundUtils.close();

		// we close all windows and exit fx platform
		try {
			LOG.debug("????????????AWT???FX?????????");
			Arrays.stream(Window.getWindows()).forEach(Window::dispose);
			Platform.exit();
		} catch (Exception ignored) {
		}

		try {
			PluginLoader.INSTANCE.close();
			LOG.info("???????????????????????????");
		} catch (IOException e) {
			LOG.warn("???????????????????????????", e);
		}

		if (restart){
			LOG.info("????????????");
			String restartCommand = "mcreator.exe";
			if (Files.exists(Paths.get(restartCommand))) {
				try {
					Runtime.getRuntime().exec(restartCommand);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		LOG.info("????????????MCreator");
		System.exit(-1);
	}

	void showWorkspaceSelector()  {
		workspaceSelector.setVisible(true);
		if (!PreferencesManager.PREFERENCES.hidden.acceptRules){
			new RulesDialog(workspaceSelector);
		}
	}

	List<RecentWorkspaceEntry> getRecentWorkspaces() {
		return workspaceSelector.getRecentWorkspaces().getList();
	}

	public List<MCreator> getOpenMCreators() {
		return openMCreators;
	}

	public DiscordClient getDiscordClient() {
		return discordClient;
	}

	public TaskbarIntegration getTaskbarIntegration() {
		return taskbarIntegration;
	}

}
