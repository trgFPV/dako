package edu.hm.dako.chat.server;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import edu.hm.dako.chat.common.AuditLogImplementationType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import edu.hm.dako.chat.common.ExceptionHandler;
import edu.hm.dako.chat.common.ImplementationType;
import edu.hm.dako.chat.common.SystemConstants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Benutzeroberflaeche zum Starten des Chat-Servers
 * 
 * @author Paul Mandl
 *
 */
public class ChatServerGUI extends Application implements ChatServerGuiInterface {

	private static Log log = LogFactory.getLog(ChatServerGUI.class);

	// Standardvalues for AuditLogServer
	static final String DEFAULT_AUDITLOGSERVER_NAME = "localhost";
	static final String DEFAULT_AUDITLOGSERVER_PORT = "40001";

	// Standard-Port des Servers
	static final String DEFAULT_SERVER_PORT = "50001";

	// Standard-und Maximal-Puffergroessen in Byte
	static final String DEFAULT_SENDBUFFER_SIZE = "300000";
	static final String DEFAULT_RECEIVEBUFFER_SIZE = "300000";
	static final String MAX_SENDBUFFER_SIZE = "500000";
	static final String MAX_RECEIVEBUFFER_SIZE = "500000";

	final VBox pane = new VBox(5);

	// Interface der Chat-Server-Implementierung
	private static ChatServerInterface chatServer;

	// Server-Startzeit als String
	private String startTimeAsString;

	// Kalender zur Umrechnung der Startzeit
	private Calendar cal;

	// Interface der Server-GUI zum Bekanntgeben von GUI-Veraenderungen
	ChatServerGuiInterface server;

	// Flag, das angibt, ob der Server gestartet werden kann (alle
	// Plausibilitaetspruefungen erfuellt)
	private boolean startable = true;

	// Combobox fuer Eingabe des Implementierungstyps
	private ComboBox<String> comboBoxImplType;

	// Combobox fuer AuditLogServer Implementierung
	private ComboBox<String> comboBoxAuditLogServerType;

	// Testfelder, Buttons und Labels der ServerGUI

	private TextField startTimeField;
	private TextField receivedRequests;
	private TextField loggedInClients;
	private TextField serverPort;
	private TextField sendBufferSize;
	private TextField receiveBufferSize;
	private TextField auditLogServerHostnameOrIp;
	private TextField auditLogServerPort;
	private Label serverPortLabel;
	private Label sendBufferSizeLabel;
	private Label receiveBufferSizeLabel;
	private Label auditLogServerHostnameOrIpLabel;
	private Label auditLogServerPortLabel;
	private Label auditLogActivate;
	private Label auditLogConnectionType;

	private CheckBox enableAuditLogServerCheckbox;

	private Button startButton;
	private Button stopButton;
	private Button finishButton;

	// Zaehler fuer die eingeloggten Clients und die empfangenen Request
	private static AtomicInteger loggedInClientCounter;
	private static AtomicInteger requestCounter;

	// Daten, die beim Start der GUI uebergeben werden
	private ServerStartData data = new ServerStartData();

	// Moegliche Belegungen des Implementierungsfeldes in der GUI
	ObservableList<String> implTypeOptions = FXCollections.observableArrayList(
			SystemConstants.IMPL_TCP_SIMPLE, SystemConstants.IMPL_TCP_ADVANCED);

	ObservableList<String> auditLogServerImplTypeOptions = FXCollections.observableArrayList(
			SystemConstants.AUDIT_LOG_SERVER_TCP_IMPL, SystemConstants.AUDIT_LOG_SERVER_UDP_IMPL);
	/**
	 * Konstruktion der ServerGUI
	 */
	public ChatServerGUI() {
		loggedInClientCounter = new AtomicInteger(0);
		requestCounter = new AtomicInteger(0);
		startTimeField = createNotEditableTextfield("");
		receivedRequests = createNotEditableTextfield("");
		loggedInClients = createNotEditableTextfield("");
	}

	public static void main(String[] args) {
		PropertyConfigurator.configureAndWatch("log4j.chatServer.properties", 60 * 1000);
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {

		stage.setTitle("ChatServerGUI");
		stage.setScene(new Scene(pane, 415, 465));
		stage.show();
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					ChatServerGUI.chatServer.stop();
				} catch (Exception ex) {
					ChatServerGUI.log.error("Fehler beim Stoppen des Chat-Servers");
					ExceptionHandler.logException(ex);
				}
			}
		});

		pane.setStyle("-fx-background-color: cornsilk");
		pane.setPadding(new Insets(10, 10, 10, 10));

		pane.getChildren().add(createSeperator("Eingabe", 315));
		pane.getChildren().add(createInputPane());

		pane.getChildren().add(createSeperator("Informationen", 285));
		pane.getChildren().add(createInfoPane());

		pane.getChildren().add(createSeperator("", 360));
		pane.getChildren().add(createButtonPane());

		reactOnStartButton();
		reactOnStopButton();
		reactOnFinishButton();
		stopButton.setDisable(true);
	}

	/**
	 * Eingabe-Pane erzeugen
	 * 
	 * @return pane
	 */
	private GridPane createInputPane() {
		final GridPane inputPane = new GridPane();

		final Label label = new Label("Serverauswahl");
		label.setMinSize(100, 25);
		label.setMaxSize(100, 25);

		serverPortLabel = createLabel("Serverport");
		sendBufferSizeLabel = createLabel("Sendepuffer in Byte");
		receiveBufferSizeLabel = createLabel("Empfangspuffer in Byte");
		auditLogServerHostnameOrIpLabel = createLabel("AuditLogServer Hostname/IP-Adr.");
		auditLogServerPortLabel = createLabel("AuditLogServer Port");
		auditLogActivate = createLabel("AuditLog aktivieren");
		auditLogConnectionType = createLabel("AuditLog-Server Verbindungstyp");

		inputPane.setPadding(new Insets(5, 5, 5, 5));
		inputPane.setVgap(1);

		comboBoxImplType = createImplTypeComboBox(implTypeOptions);
		serverPort = createEditableTextfield(DEFAULT_SERVER_PORT);
		sendBufferSize = createEditableTextfield(DEFAULT_SENDBUFFER_SIZE);
		receiveBufferSize = createEditableTextfield(DEFAULT_RECEIVEBUFFER_SIZE);
		auditLogServerHostnameOrIp = createEditableTextfield(DEFAULT_AUDITLOGSERVER_NAME);
		auditLogServerPort = createEditableTextfield(DEFAULT_AUDITLOGSERVER_PORT);
		comboBoxAuditLogServerType = createAuditLogTypeComboBox(auditLogServerImplTypeOptions);
		enableAuditLogServerCheckbox = new CheckBox();
		enableAuditLogServerCheckbox.setSelected(true);
		enableAuditLogServerCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (enableAuditLogServerCheckbox.isSelected()) {
					auditLogServerHostnameOrIp.setEditable(true);
					auditLogServerHostnameOrIp.setStyle("-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
					auditLogServerPort.setEditable(true);
					auditLogServerPort.setStyle("-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");					
					comboBoxAuditLogServerType.setEditable(true);
					comboBoxAuditLogServerType.setStyle("-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
				}
				else {
					auditLogServerHostnameOrIp.setEditable(false);
					auditLogServerHostnameOrIp.setStyle("-fx-background-color: gray;");
					auditLogServerPort.setEditable(false);
					auditLogServerPort.setStyle("-fx-background-color: gray;");
					comboBoxAuditLogServerType.setEditable(false);
					comboBoxAuditLogServerType.setStyle("-fx-background-color: gray;");
				}
			}
		});

		inputPane.add(label, 1, 3);
		inputPane.add(comboBoxImplType, 3, 3);
		inputPane.add(serverPortLabel, 1, 5);
		inputPane.add(serverPort, 3, 5);
		inputPane.add(auditLogActivate, 1, 7);
		inputPane.add(enableAuditLogServerCheckbox, 3, 7);
		inputPane.add(auditLogServerHostnameOrIpLabel, 1,9);
		inputPane.add(auditLogServerHostnameOrIp, 3, 9);
		inputPane.add(auditLogServerPortLabel, 1,11);
		inputPane.add(auditLogServerPort, 3, 11);
		inputPane.add(auditLogConnectionType, 1, 13);
		inputPane.add(comboBoxAuditLogServerType, 3, 13);
		inputPane.add(sendBufferSizeLabel, 1, 15);
		inputPane.add(sendBufferSize, 3, 15);
		inputPane.add(receiveBufferSizeLabel, 1, 17);
		inputPane.add(receiveBufferSize, 3, 17);

		return inputPane;
	}

	/**
	 * Info-Pain erzeugen
	 * 
	 * @return pane
	 */
	private GridPane createInfoPane() {
		final GridPane infoPane = new GridPane();
		infoPane.setPadding(new Insets(5, 5, 5, 5));
		infoPane.setVgap(1);

		infoPane.add(createLabel("Startzeit"), 1, 3);
		infoPane.add(startTimeField, 3, 3);

		infoPane.add(createLabel("Empfangene Requests"), 1, 5);
		infoPane.add(receivedRequests, 3, 5);

		infoPane.add(createLabel("Angemeldete Clients"), 1, 7);
		infoPane.add(loggedInClients, 3, 7);
		return infoPane;
	}

	/**
	 * Pane fuer Buttons erzeugen
	 * 
	 * @return HBox
	 */
	private HBox createButtonPane() {
		final HBox buttonPane = new HBox(5);

		startButton = new Button("Server starten");
		stopButton = new Button("Server stoppen");
		finishButton = new Button("Beenden");

		buttonPane.getChildren().addAll(startButton, stopButton, finishButton);
		buttonPane.setAlignment(Pos.CENTER);
		return buttonPane;
	}

	/**
	 * Label erzeugen
	 * 
	 * @param value
	 * @return Label
	 */
	private Label createLabel(String value) {
		final Label label = new Label(value);
		label.setMinSize(200, 25);
		label.setMaxSize(200, 25);
		return label;
	}

	/**
	 * Aufbau der Combobox fuer die Serverauswahl in der GUI
	 * 
	 * @param options Optionen fuer Implementierungstyp
	 * @return Combobox
	 */
	private ComboBox<String> createImplTypeComboBox(ObservableList<String> options) {
		ComboBox<String> comboBox = new ComboBox<>(options);
		comboBox.setMinSize(155, 28);
		comboBox.setMaxSize(155, 28);
		comboBox.setValue(options.get(0));
		comboBox.setStyle(
				"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
		return comboBox;
	}

	/**
	 * Aufbau der Combobox fuer die AuditLog-Server Verbindung
	 * 
	 * @param options Optionen fuer Verbindungstyp
	 * @return Combobox
	 */
	private ComboBox<String> createAuditLogTypeComboBox(ObservableList<String> options) {
		ComboBox<String> comboBox = new ComboBox<>(options);
		comboBox.setMinSize(155, 28);
		comboBox.setMaxSize(155, 28);
		comboBox.setValue(options.get(0));
		comboBox.setStyle(
				"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
		return comboBox;
	}
	/**
	 * Trennlinie erstellen
	 * 
	 * @param value
	 *          Text der Trennlinie
	 * @param size
	 *          Groesse der Trennlinie
	 * @return Trennlinie
	 */
	private HBox createSeperator(String value, int size) {
		// Separator erstellen
		final HBox labeledSeparator = new HBox();
		final Separator rightSeparator = new Separator(Orientation.HORIZONTAL);
		final Label textOnSeparator = new Label(value);

		textOnSeparator.setFont(Font.font(12));

		rightSeparator.setMinWidth(size);
		rightSeparator.setMaxWidth(size);

		labeledSeparator.getChildren().add(textOnSeparator);
		labeledSeparator.getChildren().add(rightSeparator);
		labeledSeparator.setAlignment(Pos.BASELINE_LEFT);

		return labeledSeparator;
	}

	/**
	 * Nicht editierbares Feld erzeugen
	 * 
	 * @param value
	 *          Feldinhalt
	 * @return Textfeld
	 */
	private TextField createNotEditableTextfield(String value) {
		TextField textField = new TextField(value);
		textField.setMaxSize(155, 28);
		textField.setMinSize(155, 28);
		textField.setEditable(false);
		textField.setStyle(
				"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
		return textField;
	}

	/**
	 * Erstellung editierbarer Textfelder
	 * 
	 * @param value
	 *          Feldinhalt
	 * @return textField
	 */
	private TextField createEditableTextfield(String value) {
		TextField textField = new TextField(value);
		textField.setMaxSize(155, 28);
		textField.setMinSize(155, 28);
		textField.setEditable(true);
		textField.setStyle(
				"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
		return textField;
	}

	/**
	 * Reaktion auf das Betaetigen des Start-Buttons
	 */
	private void reactOnStartButton() {
		startButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				startable = true;
				// Eingabeparameter einlesen
				int serverPort = readServerPort();
				int sendBufferSize = readSendBufferSize();
				int receiveBufferSize = readReceiveBufferSize();

				receivedRequests.setText("0");
				loggedInClients.setText("0");

				if (startable == true) {

					// Implementierungstyp, der zu starten ist, ermitteln und
					// Chat-Server starten
					String implType = readImplTypeComboBox();

					if (enableAuditLogServerCheckbox.isSelected()) {
						String auditLogServerHostname = readAuditLogServerHostnameOrIp();
						int auditLogServerPort = readAuditLogServerPort();
						String auditLogServerImplType = readAuditLogComboBox();

						try {
							startChatServerWithAuditLogServer(implType, serverPort, sendBufferSize, receiveBufferSize,
									auditLogServerHostname, auditLogServerPort, auditLogServerImplType);
						} catch (Exception e) {
							setAlert(
									"Der Server konnte nicht gestartet werden oder die Verbindung zum AuditLogServer konnte " +
											"nicht hergestellt werden, evtl. laeuft ein anderer Server mit dem Port");
							return;
						}
					}
					else {
						try {
							startChatServer(implType, serverPort, sendBufferSize, receiveBufferSize);
						} catch (Exception e) {
							setAlert(
									"Der Server konnte nicht gestartet werden, evtl. laeuft ein anderer Server mit dem Port");
							return;
						}
					}
					startButton.setDisable(true);
					stopButton.setDisable(false);
					finishButton.setDisable(true);

					// Startzeit ermitteln
					cal = Calendar.getInstance();
					startTimeAsString = getCurrentTime(cal);
					showStartData(data);
				} else {
					setAlert("Bitte korrigieren Sie die rot markierten Felder");
				}
			}
		});
	}

	/**
	 * Reaktion auf das Betaetigen des Stop-Buttons
	 */
	private void reactOnStopButton() {

		stopButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				try {
					chatServer.stop();
				} catch (Exception e) {
					log.error("Fehler beim Stoppen des Chat-Servers");
					ExceptionHandler.logException(e);
				}

				// Zaehler fuer CLients und Requests auf 0 stellen.
				requestCounter.set(0);
				loggedInClientCounter.set(0);

				startButton.setDisable(false);
				stopButton.setDisable(true);
				finishButton.setDisable(false);
				enableAuditLogServerCheckbox.setDisable(false);
				startTimeField.setText("");
				receivedRequests.setText("");
				loggedInClients.setText("");
			}
		});
	}

	/**
	 * Reaktion auf das Betaetigen des Finish-Buttons
	 */
	private void reactOnFinishButton() {
		finishButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					ChatServerGUI.chatServer.stop();
				} catch (Exception var3) {
					ChatServerGUI.log.error("Fehler beim Stoppen des Chat-Servers");
					ExceptionHandler.logException(var3);
				}
				System.out.println("ChatServer-GUI ordnungsgemaess beendet");
				log.debug("Schliessen-Button betaetigt");
				System.exit(0);
			}
		});
	}

	/**
	 * AuditLogServer-Typ aus GUI auslesen
	 */
	private String readAuditLogComboBox() {
		String implType;
		if (comboBoxAuditLogServerType.getValue() == null) {
			 implType = new String(SystemConstants.AUDIT_LOG_SERVER_TCP_IMPL);
		} else {
		
			 implType = new String(comboBoxAuditLogServerType.getValue().toString());
		}
		return (implType);
	}

	/**
	 * Implementierungstyp aus GUI auslesen
	 */
	private String readImplTypeComboBox() {
		String implType = new String(comboBoxImplType.getValue().toString());

		return (implType);
	}

	private void startChatServerWithAuditLogServer(String implType, int serverPort, int sendBufferSize,
								 int receiveBufferSize, String auditLogServerHostname, int auditLogServerPort,
								 String auditLogServerImplType) throws Exception {

		ImplementationType serverImpl = ImplementationType.TCPAdvancedImplementation;
		if (implType.equals(SystemConstants.IMPL_TCP_ADVANCED)) {
			serverImpl = ImplementationType.TCPAdvancedImplementation;
		} else if (implType.equals(SystemConstants.IMPL_TCP_SIMPLE)) {
			serverImpl = ImplementationType.TCPSimpleImplementation;
		}

		AuditLogImplementationType auditLogImplementationType = AuditLogImplementationType.AuditLogServerTCPImplementation;
		if (auditLogServerImplType.equals(SystemConstants.AUDIT_LOG_SERVER_TCP_IMPL)) {
			auditLogImplementationType = AuditLogImplementationType.AuditLogServerTCPImplementation;
		} else if (auditLogServerImplType.equals(SystemConstants.AUDIT_LOG_SERVER_UDP_IMPL)) {
			auditLogImplementationType = AuditLogImplementationType.AuditLogServerUDPImplementation;
		}

		try {
			log.debug("ChatServer soll mit AuditLog gestartet werden");
			chatServer = ServerFactory.getServerWithAuditLog(serverImpl, serverPort, sendBufferSize,
					receiveBufferSize, this,
					auditLogImplementationType, auditLogServerHostname, auditLogServerPort);
		} catch (Exception e) {
			log.error("Fehler beim Starten des Chat-Servers: " + e.getMessage());
			ExceptionHandler.logException(e);
			throw new Exception(e);
		}
		
		
		if (startable == false) {
			setAlert("Bitte Korrigieren sie die rot markierten Felder");
		} else {
	
			if (!ServerFactory.isAuditLogServerConnected()) {
				// AuditLog-Server Verbindung nicht vorhanden, in der GUI zeigen
				enableAuditLogServerCheckbox.setSelected(false);
				auditLogServerHostnameOrIp.setEditable(false);
				auditLogServerHostnameOrIp.setStyle("-fx-background-color: gray;");
				comboBoxAuditLogServerType.setEditable(false);
				comboBoxAuditLogServerType.setStyle("-fx-background-color: gray;");
			}
			
			// Server starten
			chatServer.start();
		}
	}

	/**
	 * Chat-Server starten
	 * 
	 * @param implType
	 *          Implementierungstyp, der zu starten ist
	 * @param serverPort
	 *          Serverport, die der Server als Listener-Port nutzen soll
	 * @param sendBufferSize
	 *          Sendpuffergroesse, die der Server nutzen soll
	 * @param receiveBufferSize
	 *          Empfangspuffergroesse, die der Server nutzen soll
	 */
	private void startChatServer(String implType, int serverPort, int sendBufferSize,
			int receiveBufferSize) throws Exception {

		ImplementationType serverImpl = ImplementationType.TCPAdvancedImplementation;
		if (implType.equals(SystemConstants.IMPL_TCP_ADVANCED)) {
			serverImpl = ImplementationType.TCPAdvancedImplementation;
		} else if (implType.equals(SystemConstants.IMPL_TCP_SIMPLE)) {
			serverImpl = ImplementationType.TCPSimpleImplementation;
		}

		try {
			chatServer = ServerFactory.getServer(serverImpl, serverPort, sendBufferSize,
					receiveBufferSize, this);
		} catch (Exception e) {
			log.error("Fehler beim Starten des Chat-Servers: " + e.getMessage());
			ExceptionHandler.logException(e);
			throw new Exception(e);
		}
		if (startable == false) {
			setAlert("Bitte Korrigieren sie die rot markierten Felder");
		} else {
			// Server starten
			chatServer.start();
		}
	}

	private String readAuditLogServerHostnameOrIp() {
		return auditLogServerHostnameOrIp.getText();
	}

	private int readAuditLogServerPort() {
		String item = new String(auditLogServerPort.getText());
		Integer iServerPort = 0;
		if (item.matches("[0-9]+")) {
			iServerPort = new Integer(auditLogServerPort.getText());
			if ((iServerPort < 1) || (iServerPort > 65535)) {
				startable = false;
				auditLogServerPortLabel.setTextFill(Color.web(SystemConstants.RED_COLOR));
			} else {
				System.out.println("Port fuer AuditLog-Server: " + iServerPort);
				auditLogServerPortLabel.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
			}
		} else {
			startable = false;
			auditLogServerPortLabel.setTextFill(Color.web(SystemConstants.RED_COLOR));

		}
		return (iServerPort);
	}

	/**
	 * Serverport aus GUI auslesen und pruefen
	 * 
	 * @return Verwendeter Serverport
	 */
	private int readServerPort() {
		String item = new String(serverPort.getText());
		Integer iServerPort = 0;
		if (item.matches("[0-9]+")) {
			iServerPort = new Integer(serverPort.getText());
			if ((iServerPort < 1) || (iServerPort > 65535)) {
				startable = false;
				serverPortLabel.setTextFill(Color.web(SystemConstants.RED_COLOR));
			} else {
				System.out.println("Serverport: " + iServerPort);
				serverPortLabel.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
			}
		} else {
			startable = false;
			serverPortLabel.setTextFill(Color.web(SystemConstants.RED_COLOR));

		}
		return (iServerPort);
	}

	/**
	 * Groesse des Sendepuffers in Byte auslesen und pruefen
	 * 
	 * @return Eingegebene Sendpuffer-Groesse
	 */
	private int readSendBufferSize() {
		String item = new String(sendBufferSize.getText());
		Integer iSendBufferSize = 0;
		if (item.matches("[0-9]+")) {
			iSendBufferSize = new Integer(sendBufferSize.getText());
			if ((iSendBufferSize <= 0)
					|| (iSendBufferSize > new Integer(MAX_SENDBUFFER_SIZE))) {
				startable = false;
				sendBufferSizeLabel.setTextFill(Color.web(SystemConstants.RED_COLOR));
			} else {
				sendBufferSizeLabel.setTextFill(Color.web(SystemConstants.BLACK_COLOR));

			}
		} else {
			startable = false;
			sendBufferSizeLabel.setTextFill(Color.web(SystemConstants.RED_COLOR));
		}
		return (iSendBufferSize);
	}

	/**
	 * Groesse des Empfangspuffers in Byte auslesen und pruefen
	 * 
	 * @return Eingegebene Empfangspuffer-Groesse
	 */
	private int readReceiveBufferSize() {
		String item = new String(receiveBufferSize.getText());
		Integer iReceiveBufferSize = 0;
		if (item.matches("[0-9]+")) {
			iReceiveBufferSize = new Integer(receiveBufferSize.getText());
			if ((iReceiveBufferSize <= 0)
					|| (iReceiveBufferSize > new Integer(MAX_RECEIVEBUFFER_SIZE))) {
				startable = false;
				receiveBufferSizeLabel.setTextFill(Color.web(SystemConstants.RED_COLOR));
			} else {
				receiveBufferSizeLabel.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
			}
		} else {
			startable = false;
			receiveBufferSizeLabel.setTextFill(Color.web(SystemConstants.RED_COLOR));
		}
		return (iReceiveBufferSize);
	}

	private String getCurrentTime(Calendar cal) {
		return new SimpleDateFormat("dd.MM.yy HH:mm:ss:SSS").format(cal.getTime());
	}

	/**
	 * GUI-Feld fuer eingeloggte CLients ueber Event-Liste des FavaFX-GUI-Threads
	 * aktualisieren
	 */
	private void updateLoggedInClients() {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				log.debug("runLater: run-Methode wird ausgefuehrt");
				log.debug("runLater: Logged in Clients: "
						+ String.valueOf(loggedInClientCounter.get()));

				loggedInClients.setText(String.valueOf(loggedInClientCounter.get()));
			}
		});
	}

	/**
	 * GUI-Feld fuer Anzahl empfangener Requests ueber Event-Liste des
	 * JavaFX-GUI-Threads aktualisieren
	 */
	private void updateNumberOfRequests() {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				log.debug("runLater: run-Methode wird ausgefuehrt");
				log.debug("runLater: Received Requests: " + String.valueOf(requestCounter.get()));

				receivedRequests.setText(String.valueOf(requestCounter.get()));
			}
		});
	}

	/**
	 * Oeffnen eines Dialogfensters, wenn ein Fehler bei der Eingabe auftritt
	 *
	 * @param message
	 */
	private void setAlert(String message) {
		;
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Fehler!");
		alert.setHeaderText(
				"Bei den von ihnen eingegebenen Parametern ist ein Fehler aufgetreten:");
		alert.setContentText(message);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				alert.showAndWait();
				//alert.show();
			}
		});
	}

	@Override
	public void showStartData(ServerStartData data) {
		startTimeField.setText(startTimeAsString);
	}

	@Override
	public void incrNumberOfLoggedInClients() {

		loggedInClientCounter.getAndIncrement();
		log.debug("Eingeloggte Clients: " + loggedInClientCounter.get());
		updateLoggedInClients();
	}

	@Override
	public void decrNumberOfLoggedInClients() {
		loggedInClientCounter.getAndDecrement();
		log.debug("Eingeloggte Clients: " + loggedInClientCounter.get());
		updateLoggedInClients();
	}

	@Override
	public void incrNumberOfRequests() {
		requestCounter.getAndIncrement();
		log.debug(requestCounter.get() + " emfangene Message Requests");
		updateNumberOfRequests();
	}
}
