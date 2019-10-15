package edu.hm.dako.chat.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import edu.hm.dako.chat.common.ExceptionHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Chat-GUI
 * 
 * @author Paul Mandl
 *
 */
public class ClientFxGUI extends Application implements ClientUserInterface {

	private static Log log = LogFactory.getLog(ClientFxGUI.class);

	private Stage stage;
	private static LoggedInGuiController lc;
	private ClientImpl communicator;
	private ClientModel model = new ClientModel();

	public static void main(String[] args) {
		PropertyConfigurator.configureAndWatch("log4j.chatClient.properties", 60 * 1000);
		launch(args);
	}

	/**
	 * Kommunikationsschnittstelle zur Kommunikation mit dem Chat-Server
	 * aktivieren
	 * 
	 * @param String
	 *          serverType Servertyp
	 * @param port
	 *          Serverport
	 * @param host
	 *          Hostname oder IP-Adresse des Servers
	 * @return Referenz auf Kommunikationsobjekt
	 */
	public ClientImpl createCommunicator(String serverType, int port, String host) {

		communicator = new ClientImpl(this, port, host, serverType);
		return communicator;
	}

	public ClientImpl getCommunicator() {
		return communicator;
	}

	public ClientModel getModel() {
		return model;
	}

	/**
	 * Diese Methode wird von Java FX bei Aufruf der launch-Methode implizit
	 * aufgerufen
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("LogInGui.fxml"));
		Parent root = loader.load();
		LogInGuiController lc = (LogInGuiController) loader.getController();
		lc.setAppController(this);
		primaryStage.setTitle("Anmelden");
		primaryStage.setScene(new Scene(root, 280, 320));
		root.setStyle("-fx-background-color: cornsilk");
		stage = primaryStage;
		primaryStage.show();
	}

	/**
	 * Benutzeroberflaeche fuer Chat erzeugen
	 */
	public void createNextGui() {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("LoggedInGui.fxml"));
				Parent root = loader.load();
				lc = loader.getController();
				lc.setAppController(this);

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						stage.setTitle("Angemeldet");
						stage.setScene(new Scene(root, 600, 400));
						root.setStyle("-fx-background-color: cornsilk");
					}
				});
			} catch (Exception e) {
				ExceptionHandler.logException(e);
			}
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					try {
						getCommunicator().logout(getModel().getUserName());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
	}
	public void switchToLogInGui(){
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LogInGui.fxml"));
			Parent root = loader.load();
			LogInGuiController lc = (LogInGuiController) loader.getController();
			lc.setAppController(this);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					stage.setTitle("Anmelden");
					stage.setScene(new Scene(root, 280, 320));
					root.setStyle("-fx-background-color: cornsilk");
				}
			});
		} catch (Exception e){
			ExceptionHandler.logException(e);
		}

	}

	@Override
	public void setUserList(Vector<String> userList) {
		final List<String> users = new ArrayList<>();
		for (String anUserList : userList) {
			if (anUserList.equals(getModel().getUserName())) {
				users.add("*" + anUserList + "*");
			} else {
				users.add(anUserList);
			}
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					getModel().users.setAll(users);
					log.debug(users);
				}
			});
		}
	}

	@Override
	public void setMessageLine(String sender, String message) {
		String messageText;
		if (sender.equals(getModel().getUserName())) {
			messageText = "*" + sender + "*: " + message;
		} else {
			messageText = sender + ": " + message;
		}
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				getModel().chats.add(messageText);
			}
		});
	}

	@Override
	public void setLock(boolean lock) {
		getModel().block.set(lock);
	}

	@Override
	public boolean getLock() {
		return false;
	}

	@Override
	public boolean isTestAborted() {
		return false;
	}

	@Override
	public void abortTest() {
	}

	@Override
	public void releaseTest() {
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public void setLastServerTime(long lastServerTime) {
	}

	@Override
	public long getLastServerTime() {
		return 0;
	}

	@Override
	public void setSessionStatisticsCounter(long numberOfSentEvents,
			long numberOfReceivedConfirms, long numberOfLostConfirms, long numberOfRetries,
			long numberOfReceivedChatMessages) {

	}

	@Override
	public long getNumberOfSentEvents() {
		return 0;
	}

	@Override
	public long getNumberOfReceivedConfirms() {
		return 0;
	}

	@Override
	public long getNumberOfLostConfirms() {
		return 0;
	}

	@Override
	public long getNumberOfRetries() {
		return 0;
	}

	@Override
	public long getNumberOfReceivedChatMessages() {
		return 0;
	}

	@Override
	public void setErrorMessage(String sender, String errorMessage, long errorCode) {
		log.debug("errorMessage: " + errorMessage);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Es ist ein Fehler im " + sender + " aufgetreten");
				alert.setHeaderText("Fehlerbehandlung (Fehlercode = " + errorCode + ")");
				alert.setContentText(errorMessage);
				alert.showAndWait();
				//alert.show();
			}
		});
	}

	@Override
	public void loginComplete() {
		log.debug("Login erfolreich");
		createNextGui();
	}

	@Override
	public void logoutComplete() {
		log.debug("Abmeldung durchgefuehrt");
		log.debug("Logout-Vorgang ist nun beendet");
		System.exit(0);
	}
}
