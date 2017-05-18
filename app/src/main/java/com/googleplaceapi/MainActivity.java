package com.googleplaceapi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private AutoCompleteTextView mAutocompleteView;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    private GoogleAddress googleAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        googleAddress = new GoogleAddress();

        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
        // functionality, which automatically sets up the API client to handle Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        // Retrieve the AutoCompleteTextView that will display Place suggestions.
        mAutocompleteView = (AutoCompleteTextView) findViewById(R.id.autocomplete_places);

        // Register a listener that receives callbacks when a suggestion has been selected
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mAutocompleteView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mAutocompleteView.setSelection(0);
            }
        });

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
        mAutocompleteView.setAdapter(mAdapter);
    }

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see GeoDataApi#getPlaceById(GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            googleAddress.setPlaceId(placeId);
            final CharSequence primaryText = item.getPrimaryText(null);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            //Toast.makeText(getApplicationContext(), "Clicked: " + primaryText, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                places.release();
                return;
            }

            final Place place = places.get(0);
            Log.d("PlaceId:", String.valueOf(place.getPlaceTypes().get(0)));

            //TODO NOT A GOOD IDEA TO GET STREET THIS WAY
            //TODO THINK ABOUT IT
            final String[] addressStr = String.valueOf(place.getAddress()).split(", ");
            if (place.getPlaceTypes().get(0) == 1020) {
                mAutocompleteView.setText(addressStr[0]);
                mAutocompleteView.setSelection(addressStr[0].length());
                mAutocompleteView.setError("Your address misses building number");
            } else if (place.getPlaceTypes().get(0) == 1021) {
                // Get the Place object from the buffer.
                String path = "https://maps.googleapis.com/maps/api/place/details/json?placeid="
                        + googleAddress.getPlaceId() + "&key=" + getString(R.string.google_maps_key)
                        + "&language=en&type=street_address";
                OkHttpProvider.getInstance().getForGoogleDetails(path, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        final String jsonResponse = response.body().string();
                        try {
                            JSONObject jsonObj = new JSONObject(jsonResponse);
                            JSONObject resultJsonObject = jsonObj.getJSONObject("result");
                            String name = resultJsonObject.getString("name");
                            String placeId = resultJsonObject.getString("place_id");
                            String description = resultJsonObject.getString("formatted_address");
                            JSONObject geometryJsonArray = resultJsonObject.getJSONObject("geometry");
                            JSONObject location = geometryJsonArray.getJSONObject("location");
                            String latitude = location.getString("lat");
                            String longitude = location.getString("lng");
                            JSONArray addressComponents = resultJsonObject.getJSONArray("address_components");
                            String city = "";
                            String house = "";
                            String street = "";
                            for (int i = 0; i < addressComponents.length(); i++) {
                                JSONObject jsonObject = addressComponents.getJSONObject(i);
                                String longName = jsonObject.getString("long_name");
                                JSONArray types = jsonObject.getJSONArray("types");
                                String Type = types.getString(0);
                                if (Type.equalsIgnoreCase("street_number")) {
                                    house = longName;
                                } else if (Type.equalsIgnoreCase("route")) {
                                    street = longName;
                                } else if (Type.equalsIgnoreCase("locality")) {
                                    city = longName;
                                }
                            }

                            Log.d("GoogleAddress", "GoogleAddress with correct PlaceId:" + name + ", " + placeId + ", " + description + ", " + latitude + ", " + longitude);
                            googleAddress.setPlaceId(placeId);
                            googleAddress.setDescription(description);
                            googleAddress.setLatitude(latitude);
                            googleAddress.setLongitude(longitude);
                            googleAddress.setHouse(house);
                            googleAddress.setStreet(street);
                            googleAddress.setCity(city);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

            } else {
                mAutocompleteView.setText("");
                mAutocompleteView.setError("Your address must contain street and building number");
            }
            places.release();
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }
}
