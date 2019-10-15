package edu.hm.dako.chat.client;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.chat.common.SystemConstants;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

/**
 * Controller fuer Login-GUI
 * 
 * @author Paul Mandl
 *
 */
public class LogInGuiController implements Initializable {

	private static Log log = LogFactory.getLog(LogInGuiController.class);

	private String userName;

	@FXML
	private TextField txtUsername;
	@FXML
	private TextField txtServername;
	@FXML
	private TextField txtServerPort;
	@FXML
	private ComboBox<String> comboServerType;
	@FXML
	private Button loginButton;
	@FXML
	private Label lblIP;
	@FXML
	private Label lblServerPort;

	private ClientFxGUI appController;

	private static final Pattern IPV6_PATTERN = Pattern
			.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
	private static final Pattern IPV4_PATTERN = Pattern.compile(
			"(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

	public void setAppController(ClientFxGUI appController) {
		this.appController = appController;
	}

	@FXML
	public void handleKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			performLogin();
		}
	}

	/**
	 * Login-Eingaben entgegennehmen, pruefen und Anmeldung beim Server
	 * durchfuehren
	 */
	public void performLogin() {

		// Benutzernamen pruefen

		userName = txtUsername.getText();
		if (userName.isEmpty() == true) {
			log.debug("Benutzername ist leer");
			appController.setErrorMessage("Chat-Client", "Benutzername fehlt", 1);
			return;
		} else {
			appController.getModel().setUserName(userName);
		}

		// IP-Adresse pruefen

		if (ipCorrect() != true) {
			appController.setErrorMessage("Chat-Client",
					"IP-Adresse nicht korrekt, z.B. 127.0.0.1, 192.168.178.2 oder localhost!", 3);
			lblIP.setTextFill(Color.web(SystemConstants.RED_COLOR));
			return;
		}
		// IP-Adresse ist korrekt
		lblIP.setTextFill(Color.web(SystemConstants.BLACK_COLOR));

		// Serverport pruefen

		int serverPort = 0;
		String valueServerPort = txtServerPort.getText();
		if (valueServerPort.matches("[0-9]+")) {
			serverPort = Integer.parseInt(valueServerPort);
			if ((serverPort < 1) || (serverPort > 65535)) {
				appController.setErrorMessage("Chat-Client",
						"Serverport ist nicht im Wertebereich von 1 bis 65535!", 2);
				log.debug("Serverport nicht im Wertebereich");
				lblServerPort.setTextFill(Color.web(SystemConstants.RED_COLOR));
				return;
			} else {
				// Serverport ist korrekt
				lblServerPort.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
			}
		} else {
			appController.setErrorMessage("Chat-Client", "Serverport ist nicht numerisch!", 3);
			lblServerPort.setTextFill(Color.web(SystemConstants.RED_COLOR));
			return;
		}

		// Verbindung herstellen und beim Server anmelden

		appController.createCommunicator((String) comboServerType.getValue(), serverPort,
				txtServername.getText());
		try {
			appController.getCommunicator().login(userName);
		} catch (Exception e2) {

			// Benutzer mit dem angegebenen Namen schon angemeldet
			log.error("Login konnte nicht zum Server gesendet werden, Server aktiv?");
			appController.setErrorMessage("Chat-Client",
					"Login konnte nicht gesendet werden, vermutlich ist der Server nicht aktiv", 4);
			// Verbindung zum Server wird wieder abgebaut
			appController.getCommunicator().cancelConnection();
		}
	}

	public String getUserName() {
		return userName;
	}

	public void exitButtonReaction() {

		System.exit(0);
	}

	/**
	 * Pruefen, ob IP-Adresse korrekt ist
	 * 
	 * @return true - korrekt, false - nicht korrekt
	 */
	private Boolean ipCorrect() {
		String testString = txtServername.getText();
		if (testString.equals("localhost")) {
			return true;
		} else if (IPV6_PATTERN.matcher(testString).matches()) {
			return true;
		} else if (IPV4_PATTERN.matcher(testString).matches()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		comboServerType.getItems().addAll(SystemConstants.IMPL_TCP_SIMPLE,
				SystemConstants.IMPL_TCP_ADVANCED);
	}
}
