package pl.edu.mimuw.sweather.network;

import com.google.gson.JsonObject;
import io.reactivex.netty.RxNetty;
import pl.edu.mimuw.sweather.event.AtmosphereDataEvent;
import rx.Observable;
import rx.exceptions.Exceptions;

public class AtmosphereDataSource extends DataSource {
    private static final String URL = "http://powietrze.gios.gov.pl/pjp/current/getAQIDetailsList?param=AQI";

    private static final String STATION_JSON_KEY = "stationId";
    private static final String ID_JSON_KEY = "530"; // Warszawa, al. Niepodległości 227/233
    private static final String VALUES_JSON_KEY = "values";

    private static final String PM25_JSON_KEY = "PM2.5";
    private static final String PM10_JSON_KEY = "PM10";

    private class AtmosphereDataNotFoundException extends Exception {
        private static final long serialVersionUID = 1L;
    };

    @Override
    protected Observable<AtmosphereDataEvent> makeRequest() {

        return RxNetty.createHttpRequest(JsonHelper.withJsonHeader(prepareHttpGETRequest(URL)))
                .compose(this::unpackResponse).map(JsonHelper::asJsonArray).map(jsonArray -> {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                        if (jsonObject.get(STATION_JSON_KEY).getAsString().equals(ID_JSON_KEY)) {
                            JsonObject values = jsonObject.get(VALUES_JSON_KEY).getAsJsonObject();
                            return new AtmosphereDataEvent(values.get(PM25_JSON_KEY).getAsFloat(),
                                    values.get(PM10_JSON_KEY).getAsFloat());
                        }
                    }
                    throw Exceptions.propagate(new AtmosphereDataNotFoundException());
                });
    }
}
