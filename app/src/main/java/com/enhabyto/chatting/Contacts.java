package com.enhabyto.chatting;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    //Override method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        view = inflater.inflate(R.layout.contacts, container, false);

        recyclerView = view.findViewById(R.id.c_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(getActivity());


      //  progressDialog = new ProgressDialog(ListOfAangadias.this);
        //progressDialog.setMessage("Loading Data...");

        // Setting RecyclerView layout as LinearLayout.
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        LoadContacts();

        //if (readContactsPermission())
        //getAllContacts();

        return view;
    }

    private void LoadContacts() {
        databaseReference.child("user_profiles").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                          //  Log.w("raky: ", name + "  "+phoneNumber);
                                            // Log.w("raky number:", phoneNumber);
                                            if (Objects.requireNonNull(contacts).getPhone_number().contains(phoneNumber)){
                                                list.add(contacts);
                                                Log.w("raky", "bingo");
                                            }
                                             //  Log.w("raky", name + " "+ phoneNumber);
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