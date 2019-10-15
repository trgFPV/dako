package edu.hm.dako.chat.client;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Controller fuer Chat-GUI
 * 
 * @author Paul Mandl
 *
 */
public class LoggedInGuiController {

	private static Log log = LogFactory.getLog(LoggedInGuiController.class);

	@FXML
	private Button btnSubmit;
	@FXML
	private TextField txtChatMessage;
	@FXML
	private ListView<String> usersList;
	@FXML
	private ListView<String> chatList;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private ScrollPane chatPane;

	private ClientFxGUI appController;


	@FXML
	public void handleEnterPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			btnSubmit_OnAction();
		}
	}

	public void setAppController(ClientFxGUI appController) {

		this.appController = appController;

		usersList.maxWidthProperty().bind(scrollPane.widthProperty().subtract(2));
		usersList.minWidthProperty().bind(scrollPane.widthProperty().subtract(2));
		usersList.maxHeightProperty().bind(scrollPane.heightProperty().subtract(2));
		usersList.minHeightProperty().bind(scrollPane.heightProperty().subtract(2));

		usersList.setItems(appController.getModel().users);
        usersList.scrollTo(appController.getModel().users.size());

		chatList.maxWidthProperty().bind(chatPane.widthProperty().subtract(2));
		chatList.minWidthProperty().bind(chatPane.widthProperty().subtract(2));
		chatList.maxHeightProperty().bind(chatPane.heightProperty().subtract(2));
		chatList.minHeightProperty().bind(chatPane.heightProperty().subtract(2));

		chatList.setItems(appController.getModel().chats);
      /*  chatList.getItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
              Platform.runLater(() ->  chatList.scrollTo(appController.getModel().users.size()-1) );
            }
        });*/
		btnSubmit.disableProperty().bind(appController.getModel().block);
	}

	public void btnLogOut_OnAction() {
		try {
			appController.getCommunicator().logout(appController.getModel().getUserName());
		} catch (IOException e) {
			log.error("Logout konnte nicht durchgefuehrt werden, Server aktiv?");
			appController.setErrorMessage("Chat-Client",
					"Abmelden beim Server nicht erfolgreich, da der Server vermutlich nicht aktiv ist. Sie werden abgemeldet...",
					5);
			appController.switchToLogInGui();
		}

		// Bei Abschluss des Logout-Vorgangs wird dies ueber die Callback-Methode
		// logoutComplete an die GUI gemeldet. Dort wird dann das Programm beendet

	}

	public void btnSubmit_OnAction() {
		try {
			// Eingegebene Chat-Nachricht an Server senden
			appController.getCommunicator().tell(appController.getModel().getUserName(),
					txtChatMessage.getText());
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    txtChatMessage.setText("");
                    chatList.scrollTo(appController.getModel().chats.size()-1);
                }
            });
		} catch (IOException e) {
			// Senden funktioniert nicht, Server vermutlich nicht aktiv
			log.error("Senden konnte nicht durchgefuehrt werden, Server aktiv?");
			appController.setErrorMessage("Chat-Client",
					"Die Nachricht konnte nicht gesendet werden, da der Server unter Umst\u00e4nden nicht mehr l\u00e4uft. Sie werden abgemeldet...",
					6);
			appController.switchToLogInGui();
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					txtChatMessage.setText("");
				}
			});
		}
	}
}
