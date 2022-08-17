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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * e-mail: 3154934427@qq.com
 * 精简版,用于翻译
 * @author cdc123
 * @classname AdvancedTraslatable
 * @date 2022/8/16 16:42
 */
public class AdvancedTranslatableComboBox<T> extends JComboBox<T> {
	private static final Logger LOG = LoggerFactory.getLogger("AdvancedTranslatableComboBox");

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

	public class TranslatableCellRender extends JLabel implements ListCellRenderer<T>{

		public TranslatableCellRender(){
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
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
			TranslatablePool pool = TranslatablePool.getPool();
			if (matchString != null) {
				String origin = matchString.apply(value);
				if (strDiction == null)
					setText(pool.getValue(nameSpace,origin)+((displayEnglish)?"(" + origin+")":""));
				else {
					String nameSpace = "zh";
					setText(pool.getValue(nameSpace, strDiction.get(origin)) + ((displayEnglish) ?
							"(" + origin + ")" :
							""));
				}
			} else {
				if (diction == null)
					setText(pool.getValue(nameSpace,value.toString())+((displayEnglish)?"("+value+")":""));
				else {
					String nameSpace = "zh";
					setText(pool.getValue(nameSpace, diction.get(value)) + ((displayEnglish) ?
							"(" + value.toString() + ")" :
							""));
				}
			}

			setHorizontalTextPosition(SwingConstants.RIGHT);
			setHorizontalAlignment(SwingConstants.LEFT);
			return this;
		}
	}
}
