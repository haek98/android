package com.example.asus.final_two.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.asus.final_two.adapters.ListAdapterClass;
import com.example.asus.final_two.helperclasses.Constants;
import com.example.asus.final_two.helperclasses.CreateFile;
import com.example.asus.final_two.helperclasses.MapClass;
import com.example.asus.final_two.helperclasses.MessageClass;
import com.example.asus.final_two.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    private ChildEventListener childEventListener;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    CreateFile obj;
    ArrayList<MessageClass> list;
    @BindView(R.id.sendText)
    EditText sendText;
    @BindView(R.id.chatList)
    ListView listView;
    ListAdapterClass listAdapter;
    String uid, TAG = "ChatTag", savedState,userName="anonymous";
    SharedPreferences sharedPreferences;
    Task<Uri> temp;
    ChildEventListener chatChildEventListener;
    StorageReference reference=null;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (savedInstanceState != null) {
            String temp = savedInstanceState.getString("progress");
            if (temp != null)
                reference = FirebaseStorage.getInstance().getReferenceFromUrl(temp);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        ButterKnife.bind(this);
        sharedPreferences = getSharedPreferences("com.example.asus.final_two", MODE_PRIVATE);
        savedState = sharedPreferences.getString("savedState", "nothing");
        list = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Uploading...");
        listAdapter = new ListAdapterClass(this, list);
        listView.setAdapter(listAdapter);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("chat_photos");
        firebaseDatabase = FirebaseDatabase.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        databaseReference = firebaseDatabase.getReference().child("messages").child(MainActivity.state);
        chatChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.e("ChatTag", dataSnapshot.toString());
                MessageClass e = dataSnapshot.getValue(MessageClass.class);
                Log.e(TAG, String.valueOf(MapClass.hash.size()));
                if (MapClass.hash.containsKey(e.uid) || e.uid.equals(uid)) {
                    list.add(e);
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(chatChildEventListener);
        if (reference != null) {
            progressDialog.show();
            List<UploadTask> tasks = reference.getActiveUploadTasks();
            if (tasks.size() > 0) {
                // Get the task monitoring the upload
                UploadTask task = tasks.get(0);

                // Add new listeners to the task using an Activity scope
                task.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    }
                }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                    }
                });
            }
        }
    }

    @OnClick(R.id.sendButton)
    void sendClick(View view) {
        String str = sendText.getText().toString();
        if (str.equals("")) {
            Toast.makeText(this, "Nothing to send", Toast.LENGTH_SHORT).show();
        } else {
            MessageClass obj = new MessageClass(userName, str, "nothing", uid);
            sendText.setText("");
            databaseReference.push().setValue(obj);
        }
    }

    @OnClick(R.id.imageButton)
    public void imageClick(View view) {
        ArrayList<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.WRITE_EXT_CODE);
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_DENIED) {
//            Log.e("TAG", Arrays.toString(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).listFiles()));
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            obj = new CreateFile(this);
            try {
                photoFile = obj.createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Some problem with external storage", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Log.e("ChatActivityLog", photoFile.toString());
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.asus.final_two.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
//            Uri uri  = Uri.parse("file:///sdcard/photo.jpg");
//            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(cameraIntent, Constants.CAMERA_REQUEST);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.CAMERA_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_DENIED) {
                    Log.e("TAG", "code granted");
                    Log.e("TAG", String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)));
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri pictureUri = Uri.fromFile(new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))));
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
                    startActivityForResult(cameraIntent, Constants.CAMERA_REQUEST);
                }
                else{
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.WRITE_EXT_CODE);
                    }
                }
            }
            break;
            case Constants.WRITE_EXT_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_DENIED) {
                    Log.e("TAG", "code granted");
                    Log.e("TAG", String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)));
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, Constants.CAMERA_REQUEST);
                }
                else{
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST_CODE);
                    }
                }
            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == Constants.CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            try{
            File f = new File(obj.getImageFilePath());
            Uri uri = Uri.fromFile(f);
            Log.e("CHAT", uri.toString());
            reference = storageReference.child(uid).child(uri.getLastPathSegment());
            progressDialog.show();
            UploadTask uploadTask = reference.putFile(uri);
            uploadTask.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e("ChatTag", "upload successful");
                    progressDialog.dismiss();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                }

            });
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Uri downloadUri = task.getResult();
                    MessageClass obj = new MessageClass(userName, "abc", downloadUri.toString(), uid);
                    databaseReference.push().setValue(obj);
                }
            });}
            catch(NullPointerException e)
        {
            Toast.makeText(this, "Please retake the image", Toast.LENGTH_SHORT).show();
        }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("progress",storageReference.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.child("messages").child(savedState).removeEventListener(chatChildEventListener);
    }
}
