package org.polushin.chat;

import com.google.gson.Gson;
import org.polushin.chat.protocol.Packet;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Коммуникатор для приема/передачи пакетов между клиетом/сервером.
 */
public class ProtocolCommunicator {

	private static final Logger log = Logger.getLogger("Protocol");

	static {
		log.setLevel(Level.WARNING);
	}

	public static final int PROTOCOL_VERSION = 1;
	private static final int OUTPUT_QUEUE_SIZE = 10;
	private static final Gson gson = new Gson();

	private final PacketsHandler handler;
	private final AsyncInputHandler inputHandler;
	private final AsyncOutputHandler outputHandler;
	private final BlockingQueue<SendingPacket> outputQueue = new ArrayBlockingQueue<>(OUTPUT_QUEUE_SIZE, true);

	private volatile CommunicateType defaultCommunicationType = CommunicateType.JSON;

	/**
	 * @param handler Обработчик входящих пакетов.
	 * @param input Поток входящих данных.
	 * @param output Поток исходящих данных
	 *
	 * @throws IOException Ошибка при создании потоков коммуникации объектами.
	 */
	public ProtocolCommunicator(PacketsHandler handler, InputStream input, OutputStream output) throws IOException {
		if (handler == null)
			throw new IllegalArgumentException("Packets handler cannot be null!");
		if (input == null)
			throw new IllegalArgumentException("Input stream cannot be null!");
		if (output == null)
			throw new IllegalArgumentException("Output stream cannot be null!");
		log.info("Communicator enabled.");
		this.handler = handler;
		outputHandler = new AsyncOutputHandler(output);
		inputHandler = new AsyncInputHandler(input);
	}

	/**
	 * Устанавливает тип взаимодействия пакетами.
	 *
	 * @param type Новый тип взаимодействия.
	 */
	public void setDefaultCommunicationType(CommunicateType type) {
		if (type == null)
			throw new IllegalArgumentException("Type cannot be null!");
		defaultCommunicationType = type;
	}

	/**
	 * @return Текущий тип взаимодействия пакетами.
	 */
	public CommunicateType getDefaultCommunicationType() {
		return defaultCommunicationType;
	}

	/**
	 * Добавляет пакет в очередь на отправку.
	 * Если очередь пакетов заполнена, отправка будет заблокирована.
	 *
	 * @param packet Добавляемый пакет.
	 *
	 * @throws InterruptedException Прерывание отправки.
	 * @see #sendPacket(Packet, CommunicateType)
	 */
	public void sendPacket(Packet packet) throws InterruptedException {
		sendPacket(packet, defaultCommunicationType);
	}

	/**
	 * Добавляет пакет с определенным типом коммуникации в очередь на отправку.
	 * Если очередь пакетов заполнена, отправка будет заблокирована.
	 *
	 * @param packet Добавляемый пакет.
	 * @param type Тип коммуникации.
	 *
	 * @throws InterruptedException Прерывание отправки.
	 */
	public void sendPacket(Packet packet, CommunicateType type) throws InterruptedException {
		if (packet == null)
			throw new IllegalArgumentException("Packet cannot be null!");
		log.info("Putin packet " + packet.getType() + " using " + type);
		outputQueue.put(new SendingPacket(packet, type));
		log.info("Queued packet " + packet.getType() + " using " + type);
	}

	/**
	 * Останавливает обработчиков входящих и исходящих пакетов.
	 */
	public void close() {
		inputHandler.interrupt();
		outputHandler.interrupt();
		try {
			inputHandler.join();
			outputHandler.join();
		} catch (InterruptedException ignored) {
		}
	}

	/**
	 * Тип коммуникации пакетами.
	 */
	public enum CommunicateType {
		JSON,
		BYTES
	}

	/**
	 * Отправляемый пакет совмещенный с его типом.
	 */
	private static class SendingPacket {
		private final Packet packet;
		private final CommunicateType type;

		public SendingPacket(Packet packet, CommunicateType type) {
			this.packet = packet;
			this.type = type;
		}
	}

	/**
	 * Асинхронный обработчик входных пакетов.
	 */
	private class AsyncInputHandler extends AsyncDataHandler {

		private final ObjectInputStream inputStream;

		AsyncInputHandler(InputStream stream) throws IOException {
			super(stream);
			inputStream = new ObjectInputStream(stream);
			start();
		}

		@Override
		public void run() {
			while (!interrupted) {
				try {
					Packet packet;
					switch (defaultCommunicationType) {
						case BYTES:
							packet = Packet.fromBytesStream(inputStream);
							break;
						case JSON:
							packet = Packet.fromJsonStream(gson, inputStream);
							break;
						default:
							throw new IllegalStateException("Unknown communication type!");
					}

					log.info("Received packet " + packet.getType() + " using " + defaultCommunicationType);

					handler.inputPacket(packet, ProtocolCommunicator.this);
				} catch (Packet.InvalidPacketException e) {
					handler.invalidPacketException(e, ProtocolCommunicator.this);
				} catch (IOException e) {
					if (!interrupted)
						handler.ioException(e, ProtocolCommunicator.this);
				} catch (InterruptedException ignored) {
				}
			}
		}
	}

	/**
	 * Асинхронный обработчик выходных пакетов.
	 */
	private class AsyncOutputHandler extends AsyncDataHandler {

		private final ObjectOutputStream outputStream;

		AsyncOutputHandler(OutputStream stream) throws IOException {
			super(stream);
			outputStream = new ObjectOutputStream(stream);
			start();
		}

		@Override
		public void run() {
			while (!interrupted) {
				try {
					SendingPacket packet = outputQueue.take();
					log.info("Sending packet " + packet.packet.getType() + " using " + packet.type);
					switch (packet.type) {
						case BYTES:
							packet.packet.toBytesStream(outputStream);
							break;
						case JSON:
							outputStream.writeObject(packet.packet.toJsonStream(gson));
							break;
						default:
							throw new IllegalStateException("Unknown communication type!");
					}
					log.info("Packet " + packet.packet.getType() + " sent using " + packet.type);
				} catch (IOException e) {
					if (!interrupted)
						handler.ioException(e, ProtocolCommunicator.this);
				} catch (InterruptedException ignored) {
				}
			}
		}
	}
}
