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
import java.util.Collections;
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

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user_chat_records");
    private DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("all_chats");
    private DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("all_chats");
    private FirebaseUser mAuth;
    String chat_uid;

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
         chat_uid = sharedPreferencesChatterDetails.getString(my_uid+receiver_uid+"uid", "");

    }


    private void LoadMessages() {
        if (TextUtils.isEmpty(chat_uid)){
            SharedPreferences.Editor editor = sharedPreferencesChatterDetails.edit();
            chat_uid = getCombinedUID();
            editor.putString(my_uid+receiver_uid+"uid", chat_uid);
            editor.apply();
        }

        databaseReference2.child(chat_uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (chat_list != null) {
                            chat_list.clear();  // v v v v important (eliminate duplication of data)

                        }

                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            RecyclerViewList chats = postSnapshot.getValue(RecyclerViewList.class);
                           // Log.w("mes123", chats.getMessage());
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
            Toast.makeText(Chatting.this, "No Chat yet!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
//            exception
        }

    }


    private void sendMessage() {


        if (TextUtils.isEmpty(chat_uid)){
            SharedPreferences.Editor editor = sharedPreferencesChatterDetails.edit();
            chat_uid = getCombinedUID();
            editor.putString(my_uid+receiver_uid+"uid", chat_uid);
            editor.apply();
            }

        final String status = sharedPreferencesChatterDetails.getString(my_uid+receiver_uid+"status", "");
        if (TextUtils.isEmpty(status)){
            SharedPreferences.Editor editor = sharedPreferencesChatterDetails.edit();
            databaseReference.child(my_uid).child(receiver_uid).child("chat_record").child("status").setValue(true);
            databaseReference.child(my_uid).child(receiver_uid).child("chat_record").child("chat_uid").setValue(chat_uid);

            databaseReference.child(receiver_uid).child(my_uid).child("chat_record").child("status").setValue(true);
            databaseReference.child(receiver_uid).child(my_uid).child("chat_record").child("chat_uid").setValue(chat_uid);
            editor.putString(my_uid+receiver_uid+"status", "posted");
            editor.apply();
        }

        final String message = textMessage.getText().toString().trim();
        final String pushKey = databaseReference.push().getKey();

        List<String> dateTime = getDateTime();
        String date = dateTime.get(0);
        String time = dateTime.get(1);

        databaseReference2.child(chat_uid).child(pushKey).child("push_uid").setValue(pushKey);
        databaseReference2.child(chat_uid).child(pushKey).child("message").setValue(message);
        databaseReference2.child(chat_uid).child(pushKey).child("date").setValue(date);
        databaseReference2.child(chat_uid).child(pushKey).child("time").setValue(time);

        String my_uid7 = my_uid.substring(my_uid.length() - 7, my_uid.length());
        databaseReference2.child(chat_uid).child(pushKey).child("identity").setValue(my_uid7);
        textMessage.setText("");

    }

    private String getCombinedUID() {
        String uid1, uid2;
        uid1 = my_uid;
        uid2 = receiver_uid;
        ArrayList<String> uid_list = new ArrayList<>();
        uid_list.add(0, uid1.substring(0,7));
        uid_list.add(1, uid2.substring(0,7));
        Collections.sort(uid_list);
        return  uid_list.get(0)+uid_list.get(1);
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
                    .load(R.drawable.ic_default_profile_image)
                    .into(profileImage);
        }

        profileName_tv.setText(nameOnDevice);
        phoneNumber_tv.setText(phoneNumber);
    }


    private List<String> getDateTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
        String date = dateFormat.format(c.getTime());
        String time = timeFormat.format(c.getTime());
        ArrayList<String> list = new ArrayList<>();
        list.add(0, date);
        list.add(1, time);
        return list;
    }

    public void onBackPressed(){
        super.onBackPressed();
    }


//    end
}
