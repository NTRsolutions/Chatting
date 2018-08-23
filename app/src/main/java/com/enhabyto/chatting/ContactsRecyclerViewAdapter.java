package com.enhabyto.chatting;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.nfc.Tag;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<RecyclerViewList> UploadInfoList;

    private SharedPreferences sharedPreferencesContactsLoadingInfo, sharedPreferencesChatterDetails;
    private static final String IS_CONTACTS_LOADED = "isContactsLoaded";
    private static final String IS_ALL_CONTACTS_LOADED = "false";

    private static final String PHONE_NAME_MATCHER = "phoneNameMatcher";
    private static final String PHONE_NAME_ON_DEVICE = "Na";

    private static final String RECEIVER_DETAILS = "receiver_details";
    private static final String UID = "uid";
    private static final String PROFILE_IMAGE_URL = "profileImageUrl";
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String PROFILE_NAME = "profileName";
    private static final String NAME_ON_DEVICE = "nameOnDevice";


    ContactsRecyclerViewAdapter(Context context, List<RecyclerViewList> TempList) {

        this.UploadInfoList = TempList;

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_contact_list, parent, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final RecyclerViewList UploadInfo = UploadInfoList.get(position);

        final SharedPreferences sharedPreferences = Objects.requireNonNull(context).getSharedPreferences(PHONE_NAME_MATCHER, MODE_PRIVATE);
        sharedPreferencesContactsLoadingInfo = Objects.requireNonNull(context).getSharedPreferences(IS_CONTACTS_LOADED, MODE_PRIVATE);
        sharedPreferencesChatterDetails = Objects.requireNonNull(context).getSharedPreferences(RECEIVER_DETAILS, MODE_PRIVATE);

        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        holder.colorBar.setBackgroundColor(color);

        holder.phoneNumber.setText(UploadInfo.getPhone_number());

        try {
            Log.w("raky", "as: "+UploadInfo.getProfile_image_url()) ;
            if (!TextUtils.isEmpty(UploadInfo.getProfile_image_url())){
                Picasso.with(context)
                        .load(UploadInfo.getProfile_image_url())
                        .centerCrop()
                        .fit()
                        .into(holder.profileImage);
            }
            else {
                Glide.with(context)
                        .load(R.drawable.ic_square_default_profile_picture)
                        .into(holder.profileImage);
            }

            }
            catch (IllegalArgumentException e){
                Glide.with(context)
                        .load(R.drawable.ic_square_default_profile_picture)
                        .into(holder.profileImage);
            }


            String name_on_device = UploadInfo.getName_saved_on_device();
        try {

            if (!TextUtils.isEmpty(name_on_device)){
                holder.profileName.setText(name_on_device);
            }
        }
        catch (NullPointerException e){
            holder.profileName.setText("~ "+UploadInfo.getProfile_name());
        }


//        check if all contacts are loaded
        int pos = holder.getAdapterPosition()+1;
        if (pos == getItemCount()){
            SharedPreferences.Editor editor = sharedPreferencesContactsLoadingInfo.edit();
            editor.putString(IS_ALL_CONTACTS_LOADED, "true");
            editor.apply();
            Log.w("rakyPo", "All Contacts are loaded and saved offline");
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  Log.w("uesruid", UploadInfo.getUid());
                SharedPreferences.Editor editor = sharedPreferencesChatterDetails.edit();
                editor.putString(UID, UploadInfo.getUid());
                editor.putString(PROFILE_IMAGE_URL, UploadInfo.getProfile_image_url());
                editor.putString(PHONE_NUMBER, UploadInfo.getPhone_number());
                editor.putString(PROFILE_NAME, UploadInfo.getProfile_name());
                editor.putString(NAME_ON_DEVICE, UploadInfo.getName_saved_on_device());
                editor.apply();
                context.startActivity(new Intent(context, Chatting.class));
                Contacts contacts = new Contacts();

            }
        });


    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return UploadInfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImage, colorBar;
        TextView profileName, phoneNumber;

        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.rc_profile_image);
            colorBar = itemView.findViewById(R.id.rc_color_bar);
            profileName = itemView.findViewById(R.id.rc_profileName);
            phoneNumber = itemView.findViewById(R.id.rc_phoneNumber);

            cardView = itemView.findViewById(R.id.rc_cardview);

        }
    }
//end
}