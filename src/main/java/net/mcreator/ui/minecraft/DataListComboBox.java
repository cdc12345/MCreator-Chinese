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

package net.mcreator.ui.minecraft;

import net.mcreator.generator.mapping.MappableElement;
import net.mcreator.minecraft.DataListEntry;
import net.mcreator.minecraft.MCItem;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.init.BlockItemIcons;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.traslatable.AdvancedTranslatableComboBox;
import net.mcreator.ui.traslatable.TranslatablePool;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.util.image.EmptyIcon;
import net.mcreator.util.image.ImageUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataListComboBox extends JComboBox<DataListEntry> {

	private String nameSpace = "";
	public DataListComboBox(MCreator mcreator, List<DataListEntry> list,String nameSpace) {
		this(mcreator,list);
		this.nameSpace = nameSpace;
		init(mcreator,nameSpace);
	}

	public DataListComboBox(MCreator mcreator, List<DataListEntry> list) {
		super(list.toArray(new DataListEntry[0]));
		init(mcreator,nameSpace);
	}

	public DataListComboBox(MCreator mcreator) {
		init(mcreator,nameSpace);
	}

	private void init(MCreator mcreator,String nameSpace) {
		addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 2)
					new AdvancedTranslatableComboBox.TranslatableSearchDialog(DataListComboBox.this);
			}
		});
		setRenderer(new CustomRenderer(mcreator,nameSpace));
	}

	public void setSelectedItem(String string) {
		this.setSelectedItem(new DataListEntry.Dummy(string));
	}

	public void setSelectedItem(MappableElement mappableElement) {
		if (mappableElement == null)
			setSelectedIndex(0);
		else
			this.setSelectedItem(new DataListEntry.Dummy(mappableElement.getUnmappedValue()));
	}

	public void setSelectedItem(DataListEntry dataListEntry) {
		super.setSelectedItem(dataListEntry);
	}

	@Override @Nonnull public DataListEntry getSelectedItem() {
		Object superretval = super.getSelectedItem();
		if (superretval == null)
			return new DataListEntry.Null();

		return (DataListEntry) super.getSelectedItem();
	}

	public static class CustomRenderer extends JLabel implements ListCellRenderer<DataListEntry> {

		private final MCreator mcreator;
		private final String nameSpace;

		public CustomRenderer(MCreator mcreator,String nameSpace) {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);

			this.nameSpace = nameSpace;
			this.mcreator = mcreator;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends DataListEntry> list, DataListEntry value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			TranslatablePool pool = TranslatablePool.getPool();
			setText(pool.getValue(nameSpace,value.getReadableName())+"("+value.getReadableName()+")");

			if (value instanceof DataListEntry.Custom) {
				setIcon(MCItem.getBlockIconBasedOnName(((DataListEntry.Custom) value).getModElement().getWorkspace(),
						value.getName()));
			} else if (value.getTexture() == null) {
				setIcon(new EmptyIcon(32, 32));
			} else {
				setIcon(BlockItemIcons.getIconForItem(value.getTexture()));
			}

			if (!value.isSupportedInWorkspace(mcreator.getWorkspace())) {
				Icon imageIcon = getIcon();
				if (imageIcon instanceof ImageIcon)
					setIcon(ImageUtils.changeSaturation((ImageIcon) imageIcon, 0.1f));
				setText(L10N.t("datalist_combobox.not_supported", getText()));
				setForeground((Color) UIManager.get("MCreatorLAF.GRAY_COLOR"));
			}

			setHorizontalTextPosition(SwingConstants.RIGHT);
			setHorizontalAlignment(SwingConstants.LEFT);

			return this;
		}

	}
}
