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
import java.util.Random;

/**
 * e-mail: 3154934427@qq.com
 * 每日一帖
 *
 * @author cdc123
 * @classname DayOfTipDialog
 * @date 2022/11/8 12:50
 */
public class DayOfTipDialog extends JDialog {
	private final String[] tipContent;

	public DayOfTipDialog(Window parent,String[] tipContent){
		super(parent);
		this.tipContent = tipContent;

		this.setTitle("每时一帖");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		JLabel content = new JLabel(tipContent[new Random().nextInt(tipContent.length)]);
		content.setHorizontalAlignment(SwingConstants.LEFT);
		content.setVerticalTextPosition(SwingConstants.TOP);

		JPanel controlPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton next = new JButton("下一条");
		JButton ok = new JButton("确定");
		controlPane.add(next);
		controlPane.add(ok);

		next.addActionListener(a->{
			content.setText(tipContent[new Random().nextInt(tipContent.length)]);
		});
		ok.addActionListener(a->{
			DayOfTipDialog.this.setVisible(false);
		});
	}

	public String[] getTipContent() {
		return tipContent;
	}
}
