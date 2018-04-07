package org.polushin.chat.protocol;

import java.util.UUID;

/**
 * Отправитель: клиент
 * Назначение: Уведомляет сервер о том, что клиент хочет завершить сессию и закрыть соединение.
 */
public class PacketDisconnect implements Packet {

	private final UUID uuid;

	/**
	 * @param uuid Уникальный идентификатор сессии.
	 */
	public PacketDisconnect(UUID uuid) {
		if (uuid == null)
			throw new IllegalArgumentException("UUID cannot be null!");
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}
}
