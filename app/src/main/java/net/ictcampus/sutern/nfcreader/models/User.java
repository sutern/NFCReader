package net.ictcampus.sutern.nfcreader.models;

import com.google.firebase.database.IgnoreExtraProperties;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

}
// [END blog_user_class]