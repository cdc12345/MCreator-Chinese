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

import net.mcreator.preferences.PreferencesManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * e-mail: 3154934427@qq.com
 *
 * @author cdc123
 * @classname RulesDialog
 * @date 2022/8/17 7:17
 */
public class RulesDialog {
	JDialog jd ;
	JPanel content;
	public RulesDialog(Frame parent) {
		jd = new JDialog(parent);
		jd.setTitle("开发须知 -- 请等待30秒");
		content = new JPanel(new BorderLayout());
		jd.setContentPane(content);

		//文本内容
		JScrollPane scroll = new JScrollPane();
		String text = """
				<html>
				<body><h1 id='div-aligncentermcreator模组开发须知div'><div align="center">MCreator模组开发须知</div></h1>
				<h4 id='在你要开始学习使用mcreator制作模组前请仔细阅读以下内容'>在你要开始学习使用MCreator制作模组前，请仔细阅读以下内容：</h4>
				<p>&nbsp;</p>
				<p>*1 MCr制作的模组一般不得用于盈利。（除玩家自愿）</p>
				<p><em>2 MCr制作的模组需标注MCr为开发工具，且不允许将它伪造成自己编写的模组且拒绝承认其是由MCreator制作的。</em></p>
				<p><em>3 MCr不得用于制作以下任何类型的模组：</em></p>
				<p><em>-无意义秒杀，外挂</em></p>
				<p>*-恶意或无意义玩梗，纯meme</p>
				<p><em>-仅添加少量物品/方块/工具装备/低质量维度</em></p>
				<p><em>-材质过度差劲，拉低MCr模组评价</em></p>
				<p><em>4 不应在未经原作者允许的情况下重制其他人的模组。</em></p>
				<p><em>5 可以参照其他模组的功能以提高自身MCr使用水平，</em></p>
				<p>6 不应出现在各大社区刷热度，刷好评等违规情况。</p>
				<p>7 不应在完成度极低/质量极差/违反规定的情况下发布模组至平台（如MC百科，mcbbs，CurseForge等）。</p>
				<p><em>8 MCreator有一定的功能局限性，切勿抱过大的希望。</em></p>
				<p><em>9 严禁发表对MCreator带有强烈主观的侮辱性/攻击性言论，如“MCr垃圾”，但可以客观评价其对模组圈造成的影响。</em></p>
				<p><em>对于违反以上规范的模组，可能会出现以下情况：</em></p>
				<p><em>-强烈的谴责和批评。</em></p>
				<p><em>-强制标注是由MCreator制作。</em></p>
				<p><em>-联系发布平台下架，作为反面教材。</em></p>
				<p><em>-要求下一版本更改。</em></p>
				<p>&nbsp;</p>
				<p>&nbsp;</p>
				<h3 id='请确保你已经认真阅读并同意所造成的一切后果由个人承担mcreator及其任何其他用户不承担责任'><strong>请确保你已经认真阅读并同意，所造成的一切后果由个人承担，MCreator及其任何其他用户不承担责任。</strong></h3>
				<h4 id='注该协议非mcreator官方提供由社区编辑发表于mcreator-chinese'><strong>注：该协议非MCreator官方提供，由社区编辑，发表于MCreator-Chinese。</strong></h4>
				<p>&nbsp;</p>
				<p><em>Q：新建不了工作区，怎么办？</em></p>
				<p><em>A：需要先设置jdk目录，在&quot;首选项/Gradle与运行&quot;可设置。</em></p>
				<p><em>Q：能够制作什么版本的模组？</em></p>
				<p><em>A：Forge1.18+，MCr一般随Forge的Latest版本更新。</em></p>
				<p><em>Q：如何在模组中添加物品模型或者实体模型？</em></p>
				<p>A：使用Blockbench。物品模型导入后需要在该元素选择材质才能应用。*</p>
				<p><em>Q：模组怎么设置作者，模组版本，网站，描述，名称，modid等？</em></p>
				<p><em>A：右上角的蓝色长方形，带一个小齿轮。点开即可。</em></p>
				<p><em>Q：能否为其他模组制作附属？</em></p>
				<p><em>A：很艰难，你需要学会阅读json，制作数据包，还需要很好的理解能力。现在也没很详细的教程，需要你自行探索。</em></p>
				<p><em>Q: 什么时候可以点同意</em></p>
				<p><em>A: 30秒后,同意之后便不会再次出现</em></p>
				<p>&nbsp;</p>
				</body>
				</html>
				""";
		JLabel con = new JLabel(text);
		scroll.setViewportView(con);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		content.add(scroll,BorderLayout.CENTER);

		//按钮面板
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton accept = new JButton("同意");
		JButton refuse = new JButton("不同意");

		accept.setEnabled(false);
		Timer timer = new Timer(30*1000,a->accept.setEnabled(true));
		timer.start();
		accept.addActionListener(a->{
			PreferencesManager.PREFERENCES.hidden.acceptRules = true;
			jd.setVisible(false);
			synchronized (RulesDialog.this){
				RulesDialog.this.notifyAll();
			}
		});
		refuse.addActionListener(a->System.exit(-1));


		buttonPanel.add(accept);
		buttonPanel.add(refuse);
		content.add(buttonPanel,BorderLayout.SOUTH);

		jd.pack();
		jd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		jd.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				System.exit(-1);
			}
		});
		jd.setLocationRelativeTo(parent);
		jd.setVisible(true);
	}
}
