package pl.edu.mimuw.sweather;

import static pl.edu.mimuw.sweather.event.EventStream.eventStream;
import static pl.edu.mimuw.sweather.event.EventStream.joinStream;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import pl.edu.mimuw.sweather.control.TooltipProlongHelper;
import pl.edu.mimuw.sweather.event.*;
import pl.edu.mimuw.sweather.network.AtmosphereDataSource;
import pl.edu.mimuw.sweather.network.DataSource;
import pl.edu.mimuw.sweather.network.MeteoWawDataSource;
import pl.edu.mimuw.sweather.network.OpenWeatherDataSource;
import rx.Observable;
import rx.Subscription;
import rx.observables.JavaFxObservable;

public class AppMain extends Application {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppMain.class);

	private static final String FXML_MAIN_FORM_TEMPLATE = "/fxml/sweather-main.fxml";
	private static final String FXML_CLOSE_DIALOG_TEMPLATE = "/fxml/close-dialog.fxml";
	private static final String FXML_SETTINGS_DIALOG_TEMPLATE = "/fxml/settings-dialog.fxml";

	private static final String FONT_CSS = "/css/jfoenix-fonts.css";
	private static final String MATERIAL_CSS = "/css/jfoenix-design.css";
	private static final String JFX_CSS = "/css/jfx.css";

	private static final String logo = "/icons/sun64.png";

	private static final String SOURCE_OPEN = "openweathermap.org";
	private static final String SOURCE_METEO = "www.meteo.waw.pl";

	private class DialogControllerBase {
		@FXML
		JFXDialog dialog;

		@FXML
		Button acceptButton;

		@FXML
		Button cancelButton;

		void initialize() {
			JavaFxObservable.actionEventsOf(cancelButton).subscribe(ignore -> {
				dialog.close();
			});
		}

		void show(StackPane pane) {
			dialog.show(pane);
		}

	}

	private class CloseDialogController extends DialogControllerBase {
		@FXML
		void initialize() {
			JavaFxObservable.actionEventsOf(acceptButton).subscribe(ignore -> {
				log.info("Exitting");
				AppMain.this.mainStage.close(); // This should terminate the
				// application
				System.exit(0); // Just for sure
			});

			JavaFxObservable.actionEventsOf(cancelButton).subscribe(ignore -> {
				dialog.close();
			});
		}

		void show(StackPane pane) {
			dialog.show(pane);
		}
	}

	private class SettingsDialogController extends DialogControllerBase {
		@FXML
		ChoiceBox<String> sourceBox;

		@FXML
		JFXTextField intervalField;

		@FXML
		void initialize() {
			super.initialize();

			sourceBox.getItems().addAll(SOURCE_OPEN, SOURCE_METEO);

			sourceBox.setValue(SOURCE_OPEN);

			intervalField.textProperty().addListener((control, newValue, oldValue) -> intervalField.validate());
			acceptButton.disableProperty().bind(intervalField.getValidators().get(0).hasErrorsProperty());

			JavaFxObservable.actionEventsOf(acceptButton).subscribe(ignore -> {
				try {
					int interval = Integer.parseInt(intervalField.getText());

					AppMain.this.sourceStreams.stream().forEach(Subscription::unsubscribe);
					AppMain.this.sourceStreams.clear();

					if (sourceBox.getValue().equals(SOURCE_OPEN)) {
						AppMain.this.setupDataSourcesFromOpenWeather(interval);
						log.info("Weather source has been changed to " + SOURCE_OPEN);
					}
					else if (sourceBox.getValue().equals(SOURCE_METEO)) {
						AppMain.this.setupDataSourcesFromMeteoWaw(interval);
						log.info("Weather source has been changed to " + SOURCE_METEO);
					}
				} finally {
					dialog.close();
				}
			});
		}
	}
	private CloseDialogController closeDialogController;
	private DialogControllerBase settingsDialogController;
	private Stage mainStage;

	private List<Subscription> sourceStreams = new LinkedList<>();

	@Override
	public void start(Stage primaryStage) throws Exception {
		log.info("Starting SWeather application...");

		mainStage = primaryStage;

		setupTooltipDuration();

		setupDataSourcesFromOpenWeather(null);

		setupEventHandler();

		Parent pane = FXMLLoader.load(AppMain.class.getResource(FXML_MAIN_FORM_TEMPLATE));
		/*
		 * Transform the main stage (aka the main window) into an undecorated
		 * window
		 */

		primaryStage.setTitle("SWeather");

		JFXDecorator decorator = new JFXDecorator(mainStage, pane, false, false, true);
		ObservableList<Node> buttonsList = ((Pane) decorator.getChildren().get(0)).getChildren();
		buttonsList.get(buttonsList.size() - 1).getStyleClass().add("close-button"); // Style
		// the
		// close
		// button
		// differently

		decorator.setOnCloseButtonAction(this::onClose);

		Scene scene = new Scene(decorator);
		scene.setFill(null);

		scene.getStylesheets().addAll(AppMain.class.getResource(FONT_CSS).toExternalForm(),
				AppMain.class.getResource(MATERIAL_CSS).toExternalForm(),
				AppMain.class.getResource(JFX_CSS).toExternalForm());

		mainStage.setScene(scene);

		mainStage.setWidth(500);
		mainStage.setHeight(300);
		mainStage.setResizable(false);

		addLogo();

		mainStage.show();

		log.info("Application's up and running!");
	}

	private void addLogo() {
		mainStage.getIcons().add(new Image(AppMain.class.getResourceAsStream(logo)));
	}

	private void onClose() {
        log.info("onClose");

        if (closeDialogController == null) {
            closeDialogController = new CloseDialogController();
            FXMLLoader loader = new FXMLLoader(AppMain.class.getResource(FXML_CLOSE_DIALOG_TEMPLATE));
            loader.setController(closeDialogController);
            try {
                loader.load();
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }

        closeDialogController.show(getMainPane());
    }


	/*private void onClose() {
		log.info("onClose");

		if (closeDialogController == null) {
			closeDialogController = new CloseDialogController();
			createDialog(closeDialogController, FXML_CLOSE_DIALOG_TEMPLATE);
		}

		closeDialogController.show(getMainPane());
	}*/

	private StackPane getMainPane() {
		return (StackPane) mainStage.getScene().getRoot().lookup("#main");
	}

	private void createDialog(Object dialogController, String fxmlPath) {
		FXMLLoader loader = new FXMLLoader(AppMain.class.getResource(fxmlPath));
		loader.setController(dialogController);
		try {
			loader.load();
		} catch (IOException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	private void setupDataSourcesFromOpenWeather(Integer pollInterval) {
		DataSource[] sources = { new OpenWeatherDataSource(),
				new AtmosphereDataSource() };
		for (DataSource source : sources) {
			sourceStreams.add(joinStream(source.dataSourceStream(pollInterval)));
		}
	}

	private void setupDataSourcesFromMeteoWaw(Integer pollInterval){
		DataSource[] sources = { new MeteoWawDataSource(),
				new AtmosphereDataSource() };
		for (DataSource source : sources) {
			sourceStreams.add(joinStream(source.dataSourceStream(pollInterval)));
		}
	}

	private void onSettingsRequested(){
		log.info("onSettingsRequested");

		if (settingsDialogController == null) {
			settingsDialogController = new SettingsDialogController();
			createDialog(settingsDialogController, FXML_SETTINGS_DIALOG_TEMPLATE);
		}

		settingsDialogController.show(getMainPane());
	}


	private void setupEventHandler() {
		Observable<SWeatherEvent> events = eventStream().events();

		events.ofType(WeatherDataEvent.class).subscribe(log::info);
		events.ofType(AtmosphereDataEvent.class).subscribe(log::info);

		events.ofType(SettingsRequestEvent.class).subscribe(e -> onSettingsRequested());
	}

	private static void setupExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(
				(t, e) -> log.error("Uncaught exception in thread \'" + t.getName() + "\'", e));
	}
	private static void setupTooltipDuration() {
		TooltipProlongHelper.setTooltipDuration(Duration.millis(500), Duration.minutes(10), Duration.millis(500));
	}

	private static void setupTextRendering() {
		/*
		 * Workaround for font rendering issue.
		 * Consult: @link{https://stackoverflow.com/questions/18382969/can-the-
		 * rendering-of-the-javafx-2-8-font-be-improved} and linked materials
		 */
		System.setProperty("prism.text", "t2k");
		System.setProperty("prism.lcdtext", "true");
	}

	public static void main(String[] args) {
		setupExceptionHandler();

		setupTextRendering();

		Platform.setImplicitExit(true); // This should exit the application when
		// the main window gets closed
		Application.launch(AppMain.class, args);
	}
}