package edu.hm.dako.chat.benchmarking;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Formatter;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;

import org.apache.log4j.PropertyConfigurator;

import edu.hm.dako.chat.common.SystemConstants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * GUI fuer den Benchmarking-Client in JavaFX-GUI-Technologie
 * 
 * Hinweis: Um die Groesse der Objekte einheitlich zu gestalten, wird jedes
 * Objekt durch eine eigene Methode erstellt, in der die Groesse festgelegt wird
 * 
 * @author Paul Mandl
 */

// Bei Serverabsturz oder Serverbeendigung solle dies im Client erkannt
// werden und die GUI wieder zum Login-Dialog springen

public class BenchmarkingClientGuiFx2 extends Application

		implements BenchmarkingClientUserInterface {

	// BenchmarkingClient
	BenchmarkingClientCoordinator benchClient;

	//Bildschirmauflösung
	Dimension dim;

	// Eingabeparameter fuer GUI erzeugen
	UserInterfaceInputParameters iParam = new UserInterfaceInputParameters();

	// Message Area erstellen
	final Label messageArea = new Label();

	// Buttons erstellen
	final Button abortButton = new Button("Abbrechen");
	final Button startButton = new Button("Starten");
	final Button newButton = new Button("Neustart");
	final Button finishButton = new Button("Beenden");

	// Vbox fuer die Zusammensetzung der einzelnen Raster erstellen
	final VBox pane = new VBox(5);
	final VBox box = new VBox();

	// Auswahl fuer Comboboxen
	ObservableList<String> implTypeOptions = FXCollections.observableArrayList(
			SystemConstants.IMPL_TCP_SIMPLE, SystemConstants.IMPL_TCP_ADVANCED);
	ObservableList<String> measureTypeOptions = FXCollections
			.observableArrayList("Variable Threads", "Variable Length");

	private final static int MIN_SCREENSIZE = 900;

	// Comboboxen
	private ComboBox<String> optionListImplType;
	private ComboBox<String> optionListMeasureType;

	// Eingabefelder
	private TextField textFieldNumberOfClientThreads;
	private TextField textFieldNumberOfMessagesPerClients;
	private TextField textFieldServerport;
	private TextField textFieldThinkTime;
	private TextField textFieldServerIpAdress;
	private TextField textFieldMessageLength;
	private TextField textFieldNumberOfMaxRetries;
	private TextField textFieldResponseTimeout;

	// Ausgabefelder fuer Laufzeitdaten
	private TextField textFieldPlannedRequests;
	private TextField textFieldTestBegin;
	private TextField textFieldSentRequests;
	private TextField textFieldTestEnd;
	private TextField textFieldReceivedResponses;
	private TextField textFieldTestDuration;
	private TextField textFieldPlannedEventMessages;
	private TextField textFieldSentEventMessages;
	private TextField textFieldReceivedConfirmEvents;
	private TextField textFieldLostConfirmEvents;
	private TextField textFieldRetriedEvents;
	private TextField textFieldNumberOfRetries;

	// Ausgabefelder fuer Messergebnisse
	private TextField textFieldAvgRTT;
	private TextField textFieldAvgServerTime;
	private TextField textFieldMaxRTT;
	private TextField textFieldMaxHeapUsage;
	private TextField textFieldMinRTT;
	private TextField textFieldAvgCpuUsage;

	// Statistische Ausgabefelder
	private TextField textField10Percentile;
	private TextField textField25Percentile;
	private TextField textField50Percentile;
	private TextField textField75Percentile;
	private TextField textField90Percentile;
	private TextField textFieldRange;
	private TextField textFieldInterquartilRange;
	private TextField textFieldStandardDeviation;
	private ProgressBar progressBarFx;

	// Labels Eingabefelder
	private Label implType;
	private Label numberOfClientThreads;
	private Label numberOfMessagesPerClients;
	private Label serverport;
	private Label thinkTime;
	private Label serverIpAddress;
	private Label messageLength;
	private Label numberOfMaxRetries;
	private Label responseTimeout;

	// Scroller fuer Message Area
	private ScrollPane scrollPane;

	// Task fuer Progressbar
	private Task<Boolean> task;
	private Label progressIndicator;
	private int progressCounter;

	// Laufzeitzaehler erzeugen
	private Long timeCounter = 0L;

	private StringProperty labelString = new SimpleStringProperty();

	// Patterns fuer die Pruefung der eingegebenen IP-Adressen
	private static final Pattern IPV6_PATTERN = Pattern
			.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
	private static final Pattern IPV4_PATTERN = Pattern.compile(
			"(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

	// Kennzeichen, ob alle Parameter ordnungsgemaess eingegeben wurden, um den
	// Benchmark zu starten
	private boolean startable = true;

	/**
	 * Start der GUI
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		PropertyConfigurator.configureAndWatch("log4j.benchmarkingClient.properties", 60 * 1000);

		launch(args);
	}

	/**
	 * Startmethode fuer FX-Application
	 */
	@Override
	public void start(final Stage stage) throws Exception {

		// BorderPane fuer Layout erstellen
		BorderPane layout = new BorderPane(pane);
		BorderPane.setAlignment(pane, Pos.CENTER);
		layout.setStyle("-fx-background-color: cornsilk");

		// Progressbar erzeugen
		progressBarFx = createProgressbar();
		progressIndicator = new Label();
		progressIndicator.setMinWidth(550);
		progressIndicator.setMaxWidth(550);

		// GUI-Pane erzeugen
		createGuiPane();

		if(dim.getHeight() >= MIN_SCREENSIZE) {
			progressBarFx.setMinSize(1210, 30);
			progressBarFx.setMaxSize(1210, 30);
		} else {
			progressBarFx.setMinSize(1050, 30);
			progressBarFx.setMaxSize(1050, 30);
		}

		// Reakionsroutinen fuer Buttons einrichten
		reactOnStartButton();
		reactOnNewButton();
		reactOnFinishButton();
		reactOnAbortButton();

		// Titel setzen und Window anzeigen
		stage.setTitle("Benchmarking Client");
		if(dim.getHeight() >= MIN_SCREENSIZE) {
			stage.setScene(new Scene(layout, 1235, 850));
		}else{
			stage.setScene(new Scene(layout, 1150, 650));
		}
		stage.show();
	}

	/**
	 * Pane fuer das Layout der GUI erzeugen
	 * 
	 * @return pane
	 */
	private Pane createGuiPane() {
		dim = Toolkit.getDefaultToolkit().getScreenSize();
		scrollPane = createScrollPane();

		pane.getChildren().add(createSeperator("Eingabeparameter"));
		pane.getChildren().add(createInputPane());

		pane.getChildren().add(createSeperator("Laufzeitdaten"));
		pane.getChildren().add(createRunTimePane());

		pane.getChildren().add(createSeperator("Messergebnisse"));
		pane.getChildren().add(createResultPane());
		pane.getChildren().add(scrollPane);
		pane.getChildren().add(createProgressPane());
		pane.getChildren().add(createSeperator(""));
		pane.getChildren().add(createButtonPane());

		pane.setPadding(new Insets(5, 5, 5, 5));
		pane.setAlignment(Pos.CENTER);
		return pane;
	}

	/**
	 * Pane fuer die Eingabeparameter erstellen
	 * 
	 * @return inputPane
	 */
	private Pane createInputPane() {

		final GridPane inputPane = new GridPane();

		optionListImplType = createCombobox(implTypeOptions);
		optionListMeasureType = createCombobox(measureTypeOptions);
		optionListImplType.setValue(SystemConstants.IMPL_TCP_SIMPLE);
		optionListMeasureType.setValue("Variable Threads");

		// Comboboxen zum Pane hinzufuegen und Labels ergaenzen
		implType = createLabel("Implementierungstyp");
		inputPane.add(implType, 1, 1);
		inputPane.add(optionListImplType, 3, 1);
		inputPane.add(createLabel("Art der Messung"), 5, 1);
		inputPane.add(optionListMeasureType, 7, 1);

		// Textfelder zum Pane hinzufuegen und Labels ergaenzen
		numberOfClientThreads = createLabel("Anzahl Client-Threads");
		inputPane.add(numberOfClientThreads, 1, 3);
		textFieldNumberOfClientThreads = createEditableTextfield(inputPane, 3, 3, "1");

		numberOfMessagesPerClients = createLabel("Anzahl Nachrichten je Client");
		inputPane.add(numberOfMessagesPerClients, 1, 5);
		textFieldNumberOfMessagesPerClients = createEditableTextfield(inputPane, 3, 5, "10");

		numberOfMaxRetries = createLabel("Max. Anzahl Wiederholungen");
		inputPane.add(numberOfMaxRetries, 1, 7);
		textFieldNumberOfMaxRetries = createEditableTextfield(inputPane, 3, 7, "1");

		messageLength = createLabel("Nachrichtenl\u00e4nge in Byte");
		inputPane.add(messageLength, 5, 3);
		textFieldMessageLength = createEditableTextfield(inputPane, 7, 3, "10");

		responseTimeout = createLabel("Response-Timeout in ms");
		inputPane.add(responseTimeout, 5, 5);
		textFieldResponseTimeout = createEditableTextfield(inputPane, 7, 5, "2000");

		thinkTime = createLabel("Denkzeit in ms");
		inputPane.add(thinkTime, 5, 7);
		textFieldThinkTime = createEditableTextfield(inputPane, 7, 7, "100");

		serverport = createLabel("Serverport");
		inputPane.add(serverport, 9, 3);
		textFieldServerport = createEditableTextfield(inputPane, 11, 3, "50001");

		serverIpAddress = createLabel("Server-IP-Adresse");
		inputPane.add(serverIpAddress, 9, 5);
		textFieldServerIpAdress = createEditableTextfield(inputPane, 11, 5, "localhost");

		if(dim.getHeight() >= MIN_SCREENSIZE){
			inputPane.add(createLabel(""), 1, 9);
			// Abstaende hinzufuegen
			inputPane.setHgap(5);
			inputPane.setVgap(3);
		} else{
			// Abstaende hinzufuegen
			inputPane.setHgap(2);
			inputPane.setVgap(2);
		}

		inputPane.setAlignment(Pos.CENTER);

		return inputPane;
	}

	/**
	 * Pane fuer die Laufzeitdaten erstellen
	 * 
	 * @return runTimePane
	 */
	private Pane createRunTimePane() {

		final GridPane runTimePane = new GridPane();

		// Textfelder zum Pane hinzufuegen und Label ergaenzen

		runTimePane.add(createLabel("Geplante Requests"), 1, 1);
		textFieldPlannedRequests = createNotEditableTextfield(runTimePane, 3, 1);

		runTimePane.add(createLabel("Gesendete Requests"), 5, 1);
		textFieldSentRequests = createNotEditableTextfield(runTimePane, 7, 1);

		runTimePane.add(createLabel("Empfangene Responses"), 9, 1);
		textFieldReceivedResponses = createNotEditableTextfield(runTimePane, 11, 1);

		runTimePane.add(createLabel("Testbeginn"), 1, 3);
		textFieldTestBegin = createNotEditableTextfield(runTimePane, 3, 3);

		runTimePane.add(createLabel("Testende"), 5, 3);
		textFieldTestEnd = createNotEditableTextfield(runTimePane, 7, 3);

		runTimePane.add(createLabel("Testdauer in s"), 9, 3);
		textFieldTestDuration = createNotEditableTextfield(runTimePane, 11, 3);

		runTimePane.add(createLabel("Kalkulierte Event-Nachrichten"), 1, 5);
		textFieldPlannedEventMessages = createNotEditableTextfield(runTimePane, 3, 5);

		runTimePane.add(createLabel("Gesendete Event-Nachrichten"), 5, 5);
		textFieldSentEventMessages = createNotEditableTextfield(runTimePane, 7, 5);

		runTimePane.add(createLabel("Wiederholte Event-Nachrichten"), 9, 5);
		textFieldRetriedEvents = createNotEditableTextfield(runTimePane, 11, 5);

		runTimePane.add(createLabel("Empfangene Confirm-Nachrichten"), 1, 7);
		textFieldReceivedConfirmEvents = createNotEditableTextfield(runTimePane, 3, 7);

		runTimePane.add(createLabel("Verlorene Confirm-Nachrichten"), 5, 7);
		textFieldLostConfirmEvents = createNotEditableTextfield(runTimePane, 7, 7);

		runTimePane.add(createLabel("Anzahl \u00dcbertragungswiederholungen"), 9, 7);
		textFieldNumberOfRetries = createNotEditableTextfield(runTimePane, 11, 7);

		if(dim.getHeight() >= MIN_SCREENSIZE){
			runTimePane.add(createLabel(""), 1, 9);
			// Abstaende hinzufuegen
			runTimePane.setHgap(5);
			runTimePane.setVgap(3);
		} else{
			// Abstaende hinzufuegen
			runTimePane.setHgap(2);
			runTimePane.setVgap(2);
		}
		runTimePane.setAlignment(Pos.CENTER);

		return runTimePane;
	}

	/**
	 * Pane fuer die Messergebnisse erstellen
	 * 
	 * @return resultPane
	 */
	private Pane createResultPane() {
		// Pane initialisieren
		final GridPane resultPane = new GridPane();

		// Textfelder zum Pane hinzufuegen und Label ergaenzen
		resultPane.add(createLabel("Mittlere RTT in ms"), 1, 1);
		textFieldAvgRTT = createNotEditableTextfield(resultPane, 3, 1);

		resultPane.add(createLabel("Maximale RTT in ms"), 5, 1);
		textFieldMaxRTT = createNotEditableTextfield(resultPane, 7, 1);

		resultPane.add(createLabel("Minimale RTT in ms"), 9, 1);
		textFieldMinRTT = createNotEditableTextfield(resultPane, 11, 1);

		resultPane.add(createLabel("Interquartilsabstand in ms"), 1, 3);
		textFieldInterquartilRange = createNotEditableTextfield(resultPane, 3, 3);

		resultPane.add(createLabel("Spannweite in ms"), 5, 3);
		textFieldRange = createNotEditableTextfield(resultPane, 7, 3);

		resultPane.add(createLabel("Standardabweichung in ms"), 9, 3);
		textFieldStandardDeviation = createNotEditableTextfield(resultPane, 11, 3);

		resultPane.add(createLabel("10%-Percentile in ms"), 1, 5);
		textField10Percentile = createNotEditableTextfield(resultPane, 3, 5);

		resultPane.add(createLabel("25%-Percentile in ms"), 5, 5);
		textField25Percentile = createNotEditableTextfield(resultPane, 7, 5);

		resultPane.add(createLabel("50%-Percentile in ms"), 9, 5);
		textField50Percentile = createNotEditableTextfield(resultPane, 11, 5);

		resultPane.add(createLabel("75%-Percentile in ms"), 1, 7);
		textField75Percentile = createNotEditableTextfield(resultPane, 3, 7);

		resultPane.add(createLabel("90%-Percentile in ms"), 5, 7);
		textField90Percentile = createNotEditableTextfield(resultPane, 7, 7);

		resultPane.add(createLabel("Mittlere Serverzeit in ms"), 1, 11);
		textFieldAvgServerTime = createNotEditableTextfield(resultPane, 3, 11);

		resultPane.add(createLabel("Maximale Heap-Belegung in MiB"), 5, 11);
		textFieldMaxHeapUsage = createNotEditableTextfield(resultPane, 7, 11);

		resultPane.add(createLabel("Mittlere CPU-Auslastung in %"), 9, 11);
		textFieldAvgCpuUsage = createNotEditableTextfield(resultPane, 11, 11);

		if(dim.getHeight() >= MIN_SCREENSIZE){
			resultPane.add(createLabel(""), 1, 13);
			// Abstaende hinzufuegen
			resultPane.setHgap(5);
			resultPane.setVgap(3);
		} else{
			// Abstaende hinzufuegen
			resultPane.setHgap(2);
			resultPane.setVgap(2);
		}
		resultPane.setAlignment(Pos.CENTER);

		return resultPane;
	}

	/**
	 * Pane fuer Buttons erzeugen
	 * 
	 * @return buttonPane
	 */
	private HBox createButtonPane() {

		final HBox buttonPane = new HBox(10);

		if(dim.getHeight() >= MIN_SCREENSIZE) {
			abortButton.setFont(Font.font(15));
			abortButton.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
			startButton.setFont(Font.font(15));
			startButton.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
			newButton.setFont(Font.font(15));
			newButton.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
			finishButton.setFont(Font.font(15));
			finishButton.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
		} else {
			abortButton.setFont(Font.font(12));
			abortButton.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
			startButton.setFont(Font.font(12));
			startButton.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
			newButton.setFont(Font.font(12));
			newButton.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
			finishButton.setFont(Font.font(12));
			finishButton.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
		}
		buttonPane.getChildren().addAll(abortButton, startButton, newButton, finishButton);
		buttonPane.setAlignment(Pos.CENTER);

		return buttonPane;
	}

	/**
	 * Fortschrittsbalken mit Fortschrittsanzeige erstellen
	 * 
	 * @return gridPane
	 */
	private StackPane createProgressPane() {
		final StackPane pane = new StackPane();
		pane.getChildren().addAll(progressBarFx, progressIndicator);
		progressIndicator.setAlignment(Pos.CENTER);
		pane.setAlignment(Pos.CENTER);
		return pane;
	}

	/**
	 * Label erstellen
	 * 
	 * @param value
	 *
	 * @return label
	 */
	private Label createLabel(String value) {

		Label label = new Label(value);

		if(dim.getHeight() >= MIN_SCREENSIZE){
			label.setMinSize(230, 20);
			label.setMaxSize(230, 20);
			label.setFont(Font.font(13));
		} else {
			label.setMinSize(190, 20);
			label.setMaxSize(190, 20);
			label.setFont(Font.font(10));
		}
		label.setAlignment(Pos.BASELINE_RIGHT);
		return label;
	}

	/**
	 * Erstellung eines Progressbars
	 * 
	 * @return Progressbar
	 */
	private ProgressBar createProgressbar() {
		final ProgressBar progressBar = new ProgressBar(0);

		progressBar.setStyle(
				"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 50px, 50px, 50px, 50px; -fx-accent: green;");
		return progressBar;
	}

	/**
	 * Erstellung nicht editierbarer Textfelder
	 * 
	 * @param pane
	 * @param columnIndex
	 * @param rowIndex
	 * @return textField
	 */
	private TextField createNotEditableTextfield(GridPane pane, int columnIndex,
			int rowIndex) {
		TextField textField = new TextField();
		pane.add(textField, columnIndex, rowIndex);
		textField.setMaxSize(155, 28);
		textField.setMinSize(155, 28);
		textField.setEditable(false);
		if(dim.getHeight() >= MIN_SCREENSIZE) {
			textField.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
		}else{
			textField.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px; -fx-font-size: 10px");
		}
		return textField;
	}

	/**
	 * Erstellung editierbarer Textfelder Erstellung nicht editierbarer Textfelder
	 * 
	 * @param pane
	 * @param columnIndex
	 * @param rowIndex
	 * @param value
	 * @return textField
	 */
	private TextField createEditableTextfield(GridPane pane, int columnIndex, int rowIndex,
			String value) {
		TextField textField = new TextField(value);
		pane.add(textField, columnIndex, rowIndex);
		textField.setMaxSize(155, 28);
		textField.setMinSize(155, 28);
		textField.setEditable(true);
		if(dim.getHeight() >= MIN_SCREENSIZE) {
			textField.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
		}else{
			textField.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px; -fx-font-size: 10px");
		}
		return textField;
	}

	/**
	 * Erstellung einses Seperators mit einem Label
	 * 
	 * @param value
	 * @return labeledSeparator
	 *
	 */
	private HBox createSeperator(String value) {
		// Separator erstellen
		final HBox labeledSeparator = new HBox();
		final Separator rightSeparator = new Separator(Orientation.HORIZONTAL);
		final Label textOnSeparator = new Label(value);
		final double labelLength;
		if(dim.getHeight() >= MIN_SCREENSIZE) {
			textOnSeparator.setFont(Font.font(18));
			labelLength = value.length();
			rightSeparator.setMinWidth(1220 - labelLength * 10);
			rightSeparator.setMaxWidth(1220 - labelLength * 10);
		}else{
			labelLength = value.length();
			textOnSeparator.setFont(Font.font(12));
			rightSeparator.setMinWidth(1050 - labelLength * 10);
			rightSeparator.setMaxWidth(1050 - labelLength * 10);
		}

		labeledSeparator.getChildren().add(textOnSeparator);
		labeledSeparator.getChildren().add(rightSeparator);
		labeledSeparator.setAlignment(Pos.CENTER);

		return labeledSeparator;
	}

	/**
	 * Erstellung von Comboboxen
	 * 
	 * @param options
	 * @return comboBox
	 */
	private ComboBox<String> createCombobox(ObservableList<String> options) {
		ComboBox<String> comboBox = new ComboBox<String>(options);
		comboBox.setMinSize(155, 28);
		comboBox.setMaxSize(155, 28);
		if(dim.getHeight() >= MIN_SCREENSIZE) {
			comboBox.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px;");
		} else {
			comboBox.setStyle(
					"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px; -fx-font-size: 10px;");
		}
		return comboBox;
	}

	/**
	 * Erstellung einer MessageArea in einem Scrollpane
	 * 
	 * @return scrollPane
	 */
	private ScrollPane createScrollPane() {
		ScrollPane scrollPane = new ScrollPane();
		if(dim.getHeight() >= MIN_SCREENSIZE) {
			scrollPane.setMinSize(1210, 55);
			scrollPane.setMaxSize(1210, 55);
		}else{
			scrollPane.setMinSize(1050, 55);
			scrollPane.setMaxSize(1050, 55);
		}

		scrollPane.setContent(box);
		box.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue,
					Number newValue) {
				scrollPane.setVvalue((Double) newValue);
			}
		});
		scrollPane.setStyle(
				"-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-radius: 5px, 5px, 5px, 5px");
		return scrollPane;
	}

	/**
	 * Es wird ein Task erstellt, der bei Erhoehung des progressCounter den
	 * Progressbar aktualisiert
	 * 
	 * @return Task
	 */
	private Task<Boolean> progressTask() {
		return new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {

				// Anzahl der erwarteten Progresscounter-Erhoehungen berechnen: Alle
				// Message-Requests ohne Logins und ohne Logouts
				int maxMessages = iParam.getNumberOfMessages() * iParam.getNumberOfClients();

				for (int i = 0; i < maxMessages; i = progressCounter) {
					updateProgress(i + 1, maxMessages);
				}

				return true;
			}
		};
	}

	/**
	 * Der ProgressBar wird an diesen Task gebunden
	 */
	private void countUpProgressBar() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final NumberFormat format = new DecimalFormat("0.00 %");
				task.progressProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {
						labelString.setValue(format.format(newValue));
					}
				});
				progressIndicator.textProperty().bind(labelString);
				if(dim.getHeight() >= MIN_SCREENSIZE) {
					progressIndicator.setFont(Font.font(13));
				}else{
					progressIndicator.setFont(Font.font(10));
				}
				progressBarFx.progressProperty().unbind();
				progressBarFx.progressProperty().bind(task.progressProperty());
				new Thread(task).start();
			}
		});
	}

	/**
	 * Reaktion auf Betaetigen des Start-Buttons
	 */
	private synchronized void reactOnStartButton() {

		startButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
			@Override
			public void handle(javafx.event.ActionEvent event) {
				abortButton.setDisable(false);
				newButton.setDisable(true);
				startButton.setDisable(true);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						task = progressTask();
						benchClient = new BenchmarkingClientCoordinator();
						startGui();
					}
				});
			}
		});
	}

	/**
	 * Reaktion auf Betaetigen des New-Buttons
	 */
	private synchronized void reactOnNewButton() {
		newButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				newAction();
				startButton.setDisable(false);
				newButton.setDisable(true);
				abortButton.setDisable(true);

			}
		});
	}

	/**
	 * Reaktion auf Betaetigen des Finish-Buttons
	 */
	private synchronized void reactOnFinishButton() {
		finishButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setMessageLine("Programm wird beendet...");
				System.exit(0);
			}
		});
	}

	/**
	 * Reaktion auf Betaetigen des Abort-Buttons
	 */
	private synchronized void reactOnAbortButton() {
		abortButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				benchClient.abortTest();
				setMessageLine("Testlauf wird abgebrochen...");
				try {
					benchClient.join();
				} catch (Exception exception) {
				}
				setMessageLine("Testlauf beendet");
				if (benchClient.isTestAborted() == true) {
					benchClient.releaseTest();
				}
			}
		});
	}

	/**
	 * Die Benchmarking-GUI wird gestartet
	 */
	private void startGui() {
		startable = true;
		scrollPane.setContent(box);

		readComboBoxes();

		setServerPort();
		setThinkTime();
		setNumberOfClientThreads();
		setNumberOfMessagesPerClient();
		setMessageLength();
		setResponseTimeOut();
		setNumberOfMaxRetries();
		setServerIpAddress();

		if (startable == false) {
			setAlert("Bitte korrigieren Sie die rot markierten Felder!");
			startButton.setDisable(false);

		} else {
			benchClient.executeTest(iParam, this);
			countUpProgressBar();

		}
	}

	/**
	 * Pruefen der Eingabe des Serverports
	 */
	private void setServerPort() {

		String testString = textFieldServerport.getText();
		if (testString.matches("[0-9]+")) {
			Integer iServerPort = new Integer(textFieldServerport.getText());
			if ((iServerPort < 1) || (iServerPort > 65535)) {
				// Nicht im Wertebereich
				// Aktualisieren des Frames auf dem Bildschirm
				startable = false;
				serverport.setTextFill(Color.web(SystemConstants.RED_COLOR));
				return;
			} else {
				System.out.println("Serverport: " + iServerPort);
				iParam.setRemoteServerPort(iServerPort.intValue());
				serverport.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
			}
		} else {
			startable = false;
			serverport.setTextFill(Color.web(SystemConstants.RED_COLOR));

			return;
		}
	}

	/**
	 * Pruefen der Eingabe der Denkzeit
	 */
	private void setThinkTime() {
		String testString2 = textFieldThinkTime.getText();
		if (!testString2.matches("[0-9]+")) {
			// Nicht numerisch
			// Aktualisieren des Frames auf dem Bildschirm
			startable = false;
			thinkTime.setTextFill(Color.web(SystemConstants.RED_COLOR));
			return;
		} else {
			Integer iThinkTime = new Integer(textFieldThinkTime.getText());
			System.out.println("Denkzeit: " + iThinkTime + " ms");
			iParam.setClientThinkTime(iThinkTime.intValue());
			thinkTime.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
		}
	}

	/**
	 * Pruefen der Eingabe der Anzahl an Clients
	 */
	private void setNumberOfClientThreads() {

		String testString3 = textFieldNumberOfClientThreads.getText();
		if (testString3.matches("[0-9]+")) {
			Integer iClientThreads = new Integer(textFieldNumberOfClientThreads.getText());
			if (iClientThreads < 1 || iClientThreads > BenchmarkingConstants.MAX_CLIENTS) {
				// Nicht im Wertebereich
				// Aktualisieren des Frames auf dem Bildschirm
				startable = false;
				numberOfClientThreads.setTextFill(Color.web(SystemConstants.RED_COLOR));
			} else {
				System.out.println("Anzahl Client-Threads:" + iClientThreads);
				iParam.setNumberOfClients(iClientThreads.intValue());
				numberOfClientThreads.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
			}
		} else {
			startable = false;
			numberOfClientThreads.setTextFill(Color.web(SystemConstants.RED_COLOR));
		}
	}

	/**
	 * Pruefen der Eingabe der Anzahl an Nachrichten pro Client
	 */
	private void setNumberOfMessagesPerClient() {

		String testString4 = textFieldNumberOfMessagesPerClients.getText();
		if (testString4.matches("[0-9]+")) {
			Integer iNumberOfMessages = new Integer(
					textFieldNumberOfMessagesPerClients.getText());
			if (iNumberOfMessages < 1
					|| iNumberOfMessages > BenchmarkingConstants.MAX_MESSAGES_PER_CLIENT) {
				// nicht numerisch
				// Aktualisieren des Frames auf dem Bildschirm
				startable = false;
				numberOfMessagesPerClients.setTextFill(Color.web(SystemConstants.RED_COLOR));
				return;
			} else {
				System.out.println("Anzahl Nachrichten:" + iNumberOfMessages);
				iParam.setNumberOfMessages(iNumberOfMessages.intValue());
				numberOfMessagesPerClients.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
			}
		} else {
			startable = false;
			numberOfMessagesPerClients.setTextFill(Color.web(SystemConstants.RED_COLOR));
			return;
		}
	}

	/**
	 * Pruefen der Eingabe des Nachrichtenlaenge
	 */
	private void setMessageLength() {

		String testString5 = textFieldMessageLength.getText();
		if (testString5.matches("[0-9]+")) {
			Integer iMessageLength = new Integer(textFieldMessageLength.getText());
			if ((iMessageLength < 1)
					|| (iMessageLength > BenchmarkingConstants.MAX_MESSAGE_LENGTH)) {
				// nicht im Wertebereich
				// Aktualisieren des Frames auf dem Bildschirm
				startable = false;
				messageLength.setTextFill(Color.web(SystemConstants.RED_COLOR));
				return;
			} else {
				System.out.println("Nachrichtenlaenge:" + iMessageLength + " Byte");
				iParam.setMessageLength(iMessageLength.intValue());
				messageLength.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
			}
		} else {
			startable = false;
			messageLength.setTextFill(Color.web(SystemConstants.RED_COLOR));
			return;
		}
	}

	/**
	 * Pruefen der Eingabe fuer den Response-Timeout
	 */
	private void setResponseTimeOut() {
		String testString6 = textFieldResponseTimeout.getText();
		if (!testString6.matches("[0-9]+")) {
			// nicht numerisch
			// Aktualisieren des Frames auf dem Bildschirm
			startable = false;
			responseTimeout.setTextFill(Color.web(SystemConstants.RED_COLOR));
			return;
		} else {
			Integer iResponseTimeout = new Integer(textFieldResponseTimeout.getText());
			System.out.println("Response-Timeout:" + iResponseTimeout);
			iParam.setResponseTimeout(iResponseTimeout.intValue());
			responseTimeout.setTextFill(Color.web(SystemConstants.BLACK_COLOR));

		}
	}

	/**
	 * Pruefen der Eingabe fuer die maximale Anzahl an Wiederholungen
	 */
	private void setNumberOfMaxRetries() {
		String testString7 = textFieldNumberOfMaxRetries.getText();
		if (testString7.matches("[0-9]+")) {
			Integer iNumberOfMaxRetries = new Integer(textFieldNumberOfMaxRetries.getText());
			if (iNumberOfMaxRetries > 5000) {
				// nicht im Wertebereich
				// Aktualisieren des Frames auf dem Bildschirm
				startable = false;
				numberOfMaxRetries.setTextFill(Color.web(SystemConstants.RED_COLOR));
			} else {
				System.out.println("Maximale Anzahl Wiederholungen:" + iNumberOfMaxRetries);
				iParam.setNumberOfRetries(iNumberOfMaxRetries.intValue());
				numberOfMaxRetries.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
			}
		} else {
			// nicht numerisch
			// Aktualisieren des Frames auf dem Bildschirm
			startable = false;
			numberOfMaxRetries.setTextFill(Color.web(SystemConstants.RED_COLOR));
			return;
		}
	}

	/**
	 * Pruefen der Eingabe fuer die IP-Adresse
	 */
	private void setServerIpAddress() {
		String testString = textFieldServerIpAdress.getText();
		if (testString.equals("localhost")) {
			System.out.println("RemoteServerAdress:" + testString);
			iParam.setRemoteServerAddress(testString);

			serverIpAddress.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
		} else if (IPV6_PATTERN.matcher(testString).matches()) {
			System.out.println("RemoteServerAdress:" + testString);
			iParam.setRemoteServerAddress(testString);

			serverIpAddress.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
		} else if (IPV4_PATTERN.matcher(testString).matches()) {
			System.out.println("RemoteServerAdress:" + testString);
			iParam.setRemoteServerAddress(testString);

			serverIpAddress.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
		} else {
			startable = false;
			serverIpAddress.setTextFill(Color.web(SystemConstants.RED_COLOR));
		}
	}

	/**
	 * Eingabe ueber die Comboboxen auslesen
	 */
	private void readComboBoxes() {
		// Comboboxen auslesen
		String item = new String(optionListImplType.getValue().toString());
		if (item.equals(SystemConstants.IMPL_TCP_ADVANCED)) {
			iParam.setImplementationType(
					edu.hm.dako.chat.common.ImplementationType.TCPAdvancedImplementation);
			implType.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
		} else if (item.equals(SystemConstants.IMPL_TCP_SIMPLE)) {
			iParam.setImplementationType(
					edu.hm.dako.chat.common.ImplementationType.TCPSimpleImplementation);
			implType.setTextFill(Color.web(SystemConstants.BLACK_COLOR));
		} else if (item.equals(SystemConstants.IMPL_UDP_ADVANCED)) {
			setAlert("Der Typ UDPAdvanced wurde noch nicht Implementiert");
			startable = false;
			implType.setTextFill(Color.web(SystemConstants.RED_COLOR));
		} else {
			setAlert("Kein Implementierungstyp ausgew\u00c4hlt!");
			startable = false;
			implType.setTextFill(Color.web(SystemConstants.RED_COLOR));
		}
		String item1 = new String(optionListMeasureType.getValue().toString());
		if (item1.equals("Variable Threads")) {
			iParam.setMeasurementType(UserInterfaceInputParameters.MeasurementType.VarThreads);
		} else if (item1.equals("Variable Length")) {
			iParam
					.setMeasurementType(UserInterfaceInputParameters.MeasurementType.VarMsgLength);
		} else {
			setAlert("Art der Messung nicht festgelegt!");
			startable = false;
		}
	}

	/**
	 * Oeffnen eines Dialogfensters, wenn ein Fehler bei der Eingabe auftritt
	 * 
	 * @param message
	 */
	private void setAlert(String message) {
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

	/**
	 * Reaktion auf das Betaetigen des Neu-Buttons
	 */
	private void newAction() {
		/*
		 * Loeschen bzw. initialisieren der Ausgabefelder
		 */
		textFieldPlannedRequests.setText("");
		textFieldTestBegin.setText("");
		textFieldSentRequests.setText("");
		textFieldTestEnd.setText("");
		textFieldReceivedResponses.setText("");
		textFieldTestDuration.setText("");
		textFieldAvgRTT.setText("");
		textFieldAvgServerTime.setText("");
		textFieldMaxRTT.setText("");
		textFieldAvgCpuUsage.setText("");
		textFieldMaxHeapUsage.setText("");
		textFieldMinRTT.setText("");
		textField10Percentile.setText("");
		textField25Percentile.setText("");
		textField50Percentile.setText("");
		textField75Percentile.setText("");
		textField90Percentile.setText("");
		textFieldRange.setText("");
		textFieldStandardDeviation.setText("");
		textFieldInterquartilRange.setText("");
		textFieldNumberOfRetries.setText("");
		textFieldPlannedEventMessages.setText("");
		textFieldSentEventMessages.setText("");
		textFieldReceivedConfirmEvents.setText("");
		textFieldLostConfirmEvents.setText("");
		textFieldRetriedEvents.setText("");
		/*
		 * scrollPane.setContent(null);
		 * 
		 * box.getChildren().clear();
		 */

		/**
		 * Um den Prozessbalken wieder auf null zu setzen wird er vom Task entbunden
		 */

		Platform.runLater(() -> {
			progressBarFx.progressProperty().unbind();
			progressIndicator.textProperty().unbind();
			progressBarFx.setProgress(0);
			progressIndicator.setText("");
			messageArea.textProperty().unbind();
			messageArea.setText("");
			progressCounter = 0;
		});
	}

	/**
	 * Anzeige der uebergebenen Startdaten
	 */
	@Override
	public void showStartData(UserInterfaceStartData data) {
		String strNumberOfRequests = (new Long(data.getNumberOfRequests())).toString();
		String strNumberOfPlannedEventMessages = (new Long(
				data.getNumberOfPlannedEventMessages())).toString();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				textFieldPlannedRequests.setText(strNumberOfRequests);
				textFieldPlannedEventMessages.setText(strNumberOfPlannedEventMessages);
				textFieldTestBegin.setText(data.getStartTime());
			}
		});

	}

	/**
	 * Ergebnisse eines Testlaufs anzeigen
	 */
	@Override
	public void showResultData(UserInterfaceResultData data) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				textFieldSentRequests
						.setText((new Long(data.getNumberOfSentRequests())).toString());
				textFieldTestEnd.setText(data.getEndTime());
				textFieldReceivedResponses
						.setText((new Long(data.getNumberOfResponses())).toString());
				textFieldMaxHeapUsage.setText((new Long(data.getMaxHeapSize())).toString());
				textFieldNumberOfRetries
						.setText((new Long(data.getNumberOfRetries())).toString());

				Formatter formatter = new Formatter();
				textFieldAvgRTT
						.setText(formatter.format("%.2f", (new Double(data.getMean()))).toString());
				formatter.close();

				formatter = new Formatter();
				textFieldAvgServerTime.setText(
						formatter.format("%.2f", (new Double(data.getAvgServerTime()))).toString());
				formatter.close();

				formatter = new Formatter();
				textFieldMaxRTT.setText(
						formatter.format("%.2f", (new Double(data.getMaximum()))).toString());
				formatter.close();

				formatter = new Formatter();
				textFieldMinRTT.setText(
						formatter.format("%.2f", (new Double(data.getMinimum()))).toString());
				formatter.close();

				formatter = new Formatter();
				textField10Percentile.setText(
						formatter.format("%.2f", (new Double(data.getPercentile10()))).toString());
				formatter.close();

				formatter = new Formatter();
				textField25Percentile.setText(
						formatter.format("%.2f", (new Double(data.getPercentile25()))).toString());
				formatter.close();

				formatter = new Formatter();
				textField50Percentile.setText(
						formatter.format("%.2f", (new Double(data.getPercentile50()))).toString());
				formatter.close();

				formatter = new Formatter();
				textField75Percentile.setText(
						formatter.format("%.2f", (new Double(data.getPercentile75()))).toString());
				formatter.close();

				formatter = new Formatter();
				textField90Percentile.setText(
						formatter.format("%.2f", (new Double(data.getPercentile90()))).toString());
				formatter.close();

				formatter = new Formatter();
				textFieldRange
						.setText(formatter.format("%.2f", (new Double(data.getRange()))).toString());
				formatter.close();

				formatter = new Formatter();
				textFieldInterquartilRange.setText(formatter
						.format("%.2f", (new Double(data.getInterquartilRange()))).toString());
				formatter.close();

				formatter = new Formatter();
				textFieldStandardDeviation.setText(formatter
						.format("%.2f", (new Double(data.getStandardDeviation()))).toString());
				formatter.close();

				formatter = new Formatter();
				textFieldAvgCpuUsage.setText(formatter
						.format("%.2f", (new Double(data.getMaxCpuUsage() * 100))).toString());
				textFieldSentEventMessages
						.setText((new Long(data.getNumberOfSentEventMessages())).toString());
				textFieldReceivedConfirmEvents
						.setText((new Long(data.getNumberOfReceivedConfirmEvents())).toString());
				textFieldLostConfirmEvents
						.setText((new Long(data.getNumberOfLostConfirmEvents())).toString());
				textFieldRetriedEvents
						.setText((new Long(data.getNumberOfRetriedEvents())).toString());
				formatter.close();
			}
		});
	}

	/**
	 * Nachrichtenzeile in der Message Area der GUI ergaenzen
	 */
	@Override
	public void setMessageLine(String message) {

		Label value = new Label();
		value.setText(message);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				box.getChildren().add(value);
			}
		});
	}

	/**
	 * Laufzeitfeld in der GUI zuruecksetzen, GUI-Aktion wird in die Queue des
	 * Event-Dispatching-Thread eingetragen
	 */
	@Override
	public void resetCurrentRunTime() {
		timeCounter = Long.valueOf(0);
		String strTimeCounter = (new Long(timeCounter)).toString();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				textFieldTestDuration.setText(strTimeCounter);
			}
		});
	}

	/**
	 * GUI-Feld fuer die Ausgabe der Laufzeit hinzufuegen, GUI-Aktion wird in die
	 * Queue des Event-Dispatching-Thread eingetragen
	 */
	@Override
	public void addCurrentRunTime(long sec) {
		timeCounter += sec;
		String strTimeCounter = (timeCounter).toString();

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				textFieldTestDuration.setText(strTimeCounter);
			}
		});
	}

	/**
	 * Buttons nach Testende aktiv schalten
	 */
	@Override
	public void testFinished() {
		abortButton.setDisable(true);
		startButton.setDisable(true);
		newButton.setDisable(false);
		finishButton.setDisable(false);
	}

	/**
	 * Progressbar fuer FX-GUI
	 */
	@Override
	public ProgressBar getProgressBarFx() {
		return progressBarFx;
	}

	/**
	 * Methode zum Aktualisieren des Progressbar. Die Methode wird immer
	 * aufgerufen wird, wenn eine Response-Nachricht empfangen oder ein Client
	 * ein- oder ausgeloggt wird
	 */
	@Override
	public void countUpProgressTask() {
		progressCounter++;
	}

	/**
	 * Dummy-Methode: Progressbar fuer Swing-GUI
	 */
	@Override
	public JProgressBar getProgressBar() {
		return null;
	}
}