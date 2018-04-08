package org.polushin.chat.protocol;

import java.util.regex.Pattern;

/**
 * Отправитель: клиент
 * Назначение: Запрашивает у сервера уникальный ключ для доступа к чату,
 * отправляя ему предпочитаемое имя пользователя.
 * Валидными считаются имена, соответствующие: [_-0-9a-zA-Z]+ и не длиннее 20 символов.
 */
public class PacketLogin implements Packet {

	public static final Pattern VALID_USERNAME = Pattern.compile("[_\\-0-9a-zA-Z]+");
	public static final int VALID_LENGTH = 20;

	private final String username;

	/**
	 * @param username Предпочитаемое имя пользователя.
	 */
	public PacketLogin(String username) {
		if (username == null)
			throw new IllegalArgumentException("Username cannot be null!");
		if (username.length() > VALID_LENGTH)
			throw new IllegalArgumentException("Username must be no longer than " + VALID_LENGTH + "characters.");
		if (!VALID_USERNAME.matcher(username).matches())
			throw new IllegalArgumentException("Username must be processed by: " + VALID_USERNAME.pattern());
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	/**
	 * @return Валидно ли имя пользователя.
	 */
	public boolean validateUsername() {
		return username.length() <= VALID_LENGTH && VALID_USERNAME.matcher(username).matches();
	}
}
