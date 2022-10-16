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

package net.mcreator.ui.dialogs.wysiwyg;

import net.mcreator.blockly.data.Dependency;
import net.mcreator.element.parts.gui.Label;
import net.mcreator.ui.component.JColor;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.IHelpContext;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.procedure.ProcedureSelector;
import net.mcreator.ui.wysiwyg.WYSIWYG;
import net.mcreator.ui.wysiwyg.WYSIWYGEditor;
import net.mcreator.workspace.elements.VariableElement;
import net.mcreator.workspace.elements.VariableTypeLoader;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class LabelDialog extends AbstractWYSIWYGDialog {

	public LabelDialog(WYSIWYGEditor editor, @Nullable Label label) {
		super(editor.mcreator, label);
		setSize(560, 180);
		setLocationRelativeTo(editor.mcreator);
		setModal(true);
		JComboBox<String> name = new JComboBox<>(new String[] { "Label text", "Text is <TextFieldName:text>",
				"This block is located at <x> <y> and <z>.", "<ENBT:number:tagName>", "<ENBT:integer:tagName>",
				"<ENBT:logic:tagName>", "<ENBT:text:tagName>", "<BNBT:number:tagName>", "<BNBT:integer:tagName>",
				"<BNBT:logic:tagName>", "<BNBT:text:tagName>", "<energy>", "<fluidlevel>" });
		name.setEditable(true);

		JCheckBox checkBox = new JCheckBox("启用");


		JTextField tk = new JTextField();
		tk.setEditable(false);

		checkBox.addActionListener(a->{
				tk.setText("label."+editor.mcreator.getWorkspaceSettings().getModID()+"."+ Objects.requireNonNull(name.getSelectedItem()).toString().replaceAll("[^a-zA-Z\\s]","").replace(' ','_'));
				tk.setEditable(checkBox.isSelected());
		});

		for (VariableElement var2 : editor.mcreator.getWorkspace().getVariableElements()) {
			name.addItem("<VAR:" + var2.getName() + ">");
			if (var2.getType() == VariableTypeLoader.BuiltInTypes.NUMBER)
				name.addItem("<VAR:integer:" + var2.getName() + ">");
		}

		ProcedureSelector displayCondition = new ProcedureSelector(
				IHelpContext.NONE.withEntry("gui/label_display_condition"), editor.mcreator,
				L10N.t("dialog.gui.label_event_display_condition"), ProcedureSelector.Side.CLIENT, false,
				VariableTypeLoader.BuiltInTypes.LOGIC,
				Dependency.fromString("x:number/y:number/z:number/world:world/entity:entity"));
		displayCondition.refreshList();

		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.PAGE_AXIS));
		options.add(PanelUtils.join(FlowLayout.LEFT, L10N.label("dialog.gui.label_text"), name));
		options.add(PanelUtils.westAndCenterElement(new JLabel("翻译键值: "),PanelUtils.westAndCenterElement(checkBox,tk)));
		add("Center", PanelUtils.totalCenterInPanel(PanelUtils.centerAndEastElement(options, displayCondition, 20, 5)));

		setTitle(L10N.t("dialog.gui.label_component_title"));

		final JColor cola = new JColor(editor.mcreator, false, false);

		if (editor.renderBgLayer.isSelected()) {
			cola.setColor(new Color(60, 60, 60));
		} else {
			cola.setColor(Color.white);
		}

		options.add(PanelUtils.join(FlowLayout.LEFT, L10N.label("dialog.gui.label_text_color"), cola));
		JButton ok = new JButton(UIManager.getString("OptionPane.okButtonText"));

		getRootPane().setDefaultButton(ok);

		JButton cancel = new JButton(UIManager.getString("OptionPane.cancelButtonText"));
		add("South", PanelUtils.join(ok, cancel));

		if (label != null) {
			checkBox.setSelected(label.enableTK);
			tk.setText(label.TK);
			ok.setText(L10N.t("dialog.common.save_changes"));
			name.setSelectedItem(label.name);
			cola.setColor(label.color);
			displayCondition.setSelectedProcedure(label.displayCondition);
		}

		cancel.addActionListener(arg01 -> setVisible(false));
		ok.addActionListener(arg01 -> {
			setVisible(false);
			String text = (String) name.getSelectedItem();
			if (text != null) {
				if (tk.getText().isEmpty())tk.setText("label."+editor.mcreator.getWorkspaceSettings().getModID()+"."+ Objects.requireNonNull(name.getSelectedItem()).toString().replaceAll("[^a-zA-Z\\s]","").replace(' ','_'));
				if (label == null) {
					int textwidth = (int) (WYSIWYG.fontMC.getStringBounds(text, WYSIWYG.frc).getWidth());
					editor.editor.setPositioningMode(textwidth, 16);
					editor.editor.setPositionDefinedListener(e -> editor.editor.addComponent(setEditingComponent(
							new Label(text, editor.editor.newlyAddedComponentPosX,
									editor.editor.newlyAddedComponentPosY, text,tk.getText(),checkBox.isSelected(), cola.getColor(),
									displayCondition.getSelectedProcedure()))));
				} else {
					if (label.enableTK) {
						editor.mcreator.getWorkspace().removeLocalizationEntryByKey(label.TK);
					}
					int idx = editor.components.indexOf(label);
					editor.components.remove(label);
					Label labelNew = new Label(text, label.getX(), label.getY(), text,tk.getText(),checkBox.isSelected(), cola.getColor(),
							displayCondition.getSelectedProcedure());
					editor.components.add(idx, labelNew);
					setEditingComponent(labelNew);
				}
				if (checkBox.isSelected())
					editor.mcreator.getWorkspace().setLocalization(tk.getText(),text);
			}
		});

		setVisible(true);
	}

}
