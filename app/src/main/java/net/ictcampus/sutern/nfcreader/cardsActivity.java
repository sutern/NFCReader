package net.ictcampus.sutern.nfcreader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.ictcampus.sutern.nfcreader.models.NFC_Tag;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.List;

public class cardsActivity extends parentClass {


    private ArrayAdapter NfcListe;
    private static final String TAG = "cardAcivity";
    private DatabaseReference mNFCReference;
    private RecyclerView mNFCRecycler;
    private EditText mNameField;
    NFCForegroundUtil nfcForegroundUtil = null;
    private String m_Text = "";
    Context context;
    private NfcAdapter mAdapter;

    boolean isUsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getColor());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNFCReference = FirebaseDatabase.getInstance().getReference().child("users").child(getUid()).child("NFC-Tags");


        mNFCRecycler = findViewById(R.id.NFC_Tags);

        mNFCRecycler.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new NfcAdapter(this, mNFCReference);
        mNFCRecycler.setAdapter(mAdapter);

        final EditText txtUrl = new EditText(this);


        nfcForegroundUtil = new NFCForegroundUtil(this);

        context = this;


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                Snackbar.make(v, R.string.snackbar_info, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        });

    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void onPause() {
        super.onPause();
        nfcForegroundUtil.disableForeground();
    }

    public void onResume() {
        super.onResume();
        nfcForegroundUtil.enableForeground();
        if (!android.nfc.NfcAdapter.getDefaultAdapter(this.getApplicationContext()).isEnabled()) {
            Toast.makeText(getApplicationContext(), R.string.Warning_NFC_turned_off, Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));

        }


    }

    public void onNewIntent(Intent intent) {
        final Tag tag = intent.getParcelableExtra(android.nfc.NfcAdapter.EXTRA_TAG);
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog);
                        } else {
                            builder = new AlertDialog.Builder(context);
                        }
                        mNameField = new EditText(context);
                        builder.setView(mNameField);

                        builder.setTitle("Name")

                                .setMessage(R.string.Message_Alert_add)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                        DatabaseReference userNameRef = rootRef.child("users").child(uid);
                                        final String id = ByteArrayToHexString(tag.getId());
                                        final ArrayList<String> ids = new ArrayList<String>();
                                        ValueEventListener eventListener = new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot iterablesnapshot : dataSnapshot.getChildren()) {
                                                    String id_db = iterablesnapshot.child("id").getValue().toString();
                                                    if (id == id_db) {
                                                        isUsed = true;
                                                    }

                                                }
                                                if (!isUsed) {
                                                    String Name = mNameField.getText().toString();


                                                    NFC_Tag tag = new NFC_Tag(id, Name);

                                                    // Push the comment, it will appear in the list
                                                    mNFCReference.push().setValue(tag);

                                                    isUsed = false;
                                                } else {
                                                    Toast.makeText(context, R.string.card_used, Toast.LENGTH_LONG).show();

                                                    isUsed = false;
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        };


                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setIcon(R.drawable.ic_dialog_add
                                );

                        mNameField = new EditText(context);
                        builder.setView(mNameField);
                        builder.show();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }


    private static class NfcViewHolder extends RecyclerView.ViewHolder {

        public TextView bodyView;

        public NfcViewHolder(View itemView) {
            super(itemView);

            bodyView = itemView.findViewById(R.id.text_info);
        }
    }

    private static class NfcAdapter extends RecyclerView.Adapter<NfcViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCommentIds = new ArrayList<>();
        private List<NFC_Tag> mComments = new ArrayList<>();
        private String TAG = "NFcadaper";

        public NfcAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    NFC_Tag tag = dataSnapshot.getValue(NFC_Tag.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(tag);
                    notifyItemInserted(mComments.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    NFC_Tag newtag = dataSnapshot.getValue(NFC_Tag.class);
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments.set(commentIndex, newtag);

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    Comment movedComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public NfcViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.tags_cards, parent, false);
            return new NfcViewHolder(view);
        }

        @Override
        public void onBindViewHolder(NfcViewHolder holder, int position) {
            NFC_Tag nfc_tag = mComments.get(position);
            holder.bodyView.setText("Name: " + nfc_tag.name + "\n" + "ID: " + nfc_tag.id);
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }


    }
}
