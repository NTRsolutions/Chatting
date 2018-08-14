package com.enhabyto.chatting;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mehdi.sakout.fancybuttons.FancyButton;

public class Home extends AppCompatActivity {

    private static final String TAG = "PhoneAuth";

    private EditText phoneText;
    private PinView codeText;
    private FancyButton verifyButton;
    private FancyButton sendButton;
    private FancyButton resendButton;
    private Button signoutButton;
    private TextView statusText;
    private CountryCodePicker countryCodePicker;

    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        verifyButton = findViewById(R.id.verifyButton);
        sendButton = findViewById(R.id.sendButton);
        resendButton = findViewById(R.id.resendButton);
        signoutButton = findViewById(R.id.signoutButton);
        statusText = findViewById(R.id.statusText);
        countryCodePicker = findViewById(R.id.p_ccp);

        verifyButton.setEnabled(false);
        resendButton.setEnabled(false);
        signoutButton.setEnabled(false);
        statusText.setText("Signed Out");

        fbAuth = FirebaseAuth.getInstance();
    }


    public void sendCode(View view) {

        new CheckNetworkConnection(Home.this, new CheckNetworkConnection.OnConnectionCallback() {
            @Override
            public void onConnectionSuccess() {


                String phoneNumber = phoneText.getText().toString();
                String countryCode = countryCodePicker.getFullNumberWithPlus();

                if (TextUtils.isEmpty(countryCode)){
                    new SweetAlertDialog(Home.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Choose your Country!")
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 10){
                    new SweetAlertDialog(Home.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Incorrect Mobile Number! ")
                            .show();
                    return;
                }

                phoneNumber = countryCode+phoneNumber;

                setUpVerificationCallbacks();

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        Home.this,               // Activity (for callback binding)
                        verificationCallbacks);

            }
            @Override
            public void onConnectionFail(String msg) {
                NoInternetConnectionAlert noInternetConnectionAlert = new NoInternetConnectionAlert(Home.this);
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
                            new SweetAlertDialog(Home.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Invalid credential")
                                    .setContentText(e.getLocalizedMessage())
                                    .show();

                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Log.d(TAG, "SMS Quota exceeded.");
                            Toast.makeText(Home.this, "SMS Quota exceeded.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {

                        String phoneNumber = phoneText.getText().toString();
                        String countryCode = countryCodePicker.getFullNumberWithPlus();

                        phoneNumber = countryCode+phoneNumber;

                        SuperActivityToast.create(Home.this, new Style())
                                .setProgressBarColor(Color.BLACK)
                                .setText("OTP Sent to "+phoneNumber)
                                .setDuration(Style.DURATION_LONG)
                                .setFrame(Style.FRAME_KITKAT)
                                .setColor(getResources().getColor(R.color.whiteSmoke))
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


        new CheckNetworkConnection(Home.this, new CheckNetworkConnection.OnConnectionCallback() {
            @Override
            public void onConnectionSuccess() {
                String code = codeText.getText().toString();

                if (TextUtils.isEmpty(code) || code.length() < 6){
                    new SweetAlertDialog(Home.this, SweetAlertDialog.ERROR_TYPE)
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
                NoInternetConnectionAlert noInternetConnectionAlert = new NoInternetConnectionAlert(Home.this);
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
                            startActivity(new Intent(Home.this, Contacts.class));
                            Toast.makeText(Home.this, "Signed in", Toast.LENGTH_SHORT).show();

                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                //Toast.makeText(Home.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                new SweetAlertDialog(Home.this, SweetAlertDialog.ERROR_TYPE)
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

        new CheckNetworkConnection(Home.this, new CheckNetworkConnection.OnConnectionCallback() {
            @Override
            public void onConnectionSuccess() {
                String phoneNumber = phoneText.getText().toString();
                String countryCode = countryCodePicker.getFullNumberWithPlus();

                if (TextUtils.isEmpty(countryCode)){
                    new SweetAlertDialog(Home.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Choose your Country!")
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 10){
                    new SweetAlertDialog(Home.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Incorrect Mobile Number! ")
                            .show();
                    return;
                }

                phoneNumber = countryCode+phoneNumber;
                setUpVerificationCallbacks();

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,
                        60,
                        TimeUnit.SECONDS,
                        Home.this,
                        verificationCallbacks,
                        resendToken);
            }
            @Override
            public void onConnectionFail(String msg) {
                NoInternetConnectionAlert noInternetConnectionAlert = new NoInternetConnectionAlert(Home.this);
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
