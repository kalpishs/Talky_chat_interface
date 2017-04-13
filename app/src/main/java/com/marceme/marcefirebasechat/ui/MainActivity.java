package com.marceme.marcefirebasechat.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marceme.marcefirebasechat.FireChatHelper.ChatHelper;
import com.marceme.marcefirebasechat.R;
import com.marceme.marcefirebasechat.adapter.GroupsChatAdapter;
import com.marceme.marcefirebasechat.adapter.UsersChatAdapter;
import com.marceme.marcefirebasechat.login.LogInActivity;
import com.marceme.marcefirebasechat.model.User;
import com.marceme.marcefirebasechat.model.group;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


/*
* CAUTION: This app is still far away from a production app
* Note: (1) Still fixing some code, and functionality and
*       I don't use FirebaseUI, but recommend you to use it.
* */

public class MainActivity extends Activity {


    private static String TAG =  MainActivity.class.getSimpleName();

    @BindView(R.id.progress_bar_users) ProgressBar mProgressBarForUsers;
    @BindView(R.id.recycler_view_users) RecyclerView mUsersRecyclerView;
    @BindView(R.id.recycler_view_groups) RecyclerView mGroupsRecyclerView;
    @BindView(R.id.add_room_btn) Button add_room;
    @BindView(R.id.room_name_edit_text) EditText room_name;

    private String mCurrentUserUid;
    private List<String>  mUsersKeyList;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserRefDatabase;

    private ChildEventListener mChildEventListener;
    private ChildEventListener mGroupEventListener;

    private UsersChatAdapter mUsersChatAdapter;
    private GroupsChatAdapter mGroupsChatAdapter;
    private DatabaseReference mGroupRefDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //room_name.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);

        // room_name.requestFocus();
        showInputMethod();
        bindButterKnife();
        setAuthInstance();

        setUsersDatabase();
        setGroupDatabase();

        setUserRecyclerView();

        setUsersKeyList();

        setAuthListener();
        setOnClicklistener_Add_Room();




    }
    public void showInputMethod() {

    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }

    private void setAuthInstance() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setUsersDatabase() {
        mUserRefDatabase = FirebaseDatabase.getInstance().getReference().child("users");
    }

    private void setGroupDatabase()
    {
        mGroupRefDatabase = FirebaseDatabase.getInstance().getReference().child("groups");
    }
    private void setUserRecyclerView() {
        mUsersChatAdapter = new UsersChatAdapter(this, new ArrayList<User>());
        mGroupsChatAdapter = new GroupsChatAdapter(this, new ArrayList<group>());
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUsersRecyclerView.setAdapter(mUsersChatAdapter);
        mGroupsRecyclerView.setAdapter(mGroupsChatAdapter);
        //  mUsersRecyclerView.setAdapter(mGroupsChatAdapter);
    }

    private void setUsersKeyList() {
        mUsersKeyList = new ArrayList<String>();
    }


    private void setAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                hideProgressBarForUsers();
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    setUserData(user);
                    queryAllUsers();
                    showgroups();
                } else {
                    // User is signed out
                    goToLogin();
                }
            }
        };
    }
    private void setOnClicklistener_Add_Room()
    {
        add_room.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                if (room_name.getText().length() == 0)
                    return;
                group gp = buildNewUser();
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(room_name.getText().toString(), "");
                mGroupRefDatabase.child(room_name.getText().toString()).child("info").setValue(gp);
                // mGroupRefDatabase.child(room_name.getText().toString()).child("messages");
                room_name.setText("");

            }
        });
    }
    private String getUserDisplayName(){
        return room_name.getText().toString().trim();
    }

    private String getUserAdmin() {
        return mCurrentUserUid;
    }

    private group buildNewUser() {
        return new group(
                getUserDisplayName(),
                getUserAdmin(),
                ChatHelper.generateRandomAvatarForUser(),
                new Date().getTime()
        );


    }




    private void setUserData(FirebaseUser user) {
        mCurrentUserUid = user.getUid();
    }

    private void queryAllUsers() {
        mChildEventListener = getChildEventListener();
        mUserRefDatabase.addChildEventListener(mChildEventListener);
    }

    private void showgroups(){
        mGroupEventListener = getGroupEventListener();
        mGroupRefDatabase.addChildEventListener(mGroupEventListener);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // LoginActivity is a New Task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // The old task when coming back to this activity should be cleared so we cannot come back to it.
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        showProgressBarForUsers();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        clearCurrentUsers();

        if (mChildEventListener != null) {
            mUserRefDatabase.removeEventListener(mChildEventListener);
        }

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }

    private void clearCurrentUsers() {
        mUsersChatAdapter.clear();
        mUsersKeyList.clear();
        mGroupsChatAdapter.clear();
    }

    private void logout() {
        showProgressBarForUsers();
        setUserOffline();
        mAuth.signOut();
    }

    private void setUserOffline() {
        if(mAuth.getCurrentUser()!=null ) {
            String userId = mAuth.getCurrentUser().getUid();
            mUserRefDatabase.child(userId).child("connection").setValue(UsersChatAdapter.OFFLINE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_logout){
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showProgressBarForUsers(){
        mProgressBarForUsers.setVisibility(View.VISIBLE);
    }

    private void hideProgressBarForUsers(){
        if(mProgressBarForUsers.getVisibility()==View.VISIBLE) {
            mProgressBarForUsers.setVisibility(View.GONE);
        }
    }

    private ChildEventListener getChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.exists()){

                    String userUid = dataSnapshot.getKey();
                    // System.out.println("MY USERID "+userUid);

                    if(dataSnapshot.getKey().equals(mCurrentUserUid)){
                        User currentUser = dataSnapshot.getValue(User.class);

                        mUsersChatAdapter.setCurrentUserInfo(userUid, currentUser.getEmail(), currentUser.getCreatedAt());
                    }else {
                        User recipient = dataSnapshot.getValue(User.class);
                        recipient.setRecipientId(userUid);
                        mUsersKeyList.add(userUid);
                        mUsersChatAdapter.refill(recipient);
                    }
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
        };
    }




    private ChildEventListener getGroupEventListener(){
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){

                    String userUid = dataSnapshot.child("info").getKey();
                    // System.out.println("MY USERID "+userUid);

                    /*if(dataSnapshot.getKey().equals(mCurrentUserUid)){
                        User currentUser = dataSnapshot.getValue(User.class);

                        mUsersChatAdapter.setCurrentUserInfo(userUid, currentUser.getEmail(), currentUser.getCreatedAt());
                    }else*/ {
                        group recipient = dataSnapshot.child("info").getValue(group.class);
                        mGroupsChatAdapter.setCurrentUserInfo(mCurrentUserUid, recipient.getDisplayName(), recipient.getCreatedAt() );
                        recipient.setRecipientId(recipient.getDisplayName().toString());
                        //    mUsersKeyList.add(userUid);
                        mGroupsChatAdapter.refill(recipient);
                    }
                }                }


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
        };
    }

}
