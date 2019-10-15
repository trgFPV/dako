package edu.hm.dako.chat.client;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Modelldaten fuer FX-GUI
 * 
 * @author Paul Mandl
 * 
 */
public class ClientModel {

	private StringProperty userName = new SimpleStringProperty();

	public StringProperty userNameProperty() {
		return userName;
	}

	public void setUserName(String name) {
		userName.set(name);
	}

	public String getUserName() {
		return userName.get();
	}

	public ObservableList<String> users = FXCollections.observableArrayList();
	public ObservableList<String> chats = FXCollections.observableArrayList();

	public BooleanProperty block = new SimpleBooleanProperty();
}
