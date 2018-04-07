package org.polushin.chat.protocol;

import java.util.Collection;

/**
 * Отправитель: сервер
 * Назначение: Возвращает список имен текущих пользователей в чате.
 */
public class PacketUsersList implements Packet {

	private final Collection<String> users;

	/**
	 * @param users Список онлайн пользователей.
	 */
	public PacketUsersList(Collection<String> users) {
		if (users == null)
			throw new IllegalArgumentException("Users set cannot be null!");
		this.users = users;
	}

	public Collection<String> getUsers() {
		return users;
	}
}
