package org.odk.collect.bdrs.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.client.http.HttpResponse;

import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.activities.viewmodels.Notice;
import org.odk.collect.bdrs.preferences.GeneralKeys;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> implements View.OnClickListener {
    private List<Notice> noticeList;
    private Context context;
   // private Button btnCollapseNow;
    //private TextView textNoticeTitle;

    private static int currentPosition = -1;

    public NoticeAdapter(List<Notice> noticeList, Context context) {
        this.noticeList = noticeList;
        this.context = context;
    }

    @Override
    public NoticeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_list_row, parent, false);
        return new NoticeViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final NoticeViewHolder holder, final int position) {
        Notice notice = noticeList.get(position);
        String noticeStatus = notice.getStatus();
        /*String formatedNotificationReadTime = "";
        if(!notice.getNotificationReadTime().equals("") ||
                notice.getNotificationReadTime() != null){
            formatedNotificationReadTime = getFormatedDate(notice.getNotificationReadTime());
        } else {
            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            //String dateToStr = format.format(today);
            formatedNotificationReadTime =getFormatedDate(format.format(today));
            formatedNotificationReadTime = "today";
        }*/

        String formatedNotificationReadTime = getFormatedDate(notice.getNotificationReadTime());

        if (noticeStatus.equals("1")) {
            noticeStatus = "Opened at " + formatedNotificationReadTime;
            holder.textNoticeTitle.setBackgroundColor(Color.GRAY);
        } else {
            noticeStatus = "Unread";
            holder.textNoticeTitle.setBackgroundColor(Color.parseColor("#3b5998"));
        }

        holder.btnOpenReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.etReplyNotice.setVisibility(View.VISIBLE);
                holder.btnReplyNotice.setVisibility(View.VISIBLE);
                holder.btnOpenReply.setVisibility(View.GONE);
            }
        });

        //Log.d("NoticeStat: ", noticeStatus);
        holder.btnReplyNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                String mainServerURL = settings.getString(GeneralKeys.KEY_SERVER_URL, context.getApplicationContext().getString(R.string.default_server_url));
                while (mainServerURL.endsWith("/")) {
                    mainServerURL = mainServerURL.substring(0, mainServerURL.length() - 1);
                }
                String securedServerBaseUrl = mainServerURL.replace("http://", "https://");

                String suid = new Integer(AndroidTutorialApp.uid).toString();
                String replyMsg = holder.etReplyNotice.getText().toString();
                String replyToID = notice.getFromUserID();
                String replyToName = notice.getFullName();

                String getSendReplyParam = "/Main/NotificationReply.php?FromUserID=" + suid + "&ToUserID=" + replyToID + "&Notification=" + replyMsg;
                String finalSendReplyURL = String.valueOf(Uri.parse(securedServerBaseUrl + getSendReplyParam));

                Log.d("ReplyURL: ", finalSendReplyURL);

                goStatusChangeURL(finalSendReplyURL);
                Toast.makeText(context, "Succesfully Replied!", Toast.LENGTH_LONG).show();
                holder.etReplyNotice.setText("");
                holder.etReplyNotice.setVisibility(View.GONE);
                holder.btnReplyNotice.setVisibility(View.GONE);
                holder.btnOpenReply.setVisibility(View.VISIBLE);
                //Log.d("ReplyMSG: ", replyMsg + " from " + suid + " to " + replyToName + "("+replyToID+")");
            }
        });
        String formatedDataEntryDate = getFormatedDate(notice.getDataEntryDate());
        holder.textNoticeTitle.setText(notice.getFullName() + "  " + formatedDataEntryDate);

        holder.textNoticeTitle.setTextColor(Color.WHITE);

        holder.btnCollapseNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reloding the list
                //currentPosition = position;
                currentPosition = -1;
                //reloding the list

               notifyDataSetChanged();
                //notifyItemChanged(position);

                //notify();

                Timber.tag("NoticeChangeURL::").d("Activity Reloaded.");

            }
        });

        holder.textNoticeFrom.setText(notice.getFullName());

        holder.textNoticeEntryDate.setText(formatedDataEntryDate);

        holder.textNoticeStatus.setText(noticeStatus);
        holder.textNoticeMessage.setText(notice.getNotification());

        holder.linearLayout.setVisibility(View.GONE);

        //if the position is equals to the item position which is to be expanded
        if (currentPosition == position) {
            //creating an animation
            Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);

            //toggling visibility
            holder.linearLayout.setVisibility(View.VISIBLE);

            //adding sliding effect
            holder.linearLayout.startAnimation(slideDown);
        }


        holder.textNoticeTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //getting the position of the item to expand it
                currentPosition = position;
                //currentPosition = 0;

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                String mainServerURL = settings.getString(GeneralKeys.KEY_SERVER_URL, context.getApplicationContext().getString(R.string.default_server_url));
                while (mainServerURL.endsWith("/")) {
                    mainServerURL = mainServerURL.substring(0, mainServerURL.length() - 1);
                }
                String securedNotificationChangeUrl = mainServerURL.replace("http://", "https://");
                //Get Notification URL
                //String getNotificationUrl = getResources().getString(R.string.default_odk_get_notification);
                String getNotificationStatusChangeUrl = "/Main/NotificationReadStatus.php?NotifyID=" + notice.getId() + "&Status=1";

                // Concatenate Server URL and Notification URL
                String notificationStatusChangeURL = securedNotificationChangeUrl + getNotificationStatusChangeUrl;

                //GoToURL(notificationStatusChangeURL);
                //String notificationStatusChangeURL = "https://ecds.solversbd.com" + "/Main/NotificationReadStatus.php?NotifyID=" + notice.getId() + "&Status=1";
                //goStatusChangeURL(notificationStatusChangeURL);

                Timber.tag("NoticeChangeURL::").d(notificationStatusChangeURL);
                //reloding the list
                //notifyDataSetChanged();
                //notifyItemChanged(position);
                notifyItemRangeChanged(position, noticeList.size());

                goStatusChangeURL(notificationStatusChangeURL);
            }
        });
    }

    @Override
    public int getItemCount() {
        Timber.tag("NoticeDataSize::").d(String.valueOf(noticeList.size()));
        return noticeList.size();

    }

    @Override
    public void onClick(View v) {

    }

    class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView textNoticeFrom, textNoticeEntryDate, textNoticeStatus, textNoticeMessage;
        TextView textNoticeTitle;
        EditText etReplyNotice;
        Button btnCollapseNow, btnOpenReply, btnReplyNotice;
        LinearLayout linearLayout;

        NoticeViewHolder(View itemView) {
            super(itemView);

            textNoticeTitle = itemView.findViewById(R.id.textNoticeTitle);
            textNoticeFrom = itemView.findViewById(R.id.textNoticeFrom);
            textNoticeEntryDate = itemView.findViewById(R.id.textNoticeEntryDate);
            textNoticeStatus = itemView.findViewById(R.id.textNoticeStatus);
            textNoticeMessage = itemView.findViewById(R.id.textNoticeMessage);
            btnCollapseNow = itemView.findViewById(R.id.btnCollapse);

            btnOpenReply = itemView.findViewById(R.id.btnOpenReply);
            etReplyNotice = itemView.findViewById(R.id.etReplyNotice);
            btnReplyNotice = itemView.findViewById(R.id.btnReplyNotice);

            linearLayout = itemView.findViewById(R.id.linearLayout);
        }
    }

    void GoToURL(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void goStatusChangeURL(String statusChangeURL) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        //String url ="https://www.google.com";
        String url = statusChangeURL;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //textView.setText("Response is: "+ response.substring(0,500));
                        Timber.tag("NoticeChangeURLRes::").d(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //textView.setText("That didn't work!");
                Timber.tag("NoticeChangeURLRes::").d(error);
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public String getFormatedDate(String inputDateFormate) {
        String formatedDate = "null";
        @SuppressLint("SimpleDateFormat") DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        @SuppressLint("SimpleDateFormat") DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy hh:mm aa");
        //String inputDateStr="2013-06-24";
        String inputDateStr = inputDateFormate;
        try {
            Date date = inputFormat.parse(inputDateStr);
            assert date != null;
            formatedDate = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formatedDate;
    }
}

