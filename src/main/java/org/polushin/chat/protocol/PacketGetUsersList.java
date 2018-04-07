package org.polushin.chat.protocol;

import java.util.UUID;

/**
 * Отправитель: клиент
 * Назначение: Запрашивает список имен текущих пользователей в чате.
 */
public class PacketGetUsersList implements Packet {

	private final UUID uuid;

	/**
	 * @param uuid Уникальный идентификатор сессии.
	 */
	public PacketGetUsersList(UUID uuid) {
		if (uuid == null)
			throw new IllegalArgumentException("UUID cannot be null!");
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}
}
