package com.enhabyto.chatting;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.collection.LLRBNode;

import java.net.IDN;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ChattingRecyclerViewAdapter extends RecyclerView.Adapter<ChattingRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<RecyclerViewList> UploadInfoList;

    private static final String LANDING_ACTIVITY = "landingActivity";
    private static final String MY_UID = "my_uid";


    private SharedPreferences sharedPreferencesChatterDetails;


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

        SharedPreferences sharedPreferences = context.getSharedPreferences(LANDING_ACTIVITY, MODE_PRIVATE);
        String my_uid = sharedPreferences.getString(MY_UID, "");
        my_uid = my_uid.substring(my_uid.length()-7, my_uid.length());

        TextView message_tv = new TextView(context);
        TextView dateTime_tv = new TextView(context);

        ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);



        message_tv.setLayoutParams(lp);
        dateTime_tv.setLayoutParams(lp);
        //message_tv.setPadding(16,12,16,12);
        message_tv.setTextColor(Color.BLACK);
        dateTime_tv.setTextColor(context.getResources().getColor(R.color.black90));
        dateTime_tv.setTextSize(12);
        message_tv.setTextSize(16);
        message_tv.setText(UploadInfo.getMessage());
        dateTime_tv.setText(UploadInfo.getDate_time().substring(UploadInfo.getDate_time().length()-8, UploadInfo.getDate_time().length()));


        if (TextUtils.equals(my_uid, UploadInfo.getIdentity())){
           // message_tv.setBackground(context.getResources().getDrawable(R.drawable.custom_rectangle_message_by_me));
            holder.relativeLayout2.setBackground(context.getResources().getDrawable(R.drawable.custom_rectangle_message_by_me));
            holder.relativeLayout2.setPadding(24,12,24,12);
            holder.relativeLayout2.addView(message_tv);
            holder.relativeLayout2.addView(dateTime_tv);
        }
        else {

          //  message_tv.setBackground(context.getResources().getDrawable(R.drawable.custom_rectangle_message_by_sender));
           // holder.relativeLayout1.addView(message_tv);
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

        TextView messageReceived, messageByMe;
        RelativeLayout relativeLayout1;
        LinearLayout relativeLayout2;


        ViewHolder(View itemView) {
            super(itemView);

          //  messageByMe = itemView.findViewById(R.id.cl_textMessageFromMe);
          //  messageReceived = itemView.findViewById(R.id.cl_textMessageFromReceiver);

            relativeLayout2 = itemView.findViewById(R.id.rcl_relativeLayout2);
            relativeLayout1 = itemView.findViewById(R.id.rcl_relativeLayout1);

        }
    }
//end
}