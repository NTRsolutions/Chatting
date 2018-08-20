package com.enhabyto.chatting;

import android.Manifest;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mehdi.sakout.fancybuttons.FancyButton;

import static android.content.Context.MODE_PRIVATE;

public class Contacts extends Fragment {

    View view;

    RecyclerView recyclerView;
    RelativeLayout emptyRelativeLayout, recyclerViewRelativeLayout;

    TextView emptyTextView;

    // Creating RecyclerView.Adapter.
    RecyclerView.Adapter adapter ;
    List<RecyclerViewList> list = new ArrayList<>();

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    LinearLayoutManager recyclerViewLayoutManager;

    private SharedPreferences sharedPreferences;
    private static final String PHONE_NAME_MATCHER = "phoneNameMatcher";
    private static final String PHONE_NAME_ON_DEVICE = "Na";

    private SharedPreferences sharedPreferencesUId;
    private static final String LANDING_ACTIVITY = "landingActivity";
    private static final String MY_UID = "my_uid";
    String uid;

    private SharedPreferences sharedPreferencesContactsLoadingInfo;
    private static final String IS_CONTACTS_LOADED = "isContactsLoaded";
    private static final String IS_ALL_CONTACTS_LOADED = "false";

    private SharedPreferences sharedPreferencesContactsData;
    private static final String CONTACTS_DATA = "contactsData";

    FancyButton refresh;
    int count =1;
    //Override method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        view = inflater.inflate(R.layout.contacts, container, false);

        recyclerView = view.findViewById(R.id.c_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(getActivity());

        sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(PHONE_NAME_MATCHER, MODE_PRIVATE);

        sharedPreferencesUId = Objects.requireNonNull(getActivity()).getSharedPreferences(LANDING_ACTIVITY, MODE_PRIVATE);
        sharedPreferencesContactsLoadingInfo = Objects.requireNonNull(getActivity()).getSharedPreferences(IS_CONTACTS_LOADED, MODE_PRIVATE);
        sharedPreferencesContactsData = Objects.requireNonNull(getActivity()).getSharedPreferences(CONTACTS_DATA, MODE_PRIVATE);

        refresh = view.findViewById(R.id.ct_refresh);

        uid = sharedPreferencesUId.getString(MY_UID, "");
      //  progressDialog = new ProgressDialog(ListOfAangadias.this);
        //progressDialog.setMessage("Loading Data...");

        // Setting RecyclerView layout as LinearLayout.
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        Log.w("raky", "is val"+sharedPreferencesContactsLoadingInfo.getString(IS_ALL_CONTACTS_LOADED, "false"));
        if (TextUtils.equals(sharedPreferencesContactsLoadingInfo.getString(IS_ALL_CONTACTS_LOADED, "false"), "false")){
            LoadContacts();
        }

        else {
            LoadDataFromSharedPref();
            Toast.makeText(getActivity(), "Contacts are already synced", Toast.LENGTH_SHORT).show();
            }

        //if (readContactsPermission())
        //getAllContacts();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadContacts();
            }
        });
        Toast.makeText(getActivity(), "a: "+list.isEmpty(), Toast.LENGTH_SHORT).show();

        return view;
    }

    private void LoadContacts() {

        databaseReference.child("user_profiles").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(list!=null) {
                    list.clear();  // v v v v important (eliminate duplication of data)

                }

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RecyclerViewList contacts = postSnapshot.getValue(RecyclerViewList.class);

                    ContentResolver contentResolver = Objects.requireNonNull(getActivity()).getContentResolver();
                    Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
                    if (Objects.requireNonNull(cursor).getCount() > 0) {
                        while (cursor.moveToNext()) {

                            int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                            if (hasPhoneNumber > 0) {
                                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                Cursor phoneCursor = contentResolver.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id},
                                        null);
                                if (phoneCursor != null) {
                                    if (phoneCursor.moveToNext()) {
                                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        try {

                                            phoneNumber = phoneNumber.replace(")","").replace("(","").replace("-","").replace(" ","");
                                            if (Objects.requireNonNull(contacts).getPhone_number().contains(phoneNumber) && !TextUtils.equals(uid, contacts.getUid())){
                                                Toast.makeText(getActivity(), ""+contacts.getUid(), Toast.LENGTH_SHORT).show();
                                                list.add(contacts);

                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString(PHONE_NAME_ON_DEVICE+contacts.getPhone_number(), name);
                                                editor.apply();

//                                                storing contacts data
                                               // String uid_store = contacts.getUid().substring(contacts.getUid().length()-7, contacts.getUid().length())
                                                SharedPreferences.Editor editor1 = sharedPreferencesContactsData.edit();
                                                editor.putString(contacts.getPhone_number()+"CN",phoneNumber); // contacts number
                                                editor.putString(contacts.getPhone_number()+"PN",contacts.getProfile_name());
                                                editor.putString(contacts.getPhone_number()+"PURL",contacts.getProfile_image_url());
                                                editor.putString(contacts.getPhone_number()+"UID",contacts.getUid());
                                                count++;
                                                editor.putString("count", String.valueOf(count));
                                                editor1.apply();

                                                Log.w("raky", "bingo");


                                            }
                                            //Log.w("raky", name+" "+phoneNumber);

                                        }
                                        catch (NullPointerException e){
                                            Log.w("raky", "nothing found");
                                        }
                                    }
                                    //At here You can add phoneNUmber and Name to you listView ,ModelClass,Recyclerview
                                    phoneCursor.close();
                                }


                            }
                        }
                    }

                }

                if (list.isEmpty())
                    showEmptyPage();

                adapter = new ContactsRecyclerViewAdapter(getActivity(), list);
                recyclerView.setAdapter(adapter);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Hiding the progress dialog.
                //progressDialog.dismiss();
            }
        });
    }

    private void LoadDataFromSharedPref(){

//        String count = sharedPreferencesContactsData.getString("count", "");
//        int c = Integer.parseInt(count);
//        for (int i=1; i<c ; i++){
//            String phoneNumber, name, uid, url;
//            phoneNumber = sharedPreferences.getString();
//            name = sharedPreferences.getString();
//            uid = sharedPreferences.getString();
//            url = sharedPreferences.getString();
//
//
//        }

        RecyclerViewList contacts = new RecyclerViewList();
        contacts.setPhone_number("+918699139235");
        contacts.setProfile_image_url("https://cloud.netlifyusercontent.com/assets/344dbf88-fdf9-42bb-adb4-46f01eedd629/242ce817-97a3-48fe-9acd-b1bf97930b01/09-posterization-opt.jpg");
        contacts.setProfile_name("Sunny");
        contacts.setUid("dsadsadsadsadsadsa");
        list.add(contacts);

        adapter = new ContactsRecyclerViewAdapter(getActivity(), list);
        recyclerView.setAdapter(adapter);




     /*   databaseReference.child("AangadiaProfile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(list!=null) {
                    list.clear();  // v v v v important (eliminate duplication of data)
                }

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RecyclerViewList aangadiaData = postSnapshot.getValue(RecyclerViewList.class);
                    list.add(aangadiaData);
                }

                if (list.isEmpty())
                    showEmptyPage();

                adapter = new ContactsRecyclerViewAdapter(getActivity(), list);
                recyclerView.setAdapter(adapter);
               // progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Hiding the progress dialog.
               // progressDialog.dismiss();
            }
        });*/
    }


    private void getAllContacts() {
        ContentResolver contentResolver = Objects.requireNonNull(getActivity()).getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (Objects.requireNonNull(cursor).getCount() > 0) {
            while (cursor.moveToNext()) {

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id},
                            null);
                    if (phoneCursor != null) {
                        if (phoneCursor.moveToNext()) {
                            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            try {
                                Log.w("raky: ", name + "  "+phoneNumber);
                               // Log.w("raky number:", phoneNumber);
                            }
                            catch (NullPointerException e){
                                Log.w("raky", "nothing found");
                            }
                            }
                            //At here You can add phoneNUmber and Name to you listView ,ModelClass,Recyclerview
                            phoneCursor.close();
                        }


                    }
                }
            }
        }


//        read contacts permission
public boolean readContactsPermission() {
    if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);
        return false;
    }
    else return true;
}

    //    show empty page
    public void showEmptyPage() {
        try {
            Toast.makeText(getActivity(), "No Contacts Yet!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
//            exception
        }

    }
}