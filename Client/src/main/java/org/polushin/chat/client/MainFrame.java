package org.polushin.chat.client;

import org.polushin.chat.ProtocolCommunicator;
import org.polushin.chat.protocol.Packet;
import org.polushin.chat.protocol.PacketLogin;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

public class MainFrame extends JFrame {

	private static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 25);

	private final JTextArea messagesArea;
	private final JTextField messageField;
	private final DefaultListModel<String> onlineUsers;
	private final JFormattedTextField address;
	private final JFormattedTextField username;
	private final JButton connectButton;
	private final JButton disconnectButton;
	private final JButton sendButton;
	private final JComboBox<String> connectionType;

	private final Client client = new Client(new ChatHandler());

	public MainFrame() {
		maximize();
		setTitle("Чатик");

		JPanel mainPanel = new JPanel();
		add(mainPanel);

		final GridBagLayout layout = new GridBagLayout();
		GridBagConstraints cn = new GridBagConstraints();
		mainPanel.setLayout(layout);

		mainPanel.setBackground(new Color(15, 127, 18));

		// Поле сообщений
		messagesArea = new JTextArea();
		messagesArea.setEditable(false);
		messagesArea.setFont(DEFAULT_FONT);
		final JScrollPane messagesScrollPane = new JScrollPane(messagesArea);
		cn.gridx = cn.gridy = 0;
		cn.gridwidth = 9;
		cn.gridheight = 14;
		cn.weightx = 1;
		cn.weighty = 1;
		cn.fill = GridBagConstraints.BOTH;
		cn.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(messagesScrollPane, cn);
		mainPanel.add(messagesScrollPane);

		// Поле ввода сообщения
		messageField = new JTextField();
		messageField.setEnabled(false);
		messageField.setFont(DEFAULT_FONT);
		cn = new GridBagConstraints();
		cn.gridx = 0;
		cn.gridy = 14;
		cn.gridwidth = 8;
		cn.gridheight = 1;
		cn.weighty = 0;
		cn.weightx = 1;
		cn.fill = GridBagConstraints.HORIZONTAL;
		cn.insets = new Insets(0, 5, 5, 5);
		layout.setConstraints(messageField, cn);
		mainPanel.add(messageField);

		// Кнопка отправки сообщения
		sendButton = new JButton("Отправить");
		sendButton.setEnabled(false);
		sendButton.setFont(DEFAULT_FONT);
		sendButton.addActionListener(e -> send());
		cn = new GridBagConstraints();
		cn.gridx = 8;
		cn.gridy = 14;
		cn.gridwidth = 1;
		cn.gridheight = 1;
		cn.weightx = 0;
		cn.weighty = 0;
		cn.fill = GridBagConstraints.NONE;
		cn.insets = new Insets(0, 0, 5, 5);
		layout.setConstraints(sendButton, cn);
		mainPanel.add(sendButton);
		JRootPane rootPane = SwingUtilities.getRootPane(sendButton);
		rootPane.setDefaultButton(sendButton);

		// Список пользователей онлайн
		final JList<String> onlineUsersList = new JList<>(onlineUsers = new DefaultListModel<>());
		onlineUsersList.setFont(DEFAULT_FONT);
		final JScrollPane onlineScrollPane = new JScrollPane(onlineUsersList);
		cn = new GridBagConstraints();
		cn.gridx = 9;
		cn.gridy = 0;
		cn.gridwidth = 1;
		cn.gridheight = 9;
		cn.weightx = 0;
		cn.weighty = 1;
		cn.fill = GridBagConstraints.BOTH;
		cn.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(onlineScrollPane, cn);
		mainPanel.add(onlineScrollPane);

		// Панель подключения
		final JPanel connectPanel = new JPanel();
		connectPanel.setBackground(mainPanel.getBackground());
		connectPanel.setLayout(new GridLayout(7, 1));
		final JLabel addressLabel = new JLabel("Адрес:порт");
		addressLabel.setFont(DEFAULT_FONT);
		connectPanel.add(addressLabel);
		connectPanel
				.add(address = new JFormattedTextField(new RegexFormatter(Pattern.compile("[\\p{L}0-9.]+:[0-9]+"))));
		address.setValue("127.0.0.1:13337");
		address.setFont(DEFAULT_FONT);
		final JLabel usernameLabel = new JLabel("Ник");
		usernameLabel.setFont(DEFAULT_FONT);
		connectPanel.add(usernameLabel);
		connectPanel.add(username = new JFormattedTextField(new RegexFormatter(PacketLogin.VALID_USERNAME)));
		username.setValue("anon");
		username.setFont(DEFAULT_FONT);
		connectPanel.add(connectButton = new JButton("Подключиться"));
		connectButton.addActionListener(e -> connect());
		connectButton.setFont(DEFAULT_FONT);
		connectPanel.add(disconnectButton = new JButton("Отключиться"));
		disconnectButton.addActionListener(e -> disconnect());
		disconnectButton.setEnabled(false);
		disconnectButton.setFont(DEFAULT_FONT);
		connectPanel.add(connectionType = new JComboBox<>(new String[] {"JSON", "Bytes"}));
		connectionType.setSelectedIndex(0);
		cn.gridx = 9;
		cn.gridy = 9;
		cn.gridwidth = 1;
		cn.gridheight = 5;
		cn.weighty = 0;
		cn.weightx = 0;
		cn.fill = GridBagConstraints.HORIZONTAL;
		cn.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(connectPanel, cn);
		mainPanel.add(connectPanel);

		setMinimumSize(new Dimension(600, 400));

		setVisible(true);
	}

	private void send() {
		if (messageField.getText().isEmpty())
			return;
		client.sendMessage(messageField.getText());
		messageField.setText("");
	}

	private void connect() {
		connectButton.setEnabled(false);
		address.setEnabled(false);
		username.setEnabled(false);
		connectionType.setEnabled(false);
		String address = (String) this.address.getValue();
		int split = address.indexOf(':');
		int port;
		try {
			if (split == -1)
				throw new NumberFormatException();
			port = Integer.parseInt(address.substring(split + 1));
		} catch (NumberFormatException e) {
			connectButton.setEnabled(true);
			this.address.setEnabled(true);
			username.setEnabled(true);
			connectionType.setEnabled(true);
			return;
		}

		ProtocolCommunicator.CommunicateType type;
		switch (connectionType.getSelectedIndex()) {
			case 0:
				type = ProtocolCommunicator.CommunicateType.JSON;
				break;
			case 1:
				type = ProtocolCommunicator.CommunicateType.BYTES;
				break;
			default:
				type = ProtocolCommunicator.CommunicateType.JSON;
		}

		messagesArea.setText("Подключение к " + address + "...");
		client.connect(address.substring(0, split), port, (String) username.getValue(), type);
	}

	private void disconnect() {
		disconnectButton.setEnabled(false);
		messageField.setEnabled(false);
		sendButton.setEnabled(false);
		client.disconnect();
	}

	private void maximize() {
		final GraphicsConfiguration config = getGraphicsConfiguration();

		Toolkit def = Toolkit.getDefaultToolkit();

		final int left = def.getScreenInsets(config).left;
		final int right = def.getScreenInsets(config).right;
		final int top = def.getScreenInsets(config).top;
		final int bottom = def.getScreenInsets(config).bottom;

		final Dimension screenSize = def.getScreenSize();
		final int width = screenSize.width - left - right;
		final int height = screenSize.height - top - bottom;

		setSize(width, height);
	}

	private class ChatHandler implements Client.InputEventsHandler {

		private final java.util.List<String> currentOnline = new ArrayList<>();

		@Override
		public void connectionEstablished(Collection<String> onlineUsers) {
			messagesArea.setText(String.format("%s\nПодключено к %s", messagesArea.getText(), address.getValue()));
			messageField.setEnabled(true);
			disconnectButton.setEnabled(true);
			sendButton.setEnabled(true);
			currentOnline.clear();
			currentOnline.addAll(onlineUsers);
			MainFrame.this.onlineUsers.clear();
			for (String user : currentOnline)
				MainFrame.this.onlineUsers.addElement(user);
		}

		@Override
		public void newMessage(String username, String message) {
			messagesArea.setText(String.format("%s\n[%s]: %s", messagesArea.getText(), username, message));
		}

		@Override
		public void onlineListUpdate(boolean isNew, String username) {
			if (isNew) {
				currentOnline.add(username);
				onlineUsers.addElement(username);
				messagesArea.setText(String.format("%s\n%s подключился.", messagesArea.getText(), username));
			} else {
				int index = currentOnline.indexOf(username);
				if (index == -1)
					return;
				currentOnline.remove(index);
				onlineUsers.remove(index);
				messagesArea.setText(String.format("%s\n%s отключился.", messagesArea.getText(), username));
			}
		}

		@Override
		public void fatalException(Exception e) {
			e.printStackTrace();
			disconnect();
			JOptionPane.showMessageDialog(MainFrame.this, e.getMessage(), "Произошла ошибка: " + e.getClass(),
			                              JOptionPane.ERROR_MESSAGE);
		}

		@Override
		public void invalidPacketException(Packet.InvalidPacketException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.this, e.getMessage(), "Получен ошибочный пакет",
			                              JOptionPane.ERROR_MESSAGE);
		}

		@Override
		public void disconnected() {
			currentOnline.clear();
			onlineUsers.clear();
			messagesArea.setText(String.format("%s\nОтключено.", messagesArea.getText()));
			address.setEnabled(true);
			username.setEnabled(true);
			connectButton.setEnabled(true);
			connectionType.setEnabled(true);
		}
	}

}
