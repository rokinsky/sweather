package pl.edu.mimuw.sweather.control;

import java.text.DecimalFormat;

import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.weathericons.WeatherIcons;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.kordamp.ikonli.weathericons.WeatherIconsIkonHandler;
import pl.edu.mimuw.sweather.event.RawDataEvent;
import rx.Observable;

public class WeatherValueControl extends Pane {

	private FontIcon noDataIcon = new FontIcon(WeatherIcons.NA);

	private HBox innerContainer;

	private Text suffixLabel;
	private Text textControl;

	private FontIcon upIcon;
	private FontIcon upRightIcon;
	private FontIcon rightIcon;
	private FontIcon downRightIcon;
	private FontIcon downIcon;
	private FontIcon downLeftIcon;
	private FontIcon leftIcon;
	private FontIcon upLeftIcon;

	private FontIcon currentIcon;


	private ObjectProperty<Observable<RawDataEvent>> sourceProperty = new SimpleObjectProperty<>();

	private String formatPattern = "0.000";
	private DecimalFormat format = new DecimalFormat(formatPattern);

	private StringProperty suffixProperty = new SimpleStringProperty("");
	private StringProperty titleProperty = new SimpleStringProperty("-");

	public Observable<RawDataEvent> getSource() {
		return sourceProperty.get();
	}

	public void setSource(Observable<RawDataEvent> source) {
		source.subscribe(e -> {
			if (innerContainer == null) createContentControls();

			if (new Float(e.getValue()).isNaN()) textControl.setText("-");
			else if (e.isDeg()) setDegIcon(degToIcon((int)e.getValue()));
			else textControl.setText(format.format(e.getValue()));
		});

		sourceProperty.set(source);
	}

	private FontIcon degToIcon(int deg) {
		switch ((deg % 360) / (360 / 8)) {
			case 0:
				return upIcon;
			case 1:
				return upRightIcon;
			case 2:
				return rightIcon;
			case 3:
				return downRightIcon;
			case 4:
				return downIcon;
			case 5:
				return downLeftIcon;
			case 6:
				return leftIcon;
			case 7:
				return upLeftIcon;
		}
		return currentIcon;
	}

	private void setDegIcon(FontIcon icon) {
		if (currentIcon == icon) {
			return;
		}
		if (currentIcon != null) {
			removeTrendIcon();
		}
		currentIcon = icon;
		if (currentIcon != null) {
			addTrendIcon(currentIcon);
		}
	}

	private void removeTrendIcon() {
		getChildren().remove(currentIcon);
	}

	private void addTrendIcon(Node icon) {
		getChildren().add(icon);
	}


	public String getFormat() {
		return formatPattern;
	}

	public void setFormat(String pattern) {
		formatPattern = pattern;
		format = new DecimalFormat(pattern);
	}

	public String getSuffix() {
		return suffixProperty.get();
	}

	public void setSuffix(String sufix) {
		suffixProperty.set(sufix);
	}

	public String getTitle() {
		return titleProperty.get();
	}

	public void setTitle(String title) {
		titleProperty.set(title);
	}

	public WeatherValueControl() {
		noDataIcon.getStyleClass().add("no-data");
		getChildren().add(noDataIcon);
	}

	private void createContentControls() {
		getChildren().remove(noDataIcon);

		textControl = new Text();
		textControl.getStyleClass().add("weather-value");

		suffixLabel = new Text();
		suffixLabel.textProperty().bind(suffixProperty);
		suffixLabel.getStyleClass().add("helper-label");

		innerContainer = new HBox();
		innerContainer.getStyleClass().add("value-container");
		innerContainer.getChildren().addAll(textControl, suffixLabel);

		getChildren().add(innerContainer);

		upIcon = new FontIcon(WeatherIcons.DIRECTION_UP);
		upRightIcon = new FontIcon(WeatherIcons.DIRECTION_UP_RIGHT);
		rightIcon = new FontIcon(WeatherIcons.DIRECTION_RIGHT);
		downRightIcon = new FontIcon(WeatherIcons.DIRECTION_DOWN_RIGHT);
		downIcon = new FontIcon(WeatherIcons.DIRECTION_DOWN);
		downLeftIcon = new FontIcon(WeatherIcons.DIRECTION_DOWN_LEFT);
		leftIcon = new FontIcon(WeatherIcons.DIRECTION_LEFT);
		upLeftIcon = new FontIcon(WeatherIcons.DIRECTION_UP_LEFT);
	}

	@Override
	protected void layoutChildren() {
		/* Custom children positioning */
		super.layoutChildren();

		if (noDataIcon.isVisible()) {
			noDataIcon.relocate((getWidth() - noDataIcon.getLayoutBounds().getWidth()) / 2,
					(getHeight() - noDataIcon.getLayoutBounds().getHeight()) / 2);
		}

		if (innerContainer != null) {
			innerContainer.relocate((getWidth() - innerContainer.getLayoutBounds().getWidth()) / 2,
					(getHeight() - innerContainer.getLayoutBounds().getHeight()) / 2);
		}

		if (currentIcon != null) {
			currentIcon.relocate((getWidth() - innerContainer.getLayoutBounds().getWidth()) / 2 - 5,
					(getHeight() - innerContainer.getLayoutBounds().getHeight())/ 2 - 15);
			currentIcon.getStyleClass().add("weather-icon");
		}
	}
}