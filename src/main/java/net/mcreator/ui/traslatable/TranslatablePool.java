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

import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Objects;

/**
 * e-mail: 3154934427@qq.com
 *	翻译池,便于贡献者翻译
 *	不要出bug啊..
 * @author cdc123
 * @classname TranslatablePool
 * @date 2022/8/17 9:30
 */
public class TranslatablePool {

	private static JsonObject json;
	private static TranslatablePool instance;
	public static TranslatablePool getPool(){
		if (instance == null) instance = new TranslatablePool();
		return instance;
	}

	private TranslatablePool(){
		json = new Gson().fromJson(new InputStreamReader(
				Objects.requireNonNull(this.getClass().getResourceAsStream("/pools.tra"))),JsonObject.class);
	}

	public String getValue(String key){
		key = key.toLowerCase(Locale.ENGLISH);
		JsonElement element = json.get(key);
		if (element == null)
			return key;
		else
			return element.getAsString();
	}

	public JsonObject getPoolSource(){
		return json;
	}

}
