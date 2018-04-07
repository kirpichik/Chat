package org.polushin.chat.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Пакет протокола обмена сообщениями.
 */
public interface Packet extends Serializable {

	/**
	 * @param gson Сериалайзер для JSON-а.
	 *
	 * @return Сериализованный в JSON объект.
	 */
	default String toJson(Gson gson) {
		JsonObject object = new JsonObject();
		object.add("type", new JsonPrimitive(getType()));
		object.add("value", gson.toJsonTree(this));
		return object.toString();
	}

	/**
	 * Записывает сериализованный объект в поток.
	 *
	 * @param stream Поток для записи.
	 *
	 * @throws IOException Ошибка при записи в поток.
	 */
	default void toStream(ObjectOutputStream stream) throws IOException {
		if (stream == null)
			throw new IllegalArgumentException("Stream cannot be null!");
		stream.writeObject(this);
	}

	/**
	 * @return Имя типа пакета. Если имя наследника начинается с "Packet", это слово будет обрезано.
	 */
	default String getType() {
		if (getClass().getSimpleName().startsWith("Packet"))
			return getClass().getSimpleName().substring("Packet".length());
		return getClass().getSimpleName();
	}

}
