package com.enhabyto.chatting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mehdi.sakout.fancybuttons.FancyButton;

public class PhoneLogin extends AppCompatActivity {

    private static final String TAG = "PhoneAuth";

    private EditText phoneText;
    private PinView codeText;
    private FancyButton verifyButton;
    private FancyButton sendButton;
    private FancyButton resendButton;
    private Button signoutButton;
    private TextView statusText;
    private CountryCodePicker countryCodePicker;
    private RelativeLayout otpRelativeLayout;
    private SpinKitView spinKitView;

    private SharedPreferences sharedPreferences;
    private static final String LANDING_ACTIVITY = "landingActivity";
    private static final String FIRST_SCREEN = "firstScreen";
    private static final String MAIN_PAGE = "mainPage";
    private static final String PROFILE_SETUP = "profileSetup";
    private static final String MY_UID = "my_uid";

    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private FirebaseAuth fbAuth;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user_profiles");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        verifyButton = findViewById(R.id.verifyButton);
        sendButton = findViewById(R.id.sendButton);
        resendButton = findViewById(R.id.resendButton);
        signoutButton = findViewById(R.id.signoutButton);
        statusText = findViewById(R.id.statusText);
        countryCodePicker = findViewById(R.id.p_ccp);
        otpRelativeLayout = findViewById(R.id.p_otpRelativeLayout);
        spinKitView = findViewById(R.id.p_spin_kit);

        //verifyButton.setEnabled(false);
        resendButton.setEnabled(false);
        signoutButton.setEnabled(false);
        statusText.setText("Signed Out");

        sharedPreferences = getSharedPreferences(LANDING_ACTIVITY, MODE_PRIVATE);

        fbAuth = FirebaseAuth.getInstance();

    }

    public void onStart(){
        super.onStart();
        checkLandingPage();
    }

    private void checkLandingPage() {
        String decider = sharedPreferences.getString(FIRST_SCREEN, "");
        if (TextUtils.equals(decider, PROFILE_SETUP)) {
            startActivity(new Intent(PhoneLogin.this, ProfileSetup.class));
            PhoneLogin.this.finish();
        }
        else if (TextUtils.equals(decider, MAIN_PAGE)) {
            startActivity(new Intent(PhoneLogin.this, MainPage.class));
            PhoneLogin.this.finish();
        }

    }


    public void sendCode(View view) {

        new CheckNetworkConnection(PhoneLogin.this, new CheckNetworkConnection.OnConnectionCallback() {
            @Override
            public void onConnectionSuccess() {


                String phoneNumber = phoneText.getText().toString();
                String countryCode = countryCodePicker.getFullNumberWithPlus();

                if (TextUtils.isEmpty(countryCode)){
                    new SweetAlertDialog(PhoneLogin.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Choose your Country!")
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 10){
                    new SweetAlertDialog(PhoneLogin.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Incorrect Mobile Number! ")
                            .show();
                    return;
                }

                spinKitView.setVisibility(View.VISIBLE);
                phoneNumber = countryCode+phoneNumber;

                setUpVerificationCallbacks();

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        PhoneLogin.this,               // Activity (for callback binding)
                        verificationCallbacks);


            }
            @Override
            public void onConnectionFail(String msg) {
                NoInternetConnectionAlert noInternetConnectionAlert = new NoInternetConnectionAlert(PhoneLogin.this);
                noInternetConnectionAlert.DisplayNoInternetConnection();

            }
        }).execute();


    }

    private void setUpVerificationCallbacks() {

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(
                            PhoneAuthCredential credential) {

                        signoutButton.setEnabled(true);
                       // statusText.setText("Signed In");
                        resendButton.setEnabled(false);
                        verifyButton.setEnabled(false);
                        codeText.setText("");

                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                          //  Log.d(TAG, "Invalid credential: "
                           //         + e.getLocalizedMessage());
                            new SweetAlertDialog(PhoneLogin.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Invalid credential")
                                    .setContentText(e.getLocalizedMessage())
                                    .show();
                            spinKitView.setVisibility(View.GONE);

                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Log.d(TAG, "SMS Quota exceeded.");
                            Toast.makeText(PhoneLogin.this, "SMS Quota exceeded.", Toast.LENGTH_SHORT).show();
                            spinKitView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {

                        spinKitView.setVisibility(View.GONE);
                        otpRelativeLayout.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInUp)
                                .duration(700)
                                .repeat(0)
                                .playOn(otpRelativeLayout);

                        String phoneNumber = phoneText.getText().toString();
                        String countryCode = countryCodePicker.getFullNumberWithPlus();

                        phoneNumber = countryCode+phoneNumber;

                        SuperActivityToast.create(PhoneLogin.this, new Style())
                                .setProgressBarColor(Color.BLACK)
                                .setText("  OTP Sent to "+phoneNumber)
                                .setDuration(Style.DURATION_LONG)
                                .setIconResource(R.drawable.ic_info)
                                .setFrame(Style.FRAME_STANDARD)
                                .setColor(getResources().getColor(R.color.black90))
                                .setAnimations(Style.ANIMATIONS_POP).show();

                        phoneVerificationId = verificationId;
                        resendToken = token;

                        verifyButton.setEnabled(true);
                        sendButton.setEnabled(false);
                        resendButton.setEnabled(true);
                    }
                };
    }

    public void verifyCode(View view) {


        new CheckNetworkConnection(PhoneLogin.this, new CheckNetworkConnection.OnConnectionCallback() {
            @Override
            public void onConnectionSuccess() {
                String code = codeText.getText().toString();

                if (TextUtils.isEmpty(code) || code.length() < 6){
                    new SweetAlertDialog(PhoneLogin.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("enter full code!")
                            .show();
                    return;
                }

                PhoneAuthCredential credential =
                        PhoneAuthProvider.getCredential(phoneVerificationId, code);
                signInWithPhoneAuthCredential(credential);
            }
            @Override
            public void onConnectionFail(String msg) {
                NoInternetConnectionAlert noInternetConnectionAlert = new NoInternetConnectionAlert(PhoneLogin.this);
                noInternetConnectionAlert.DisplayNoInternetConnection();

            }
        }).execute();


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                           // signoutButton.setEnabled(true);
                            codeText.setText("");
                          //  statusText.setText("Signed In");
                            resendButton.setEnabled(false);
                            verifyButton.setEnabled(false);
                            //FirebaseUser user = task.getResult().getUser();
                            String user_uid = Objects.requireNonNull(fbAuth.getCurrentUser()).getUid();
                            String phone_number = Objects.requireNonNull(fbAuth.getCurrentUser()).getPhoneNumber();

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(FIRST_SCREEN, PROFILE_SETUP);
                            editor.putString(MY_UID, user_uid);
                            editor.apply();



                            databaseReference.child(user_uid).child("uid").setValue(user_uid);
                            databaseReference.child(user_uid).child("phone_number").setValue(phone_number);
//                            DatabaseReference secret = FirebaseDatabase.getInstance().getReference();
//                            secret.child("registered_contacts").child(user_uid).child("phone_number").setValue(phone_number);

                            startActivity(new Intent(PhoneLogin.this, ProfileSetup.class));
                            PhoneLogin.this.finish();


                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                //Toast.makeText(PhoneLogin.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                new SweetAlertDialog(PhoneLogin.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Error")
                                        .setContentText(task.getException().getLocalizedMessage())
                                        .show();
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    public void resendCode(View view) {

        new CheckNetworkConnection(PhoneLogin.this, new CheckNetworkConnection.OnConnectionCallback() {
            @Override
            public void onConnectionSuccess() {
                String phoneNumber = phoneText.getText().toString();
                String countryCode = countryCodePicker.getFullNumberWithPlus();

                if (TextUtils.isEmpty(countryCode)){
                    new SweetAlertDialog(PhoneLogin.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Choose your Country!")
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 10){
                    new SweetAlertDialog(PhoneLogin.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Incorrect Mobile Number! ")
                            .show();
                    return;
                }

                spinKitView.setVisibility(View.VISIBLE);
                phoneNumber = countryCode+phoneNumber;
                setUpVerificationCallbacks();

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,
                        60,
                        TimeUnit.SECONDS,
                        PhoneLogin.this,
                        verificationCallbacks,
                        resendToken);
            }
            @Override
            public void onConnectionFail(String msg) {
                NoInternetConnectionAlert noInternetConnectionAlert = new NoInternetConnectionAlert(PhoneLogin.this);
                noInternetConnectionAlert.DisplayNoInternetConnection();

            }
        }).execute();



    }

    public void signOut(View view) {
        fbAuth.signOut();
        statusText.setText("Signed Out");
        signoutButton.setEnabled(false);
        sendButton.setEnabled(true);
    }

}
