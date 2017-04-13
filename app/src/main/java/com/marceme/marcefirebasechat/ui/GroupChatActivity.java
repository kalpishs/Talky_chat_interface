package com.marceme.marcefirebasechat.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marceme.marcefirebasechat.FireChatHelper.ExtraIntent;
import com.marceme.marcefirebasechat.R;
import com.marceme.marcefirebasechat.adapter.MessageChatAdapter;
import com.marceme.marcefirebasechat.model.ChatMessage;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupChatActivity extends Activity {

    private static final String TAG = ChatActivity.class.getSimpleName();

    @BindView(R.id.recycler_view_chat) RecyclerView mChatRecyclerView;
    @BindView(R.id.edit_text_message) EditText mUserMessageChatText;
    @BindView(R.id.toggleButton3) ToggleButton toggleBt;
    private String mRecipientId;
    private String mCurrentUserId;
    private MessageChatAdapter messageChatAdapter;
    private DatabaseReference messageChatDatabase;
    private ChildEventListener messageChatListener;
    private String processstatus= "";
    private String togglebuttonstatus="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        bindButterKnife();
        setDatabaseInstance();
        setUsersId();
        setChatRecyclerView();
    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }
    private void setDatabaseInstance() {
        String chatRef = getIntent().getStringExtra(ExtraIntent.EXTRA_CHAT_REF);
        messageChatDatabase = FirebaseDatabase.getInstance().getReference().child(chatRef);

/*
        messageChatDatabase = FirebaseDatabase.getInstance().getReference("groups").child(chatRef).child("message");
*/
    }

    private void setUsersId() {
        mRecipientId = getIntent().getStringExtra(ExtraIntent.EXTRA_RECIPIENT_ID);
        mCurrentUserId = getIntent().getStringExtra(ExtraIntent.EXTRA_CURRENT_USER_ID);
    }

    private void setChatRecyclerView() {
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChatRecyclerView.setHasFixedSize(true);
        messageChatAdapter = new MessageChatAdapter(new ArrayList<ChatMessage>());
        mChatRecyclerView.setAdapter(messageChatAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        messageChatListener = messageChatDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {

                if(dataSnapshot.exists()){
                    ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                    if(newMessage.getSender().equals(mCurrentUserId)){
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.SENDER);
                    }else{
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.RECIPIENT);
                    }
                    messageChatAdapter.refillAdapter(newMessage);
                    mChatRecyclerView.scrollToPosition(messageChatAdapter.getItemCount()-1);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();

        if(messageChatListener != null) {
            messageChatDatabase.removeEventListener(messageChatListener);
        }
        messageChatAdapter.cleanUp();

    }



    @OnClick(R.id.btn_send_message)
    public void btnSendMsgListener(View sendButton){

        String senderMessage = mUserMessageChatText.getText().toString().trim();

        toggleBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    togglebuttonstatus = "True";
                    processstatus = "Not Done";
                }
                else
                {
                    togglebuttonstatus = "False";
                    processstatus = "Done";
                }
            }
        });

        if(!senderMessage.isEmpty())
        {
            if(togglebuttonstatus.isEmpty())
            {
                if(toggleBt.isChecked()) {
                    togglebuttonstatus = "True";
                    processstatus = "Not Done";
                }
                else
                {
                    togglebuttonstatus= "False";
                    processstatus = "Done";
                }
            }
            if( !togglebuttonstatus.isEmpty() && !processstatus.isEmpty()) {
                ChatMessage newMessage = new ChatMessage(senderMessage, mCurrentUserId, mRecipientId, togglebuttonstatus, processstatus);
                messageChatDatabase.push().setValue(newMessage);
                mUserMessageChatText.setText("");
            }
        }
    }




}
