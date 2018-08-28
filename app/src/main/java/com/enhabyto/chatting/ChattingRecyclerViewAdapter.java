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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ChattingRecyclerViewAdapter extends RecyclerView.Adapter<ChattingRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<RecyclerViewList> UploadInfoList;

    private static final String LANDING_ACTIVITY = "landingActivity";
    private static final String MY_UID = "my_uid";

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("all_chats");

    private SharedPreferences sharedPreferencesChatterDetails;

    private static final String RECEIVER_DETAILS = "receiver_details";
    private static final String UID = "uid";

    private String status;

    ChattingRecyclerViewAdapter(Context context, List<RecyclerViewList> TempList) {

        this.UploadInfoList = TempList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_chatting_list, parent, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final RecyclerViewList UploadInfo = UploadInfoList.get(position);

        String my_uid, receiver_uid, my_uid7, receiver_uid7;

        SharedPreferences sharedPreferences = context.getSharedPreferences(LANDING_ACTIVITY, MODE_PRIVATE);
        sharedPreferencesChatterDetails = context.getSharedPreferences(RECEIVER_DETAILS, MODE_PRIVATE);
        my_uid = sharedPreferences.getString(MY_UID, "");
        receiver_uid = sharedPreferencesChatterDetails.getString(UID, "");
        my_uid7 = my_uid.substring(my_uid.length()-7, my_uid.length());
        receiver_uid7 = receiver_uid.substring(receiver_uid.length()-7, receiver_uid.length());
        status = UploadInfo.getStatus();

        if (TextUtils.equals(my_uid7, UploadInfo.getIdentity())){
           holder.messageByMe.setText(UploadInfo.getMessage());
           holder.dateTime2.setText(UploadInfo.getTime());
           holder.relativeLayout2.setVisibility(View.VISIBLE);
           set_tick(status, holder);
        }
        else if (TextUtils.equals(receiver_uid7, UploadInfo.getIdentity())){
            holder.messageReceived.setText(UploadInfo.getMessage());
            holder.dateTime1.setText(UploadInfo.getTime());
            holder.relativeLayout1.setVisibility(View.VISIBLE);
            final String chat_uid = sharedPreferencesChatterDetails.getString(my_uid+receiver_uid+"uid","");
            databaseReference.child(chat_uid).child(UploadInfo.getPush_uid()).child("status").setValue("seen");
        }

    }

    private void set_tick(String status, ViewHolder viewHolder){
        this.status = status;
            if (TextUtils.isEmpty(status))
                viewHolder.tick.setBackgroundResource(R.drawable.ic_single_tick);
            else if (TextUtils.equals(status, "delivered"))
                viewHolder.tick.setBackgroundResource(R.drawable.ic_double_tick);
            else if (TextUtils.equals(status, "seen"))
                viewHolder.tick.setBackgroundResource(R.drawable.ic_double_tick_seen);
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

        TextView messageReceived, messageByMe, dateTime1, dateTime2;
        RelativeLayout relativeLayout1, relativeLayout2;
        ImageView tick;


        ViewHolder(View itemView) {
            super(itemView);

            messageByMe = itemView.findViewById(R.id.cl_textMessageFromMe);
            messageReceived = itemView.findViewById(R.id.cl_textMessageFromReceiver);
            dateTime1 = itemView.findViewById(R.id.rcl_dateTime1);
            dateTime2 = itemView.findViewById(R.id.rcl_dateTime2);

            relativeLayout2 = itemView.findViewById(R.id.rcl_relativeLayout2);
            relativeLayout1 = itemView.findViewById(R.id.rcl_relativeLayout1);

            tick = itemView.findViewById(R.id.cl_tick);
        }
    }
//end
}