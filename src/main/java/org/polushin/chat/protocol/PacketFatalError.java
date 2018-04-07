package org.polushin.chat.protocol;

/**
 * Отправитель: клиент/сервер
 * Назначение: Уведомляет цель о произошедшей фатальной ошибке.
 * Сразу после отправки данного пакета, отправитель закрывает соединение.
 */
public class PacketFatalError implements Packet {

	private final String reason;

	/**
	 * @param reason Причина ошибки.
	 */
	public PacketFatalError(String reason) {
		if (reason == null)
			throw new IllegalArgumentException("Reason cannot be null!");
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}
}
