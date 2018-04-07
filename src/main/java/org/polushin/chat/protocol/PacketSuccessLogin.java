package org.polushin.chat.protocol;

import java.util.UUID;

/**
 * Отправитель: сервер
 * Назначение: После успешного подключения клиента к чату, возвращает уникальный ключ сессии клиента.
 */
public class PacketSuccessLogin implements Packet {

	private final UUID uuid;

	/**
	 * @param uuid Уникальный идентификатор сессии.
	 */
	public PacketSuccessLogin(UUID uuid) {
		if (uuid == null)
			throw new IllegalArgumentException("UUID cannot be null!");
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}
}
