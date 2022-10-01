/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2022, Pylo, opensource contributors
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

package net.mcreator.ui.traslatable;

import net.mcreator.ui.validation.component.VComboBox;
import org.jboss.forge.roaster._shade.org.eclipse.jdt.internal.core.DocumentAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.Function;

/**
 * e-mail: 3154934427@qq.com
 * 精简版,用于翻译
 * @author cdc123
 * @classname AdvancedTraslatable
 * @date 2022/8/16 16:42
 */
public class AdvancedTranslatableComboBox<T> extends JComboBox<T> {

	Map<T,String> diction;
	Map<String,String> strDiction;
	Function<T,String> matchString;
	private boolean displayEnglish = true;
	private String nameSpace;

	public AdvancedTranslatableComboBox(T[] origin){
		this(origin,"");
	}
	public AdvancedTranslatableComboBox(T[] origin,String nameSpace){
		this(origin, (Function<T, String>) null);
		this.nameSpace = nameSpace;
	}
	public AdvancedTranslatableComboBox(T[] origin,Function<T,String> stringFunction){
		super(origin);
		this.matchString = stringFunction;
		setRenderer(new TranslatableCellRender());
	}

	public AdvancedTranslatableComboBox(){
		super();
		setRenderer(new TranslatableCellRender());
	}
	public AdvancedTranslatableComboBox(Map<String,String> diction, Function<T,String> stringMatcher){
		super();
		strDiction = diction;
		matchString = stringMatcher;
		setRenderer(new TranslatableCellRender());
	}
	public AdvancedTranslatableComboBox(T[] origin,Map<T,String> diction){
		super(origin);
		this.diction = diction;
		setRenderer(new TranslatableCellRender());
	}
	public AdvancedTranslatableComboBox(T[] origin,String[] translated){
		super(origin);
		diction = new HashMap<>();
		for (int i=0;i<origin.length;i++){
			if (i<translated.length)
				diction.put(origin[i],translated[i]);
			else
				diction.put(origin[i],origin[i].toString());
		}
		setRenderer(new TranslatableCellRender());
	}

	public void setDisplayEnglish(boolean displayEnglish){
		this.displayEnglish = displayEnglish;
	}

	public String getDisplayText(T value){
		TranslatablePool pool = TranslatablePool.getPool();
		if (matchString != null) {
			String origin = matchString.apply(value);
			if (strDiction == null)
				return pool.getValue(nameSpace,origin)+((displayEnglish)?"(" + origin+")":"");
			else {
				String nameSpace = "zh";
				return pool.getValue(nameSpace, strDiction.getOrDefault(origin,origin)) + ((displayEnglish) ?
						"(" + origin + ")" :
						"");
			}
		} else {
			if (diction == null)
				return pool.getValue(nameSpace,value.toString())+((displayEnglish)?"("+value+")":"");
			else {
				String nameSpace = "zh";
				return pool.getValue(nameSpace, diction.getOrDefault(value,value.toString())) + ((displayEnglish) ?
						"(" + value + ")" :
						"");
			}
		}
	}

	public class TranslatableCellRender extends JLabel implements ListCellRenderer<T>{

		public TranslatableCellRender(){
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
			AdvancedTranslatableComboBox.this.addMouseListener(new MouseAdapter() {
				@Override public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 2){
						new TranslatableSearchDialog(AdvancedTranslatableComboBox.this,nameSpace);
					}
				}
			});
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setText(getDisplayText(value));
			setHorizontalTextPosition(SwingConstants.RIGHT);
			setHorizontalAlignment(SwingConstants.LEFT);
			return this;
		}
	}

	public static class TranslatableSearchDialog extends Dialog{
		public <E> TranslatableSearchDialog(JComboBox<E> comboBox){
			this(comboBox,"");
		}
		public<E> TranslatableSearchDialog(JComboBox<E> comboBox,String nameSpace){
			super((Frame) null);
			setTitle("可翻译组件搜索对话窗口");
			setBackground(Color.black);
			setLayout(new BorderLayout());

			JTextField searchText = new JTextField();
			final ComboBoxModel<E> ab = comboBox.getModel();
			final ArrayList<E> list1 = new ArrayList<>();
			for (int index = 0 ;index < ab.getSize();index++){
				list1.add(ab.getElementAt(index));
			}

			JScrollPane scrollPane = new JScrollPane();
			JList<E> list = new JList<>(new DefaultListModel<>() {
				@Override public int getSize() {
					return list1.size();
				}

				@Override public E getElementAt(int index) {
					return list1.get(index);
				}
			});
			scrollPane.setViewportView(list);

			searchText.getDocument().addDocumentListener(new DocumentListener() {
				@Override public void insertUpdate(DocumentEvent e) {
					update();
				}

				@Override public void removeUpdate(DocumentEvent e) {
					update();
				}

				@Override public void changedUpdate(DocumentEvent e) {
					update();
				}

				private void update(){
					list1.clear();
					for (int index = 0 ;index < ab.getSize();index++){
						if ((TranslatablePool.getPool().getValue(nameSpace,ab.getElementAt(index).toString())+ab.getElementAt(index)).toLowerCase(Locale.ROOT).contains(searchText.getText().toLowerCase(
								Locale.ROOT))||searchText.getText().isEmpty()){
							list1.add(ab.getElementAt(index));
						}
					}
					list.updateUI();
				}
			});

			list.setCellRenderer(new DefaultListCellRenderer(){
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if (isSelected) {
						setBackground(list.getSelectionBackground());
						setForeground(list.getSelectionForeground());
					} else {
						setBackground(list.getBackground());
						setForeground(list.getForeground());
					}
					this.setText(TranslatablePool.getPool().getValue(nameSpace,value.toString()));
					return this;
				}
			});
			list.addMouseListener(new MouseAdapter() {
				@Override public void mousePressed(MouseEvent e) {
					super.mousePressed(e);
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
						TranslatableSearchDialog.this.setVisible(false);
						comboBox.setSelectedItem(list.getSelectedValue());
					}
				}
			});

			add(searchText,"North");
			add(scrollPane,"Center");

			this.addWindowListener(new WindowAdapter() {

				@Override public void windowClosing(WindowEvent e) {
					TranslatableSearchDialog.this.setVisible(false);
				}
			});
			pack();
			setAlwaysOnTop(true);
			setLocationRelativeTo(null);

			setVisible(true);
		}
	}
}
