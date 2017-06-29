package pl.edu.mimuw.sweather.network;

import io.reactivex.netty.RxNetty;
import pl.edu.mimuw.sweather.event.WeatherDataEvent;
import rx.Observable;

public class OpenWeatherDataSource extends DataSource {
    private static final String URL = "http://api.openweathermap.org/data/2.5/weather?q=Warsaw,pl&units=metric&APPID=9ca3e0d6ae1c93f3a245913b58304b15";

    private static final String MAIN_JSON_KEY = "main";
    private static final String TEMP_JSON_KEY = "temp";
    private static final String HUMIDITY_JSON_KEY = "humidity";
    private static final String PRESSURE_JSON_KEY = "pressure";

    private static final String WIND_JSON_KEY = "wind";
    private static final String SPEED_JSON_KEY = "speed";
    private static final String DEG_JSON_KEY = "deg";

    private static final String CLOUDS_JSON_KEY = "clouds";
    private static final String ALL_JSON_KEY = "all";

    public OpenWeatherDataSource() {}

    @Override
    protected Observable<WeatherDataEvent> makeRequest() {

        return RxNetty
                .createHttpRequest(JsonHelper.withJsonHeader(prepareHttpGETRequest(URL)))
                .compose(this::unpackResponse)
                .map(JsonHelper::asJsonObject)
                .map(jsonObject -> new WeatherDataEvent(
                            jsonObject.get(MAIN_JSON_KEY).getAsJsonObject().get(TEMP_JSON_KEY).getAsFloat(),
                            jsonObject.get(MAIN_JSON_KEY).getAsJsonObject().get(PRESSURE_JSON_KEY).getAsFloat(),
                            jsonObject.get(MAIN_JSON_KEY).getAsJsonObject().get(HUMIDITY_JSON_KEY).getAsFloat(),
                            jsonObject.get(WIND_JSON_KEY).getAsJsonObject().get(SPEED_JSON_KEY).getAsFloat(),
                            jsonObject.get(WIND_JSON_KEY).getAsJsonObject().get(DEG_JSON_KEY).getAsFloat(),
                            jsonObject.get(CLOUDS_JSON_KEY).getAsJsonObject().get(ALL_JSON_KEY).getAsFloat()
                        )
                );
    }
}