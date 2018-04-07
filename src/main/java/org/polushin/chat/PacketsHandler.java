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
	 * @param communicator Коммуникатор, который получил данный пакет.
	 */
	default void ioException(IOException e, ProtocolCommunicator communicator) {
		e.printStackTrace(System.err);
	}

	/**
	 * Обработчик исключения при получении ошибочного пакета.
	 *
	 * @param e Обрабатываемое исключение.
	 * @param communicator Коммуникатор, который получил данный пакет.
	 */
	default void invalidPacketException(Packet.InvalidPacketException e, ProtocolCommunicator communicator) {
		e.printStackTrace(System.err);
	}

}
