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

package net.mcreator.util.locale;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import static org.apache.logging.log4j.core.util.NameUtil.md5;

/**
 * e-mail: 3154934427@qq.com
 * 翻译api
 *
 * @author cdc123
 * @classname TranslatorUtils
 * @date 2022/10/9 16:35
 */
public class TranslatorUtils {
	public static String translateCNToEN(String origin) throws IOException, ParserConfigurationException, SAXException {
		if (StringUtils.isEnglish(origin)) return origin;
		switch (PreferencesManager.PREFERENCES.external.translatorEngine){
			case "百度" -> {
				return translateBaidu(origin,"","en");
			}
			case "Kate" ->{
				return translateKate(origin);
			}
			default -> {
				return origin;
			}
		}
	}

	/**
	 * 作者自己注册的,每个月有限额
	 * 请不要滥用
	 * @param origin 原始文本
	 * @return json元素
	 * @throws IOException 无法读取等
	 */
	public static String translateBaidu(String origin,String from,String to) throws IOException {
		String urlString = "https://fanyi-api.baidu.com/api/trans/vip/translate";
		String appidString = "20220101001043872";
		String passwordString = "pHikfC_jnDKxpFXDnht5";
		String saltString = "absabsjajsa";
		String signString = md5(appidString+origin+saltString+passwordString);
		URL url = new URL(String.format(urlString+"?q="+origin+"&from=%s&to=%s&appid="+appidString+"&salt="+saltString+"&sign="+signString,from,to));
		return new Gson().fromJson(new InputStreamReader(url.openStream()), JsonObject.class).get("trans_result")
				.getAsJsonArray().get(0).getAsJsonObject().get("dst").getAsString();
	}


	public static String translateKate(String origin) throws IOException {
		String urlString = "https://api.66mz8.com/api/translation.php?info=%s";
		URL url = new URL(String.format(urlString,origin));
		return new Gson().fromJson(new InputStreamReader(url.openStream()),JsonObject.class).get("fanyi").getAsString();
	}
}
