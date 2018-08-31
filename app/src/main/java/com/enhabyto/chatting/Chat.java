package com.enhabyto.chatting;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class Chat extends Fragment {

    View view;
    RecyclerView recyclerView;

    // Creating RecyclerView.Adapter.
    RecyclerView.Adapter adapter ;
    List<RecyclerViewList> list = new ArrayList<>();

    HashMap<String, String > messengerHashMap = new HashMap<>();

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    LinearLayoutManager recyclerViewLayoutManager;

    private SharedPreferences sharedPreferencesUId;
    private static final String LANDING_ACTIVITY = "landingActivity";
    private static final String MY_UID = "my_uid";
    private String my_uid;

    private SharedPreferences sharedPreferencesChatLoadingInfo;
    private static final String CHATS_LOADED = "chatsLoaded";

    String messenger_uid;
    String chat_uid;

    RecyclerViewList recyclerViewList = new RecyclerViewList();

    //Override method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.chat, container, false);
        recyclerView = view.findViewById(R.id.ch_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        sharedPreferencesUId = Objects.requireNonNull(getActivity()).getSharedPreferences(LANDING_ACTIVITY, MODE_PRIVATE);
        sharedPreferencesChatLoadingInfo = Objects.requireNonNull(getActivity()).getSharedPreferences(CHATS_LOADED, MODE_PRIVATE);
        my_uid = sharedPreferencesUId.getString(MY_UID, "");

        getChatDetails();
        return view;
    }

    private void getChatDetails(){

        // first getting uid
        databaseReference.child("user_chat_records").child(my_uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            final String messenger_uid = snapshot.getKey();
                            final String chat_uid = snapshot.child("chat_record").child("chat_uid").getValue(String.class);
                            messengerHashMap.put(messenger_uid, chat_uid);

                        }
                      getUserDetailsAndLastMessage();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        }

    private void getUserDetailsAndLastMessage(){
        HashMap<String, String> messengerHashMap1 = messengerHashMap;

        for (Map.Entry<String, String> entry : messengerHashMap1.entrySet()) {
             messenger_uid = entry.getKey();
             chat_uid = entry.getValue();
             databaseReference.child("user_profiles").child(messenger_uid)
                     .addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                             final String phoneNumber, profileImageUrl, profileName;
                             phoneNumber = dataSnapshot.child("phone_number").getValue(String.class);
                             profileImageUrl = dataSnapshot.child("profile_image_url").getValue(String.class);
                             profileName = dataSnapshot.child("profile_name").getValue(String.class);

                             recyclerViewList.setPhone_number(phoneNumber);
                             recyclerViewList.setProfile_image_url(profileImageUrl);
                             recyclerViewList.setProfile_image_url(profileName);
                             recyclerViewList.setMessenger_uid(messenger_uid);
                             recyclerViewList.setChat_uid(chat_uid);

                             final Cursor phones = (Objects.requireNonNull(getActivity())).getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null,ContactsContract.Contacts.SORT_KEY_PRIMARY+" ASC");
                             while (Objects.requireNonNull(phones).moveToNext())
                             {
                                 String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                 String phoneNumberOnDevice = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                         if (TextUtils.equals(phoneNumberOnDevice,phoneNumber)){
                                             recyclerViewList.setName_saved_on_device(name);
                                         }

                             } //while
                             phones.close();

                             list.add(recyclerViewList);

                             adapter = new ChatRecyclerViewAdapter(getActivity(), list);
                             recyclerView.setAdapter(adapter);
                             }

                         @Override
                         public void onCancelled(@NonNull DatabaseError databaseError) {

                         }
                     });

        }

    }



//    end

}