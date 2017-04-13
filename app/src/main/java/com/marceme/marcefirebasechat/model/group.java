package com.marceme.marcefirebasechat.model;

import com.google.firebase.database.Exclude;

/**
 * @author Marcelino Yax-marce7j@gmail.com-Android Developer
 *         Created on 12/23/2016.
 */

public class group {

    private String displayName;
    private String admin;
    private int avatarId;
    private long createdAt;

    private String mRecipientId;

    public group() {
    }

    public group(String displayName, String admin, int avatarId, long createdAt) {
        this.displayName = displayName;
        this.admin = admin;
        this.avatarId = avatarId;
        this.createdAt = createdAt;
    }


    public long getCreatedAt() {
        return createdAt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAdmin() {
        return admin;
    }

    public int getAvatarId() {
        return avatarId;
    }

    @Exclude
    public String getRecipientId() {
        return mRecipientId;
    }

    public void setRecipientId(String recipientId) {
        this.mRecipientId = recipientId;
    }
}
