package org.polushin.chat.client;

import org.polushin.chat.PacketsHandler;
import org.polushin.chat.ProtocolCommunicator;
import org.polushin.chat.protocol.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.UUID;

/**
 * Реализация клиента.
 */
public class Client implements PacketsHandler {

	private final InputEventsHandler handler;

	private ProtocolCommunicator communicator;
	private String login;
	private volatile UUID uuid;
	private ProtocolCommunicator.CommunicateType type;

	/**
	 * @param handler Обработчик событий.
	 */
	public Client(InputEventsHandler handler) {
		if (handler == null)
			throw new IllegalArgumentException("Handler cannot be null!");
		this.handler = handler;
	}

	/**
	 * Подключается к новому серверу.
	 *
	 * @param address Адрес сервера.
	 * @param port Порт сервера.
	 */
	public void connect(String address, int port, String login, ProtocolCommunicator.CommunicateType type) {
		if (communicator != null)
			throw new IllegalStateException("Previous connection is not closed!");
		if (address == null)
			throw new IllegalArgumentException("Address cannot be null!");
		this.login = login;
		this.type = type;
		try {
			Socket socket = new Socket(address, port);
			communicator = new ProtocolCommunicator(this, socket.getInputStream(), socket.getOutputStream());
			communicator.sendPacket(new PacketConnect(type));
		} catch (IOException | InterruptedException e) {
			handler.fatalException(e);
		}
	}

	/**
	 * Отправляет сообщение.
	 *
	 * @param message Сообщение.
	 */
	public void sendMessage(String message) {
		if (uuid == null)
			throw new IllegalStateException("No connection!");
		try {
			communicator.sendPacket(new PacketSendMessage(uuid, message));
		} catch (InterruptedException e) {
			handler.fatalException(e);
		}
	}

	/**
	 * Отключается от сервера.
	 */
	public void disconnect() {
		if (communicator == null) {
			handler.disconnected();
			return;
		}
		try {
			communicator.sendPacket(new PacketDisconnect(uuid));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		communicator.close();
		communicator = null;
		uuid = null;
		login = null;
		type = null;
		handler.disconnected();
	}

	@Override
	public void inputPacket(Packet packet, ProtocolCommunicator communicator) throws InterruptedException {
		switch (packet.getType()) {
			case "ConnectionAccept":
				communicator.setDefaultCommunicationType(type);
				communicator.sendPacket(new PacketLogin(login));
				break;
			case "SuccessLogin":
				uuid = ((PacketSuccessLogin) packet).getUuid();
				communicator.sendPacket(new PacketGetUsersList(uuid));
				break;
			case "FatalError":
				handler.fatalException(new RuntimeException(
						"Received Fatal Error! Reason: " + ((PacketFatalError) packet).getReason()));
				break;
			case "UsersList":
				handler.connectionEstablished(((PacketUsersList) packet).getUsers());
				break;
			case "UsersListUpdate":
				PacketUsersListUpdate update = (PacketUsersListUpdate) packet;
				handler.onlineListUpdate(update.isNewMember(), update.getUsername());
				break;
			case "NewMessage":
				PacketNewMessage message = (PacketNewMessage) packet;
				handler.newMessage(message.getSender(), message.getMessage());
				break;
			case "Goodbye":
				handler.disconnected();
				break;
			default:
				handler.invalidPacketException(
						new Packet.InvalidPacketException("Received unhandled packet type: " + packet.getType()));
		}
	}

	@Override
	public void ioException(IOException e, ProtocolCommunicator communicator) {
		communicator.close();
		uuid = null;
		login = null;
		type = null;
		handler.fatalException(e);
	}

	@Override
	public void invalidPacketException(Packet.InvalidPacketException e, ProtocolCommunicator communicator) {
		handler.invalidPacketException(e);
	}

	/**
	 * Обработчик событий клиента.
	 */
	public interface InputEventsHandler {

		/**
		 * Уведомляет об установлении соединения и устанавливает список онлайн пользователей.
		 *
		 * @param onlineUsers Список пользователей.
		 */
		void connectionEstablished(Collection<String> onlineUsers);

		/**
		 * Новое сообщение в чате.
		 *
		 * @param username Имя отправителя.
		 * @param message Сообщение.
		 */
		void newMessage(String username, String message);

		/**
		 * Уведомляет об изменении списка пользователей онлайн.
		 *
		 * @param isNew Добавить пользователя или удалить.
		 * @param username Пользователь.
		 */
		void onlineListUpdate(boolean isNew, String username);

		/**
		 * Новое исключение сети.
		 *
		 * @param e Исключение.
		 */
		void fatalException(Exception e);

		/**
		 * Новое исключение пакета.
		 *
		 * @param e Исключение.
		 */
		void invalidPacketException(Packet.InvalidPacketException e);

		/**
		 * Уведомление об отключении.
		 */
		void disconnected();

	}
}
