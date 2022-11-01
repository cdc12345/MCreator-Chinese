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

import net.mcreator.io.FileIO;
import net.mcreator.minecraft.ElementUtil;
import net.mcreator.minecraft.MCItem;
import net.mcreator.plugin.PluginLoader;
import net.mcreator.ui.init.UIRES;
import net.mcreator.util.image.ImageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * e-mail: 3154934427@qq.com
 * 精简版,用于翻译
 * @author cdc123
 * @classname AdvancedTraslatable
 * @date 2022/8/16 16:42
 */
public class AdvancedTranslatableComboBox<T> extends JComboBox<T> {

	final Logger LOGGER = LogManager.getLogger("TranslatableComboBox");

	Map<T,String> diction;
	/**
	 * 字符串匹配,主要用于盲盒汉化
	 */
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
	public AdvancedTranslatableComboBox(String nameSpace){
		super();
		this.nameSpace = nameSpace;
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
				return pool.getValue(nameSpace,origin)+(displayEnglish?"(" + origin+")":"");
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
			AdvancedTranslatableComboBox.this.addMouseListener(new MouseAdapter() {
				@Override public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 2){
						new TranslatableSearchDialog(AdvancedTranslatableComboBox.this,nameSpace);
					}
				}
			});
		}


		static Map<String,MCItem> itemMap;
		static Map<String,Icon> iconMap = new HashMap<>();

		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel result = new JLabel();
			result.setOpaque(true);
			result.setHorizontalAlignment(CENTER);
			result.setVerticalAlignment(CENTER);
			if (isSelected) {
				result.setBackground(list.getSelectionBackground());
				result.setForeground(list.getSelectionForeground());
			} else {
				result.setBackground(list.getBackground());
				result.setForeground(list.getForeground());
			}
			result.setText(getDisplayText(value));
			result.setHorizontalTextPosition(SwingConstants.RIGHT);
			result.setHorizontalAlignment(SwingConstants.LEFT);


			String name;
			if (matchString != null)
				name = matchString.apply(value);
			else
				name = value.toString();
			name = name.toLowerCase(Locale.ROOT);

			int size = 32;

			if (iconMap.containsKey(name)) {
				result.setIcon(iconMap.get(name));
				return result;
			}

			if (itemMap == null) {
				itemMap = new HashMap<>();
				List<MCItem> items = ElementUtil.loadBlocksAndItems(null);
				items.forEach(a->itemMap.put(a.getReadableName().toLowerCase(Locale.ROOT),a));
			}

			//如果为default
			Map<String,String> spec = getDefault();
			if (spec.containsKey(name)){
				result.setIcon(new ImageIcon(ImageUtils.resize(
						UIRES.getImageFromResourceID(spec.get(name)).getImage(),size)));
				return result;
			}
			//如果名字为颜色单词,那么就自动配图 比如 red 配纯红色
			try {
				Color color = (Color) Color.class.getField(name).get(null);
				result.setIcon(ImageUtils.colorize(
						new ImageIcon(new BufferedImage(size,size,BufferedImage.TYPE_INT_RGB)),color,true));
				return result;
			} catch (IllegalAccessException | NoSuchFieldException ignore) {
				Map<String,Color> extraColors = Map.of("purple",new Color(128,0,128),"gold",new Color(255,215,0));
				Color color = extraColors.get(name);
				if (color != null) {
					result.setIcon(ImageUtils.colorize(
							new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)), color, true));
					iconMap.put(name,result.getIcon());
					return result;
				}
			}
			//mc item
			MCItem item = itemMap.get(name.replace('_',' '));
			if (item != null){
				result.setIcon(item.icon);
				iconMap.put(name,result.getIcon());
				return result;
			}

			//datalists
			Set<String> images = PluginLoader.INSTANCE.getResources("datalists.icons",Pattern.compile("(?i)"+name+"\\.png"));
			if (images != null){
				for (String image:images) {
					try {
						ImageIcon icon = new ImageIcon(ImageUtils.resize(
								UIRES.getImageFromResourceID(image).getImage(),size));
						result.setIcon(icon);
						iconMap.put(name,icon);
						return result;
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			}
			return result;
		}

		private Map<String,String> getDefault(){
			Map<String,String> spec =  new HashMap<>(
					Map.of("default", "datalists/icons/BARRIER.png", "foliage", "datalists/icons/LEAVES#0.png",
							"no tint", "datalists/icons/BARRIER.png", "flower", "datalists/icons/RED_FLOWER#0.png",
							"nether", "datalists/icons/NETHERBRICK.png", "wood", "datalists/icons/OAK_WOOD.png"));
			spec.put("plains","datalists/icons/GRASS.png");
			spec.put("desert","datalists/icons/SAND#0.png");
			spec.put("beach","datalists/icons/WATER.png");
			spec.put("cave","datalists/icons/WEB.png");
			spec.put("crop","datalists/icons/WHEAT.png");
			spec.put("iron","datalists/icons/IRON_INGOT.png");
			return spec;
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
						TranslatableSearchDialog.this.dispose();
					}
				}
			});

			add(searchText,"North");
			add(scrollPane,"Center");

			this.addWindowListener(new WindowAdapter() {

				@Override public void windowClosing(WindowEvent e) {
					TranslatableSearchDialog.this.setVisible(false);
					TranslatableSearchDialog.this.dispose();
				}
			});
			pack();
			setAlwaysOnTop(true);
			setLocationRelativeTo(null);

			setVisible(true);
		}
	}
}
