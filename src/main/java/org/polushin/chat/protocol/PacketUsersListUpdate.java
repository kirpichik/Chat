package org.polushin.chat.protocol;

/**
 * Отправитель: сервер
 * Назначение: Уведомляет клиента об изменении списка онлайн пользоватлей.
 * Содержит присоединившегося или отключившегося участника чата.
 */
public class PacketUsersListUpdate implements Packet {

	private final boolean newMember;
	private final String username;

	/**
	 * @param isNewMember Новый участник чата или отключившийся бывший участник.
	 * @param username Имя пользователя.
	 */
	public PacketUsersListUpdate(boolean isNewMember, String username) {
		if (username == null)
			throw new IllegalArgumentException("Username cannot be null!");
		newMember = isNewMember;
		this.username = username;
	}

	public boolean isNewMember() {
		return newMember;
	}

	public String getUsername() {
		return username;
	}
}
