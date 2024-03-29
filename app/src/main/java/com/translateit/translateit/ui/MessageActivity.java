package com.translateit.translateit.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.google.android.gms.common.api.Api;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.translateit.translateit.R;
import com.translateit.translateit.adapters.MessageAdapter;
import com.translateit.translateit.fragments.APIService;
import com.translateit.translateit.models.Chat;
import com.translateit.translateit.models.User;
import com.translateit.translateit.notifictions.Client;
import com.translateit.translateit.notifictions.Data;
import com.translateit.translateit.notifictions.MyResponse;
import com.translateit.translateit.notifictions.Sender;
import com.translateit.translateit.notifictions.Token;
import com.translateit.translateit.utils.QueryUtils;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.translateit.translateit.utils.GlobalVars.BASE_REQ_URL;

public class MessageActivity extends AppCompatActivity {

    private CircleImageView profile_image;
    private TextView username, status;
    private FirebaseUser fuser;
    private String receiverLanguage;
    private String senderLanuage;
    private DatabaseReference reference;
    private ImageButton btn_send;
    private ImageButton btn_voice;
    private EditText text_send;
    private MessageAdapter messageAdapter;
    private List<Chat> mchat;
    private RecyclerView recyclerView;
    private Intent intent;
    private ValueEventListener seenListener;
    private String userid;
    private APIService apiService;
    private boolean notify = false;
    volatile boolean activityRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        initWidets(); // methods that initialize the widgets
        intServices();// methods that initialize the services need i.e the google
        getLanguage();// methods used get the language for both sender and receiver
        onSendMessages();// this method listen to a click when send button is clicked
        setReceiverProfile();// this method get the receiver profile i.e name and profile

        text_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    btn_send.setVisibility(View.INVISIBLE);
                    btn_voice.setVisibility(View.VISIBLE);


                } else {
                    btn_voice.setVisibility(View.INVISIBLE);
                    btn_send.setVisibility(View.VISIBLE);


                }

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });


    }

    private void setReceiverProfile() {

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getStatus().equals("online")) {
                    status.setText("Online");

                } else {
                    status.setText(user.getStatus());
                }


                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.drawable.ic_users);
                } else {

                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }

                readMesagges(fuser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenMessage(userid);

    }

    private void onSendMessages() {
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                final String[] msg = {text_send.getText().toString()};

                if (!msg[0].equals("")) {
                    new TranslateText().execute(msg[0]);
                    Toast.makeText(MessageActivity.this,msg[0],Toast.LENGTH_LONG).show();


                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

    }

    private void intServices() {
        activityRunning = true;
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
    }


    private void initWidets() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        btn_voice = findViewById(R.id.btn_voice);
        text_send = findViewById(R.id.text_send);
        status = findViewById(R.id.status);



    }

    private void getLanguage() {
        /*Getting language for a receiver*/
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        ref.keepSynced(true);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String language = user.getLanguage();
                setReceiverLanguage(language);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /*getting language for sender*/
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        ref.keepSynced(true);
        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String language = user.getLanguage();
                setSenderLanguage(language);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void setReceiverLanguage(String language) {
        receiverLanguage = language;


    }

    private void setSenderLanguage(String language) {
        senderLanuage = language;


    }

    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.keepSynced(true);
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);


        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());
        chatRefReceiver.keepSynced(true);

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void readMesagges(final String myid, final String userid, final String imageurl) {
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }


    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status(setCurrentTime());
        currentUser("none");
    }

    private String setCurrentTime()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
        String format ="Last seen,"+simpleDateFormat.format(new Date());

        return format;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityRunning = false;
    }


    private void sendNotifiaction(String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher, username+": "+message, "New Message",
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    /*Class that translate text in background and sent the text as translate*/
    private class TranslateText extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... input) {
            if (input[0].isEmpty()) {
                return "";

            } else {

                Uri baseUri = Uri.parse(BASE_REQ_URL);
                Uri.Builder uriBuilder = baseUri.buildUpon();
                uriBuilder
                        .appendQueryParameter("key", getString(R.string.Yandex_Api))
                        .appendQueryParameter("target",receiverLanguage)
                        .appendQueryParameter("source",senderLanuage)
                        .appendQueryParameter("q", input[0]);
                Log.e("String Url ---->", uriBuilder.toString());
                Log.e("Translate", "sender:"+senderLanuage+" "+"Receiver:"+receiverLanguage);
                return QueryUtils.fetchTranslation(uriBuilder.toString());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (activityRunning)
                sendMessage(fuser.getUid(), userid, result);
        }
    }
}
