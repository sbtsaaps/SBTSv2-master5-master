package sbts.dmw.com.sbtrackingsystem.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import sbts.dmw.com.sbtrackingsystem.R;
import sbts.dmw.com.sbtrackingsystem.adapter.RecyclerViewAdapter;
import sbts.dmw.com.sbtrackingsystem.classes.SingletonClass;
import sbts.dmw.com.sbtrackingsystem.model.Student;

import static android.content.Context.MODE_PRIVATE;

public class ParentHome extends Fragment {
    ImageView imageView;
    TextView name, email, mobile1, bus_no, dob, address, student_name;
    View view;
    SharedPreferences sharedPreferences;
//    RequestOptions option;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        option = new RequestOptions()
//                .centerCrop()
//                .placeholder(R.drawable.placeholder)
//                .error(R.drawable.loading_shape)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .skipMemoryCache(true);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_parent_home, container, false);

        getActivity().setTitle("Profile");
        imageView = (ImageView) view.findViewById(R.id.parent_profile_image);
        name = (TextView) view.findViewById(R.id.parent_profile_name);
        email = (TextView) view.findViewById(R.id.parent_profile_email);
        mobile1 = (TextView) view.findViewById(R.id.parent_profile_mobile1);
        bus_no = (TextView) view.findViewById(R.id.parent_profile_bus);
        dob = (TextView) view.findViewById(R.id.parent_profile_DOB);
        student_name = (TextView) view.findViewById(R.id.parent_profile_student_name);
        address = (TextView) view.findViewById(R.id.parent_profile_address);

        sharedPreferences = getActivity().getSharedPreferences("LOGIN", Context.MODE_PRIVATE);

        String Full_Name = sharedPreferences.getString("Full_Name", null);
        String Photo = sharedPreferences.getString("Photo", null);
        String Email = sharedPreferences.getString("Email", null);
        String Mobile_No1 = sharedPreferences.getString("Mobile_No1", null);
        String Bus_No = sharedPreferences.getString("Bus_No", null);
        String DOB = sharedPreferences.getString("DOB", null);
        String Student_Name = sharedPreferences.getString("Student_Name", null);
        String Address = sharedPreferences.getString("Address", null);


        name.setText(Full_Name);
//        Glide.with(getActivity().getApplicationContext())
//                .load(Photo)
//                .apply(option)
//                .into(imageView);

        byte[] imagebit=  Base64.decode(Photo,Base64.DEFAULT);
        imageView.setImageBitmap(BitmapFactory.decodeByteArray(imagebit, 0,imagebit.length));



        email.setText(Email);
        mobile1.setText(Mobile_No1);
        bus_no.setText(Bus_No);
        dob.setText(DOB);
        student_name.setText(Student_Name);
        address.setText(Address);
        return view;
    }


}

