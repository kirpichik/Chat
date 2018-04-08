package org.polushin.chat.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Пакет протокола обмена сообщениями.
 */
public interface Packet extends Serializable {

	String PACKET_PREFIX = "Packet";

	/**
	 * @param gson Сериалайзер JSON.
	 *
	 * @return Сериализованный JSON объект.
	 */
	default String toJsonStream(Gson gson) {
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
	default void toBytesStream(ObjectOutputStream stream) throws IOException {
		if (stream == null)
			throw new IllegalArgumentException("Stream cannot be null!");
		stream.writeObject(this);
	}

	/**
	 * @return Имя типа пакета. Если имя наследника начинается с "Packet", это слово будет обрезано.
	 */
	default String getType() {
		if (getClass().getSimpleName().startsWith(PACKET_PREFIX))
			return getClass().getSimpleName().substring(PACKET_PREFIX.length());
		return getClass().getSimpleName();
	}

	/**
	 * Читает пакет из Byte потока.
	 *
	 * @param stream Поток для чтения.
	 *
	 * @return Принятый из потока пакет.
	 *
	 * @throws IOException Ошибка чтения пакета.
	 * @throws InvalidPacketException Ошибка разбора пакета.
	 */
	static Packet fromBytesStream(ObjectInputStream stream) throws IOException, InvalidPacketException {
		try {
			Object object = stream.readObject();
			if (!(object instanceof Packet))
				throw new ClassCastException("Received object is not Packet!");
			return (Packet) object;
		} catch (ClassNotFoundException | ClassCastException e) {
			throw new InvalidPacketException(e);
		}
	}

	/**
	 * Читает пакет из JSON потока.
	 *
	 * @param gson Парсер JSON.
	 * @param stream Поток для чтения.
	 *
	 * @return Принятый из потока пакет.
	 *
	 * @throws IOException Ошибка чтения пакета.
	 * @throws InvalidPacketException Ошибка разбора пакета.
	 */
	static Packet fromJsonStream(Gson gson, ObjectInputStream stream) throws IOException, InvalidPacketException {
		try {
			JsonObject object = gson.fromJson((String) stream.readObject(), JsonObject.class);
			String type = object.get("type").getAsString();
			Class<?> clazz;
			try {
				clazz = Class.forName(Packet.class.getName() + type);
			} catch (ClassNotFoundException e) {
				clazz = Class.forName(type);
			}
			if (!Packet.class.isAssignableFrom(clazz))
				throw new ClassCastException("Received object is not Packet!");
			return (Packet) gson.fromJson(object.get("value"), clazz);
		} catch (JsonSyntaxException | ClassCastException | ClassNotFoundException e) {
			throw new InvalidPacketException(e);
		}
	}

	/**
	 * Ошибка при разборе пакета.
	 */
	class InvalidPacketException extends Exception {

		public InvalidPacketException() {

		}

		public InvalidPacketException(String message) {
			super(message);
		}

		public InvalidPacketException(Throwable ex) {
			super(ex);
		}

		public InvalidPacketException(String message, Throwable ex) {
			super(message, ex);
		}

	}

}
