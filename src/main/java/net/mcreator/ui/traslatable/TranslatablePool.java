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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mcreator.io.UserFolderManager;
import net.mcreator.ui.MCreatorApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;

import static org.apache.logging.log4j.core.util.NameUtil.md5;

/**
 * e-mail: 3154934427@qq.com
 *	翻译池,便于贡献者翻译
 *	不要出bug啊..
 * @author cdc123
 * @classname TranslatablePool
 * @date 2022/8/17 9:30
 */
public class TranslatablePool {
	private final Logger logger = LogManager.getLogger("TP");

	private final JsonObject json;
	private static TranslatablePool instance;
	public static TranslatablePool getPool(){
		if (instance == null) {
				instance = new TranslatablePool();
		}
		return instance;
	}

	private TranslatablePool() {
		InputStream defaultPoolInput = null;
		if (MCreatorApplication.isInternet) {
			try {
				URL url = new URL(
						"https://raw.githubusercontent.com/cdc12345/MCreator-Chinese/master/src/main/resources/pools.tra");
				logger.info("系统正在与在线翻译池沟通");
				defaultPoolInput = url.openStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		json = new Gson().fromJson(new InputStreamReader(
				Objects.requireNonNullElse(defaultPoolInput,this.getClass().getResourceAsStream("/pools.tra"))),
				JsonObject.class);
		logger.info("翻译池已经准备就绪");
	}
	public boolean containValue(String key){
		return containValue("",key);
	}

	public boolean containValue(String nameSpace,String key){
		if (key == null){
			return false;
		}
		String lowerKey = key.toLowerCase(Locale.ENGLISH);
		if (nameSpace != null&&!"".equals(nameSpace)){
			lowerKey = nameSpace+":"+lowerKey;
		}
		return json.has(lowerKey);
	}

	public String getValue(String key){
		return getValue("",key);
	}

	public String getValue(String nameSpace,final String key){
		if (key == null){
			return null;
		}
		String lowerKey = key.toLowerCase(Locale.ENGLISH);
		String oLowerKey = lowerKey;
		if (nameSpace != null&&!"".equals(nameSpace)){
			oLowerKey = nameSpace+":"+lowerKey;
		}
		try {
			JsonElement element = json.get(oLowerKey);
			return element.getAsString();
		} catch (Exception e){
			try {
				return json.get(lowerKey).getAsString();
			} catch (NullPointerException en) {
				return key;
			}
		}
	}

	public JsonElement translateBaidu(String origin) throws IOException {
		logger.info("翻译api正在被调用");
		String urlString = "https://fanyi-api.baidu.com/api/trans/vip/translate";
		String appidString = "20220101001043872";
		String passwordString = "pHikfC_jnDKxpFXDnht5";
		String saltString = "absabsjajsa";
		String signString = md5(appidString+origin+saltString+passwordString);
		URL url = new URL(urlString+"?q="+origin+"&from=en&to=zh&appid="+appidString+"&salt="+saltString+"&sign="+signString);
		return new Gson().fromJson(new InputStreamReader(url.openStream()),JsonObject.class).get("trans_result")
				.getAsJsonArray().get(0).getAsJsonObject().get("dst");
	}

}
