package org.polushin.chat.protocol;

import java.util.Set;

/**
 * Отправитель: сервер
 * Назначение: Возвращает список имен текущих пользователей в чате.
 */
public class PacketUsersList implements Packet {

	private final Set<String> users;

	/**
	 * @param users Список онлайн пользователей.
	 */
	public PacketUsersList(Set<String> users) {
		if (users == null)
			throw new IllegalArgumentException("Users set cannot be null!");
		this.users = users;
	}

	public Set<String> getUsers() {
		return users;
	}
}
