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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;

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
	private static final Logger LOGGER = LogManager.getLogger("Translator");

	private static final HashMap<String,String> translation = new HashMap<>();
	public static String translateCNToEN(String origin)  {
		if (origin == null) return "";
		if (StringUtils.isEnglish(origin)) return origin;
		if (translation.containsKey(origin)) return translation.get(origin);
		String result = origin;
		try {
			switch (PreferencesManager.PREFERENCES.external.translatorEngine) {
//				case "百度" -> result = translateBaidu(origin, "auto", "en");
				case "Kate" -> result = translateKate(origin);
				case "Han" -> result = translateHan(origin);
			default -> {
				return result;
			}
			}
		} catch (Exception e){
			e.printStackTrace();
			return result;
		}
		LOGGER.info("翻译结果:"+result);
		translation.put(origin,result);
		translation.put(result,origin);
		return result;
	}

	/**
	 * 作者自己注册的,每个月有限额
	 * 请不要滥用
	 * @param origin 原始文本
	 * @return json元素
	 * @throws IOException 无法读取等
	 */
	public static String translateBaidu(String origin,String from,String to) throws IOException {
		LOGGER.info("使用百度翻译文本:"+origin+"("+from+" to "+to);
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
		LOGGER.info("使用kate翻译文本:"+origin);
		String urlString = "https://api.66mz8.com/api/translation.php?info=%s";
		URL url = new URL(String.format(urlString,origin));
		return new Gson().fromJson(new InputStreamReader(url.openStream()),JsonObject.class).get("fanyi").getAsString();
	}

	public static String translateHan(String origin) throws IOException {
		LOGGER.info("正在使用Han翻译文本:"+origin);
		String urlString = "https://api.vvhan.com/api/fy?text=%s";
		URL url = new URL(String.format(urlString,origin));
		return new Gson().fromJson(new InputStreamReader(url.openStream()),JsonObject.class).getAsJsonObject("data").get("fanyi").getAsString();
	}
}
