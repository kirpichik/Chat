package org.polushin.chat.protocol;

import java.util.UUID;

/**
 * Отправитель: клиент
 * Назначение: Отправляет сообщение в чат.
 */
public class PacketSendMessage implements Packet {

	private final UUID uuid;
	private final String message;

	/**
	 * @param uuid Уникальный идентификатор сессии.
	 * @param message Сообщение.
	 */
	public PacketSendMessage(UUID uuid, String message) {
		if (uuid == null)
			throw new IllegalArgumentException("UUID cannot be null!");
		if (message == null)
			throw new IllegalArgumentException("Message cannot be null!");
		this.uuid = uuid;
		this.message = message;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getMessage() {
		return message;
	}
}
