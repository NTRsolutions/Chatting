package com.enhabyto.chatting;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<RecyclerViewList> UploadInfoList;

    private SharedPreferences sharedPreferencesContactsLoadingInfo;
    private static final String IS_CONTACTS_LOADED = "isContactsLoaded";
    private static final String IS_ALL_CONTACTS_LOADED = "false";

    private static final String PHONE_NAME_MATCHER = "phoneNameMatcher";
    private static final String PHONE_NAME_ON_DEVICE = "Na";


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
        Log.w("raky", "adapter worked");

        SharedPreferences sharedPreferences = Objects.requireNonNull(context).getSharedPreferences(PHONE_NAME_MATCHER, MODE_PRIVATE);
        sharedPreferencesContactsLoadingInfo = Objects.requireNonNull(context).getSharedPreferences(IS_CONTACTS_LOADED, MODE_PRIVATE);

        Picasso.with(context)
                .load(UploadInfo.getProfile_image_url())
                .into(holder.profileImage);

        String name_on_device = sharedPreferences.getString(PHONE_NAME_ON_DEVICE+UploadInfo.getPhone_number(), "");
        try {

            if (!TextUtils.isEmpty(name_on_device)){
                holder.profileName.setText(name_on_device);
            }
        }
        catch (NullPointerException e){
            holder.profileName.setText(UploadInfo.getPhone_number()+"   ~"+UploadInfo.getProfile_name());
        }

        holder.profileName.setText(UploadInfo.getProfile_name());
//        check if all contacts are loaded
        int pos = holder.getAdapterPosition()+1;
        if (pos == getItemCount()){
            SharedPreferences.Editor editor = sharedPreferencesContactsLoadingInfo.edit();
            editor.putString(IS_ALL_CONTACTS_LOADED, "true");
            editor.apply();
            Log.w("rakyPo", "All Contacts are loaded");
        }


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

        CircleImageView profileImage;
        TextView profileName;

        ViewHolder(View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.rc_profile_image);
            profileName = itemView.findViewById(R.id.rc_profileName);

        }
    }
//end
}