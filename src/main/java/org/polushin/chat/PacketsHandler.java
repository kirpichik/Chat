package org.polushin.chat;

import org.polushin.chat.protocol.Packet;

import java.io.IOException;

/**
 * Обработчик входящих пакетов.
 */
public interface PacketsHandler {

	/**
	 * Обрабатывает входящий пакет.
	 * Обработка происходит асинхронно.
	 *
	 * @param packet Входящий пакет.
	 * @param communicator Коммуникатор, который получил данный пакет.
	 */
	void inputPacket(Packet packet, ProtocolCommunicator communicator) throws InterruptedException;

	/**
	 * Обработчик исключения при чтении/записи в пакетов.
	 *
	 * @param e Обрабатываемое исключение.
	 */
	default void ioException(IOException e) {
		e.printStackTrace(System.err);
	}

	/**
	 * Обработчик исключения при получении ошибочного пакета.
	 *
	 * @param e Обрабатываемое исключение.
	 */
	default void invalidPacketException(Packet.InvalidPacketException e) {
		e.printStackTrace(System.err);
	}

}
