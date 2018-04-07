package org.polushin.chat.server;

import java.io.IOException;

public class Main {

	public static final int DEFAULT_PORT = 13337;
	public static final int DEFAULT_MAX_CONNECTIONS = 50;

	private static final String PORT_ARG_PREFIX = "--port=";
	private static final String SLOTS_ARG_PREFIX = "--slots=";

	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		int slots = DEFAULT_MAX_CONNECTIONS;
		try {
			for (String arg : args) {
				if (arg.startsWith(PORT_ARG_PREFIX))
					port = Integer.parseInt(arg.substring(PORT_ARG_PREFIX.length()));
				else if (arg.startsWith(SLOTS_ARG_PREFIX))
					slots = Integer.parseInt(arg.substring(SLOTS_ARG_PREFIX.length()));
				else {
					System.err.println("Unrecognised option: " + arg);
					System.exit(-1);
				}
			}
		} catch (NumberFormatException e) {
			System.err.println("Invalid parameters type.");
			System.exit(-1);
		}

		System.out.println(String.format("Staring server on port %d with %d slots...", port, slots));

		Server server;
		try {
			server = new Server(port, slots);
		} catch (IOException e) {
			e.printStackTrace(System.err);
			return;
		}
		System.out.println("Done. Type something to stop the server.");

		try {
			System.in.read();
		} catch (IOException ignored) {
		}

		System.out.println("Finishing tasks...");

		server.interrupt();
		try {
			server.join();
		} catch (InterruptedException ignored) {
		}

		System.out.println("Goodbye!");
	}

}
