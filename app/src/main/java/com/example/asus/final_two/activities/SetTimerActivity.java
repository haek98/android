package com.example.asus.final_two.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.asus.final_two.asynctasks.TimerAsyncGeocoder;
import com.example.asus.final_two.helperclasses.Constants;
import com.example.asus.final_two.helperclasses.myLocation;
import com.example.asus.final_two.R;
import com.example.asus.final_two.services.SetFutureService;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SetTimerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Bundle> {
    @BindView(R.id.timerButton)
    Button timerButton;
    @BindView(R.id.futureDestEditText)
    EditText fDestEditText;
    @BindView(R.id.futureStartEditText)
    EditText fStartEditText;
    @BindView(R.id.alternateImage)
    ImageView alternateImage;
    @BindView(R.id.futureStartEditLayout)
    LinearLayout startLayout;
    @BindView(R.id.futureDestEditLayout)
            LinearLayout endLayout;
    SharedPreferences preferences;
    String savedState, startStr, endStr, uid;
    LatLng state;
    Context context;
    Double lat,lon;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null)
        {
            lat=savedInstanceState.getDouble("lat");
            lon=savedInstanceState.getDouble("lon");
        }
        setContentView(R.layout.activity_set_timer);
        Toolbar toolbar = findViewById(R.id.timerToolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        ButterKnife.bind(this);
        context = this;
        alternateImage.setVisibility(View.GONE);
        preferences = getSharedPreferences("com.example.asus.final_two", MODE_PRIVATE);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        savedState = preferences.getString("savedState", "nothing");
        if (!savedState.equals("nothing")) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(savedState).child(uid);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        timerButton.setVisibility(View.GONE);
                        startLayout.setVisibility(View.GONE);
                        endLayout.setVisibility(View.GONE);
                        alternateImage.setVisibility(View.VISIBLE);
                        ConstraintLayout tempLayout=findViewById(R.id.timer_layout_id);
                        tempLayout.setBackgroundResource(0);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        fStartEditText.setFocusable(false);
        fDestEditText.setFocusable(false);
        fStartEditText.setClickable(true);
        fDestEditText.setClickable(true);
        fStartEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build((Activity) context), Constants.T_START_PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        fDestEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build((Activity) context), Constants.T_DEST_PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @OnClick(R.id.timerButton)
    void timerClick(View view) {
        progressDialog = new ProgressDialog(SetTimerActivity.this);
        progressDialog.setMessage("Setting Timer...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        startStr = fStartEditText.getText().toString();
        endStr = fDestEditText.getText().toString();
        Log.e("TimerTag","timer clicked");
        if (startStr.equals("") || endStr.equals("")) {
            Toast.makeText(context, "Provide Complete Details", Toast.LENGTH_SHORT).show();
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("start", startStr);
            bundle.putString("end", endStr);
            bundle.putDouble("lat", state.latitude);
            bundle.putDouble("long", state.longitude);
            getSupportLoaderManager().initLoader(11, bundle, this).forceLoad();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.T_START_PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                fStartEditText.setText(place.getAddress());

            }
        } else if (requestCode == Constants.T_DEST_PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                fDestEditText.setText(place.getAddress());
                state = place.getLatLng();
                lat=state.latitude;
                lon=state.longitude;
            }
        }
    }

    @NonNull
    @Override
    public Loader<Bundle> onCreateLoader(int id, @Nullable Bundle args) {
        return new TimerAsyncGeocoder(context, args);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Bundle> loader, Bundle data) {
        myLocation myLocation = new myLocation();
        String state1 = data.getString("state");
        Double startLat = data.getDouble("startLat");
        Double startLong = data.getDouble("startLong");
        Double destLat = data.getDouble("destLat");
        Double destLong = data.getDouble("destLong");
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("savedState", state1);
        editor.apply();
        myLocation.setPermanent(false);
        myLocation.setmLongi(startLong);
        myLocation.setmLati(startLat);
        myLocation.setDestLati(destLat);
        myLocation.setDestLongi(destLong);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(state1).child(uid);
        databaseReference.setValue(myLocation);
        Intent intent = new Intent(this, SetFutureService.class);
        intent.setAction(Constants.ACTION_START_SERVICE);
        intent.putExtra("state", state1);
        intent.putExtra("uid", uid);
        intent.putExtra("latKey", startLat);
        intent.putExtra("longKey", startLong);
        startService(intent);
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        Intent mainIntent=new Intent(this, MainActivity.class);
        finish();
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(mainIntent);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Bundle> loader) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(lat!=null){
        outState.putDouble("lat",lat);
        outState.putDouble("lon",lon);}
    }
}
