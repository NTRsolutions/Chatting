package com.enhabyto.chatting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import id.zelory.compressor.Compressor;
import mehdi.sakout.fancybuttons.FancyButton;

public class ProfileSetup extends AppCompatActivity {

    CircularImageView profileImage;
    final FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    FancyButton nextButton;
    Uri ImageFilePath;
    String imageUploadUrl;
    EditText profileName;

    SpinKitView spinKitView;

    ImageView demoImage;

    private SharedPreferences sharedPreferences;
    private static final String LANDING_ACTIVITY = "landingActivity";
    private static final String FIRST_SCREEN = "firstScreen";
    private static final String MAIN_PAGE = "mainPage";
    private static final String PROFILE_SETUP = "profileSetup";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        profileImage = findViewById(R.id.ps_profile_image);
        nextButton = findViewById(R.id.ps_nextButton);
        profileName = findViewById(R.id.ps_profileName);
        spinKitView = findViewById(R.id.ps_spin_kit);


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
// selecting image
                if (checkStoragePermission()){
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(ProfileSetup.this);
                }
            }
        });

        sharedPreferences = getSharedPreferences(LANDING_ACTIVITY, MODE_PRIVATE);


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(profileName.getText().toString())){
                    SuperActivityToast.create(ProfileSetup.this, new Style())
                            .setProgressBarColor(Color.BLACK)
                            .setText("  Enter Profile Name")
                            .setDuration(Style.DURATION_SHORT)
                            .setIconResource(R.drawable.warning_sigh)
                            .setFrame(Style.FRAME_STANDARD)
                            .setColor(getResources().getColor(R.color.black90))
                            .setAnimations(Style.ANIMATIONS_POP).show();
                    return;
                }
                spinKitView.setVisibility(View.VISIBLE);
                new CheckNetworkConnection(ProfileSetup.this, new CheckNetworkConnection.OnConnectionCallback() {
                    @Override
                    public void onConnectionSuccess() {

                        String uid = mAuth.getUid();
                        databaseReference.child("user_profiles").child(uid).child("profile_name").setValue(profileName.getText().toString());
                        startActivity(new Intent(ProfileSetup.this, MainPage.class));


                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(FIRST_SCREEN, MAIN_PAGE);
                        editor.apply();

                        ProfileSetup.this.finish();
                        spinKitView.setVisibility(View.GONE);

                    }
                    @Override
                    public void onConnectionFail(String msg) {
                        NoInternetConnectionAlert noInternetConnectionAlert = new NoInternetConnectionAlert(ProfileSetup.this);
                        noInternetConnectionAlert.DisplayNoInternetConnection();
                        spinKitView.setVisibility(View.GONE);
                    }
                }).execute();



            }
        });

        setImage();
    }

    //selecting image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //selectedImage_iv.setVisibility(View.VISIBLE);
                ImageFilePath = result.getUri();
                Picasso.with(ProfileSetup.this)
                        .load(ImageFilePath)
                        .fit()
                        .centerCrop()
                        .into(profileImage);
                //selectImage_btn.setText("re-select");
                UploadImageFileToFirebaseStorage();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                new SweetAlertDialog(ProfileSetup.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(error.toString())
                        .show();
            }
        }
    }
    //selecting image

    //    upload image to firebase
    public void UploadImageFileToFirebaseStorage() {
        try{    // try block 1
            if (ImageFilePath != null) {
                File actualImage = FileUtils.getFile(ProfileSetup.this, ImageFilePath);

                // try block 2
                try {
                    File compressedImageFile = new Compressor(ProfileSetup.this)
                            .setQuality(20)
                            .setCompressFormat(Bitmap.CompressFormat.WEBP)
                            .compressToFile(actualImage);

                    ImageFilePath = Uri.fromFile(compressedImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                } //  try block 2 ends

                assert mAuth != null;
                final String uid = mAuth.getUid();
                final StorageReference storageReferenceChild = storageReference.child("user_profiles/").child(uid+"/")
                        .child("profile_image").child("user_image.jpg");

                storageReferenceChild.putFile(ImageFilePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storageReferenceChild.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                           imageUploadUrl = uri.toString();
                                        databaseReference.child("user_profiles").child(uid).child("profile_image_url").setValue(imageUploadUrl);

                                    }
                                });
                            }
                        })  // addOnSuccessListener ends
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {


                            }
                        })  // addOnFailureListener ends
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                            }
                        }); // addOnProgressListener ends
            }

        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }  //  try block 1 ends

    }  // UploadImageFileToFirebaseStorage ends


    public boolean checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(ProfileSetup.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProfileSetup.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return false;
        }
        else return true;
    }




    //handling profile image
    //    setting image
    public void setImage(){
        Picasso.with(ProfileSetup.this)
                .load(R.drawable.ic_default_profile_image)
                .into(profileImage);
        databaseReference.child("user_profiles").child(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String imagePath = dataSnapshot.child("profile_image_url").getValue(String.class);

                        Picasso.with(ProfileSetup.this)
                                .load(imagePath)
                                .placeholder(R.drawable.ic_default_profile_image)
                                .error(R.drawable.ic_default_profile_image)
                                .into(profileImage);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    } // set image ends


    //ends
}
