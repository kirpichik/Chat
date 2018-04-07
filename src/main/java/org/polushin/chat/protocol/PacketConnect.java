package org.polushin.chat.protocol;

import org.polushin.chat.ProtocolCommunicator;

/**
 * Отправитель: клиент
 * Назначение: Уведомляет сервер о версии протокола клиента и о методе общения
 * пакетами, которые хочет клиент. Данный пакет всегда отправляется при
 * подключении и всегда в JSON виде.
 */
public class PacketConnect implements Packet {

	private final ProtocolCommunicator.CommunicateType type;
	private final int version;

	/**
	 * @param type Тип коммуникации пакетами.
	 */
	public PacketConnect(ProtocolCommunicator.CommunicateType type) {
		this(type, ProtocolCommunicator.PROTOCOL_VERSION);
	}

	/**
	 * Для поддержки протоколов иной версии.
	 *
	 * @param type Тип коммуникации пакетами.
	 * @param version Версия протокола.
	 */
	public PacketConnect(ProtocolCommunicator.CommunicateType type, int version) {
		this.type = type;
		this.version = version;
	}

	public ProtocolCommunicator.CommunicateType getCommunicateType() {
		return type;
	}

	public int getVersion() {
		return version;
	}
}
