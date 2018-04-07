package org.polushin.chat.protocol;

/**
 * Отправитель: сервер
 * Назначение: Уведомление клиента о фатальной ошибке.
 * Сразу после отправки данного пакета сервер закроет соединение.
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
