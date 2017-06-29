package pl.edu.mimuw.sweather.controller;

import static pl.edu.mimuw.sweather.event.EventStream.binding;
import static pl.edu.mimuw.sweather.event.EventStream.eventStream;
import static pl.edu.mimuw.sweather.event.EventStream.joinStream;
import static pl.edu.mimuw.sweather.event.EventStream.onEvent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import pl.edu.mimuw.sweather.control.WeatherTooltip;
import pl.edu.mimuw.sweather.control.WeatherValueControl;
import pl.edu.mimuw.sweather.event.*;
import pl.edu.mimuw.sweather.event.SWeatherEvent;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.JavaFxObservable;
import rx.schedulers.JavaFxScheduler;

public class SWeatherAppController {
	private static final int ERROR_MSG_MAX_LENGTH = 400;
	private static final int ERROR_MSG_DURATION = 30; // Show error icon for 30
	// seconds

	@FXML
	private WeatherValueControl tempControl;

	@FXML
	private WeatherValueControl pressureControl;

	@FXML
	private WeatherValueControl humidityControl;

	@FXML
	private WeatherValueControl speedControl;

	@FXML
	private WeatherValueControl degControl;

	@FXML
	private WeatherValueControl cloudsControl;

	@FXML
	private WeatherValueControl PM25Control;

	@FXML
	private WeatherValueControl PM10Control;


	@FXML
	private Node errorIcon;

	@FXML
	private Node workingIcon;

	@FXML
	private Button refreshButton;

	@FXML
	private Button settingsButton;

	@FXML
	private Text timeControl;

	@FXML
	private void initialize() {
		initializeStatus();

		initalizeRefreshHandler();
		initializeSettingsHandler();

		initializeTooltips();

	}

	public Observable<RawDataEvent> getTempValue() {
		return getWeatherStatsStream(WeatherDataEvent::getTemp);
	}
	public Observable<RawDataEvent> getPressureValue() {
		return getWeatherStatsStream(WeatherDataEvent::getPressure);
	}
	public Observable<RawDataEvent> getHumidityValue() {
		return getWeatherStatsStream(WeatherDataEvent::getHumidity);
	}
	public Observable<RawDataEvent> getSpeedValue() {
		return getWeatherStatsStream(WeatherDataEvent::getSpeed);
	}

	public Observable<RawDataEvent> getDegValue() {
		return getDegStream(WeatherDataEvent::getDeg);
	}

	public Observable<RawDataEvent> getCloudinessValue() {
		return getWeatherStatsStream(WeatherDataEvent::getClouds);
	}



	public Observable<RawDataEvent> getPM25Value() {
		return getPMStatsStream(AtmosphereDataEvent::getPM25);
	}
	public Observable<RawDataEvent> getPM10Value() {
		return getPMStatsStream(AtmosphereDataEvent::getPM10);
	}

	private void initalizeRefreshHandler() {
		joinStream(JavaFxObservable.actionEventsOf(refreshButton).map(e -> new RefreshRequestEvent()));
	}
	private void initializeSettingsHandler() {
		joinStream(JavaFxObservable.actionEventsOf(settingsButton).map(e -> new SettingsRequestEvent()));
	}

	private void initializeStatus() {
		Observable<SWeatherEvent> events = eventStream().eventsInFx();

		// Basically, we keep track of the difference between issued requests
		// and completed requests
		// If this difference is > 0 we display the spinning icon...
		workingIcon.visibleProperty()
				.bind(binding(events.ofType(NetworkRequestIssuedEvent.class).map(e -> 1) // Every
						// issued
						// request
						// contributes
						// +1
						.mergeWith(events.ofType(NetworkRequestFinishedEvent.class).map(e -> -1) // Every
								// completed
								// request
								// contributes
								// -1
								.delay(2, TimeUnit.SECONDS, JavaFxScheduler.getInstance())) // We delay
						// completion
						// events for 2
						// seconds so
						// that the
						// spinning icon
						// is always
						// displayed for
						// at least 2
						// seconds and
						// it does not
						// blink
						.scan(0, (x, y) -> x + y).map(v -> v > 0))

				);

		/*
		 * This should show the error icon when an error event arrives and hides
		 * the icon after 30 seconds unless another error arrives
		 */
		Observable<ErrorEvent> errors = events.ofType(ErrorEvent.class);
		errorIcon.visibleProperty()
				.bind(onEvent(errors, true).andOn(
						errors.throttleWithTimeout(ERROR_MSG_DURATION, TimeUnit.SECONDS, JavaFxScheduler.getInstance()),
						false).toBinding());
	}

	private void initializeTooltips() {
		Tooltip.install(workingIcon, new Tooltip("Fetching data..."));

		Tooltip errorTooltip = new Tooltip();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		eventStream().eventsInFx().ofType(ErrorEvent.class).subscribe(e -> {
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			e.getCause().printStackTrace(new PrintStream(ostream));
			String details = new String(ostream.toByteArray());
			if (details.length() > ERROR_MSG_MAX_LENGTH) {
				details = details.substring(0, ERROR_MSG_MAX_LENGTH) + "\u2026"; // Add
				// ellipsis
				// (...)
				// at
				// the
				// end
			}

			errorTooltip.setText(MessageFormat.format("An error has occurred ({0}):\n{1}",
					e.getTimestamp().format(formatter), details));
		});
		Tooltip.install(errorIcon, errorTooltip);

		eventStream().eventsInFx().ofType(WeatherDataEvent.class).subscribe(e -> {
			timeControl.setText(e.getTimestamp().format(formatter));
		});

		WeatherValueControl[] weatherValueControls = {tempControl, pressureControl,  humidityControl, speedControl,
				degControl, PM25Control, PM10Control, cloudsControl};
		for (WeatherValueControl control : weatherValueControls) {
			Tooltip tooltipPopup = new Tooltip();
			WeatherTooltip tooltipContent = new WeatherTooltip(control.getSource());

			tooltipPopup.setGraphic(tooltipContent);

			Tooltip.install(control, tooltipPopup);
		}
	}

	private Observable<RawDataEvent> getWeatherStatsStream(Func1<WeatherDataEvent, Float> extractor) {
		return eventStream().eventsInFx().ofType(WeatherDataEvent.class)
				.map(e -> new RawDataEvent(e.getTimestamp(), extractor.call(e),false));
	}

	private Observable<RawDataEvent> getPMStatsStream(Func1<AtmosphereDataEvent, Float> extractor) {
		return eventStream().eventsInFx().ofType(AtmosphereDataEvent.class)
				.map(e -> new RawDataEvent(e.getTimestamp(), extractor.call(e), false));
	}

    private Observable<RawDataEvent> getDegStream(Func1<WeatherDataEvent, Float> extractor) {
        return eventStream().eventsInFx().ofType(WeatherDataEvent.class)
                .map(e -> new RawDataEvent(e.getTimestamp(), extractor.call(e), true));
    }
}