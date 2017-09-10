package com.vccare.mananwason.vcare;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    public static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    public static final String GOOGLE_BROWSER_API_KEY =
            "AIzaSyA4Ob_S6rGNp3zGlsjpFdmC5JU6pYzHqcY";
    public static final String RESULTS = "results";
    public static final String STATUS = "status";

    public static final String OK = "OK";
    public static final String ZERO_RESULTS = "ZERO_RESULTS";
    public static final String REQUEST_DENIED = "REQUEST_DENIED";
    public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";

    //    Key for nearby places json from google
    public static final String GEOMETRY = "geometry";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";
    public static final String ICON = "icon";
    public static final String SUPERMARKET_ID = "id";
    public static final String NAME = "name";
    public static final String PLACE_ID = "place_id";
    public static final String REFERENCE = "reference";
    public static final String VICINITY = "vicinity";
    public static final String PLACE_NAME = "place_name";

    public static final int PROXIMITY_RADIUS = 5000;

    LocationManager locationManager;
    CoordinatorLayout mainCoordinatorLayout;
    public InputStreamReader insr;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();
    private TextView textView;
    private Boolean isDonation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isGooglePlayServicesAvailable()) {
            return;
        }

        setContentView(R.layout.activity_maps);
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_funds:
                        isDonation = false;
                        return true;
                    case R.id.navigation_donate:
                        isDonation = true;
                        Log.d(TAG,isDonation+"");
                        return true;
                }
                return true;
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mainCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.mainCoordinatorLayout);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showLocationSettings();
        }
    }

    private void showLocationSettings() {
        Snackbar snackbar = Snackbar
                .make(mainCoordinatorLayout, "Location Error: GPS Disabled!",
                        Snackbar.LENGTH_LONG)
                .setAction("Enable", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView
                .findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        snackbar.show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(isDonation){
                    Intent intent = new Intent(MapsActivity.this, MakeDonationActivity.class);
                    intent.putExtra("HOSP", marker.getTitle().split(":")[0]);
                    startActivity(intent);
                }
                return false;
            }
        });

        showCurrentLocation();
    }

//    public void requestPerms(){
//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission},
//                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//
//        }

    private void showCurrentLocation() {
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(bestProvider);

        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
    }


    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        try {
            loadNearByPlaces(latitude, longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadNearByPlaces(final double latitude, final double longitude) throws Exception {
        final HttpURLConnection con;
        String type = "hospital";
        StringBuilder googlePlacesUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlacesUrl.append("&radius=").append(PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=").append(type);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + GOOGLE_BROWSER_API_KEY);
        final String places = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=39.329901,-76.620518&radius=5000&types=hospital&sensor=true&key=AIzaSyCR4qh6Wv-3669m2qPtrjYjqtLR0xDVoxw";

//        JsonObjectRequest request = new JsonObjectRequest(places,
//                new com.android.volley.Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject result) {
//
//                        Log.i(TAG, "onResponse: Result= " + result.toString());
//                        parseLocationResult(result);
//                    }
//                },
//                new com.android.volley.Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e(TAG, "onErrorResponse: Error= " + error);
//                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
//                    }
//                });
//        URL obj = new URL(places);
//        con = (HttpURLConnection) obj.openConnection();
//
//        // optional default is GET
//        con.setRequestMethod("GET");
//
//        //add request header
//        con.setRequestProperty("User-Agent", "Mozilla/5.0");
//
//        CommonTaskLoop.getInstance().post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//
//
//                    int responseCode = con.getResponseCode();
//                    System.out.println("\nSending 'GET' request to URL : " + places);
//                    System.out.println("Response Code : " + responseCode);
//                    insr = new InputStreamReader(con.getInputStream());
//                } catch (Exception e) {
//                    System.out.println("e = " + e);
//                }
//
//
//            }
//        });
//        BufferedReader in = new BufferedReader(insr);

//        String inputLine;
//
//        StringBuffer response = new StringBuffer();
//
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();
//        JSONArray array = new JSONArray(response.toString());
//        Log.d(TAG, "PRINT" + response.toString());

//        JSONObject jsonObj  = array.getJSONObject(1);
//        try {
//            JSONObject obj = new JSONObject(loadJSONFromAsset());
//            JSONArray m_jArry = obj.getJSONArray("results");
//            ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
//            HashMap<String, String> m_li;
//
//            for (int i = 0; i < m_jArry.length(); i++) {
//                JSONObject jo_inside = m_jArry.getJSONObject(i);
//                Log.d("Details-->", jo_inside.getString("formule"));
//                String formula_value = jo_inside.getString("formule");
//                String url_value = jo_inside.getString("url");
//
//                //Add your values in your `ArrayList` as below:
//                m_li = new HashMap<String, String>();
//                m_li.put("formule", formula_value);
//                m_li.put("url", url_value);
//
//                formList.add(m_li);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        parseLocationResult(loadJSONFromAsset());
        //print result


//        AppController.getInstance().addToRequestQueue(request);
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("nearby.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    private void parseLocationResult(String result) {
        Log.d("ERER", "PARSE");
        try {

            String id, place_id, placeName = null, reference, icon, vicinity = null;
            double latitude, longitude;
            JSONObject jObject = new JSONObject(result.trim());
            Iterator<?> keys = jObject.keys();
            while (keys.hasNext()) {

                String key = (String) keys.next();
                if (Objects.equals(key, "results")) {
                    // do what ever you want with the JSONObject.....
                    mMap.clear();
                    JSONArray jsonArray = jObject.getJSONArray(key);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject place = jsonArray.getJSONObject(i);
                        id = place.getString(SUPERMARKET_ID);
                        place_id = place.getString(PLACE_ID);
                        if (!place.isNull(NAME)) {
                            placeName = place.getString(NAME);
                        }
                        if (!place.isNull(VICINITY)) {
                            vicinity = place.getString(VICINITY);
                        }
                        latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                .getDouble(LATITUDE);
                        longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                .getDouble(LONGITUDE);
                        reference = place.getString(REFERENCE);
                        icon = place.getString(ICON);
                        final MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(latitude, longitude);
                        markerOptions.position(latLng);
                        final Query fundQuery = ref.child("Funds").child(placeName.replace(".", ""));
                        final DataSnapshot fundsDataSnapshot = null;
                        final StringBuffer buffer = new StringBuffer();
                        buffer.append(placeName).append(" : ");
                        fundQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    buffer.append(dataSnapshot.getValue());
                                } else {
                                    buffer.append("No Funds Available");
                                }
                                markerOptions.title(buffer.toString());
                                mMap.addMarker(markerOptions);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d("SD", "D1S");

                            }
                        });
                        Log.d("SD", "DS");
                        Log.d(TAG, buffer.toString());

//                }

                    }
                }


//            if (result.getString(STATUS).equalsIgnoreCase(OK)) {
//
//                mMap.clear();
//                Log.d(TAG, "HERE 1" + " " + jsonArray.length() );
//
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    JSONObject place = jsonArray.getJSONObject(i);
//                    Log.d(TAG, place.toString());
//                    id = place.getString(SUPERMARKET_ID);
//                    place_id = place.getString(PLACE_ID);
//                    if (!place.isNull(NAME)) {
//                        placeName = place.getString(NAME);
//                    }
//                    if (!place.isNull(VICINITY)) {
//                        vicinity = place.getString(VICINITY);
//                    }
//                    latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
//                            .getDouble(LATITUDE);
//                    longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
//                            .getDouble(LONGITUDE);
//                    reference = place.getString(REFERENCE);
//                    icon = place.getString(ICON);
//
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    LatLng latLng = new LatLng(latitude, longitude);
//                    markerOptions.position(latLng);
//                    markerOptions.title(placeName + " : " + vicinity);
//
//                    mMap.addMarker(markerOptions);
//                }
//
//                Toast.makeText(getBaseContext(), jsonArray.length() + " Hospitals found!",
//                        Toast.LENGTH_LONG).show();
//            } else if (result.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS)) {
//                Toast.makeText(getBaseContext(), "No Hospitals found in 5KM radius!!!",
//                        Toast.LENGTH_LONG).show();
//            }
//
            }
        } catch (JSONException e) {

            e.printStackTrace();
            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
        }
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}


