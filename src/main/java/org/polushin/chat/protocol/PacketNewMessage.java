package org.polushin.chat.protocol;

/**
 * Отправитель: сервер
 * Назначение: Уведомляет клиент о новом сообщении в чате.
 */
public class PacketNewMessage implements Packet {

	private final String sender;
	private final String message;

	/**
	 * @param sender Отправитель сообщения.
	 * @param message Сообщение.
	 */
	public PacketNewMessage(String sender, String message) {
		if (sender == null)
			throw new IllegalArgumentException("Sender cannot be null!");
		if (message == null)
			throw new IllegalArgumentException("Message cannot be null!");
		this.message = message;
		this.sender = sender;
	}

	public String getSender() {
		return sender;
	}

	public String getMessage() {
		return message;
	}
}
