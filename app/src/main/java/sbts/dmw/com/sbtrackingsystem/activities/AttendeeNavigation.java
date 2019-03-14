package sbts.dmw.com.sbtrackingsystem.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import sbts.dmw.com.sbtrackingsystem.R;
import sbts.dmw.com.sbtrackingsystem.classes.SessionManager;
import sbts.dmw.com.sbtrackingsystem.classes.SingletonClass;
import sbts.dmw.com.sbtrackingsystem.fragments.AttendeeHome;
import sbts.dmw.com.sbtrackingsystem.fragments.StudentList;
import sbts.dmw.com.sbtrackingsystem.fragments.changepassword;
import sbts.dmw.com.sbtrackingsystem.fragments.map;

public class AttendeeNavigation extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    SessionManager sessionManager;
    Toolbar toolbar;
    NavigationView navigationView;

    ImageView imageView;
    String url;

    TextView name, email;
    StringRequest stringRequest;
    Bitmap bitmap;
    View header_view;
    private String User;
    final int PICK_CODE = 1;
    String[] str;
    Bundle bundle;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee_navigation);
        sharedPreferences = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        navigationView = findViewById(R.id.attendeeNavigationView);
        header_view = navigationView.getHeaderView(0);
        imageView = header_view.findViewById(R.id.menu_photo);
        name = header_view.findViewById(R.id.menu_name);
        email = header_view.findViewById(R.id.menu_email);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_CODE);

            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        sessionManager = new SessionManager(this);
        bundle = new Bundle();
        HashMap<String, String> user = sessionManager.getUserDetails();
        User = user.get(SessionManager.USERNAME);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        drawerLayout = findViewById(R.id.attendeeDrawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());


        if (savedInstanceState == null) {
            String url = "https://sbts2019.000webhostapp.com/studentList.php";
            bundle.putString("url", url);
            StudentList studentList = new StudentList();
            studentList.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_frame, studentList).commit();
            navigationView.setCheckedItem(R.id.nav_studentsPresent);
            toolbar.setTitle("Student List");
        }
        getData();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
      }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(final Location location:locationResult.getLocations()){
                    String url = "https://sbts2019.000webhostapp.com/locationout.php";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (!response.trim().contains("success")) {
                                        Toast.makeText(getApplicationContext(), "Failed to capture location.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("username", User);
                            params.put("latitude", String.valueOf(location.getLatitude()));
                            params.put("longitude", String.valueOf(location.getLongitude()));
                            return params;
                        }
                    };
                    SingletonClass.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                }
            }
        };
    }

    @SuppressLint("RestrictedApi")
    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_logout: {
                sessionManager.logout();
                break;
            }
            case R.id.nav_studentsPresent: {

                String url = "https://sbts2019.000webhostapp.com/studentList.php";
                bundle.putString("url", url);
                StudentList studentList = new StudentList();
                studentList.setArguments(bundle);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_frame, studentList)
                        .addToBackStack(null)
                        .commit();
                toolbar.setTitle("Student List");
                break;
            }
            case R.id.nav_profile: {

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_frame, new AttendeeHome())
                        .addToBackStack(null)
                        .commit();
                toolbar.setTitle("Profile");
                break;
            }
            case R.id.nav_map: {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_frame, new map())
                        .addToBackStack(null)
                        .commit();
                toolbar.setTitle("Map");
                break;
            }
            case R.id.nav_change_password: {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_frame, new changepassword())
                        .addToBackStack(null)
                        .commit();
                toolbar.setTitle("Change Password");
                break;
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            this.finish();

        } else {
            getFragmentManager().popBackStack();
        }
    }



    private void getData() {

        url = "https://sbts2019.000webhostapp.com/attendee.php";
        stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        editor = sharedPreferences.edit();
                        str = Pattern.compile(",").split(response);
                        editor.putString("Full_Name", str[0]);
                        editor.putString("Photo", str[1]);
                        editor.putString("Email", str[2]);
                        editor.putString("Mobile_No1", str[3]);
                        editor.putString("Bus_No", str[4]);
                        editor.putString("DOB", str[5]);
                        editor.putString("Address", str[6]);
                        editor.apply();

                        String Photo = sharedPreferences.getString("Photo", null);
                        byte[] imagebit=  Base64.decode(Photo,Base64.DEFAULT);
                        imageView.setImageBitmap(BitmapFactory.decodeByteArray(imagebit, 0,imagebit.length));



                        name.setText(sharedPreferences.getString("Full_Name", null));
                        email.setText(sharedPreferences.getString("Email", null));

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", User);
                return params;
            }
        };
        SingletonClass.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CODE && resultCode == RESULT_OK && data != null) {

            Uri path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                upload();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private String imagetoString(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void upload() {

        String imageURL = "https://sbts2019.000webhostapp.com/uploadprofile.php";

        StringRequest imagerequest = new StringRequest(Request.Method.POST, imageURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("name", User);
                params.put("image", imagetoString(bitmap));

                return params;
            }
        };

        SingletonClass.getInstance(getApplicationContext()).addToRequestQueue(imagerequest);
    }
}
