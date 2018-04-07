package org.polushin.chat.server;

import org.polushin.chat.PacketsHandler;
import org.polushin.chat.ProtocolCommunicator;
import org.polushin.chat.protocol.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Реализация сервера.
 */
public class Server extends Thread implements PacketsHandler {

	private static final UUID DEFAULT_UUID = new UUID(0, 0);

	private final int maxConnections;
	private final ServerSocket socket;
	private final ConcurrentMap<UUID, String> users = new ConcurrentHashMap<>();
	private final ConcurrentMap<ProtocolCommunicator, UUID> communicators = new ConcurrentHashMap<>();

	private volatile boolean interrupted;

	/**
	 * @param port Порт для прослушивания входящих подключений.
	 *
	 * @throws IOException Ошибка создания сервера.
	 */
	public Server(int port, int maxConnections) throws IOException {
		if (maxConnections < 1)
			throw new IllegalArgumentException("Max connections must be positive!");
		this.maxConnections = maxConnections;
		socket = new ServerSocket(port);
		start();
	}

	@Override
	public void run() {
		while (!interrupted) {
			try {
				Socket socket = this.socket.accept();
				ProtocolCommunicator communicator = new ProtocolCommunicator(this, socket.getInputStream(),
				                                                             socket.getOutputStream());
				if (communicators.size() >= maxConnections) {
					try {
						communicator.sendPacket(new PacketFatalError("Server is full!"));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					communicator.close();
					continue;
				}
				communicators.put(communicator, DEFAULT_UUID);
			} catch (IOException e) {
				if (!interrupted)
					e.printStackTrace();
				return;
			}
		}
	}

	@Override
	public void inputPacket(Packet packet, ProtocolCommunicator communicator) throws InterruptedException {
		switch (packet.getType()) {
			case "Connect":
				packetConnect((PacketConnect) packet, communicator);
				break;
			case "Login":
				packetLogin((PacketLogin) packet, communicator);
				break;
			case "GetUsersList":
				packetGetUsersList((PacketGetUsersList) packet, communicator);
				break;
			case "SendMessage":
				packetSendMessage((PacketSendMessage) packet, communicator);
				break;
			case "Disconnect":
				packetDisconnect((PacketDisconnect) packet, communicator);
				break;
			default:
				sendFatalError(communicator, "Received unhandled packet type:" + packet.getType());
		}
	}

	@Override
	public void ioException(IOException e, ProtocolCommunicator communicator) {
		removeCommunicator(communicator);
		communicator.close();
	}

	@Override
	public void invalidPacketException(Packet.InvalidPacketException e, ProtocolCommunicator communicator) {
		removeCommunicator(communicator);
		try {
			sendFatalError(communicator, "Received invalid packet.");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		communicator.close();
	}

	@Override
	public void interrupt() {
		interrupted = true;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			broadcastPacket(new PacketFatalError("Server closed."));
		} catch (InterruptedException ignored) {
		}
		communicators.keySet().forEach(ProtocolCommunicator::close);
		super.interrupt();
	}

	/**
	 * Отправляет фатальную ошибку и закрывает коммуникатор.
	 *
	 * @param communicator Коммуникатор.
	 * @param reason Причина ошибки.
	 *
	 * @throws InterruptedException Прерывание отправки пакета.
	 */
	private void sendFatalError(ProtocolCommunicator communicator, String reason) throws InterruptedException {
		communicator.sendPacket(new PacketFatalError(reason));
		communicator.close();
	}

	/**
	 * Посылает данный пакет всем подключенным и имеющим ключ сессии на данный момент клиентам.
	 *
	 * @param packet Посылаемый пакет.
	 */
	private void broadcastPacket(Packet packet) throws InterruptedException {
		for (Map.Entry<ProtocolCommunicator, UUID> entry : communicators.entrySet())
			if (!entry.getValue().equals(DEFAULT_UUID))
				entry.getKey().sendPacket(packet);
	}

	/**
	 * Удаляет коммуникатора из списка активных и если коммуникатор имел открытую сессию,
	 * закрывает ее и уведомляет всех участников чата о том, что данный клиент отключен.
	 *
	 * @param communicator Удаляемый коммуникатор.
	 */
	private void removeCommunicator(ProtocolCommunicator communicator) {
		UUID uuid = communicators.remove(communicator);
		if (uuid != null && !uuid.equals(DEFAULT_UUID)) {
			String username = users.remove(uuid);
			if (username != null) {
				try {
					broadcastPacket(new PacketUsersListUpdate(false, username));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void packetConnect(PacketConnect packet, ProtocolCommunicator communicator) throws InterruptedException {
		if (packet.getVersion() != ProtocolCommunicator.PROTOCOL_VERSION) {
			removeCommunicator(communicator);
			sendFatalError(communicator, "Unknown protocol version: " + packet.getVersion());
			return;
		}

		communicator.setDefaultCommunicationType(packet.getCommunicateType());
		communicator.sendPacket(new PacketConnectionAccept(), ProtocolCommunicator.CommunicateType.JSON);
	}

	private void packetLogin(PacketLogin packet, ProtocolCommunicator communicator) throws InterruptedException {
		if (!packet.validateUsername()) {
			removeCommunicator(communicator);
			sendFatalError(communicator, "Invalid username: " + packet.getUsername());
			return;
		}
		UUID uuid = UUID.randomUUID();
		users.putIfAbsent(uuid, packet.getUsername());
		communicators.put(communicator, uuid);
		communicator.sendPacket(new PacketSuccessLogin(uuid));
		broadcastPacket(new PacketUsersListUpdate(true, packet.getUsername()));
	}

	private void packetGetUsersList(PacketGetUsersList packet, ProtocolCommunicator communicator) throws
			InterruptedException {
		if (!users.containsKey(packet.getUuid())) {
			removeCommunicator(communicator);
			sendFatalError(communicator, "Invalid UUID: " + packet.getUuid());
			return;
		}

		communicator.sendPacket(new PacketUsersList(users.values()));
	}

	private void packetSendMessage(PacketSendMessage packet, ProtocolCommunicator communicator) throws
			InterruptedException {
		if (!users.containsKey(packet.getUuid())) {
			removeCommunicator(communicator);
			sendFatalError(communicator, "Invalid UUID: " + packet.getUuid());
			return;
		}

		broadcastPacket(new PacketNewMessage(users.get(packet.getUuid()), packet.getMessage()));
	}

	private void packetDisconnect(PacketDisconnect packet, ProtocolCommunicator communicator) throws
			InterruptedException {
		if (!users.containsKey(packet.getUuid())) {
			removeCommunicator(communicator);
			sendFatalError(communicator, "Invalid UUID: " + packet.getUuid());
			return;
		}

		removeCommunicator(communicator);
		communicator.sendPacket(new PacketGoodbye());
		communicator.close();
	}

}
