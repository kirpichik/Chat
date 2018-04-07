package org.polushin.chat.protocol;

/**
 * Отправитель: сервер
 * Назначение: Уведомляет клиент о версии протокола сервера и о методе общения
 * пакетами, которые хочет сервер. Данный пакет всегда отправляется при
 * подключении и всегда при помощи class-bytes-serialize.
 */
public class PacketConnect implements Packet {

	public static final int PROTOCOL_VERSION = 1;

	public static final boolean USE_JSON = true;
	public static final boolean USE_BYTE = false;

	private final boolean useJson;
	private final int version;

	/**
	 * @param useJson Использовать JSON формат общения или class-bytes-serialize.
	 */
	public PacketConnect(boolean useJson) {
		this(useJson, PROTOCOL_VERSION);
	}

	/**
	 * Для поддержки устаревших протоколов.
	 *
	 * @param useJson Использовать JSON формат общения или class-bytes-serialize.
	 * @param version Версия протокола.
	 */
	public PacketConnect(boolean useJson, int version) {
		this.useJson = useJson;
		this.version = version;
	}

	public boolean useJson() {
		return useJson;
	}

	public int getVersion() {
		return version;
	}
}
