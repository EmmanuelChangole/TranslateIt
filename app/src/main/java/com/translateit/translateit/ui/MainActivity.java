package com.translateit.translateit.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.translateit.translateit.R;
import com.translateit.translateit.fragments.ChatsFragment;
import com.translateit.translateit.fragments.TranslateFragment;
import com.translateit.translateit.models.Chat;
import com.translateit.translateit.models.User;
import com.translateit.translateit.utils.FireBaseMethods;
import com.translateit.translateit.utils.NetworkCheck;
import com.translateit.translateit.utils.ViewPagerAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{

   private CircleImageView profile_image;
   private TextView username,tvError;
   private FirebaseUser firebaseUser;
   private DatabaseReference reference;
   private TabLayout tabLayout;
   private ViewPager viewPager;
   private FireBaseMethods fireBaseMethods;
   private FloatingActionButton butNewMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        intFireBase();
        initWidgets();
        setChats();
        setProfile();

       /* if(NetworkCheck.isNetworkAvailable(getApplicationContext()))
        {    tvError.setVisibility(View.GONE);
            setChats();
            setProfile();

        }
        else
            {
                tvError.setVisibility(View.VISIBLE);




            }*/





    }




    private void initWidgets()
    {

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        butNewMessage=findViewById(R.id.butNewMessage);
        tvError=findViewById(R.id.tvError);
        tvError.setVisibility(View.GONE);


    }
    private void setProfile()
    {
        if(fireBaseMethods.onChangeState())
        {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            reference.keepSynced(true);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    username.setText(user.getUsername());
                    if (user.getImageURL().equals("default")){
                        profile_image.setImageResource(R.drawable.ic_users);
                    } else {


                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }


    }
    private void setChats()
    {
        if(fireBaseMethods.onChangeState())
        {
            reference = FirebaseDatabase.getInstance().getReference("Chats");
            reference.keepSynced(true);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                    int unread = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()){
                            unread++;
                        }
                    }

                    if (unread == 0)
                    {
                        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                    } else {
                        viewPagerAdapter.addFragment(new ChatsFragment(), "("+unread+") Chats");
                    }

                    viewPagerAdapter.addFragment(new TranslateFragment(),"Translate");
                    butNewMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(MainActivity.this,UsersActivity.class);
                            startActivity(intent);
                        }
                    });

                    viewPager.setAdapter(viewPagerAdapter);
                    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
                        {
                            if(viewPagerAdapter.getItem(position)instanceof ChatsFragment)
                            {
                                butNewMessage.setVisibility(View.VISIBLE);
                                butNewMessage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent=new Intent(MainActivity.this,UsersActivity.class);
                                        startActivity(intent);
                                    }
                                });

                            }

                            else
                                {
                                    butNewMessage.setVisibility(View.GONE);

                                }

                        }

                        @Override
                        public void onPageSelected(int position)
                        {
                            if(viewPagerAdapter.getItem(position)instanceof ChatsFragment)
                            {
                                butNewMessage.setVisibility(View.VISIBLE);
                                butNewMessage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent=new Intent(MainActivity.this,UsersActivity.class);
                                        startActivity(intent);
                                    }
                                });

                            }

                            else
                            {
                                butNewMessage.setVisibility(View.GONE);

                            }



                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                    tabLayout.setupWithViewPager(viewPager);
                    tabLayout.getTabAt(1).setIcon(R.drawable.ic_translate);
                    tabLayout.getTabAt(0).setIcon(R.drawable.ic_message);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }





    }

    private void status(String status){
        if(NetworkCheck.isNetworkAvailable(getApplicationContext()))
        { if( fireBaseMethods.onChangeState())
            {
                reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("status", status);
                reference.updateChildren(hashMap);

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status(setCurrentTime());
    }



    private void intFireBase() {

        fireBaseMethods = new FireBaseMethods(this);
        fireBaseMethods.initFirebase();

    }

    public void onStart() {
        super.onStart();
       fireBaseMethods.onChangeState();

    }

    protected void onStop() {
        super.onStop();
        fireBaseMethods.clearState();


    }
    private String setCurrentTime()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
        String format ="Last seen,"+simpleDateFormat.format(new Date());
        return format;
    }
}
