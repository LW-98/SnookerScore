package com.wilcox.snookerscoring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Account extends AppCompatActivity {

    private String id;
    private boolean changed = false;
    // Class that adds a circular image (Creator: https://github.com/hdodenhof/CircleImageView)
    private CircleImageView profileImage;
    private EditText mFirstName, mLastName;
    private Button saveAccountButton;
    private Uri imageURI = null;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);

        // Initialise link to objects from xml file
        profileImage = findViewById(R.id.profileImage);
        mFirstName = findViewById(R.id.firstNameAccount);
        mLastName = findViewById(R.id.lastNameAccount);
        saveAccountButton = findViewById(R.id.accountButton);
        progressBar = findViewById(R.id.accountProgress);

        // Creates app bar
        Toolbar accountToolbar = findViewById(R.id.accountToolbar);
        setSupportActionBar(accountToolbar);
        getSupportActionBar().setTitle("Account Settings");

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        id = firebaseAuth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);

        // Loads information about current user allowing them to update it
        firestore.collection("Users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        imageURI = Uri.parse(image);
                        mFirstName.setText(name.substring(0, name.indexOf(" ")));
                        mLastName.setText(name.substring(name.indexOf(" ") + 1));

                        // This loads a profile picture, either the users or a placeholder if they haven't got one, using the glide Library (author: https://github.com/bumptech/glide)
                        RequestOptions placeholder = new RequestOptions();
                        placeholder.placeholder(R.drawable.profile);
                        Glide.with(Account.this).setDefaultRequestOptions(placeholder).load(imageURI)
                                .apply(new RequestOptions()
                                        .fitCenter()
                                        .format(DecodeFormat.PREFER_ARGB_8888)
                                        .override(Target.SIZE_ORIGINAL))
                                .into(profileImage);
                    }
                } else {
                    Toast.makeText(Account.this, "Failed to Retrieve data from Firestore: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://snooker-scoring.appspot.com");

        // Function that is called when the save account button is tapped
        saveAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String firstName = mFirstName.getText().toString();
                final String lastName = mLastName.getText().toString();

                progressBar.setVisibility(View.VISIBLE);

                if (changed) {
                    // Checks to make sure all fields are filled in
                    if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {

                        // Stores image in a cloud storage folder called profile_images
                        StorageReference storage = storageReference.child("profile_images/" + id + ".jpg");
                        UploadTask uploadTask = storage.putFile(imageURI);

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storeFirestore(taskSnapshot, firstName, lastName);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(Account.this, "Image Error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    storeFirestore(null, firstName, lastName);
                }
            }
        });

        // Allows user to change profile picture by tapping it
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(Account.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(Account.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(Account.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        crop();
                    }
                } else {
                    crop();
                }
            }
        });
    }

    // Method that handles uploading changes to database
    public void storeFirestore(UploadTask.TaskSnapshot taskSnapshot, String fname, String lname) {

        // Gets Uri of image chosen by user
        Uri downloadUri;
        if (taskSnapshot != null) {
            downloadUri = Uri.parse(taskSnapshot.getUploadSessionUri().toString().substring(0, taskSnapshot.getUploadSessionUri().toString().indexOf("&uploadType")) + "&alt=media");
        } else {
            downloadUri = imageURI;
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", fname + " " + lname);
        if (downloadUri == null)
            downloadUri = Uri.parse("android.resource://com.wilcox.snookerscoring/drawable/profile");
        userMap.put("image", downloadUri.toString());

        // This functions stores the name and profile picture in a database which is categorized by a unique ID
        firestore.collection("Users").document(id).set(userMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Account.this, "Account Updated", Toast.LENGTH_LONG).show();
                    Intent dashboard = new Intent(Account.this, Dashboard.class);
                    startActivity(dashboard);
                    finish();
                } else {
                    Toast.makeText(Account.this, "Firestore Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    // This method initiates the cropping process using a library (Creator: https://github.com/ArthurHub/Android-Image-Cropper)
    private void crop() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(Account.this);
    }

    // This method is called when user is finished cropping
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageURI = result.getUri();
                profileImage.setImageURI(imageURI);
                changed = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}