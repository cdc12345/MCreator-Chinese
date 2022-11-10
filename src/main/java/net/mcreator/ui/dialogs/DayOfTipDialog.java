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

package net.mcreator.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * e-mail: 3154934427@qq.com
 * 每日一帖
 *
 * @author cdc123
 * @classname DayOfTipDialog
 * @date 2022/11/8 12:50
 */
public class DayOfTipDialog extends JDialog {
	public static DayOfTipDialog getInstance(Window parent){
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("tips.txt"))));
		return new DayOfTipDialog(parent,reader.lines().collect(Collectors.toList()).toArray(new String[0]));
	}

	private final String[] tipContent;

	public DayOfTipDialog(Window parent,String[] tipContent){
		super(parent);
		this.tipContent = tipContent;

		this.setTitle("每时一帖");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

		JTextArea content = new JTextArea(tipContent[new Random().nextInt(tipContent.length)]);
		content.setPreferredSize(new Dimension(content.getPreferredSize().width,100));
		content.setEditable(false);
		content.setColumns(3);
		content.setRows(4);

		JPanel controlPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton next = new JButton("随机");
		JButton ok = new JButton("确定");
		controlPane.add(next);
		controlPane.add(ok);

		next.addActionListener(a->{
			content.setText(tipContent[new Random().nextInt(tipContent.length)]);
			pack();
		});
		ok.addActionListener(a->{
			DayOfTipDialog.this.setVisible(false);
		});
		controlPane.add(next);
		controlPane.add(ok);

		add(content,"North");
		add(controlPane,"South");

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}
}
