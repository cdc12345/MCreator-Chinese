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

package net.mcreator.ui.dialogs.preferences;

import net.mcreator.io.FileIO;
import net.mcreator.io.UserFolderManager;
import net.mcreator.ui.component.JEmptyBox;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.dialogs.file.FileDialogs;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.util.DesktopUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

class EditTemplatesPanel {

	EditTemplatesPanel(PreferencesDialog preferencesDialog, String name, String templatesFolder, String templateExt) {
		preferencesDialog.model.addElement(name);

		JPanel sectionPanel = new JPanel(new BorderLayout(15, 15));

		sectionPanel.add("North", L10N.label("dialog.preferences.change_language", name.toLowerCase(), templateExt));
		sectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 10));

		JToolBar opts = new JToolBar();
		opts.setFloatable(false);

		JButton add = L10N.button("dialog.preferences.add_language", name.toLowerCase());
		add.setIcon(UIRES.get("16px.add.gif"));
		opts.add(add);

		opts.add(new JEmptyBox(5, 5));

		JButton remove = L10N.button("dialog.preferences.remove_selected_language");
		remove.setIcon(UIRES.get("16px.delete.gif"));
		opts.add(remove);

		opts.add(new JEmptyBox(5, 5));

		JButton openFolder = L10N.button("dialog.preferences.open_folder", name.toLowerCase());
		openFolder.setIcon(UIRES.get("16px.open.gif"));
		opts.add(openFolder);

		DefaultListModel<String> tmodel = new DefaultListModel<>();
		JList<String> templates = new JList<>(tmodel);
		templates.setCellRenderer(new TemplateRender(templatesFolder));

		openFolder.addActionListener(
				e -> DesktopUtils.openSafe(UserFolderManager.getFileFromUserFolder(templatesFolder)));

		remove.addActionListener(e -> templates.getSelectedValuesList().forEach(el -> {
			new File(UserFolderManager.getFileFromUserFolder(templatesFolder), el).delete();
			tmodel.removeElement(el);
		}));

		add.addActionListener(e -> {
			File[] files = FileDialogs.getMultiOpenDialog(preferencesDialog, new String[] { templateExt });
			if (files != null) {
				Arrays.stream(files).forEach(f -> {
					FileIO.copyFile(f, new File(UserFolderManager.getFileFromUserFolder(templatesFolder), f.getName()));
					tmodel.addElement(f.getName());
				});
			}
		});

		File[] files = UserFolderManager.getFileFromUserFolder(templatesFolder).listFiles();
		if (files != null) {
			Arrays.stream(files).forEach(f -> {
				if (f.getName().toLowerCase(Locale.ENGLISH).endsWith(templateExt.toLowerCase(Locale.ENGLISH)))
					tmodel.addElement(f.getName());
			});
		}

		sectionPanel.add("Center", PanelUtils.northAndCenterElement(opts, new JScrollPane(templates), 5, 5));

		preferencesDialog.preferences.add(sectionPanel, name);
	}

	private static class TemplateRender extends DefaultListCellRenderer{

		private final String folder;
		public TemplateRender(String templateFolder){
			this.folder = templateFolder;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			String[] prefix = {".png",".jpg"};
			if (Arrays.stream(prefix).anyMatch(value.toString()::endsWith)) {
				var icon = new File(UserFolderManager.getFileFromUserFolder(folder),value.toString());
				try {
					int hei = this.getFontMetrics(this.getFont()).getHeight()+3;
					this.setIcon(new ImageIcon(ImageIO.read(icon).getScaledInstance(hei,hei,Image.SCALE_SMOOTH)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			setOpaque(isSelected);
			return this;
		}
	}

}
