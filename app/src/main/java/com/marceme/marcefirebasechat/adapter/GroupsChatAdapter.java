package com.marceme.marcefirebasechat.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marceme.marcefirebasechat.FireChatHelper.ChatHelper;
import com.marceme.marcefirebasechat.FireChatHelper.ExtraIntent;
import com.marceme.marcefirebasechat.R;
import com.marceme.marcefirebasechat.model.group;
import com.marceme.marcefirebasechat.ui.ChatActivity;

import java.util.List;

public class GroupsChatAdapter extends RecyclerView.Adapter<GroupsChatAdapter.ViewHolderUsers> {

    private List<group> mUsers;
    private Context mContext;
    private String mCurrentAdmin;
    private Long mCurrentUserCreatedAt;
    private String mCurrentUserId;

    public GroupsChatAdapter(Context context, List<group> fireChatUsers) {
        mUsers = fireChatUsers;
        mContext = context;
    }


    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderUsers(mContext,LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderUsers holder, int position) {

        group fireChatUser = mUsers.get(position);

        // Set avatar
        int userAvatarId= ChatHelper.getDrawableAvatarId(fireChatUser.getAvatarId());
        Drawable  avatarDrawable = ContextCompat.getDrawable(mContext,userAvatarId);
        holder.getUserAvatar().setImageDrawable(avatarDrawable);

        // Set display name
        holder.getUserDisplayName().setText(fireChatUser.getDisplayName());

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void refill(group users) {
        mUsers.add(users);
        notifyDataSetChanged();
    }

    public void changeUser(int index, group user) {
        mUsers.set(index,user);
        notifyDataSetChanged();
    }

    public void setCurrentUserInfo(String userUid, String admin, long createdAt) {
        mCurrentUserId = userUid;
        mCurrentAdmin = admin;
        mCurrentUserCreatedAt = createdAt;
    }

    public void clear() {
        mUsers.clear();
    }


    /* ViewHolder for RecyclerView */
    public class ViewHolderUsers extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView mUserAvatar;
        private TextView mUserDisplayName;
        private TextView mStatusConnection;
        private Context mContextViewHolder;

        public ViewHolderUsers(Context context, View itemView) {
            super(itemView);
            mUserAvatar = (ImageView)itemView.findViewById(R.id.img_avatar);
            mUserDisplayName = (TextView)itemView.findViewById(R.id.text_view_display_name);
            mContextViewHolder = context;

            itemView.setOnClickListener(this);
        }

        public ImageView getUserAvatar() {
            return mUserAvatar;
        }

        public TextView getUserDisplayName() {
            return mUserDisplayName;
        }
        public TextView getStatusConnection() {
            return mStatusConnection;
        }


        @Override
        public void onClick(View view) {


            group user = mUsers.get(getLayoutPosition());
           AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
            builder1.setMessage("Recipent: "+user.getRecipientId()+"\n"+mCurrentUserId+"\n "+" "+user.getAdmin());
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        /*   Intent chatIntent = new Intent(mContextViewHolder, GroupChatActivity.class);
            chatIntent.putExtra(ExtraIntent.EXTRA_CURRENT_USER_ID, mCurrentUserId);
            chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_ID, user.getRecipientId());
            chatIntent.putExtra(ExtraIntent.EXTRA_Admin, user.getAdmin());
*/
            //mContextViewHolder.startActivity(chatIntent);


            /*

            Intent chatIntent = new Intent(mContextViewHolder, ChatActivity.class);
            chatIntent.putExtra(ExtraIntent.EXTRA_CURRENT_USER_ID, mCurrentUserId);
            chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_ID, user.getRecipientId());
            // Start new activity
            mContextViewHolder.startActivity(chatIntent);
        */

        }
    }

}
