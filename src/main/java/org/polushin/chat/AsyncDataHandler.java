package org.polushin.chat;

import java.io.Closeable;
import java.io.IOException;

/**
 * Асинхронный обработчик потоковых данных.
 */
public abstract class AsyncDataHandler extends Thread {

	protected volatile boolean interrupted;
	private final Closeable closeable;

	protected AsyncDataHandler(Closeable stream) {
		closeable = stream;
		start();
	}

	@Override
	public abstract void run();

	@Override
	public void interrupt() {
		interrupted = true;
		try {
			closeable.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.interrupt();
	}

}
