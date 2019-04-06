package sbts.dmw.com.sbtrackingsystem.fragments;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import sbts.dmw.com.sbtrackingsystem.R;
import sbts.dmw.com.sbtrackingsystem.classes.SingletonClass;
import sbts.dmw.com.sbtrackingsystem.model.DirectionsJSONParser;

public class map extends Fragment implements OnMapReadyCallback {
    GoogleMap gMap;
    MarkerOptions att;
    Boolean once = true;
    String Bus_No;
    String requrl;

    private Handler handler = new Handler();
    String[] str;
    SharedPreferences sharedPreferences;
    Marker marker;

    public map() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sharedPreferences = getActivity().getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.maptype_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.hybrid: {
                gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            }
            case R.id.normal: {
                gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            }
            case R.id.terrain: {
                gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            }
            case R.id.satellite: {
                gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            }
            case R.id.bus: {
                LatLng pos = marker.getPosition();
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
                return true;
            }
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map2, container, false);
        getActivity().setTitle("Map");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bus_No = sharedPreferences.getString("Bus_No", null);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onStart() {
        super.onStart();
        attloc.run();
    }


    private Runnable attloc = new Runnable() {
        @Override
        public void run() {

            String url = "https://sbts2019.000webhostapp.com/locationin.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            str = Pattern.compile(",").split(response);
                            att = new MarkerOptions().position(new LatLng(Double.valueOf(str[0]), Double.valueOf(str[1]))).title("Bus");
                            att.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_station_location__48));
                            marker = gMap.addMarker(att);

                            if (once) {
                                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(str[0]), Double.valueOf(str[1])), 17));
                                once = false;
                                if(sharedPreferences.getString("ROLE", "Attendee").equals("Parent")){
                                    gMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(sharedPreferences.getString("Latitude",null)),Double.valueOf(sharedPreferences.getString("Longitude",null)))).title("Pick-up spot"));
                                    requrl = getRequestURL(new LatLng(Double.valueOf(str[0]), Double.valueOf(str[1]))
                                            , new LatLng(Double.valueOf(sharedPreferences.getString("Latitude",null)),Double.valueOf(sharedPreferences.getString("Longitude",null))));
                                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                                    taskRequestDirections.execute(requrl);
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getActivity().getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Bus_no", Bus_No);
                    return params;
                }
            };
            SingletonClass.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);

            handler.postDelayed(this, 1000);


        }
    };

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(attloc);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setTrafficEnabled(false);
        gMap.setBuildingsEnabled(true);
        gMap.setIndoorEnabled(true);
        if (sharedPreferences.getString("ROLE", "Attendee").equals("Parent")) {
            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            gMap.setMyLocationEnabled(true);
        }
        gMap.getUiSettings().setZoomControlsEnabled(true);
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException ignored) {
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsJSONParser directionsJSONParser = new DirectionsJSONParser();
                routes = directionsJSONParser.parse(jsonObject);
            } catch (JSONException ignored) {
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            ArrayList points = null;
            PolylineOptions polylineOptions = null;
            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    points.add(new LatLng(lat, lng));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions != null) {
                gMap.addPolyline(polylineOptions);
            }
        }
    }

    private String getRequestURL(LatLng origin, LatLng destination) {
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;
        String str_des = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String param = str_org + "&" + str_des + "&" + sensor + "&" + mode;
        String out = "json";
        String key = "AIzaSyB7yTGSqwekPbk1CyjU1pwnmovegdndE5c";
        //String deummy = "https://maps.googleapis.com/maps/api/directions/json?origin=Disneyland&destination=Universal+Studios+Hollywood&key=YOUR_API_KEY";
        String url = "https://maps.googleapis.com/maps/api/directions/" + out + "?" + param + "&key=" + key;
        return url;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStream.close();
        } catch (IOException ignored) {
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

}
