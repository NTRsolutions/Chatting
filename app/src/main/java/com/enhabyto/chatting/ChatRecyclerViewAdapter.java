package com.enhabyto.chatting;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;


public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<RecyclerViewList> UploadInfoList;

    private static final String LANDING_ACTIVITY = "landingActivity";
    private static final String MY_UID = "my_uid";

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("all_chats");

    private SharedPreferences sharedPreferencesChatterDetails;

    private static final String RECEIVER_DETAILS = "receiver_details";
    private static final String UID = "uid";

    private String status;

    ChatRecyclerViewAdapter(Context context, List<RecyclerViewList> TempList) {

        this.UploadInfoList = TempList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_chat_list, parent, false);

        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final RecyclerViewList UploadInfo = UploadInfoList.get(position);

        holder.profileName.setText(UploadInfo.getName_saved_on_device());

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

        ImageView profileImage;
        TextView profileName;

        ViewHolder(View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.lc_profile_image);
            profileName = itemView.findViewById(R.id.lc_profileName);

        }
    }
//end
}
