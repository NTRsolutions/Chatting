package com.enhabyto.chatting;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chatting extends AppCompatActivity {

    private RecyclerView recyclerView;

    // Creating RecyclerView.Adapter.
    private RecyclerView.Adapter adapter;
    private List<RecyclerViewList> chat_list = new ArrayList<>();

    private static final String LANDING_ACTIVITY = "landingActivity";
    private static final String MY_UID = "my_uid";


    private SharedPreferences sharedPreferencesChatterDetails, sharedPreferences;

    private static final String RECEIVER_DETAILS = "receiver_details";
    private static final String UID = "uid";
    private static final String PROFILE_IMAGE_URL = "profileImageUrl";
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String PROFILE_NAME = "profileName";
    private static final String NAME_ON_DEVICE = "nameOnDevice";

    CircleImageView profileImage;
    TextView profileName_tv, phoneNumber_tv;
    EditText textMessage;
    ImageButton sendMessage, back;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("all_chats");
    private DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("all_chats");
    private FirebaseUser mAuth;

    String my_uid, receiver_uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        sharedPreferencesChatterDetails = getSharedPreferences(RECEIVER_DETAILS, MODE_PRIVATE);

        recyclerView = findViewById(R.id.ac_recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager recyclerViewLayoutManager = new LinearLayoutManager(Chatting.this);
        recyclerViewLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);


        profileImage = findViewById(R.id.ac_profileImage);
        profileName_tv = findViewById(R.id.ac_profileName);
        phoneNumber_tv = findViewById(R.id.ac_phoneNumber);

        sendMessage = findViewById(R.id.ac_send_message);
        back = findViewById(R.id.ac_back);

        textMessage = findViewById(R.id.ac_messageText);

        mAuth = FirebaseAuth.getInstance().getCurrentUser();

        setChatterDetails();

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(textMessage.getText().toString().trim()))
                    sendMessage();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chatting.super.onBackPressed();
            }
        });


        sharedPreferences = getSharedPreferences(LANDING_ACTIVITY, MODE_PRIVATE);
        my_uid = sharedPreferences.getString(MY_UID, "");
        receiver_uid = sharedPreferencesChatterDetails.getString(UID, "");

         LoadMessages();

    }


    private void LoadMessages() {
        final String uid = sharedPreferences.getString(MY_UID, "");

        databaseReference1.child(uid).child(receiver_uid).child("chat")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (chat_list != null) {
                            chat_list.clear();  // v v v v important (eliminate duplication of data)

                        }

                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            RecyclerViewList chats = postSnapshot.getValue(RecyclerViewList.class);
                            Log.w("mes123", chats.getMessage());
                            chat_list.add(chats);
                        }


                        if (chat_list.isEmpty())
                            showEmptyPage();

                        adapter = new ChattingRecyclerViewAdapter(getApplicationContext(), chat_list);
                        recyclerView.setAdapter(adapter);
                        recyclerView.scrollToPosition(chat_list.size());


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Hiding the progress dialog.
                        //progressDialog.dismiss();
                    }
                });
    }


    //    show empty page
    public void showEmptyPage() {
        try {
            Toast.makeText(Chatting.this, "No Chat yett!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
//            exception
        }

    }


    private void sendMessage() {

        final String message = textMessage.getText().toString().trim();


        String pushKey = databaseReference.push().getKey();


        String dateTime = getDateTime();

        //if (!TextUtils.isEmpty(message))
        databaseReference.child(my_uid).child(receiver_uid).child("chat").child(pushKey).child("message").setValue(message);
        databaseReference.child(my_uid).child(receiver_uid).child("chat").child(pushKey).child("date_time").setValue(dateTime);

        String my_uid7 = my_uid.substring(my_uid.length() - 7, my_uid.length());
        databaseReference.child(my_uid).child(receiver_uid).child("chat").child(pushKey).child("identity").setValue(my_uid7);
        textMessage.setText("");

    }



    private void setChatterDetails() {
        String phoneNumber, profileImageUrl, uid, profileName, nameOnDevice;
        profileImageUrl = sharedPreferencesChatterDetails.getString(PROFILE_IMAGE_URL,"");
        profileName = sharedPreferencesChatterDetails.getString(PROFILE_NAME,"");
        uid = sharedPreferencesChatterDetails.getString(UID,"");
        nameOnDevice = sharedPreferencesChatterDetails.getString(NAME_ON_DEVICE,"");
        phoneNumber = sharedPreferencesChatterDetails.getString(PHONE_NUMBER,"");

        if (!TextUtils.isEmpty(profileImageUrl)){
            Picasso.with(Chatting.this)
                    .load(profileImageUrl)
                    .centerCrop()
                    .fit()
                    .into(profileImage);
        }
        else {
            Glide.with(Chatting.this)
                    .load(R.drawable.ic_square_default_profile_picture)
                    .into(profileImage);
        }

        profileName_tv.setText(nameOnDevice);
        phoneNumber_tv.setText(phoneNumber);
    }


    private String getDateTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");
        String datetime = dateformat.format(c.getTime());
        System.out.println(datetime);
        Log.w("raky", datetime);
        return datetime;
    }

    public void onBackPressed(){
        super.onBackPressed();
    }


//    end
}
