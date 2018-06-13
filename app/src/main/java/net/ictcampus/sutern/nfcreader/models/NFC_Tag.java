package net.ictcampus.sutern.nfcreader.models;

import com.google.firebase.database.IgnoreExtraProperties;

// [START blog_user_class]
@IgnoreExtraProperties
public class NFC_Tag {

    public String id;
    public String name;

    public NFC_Tag() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public NFC_Tag(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
// [END blog_user_class]



