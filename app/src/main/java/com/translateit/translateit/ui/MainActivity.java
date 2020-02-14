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
import com.translateit.translateit.utils.ViewPagerAdapter;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{

   private CircleImageView profile_image;
   private TextView username;
   private FirebaseUser firebaseUser;
   private DatabaseReference reference;
   private TabLayout tabLayout;
   private ViewPager viewPager;
   private Toolbar toolbar;
   private FireBaseMethods fireBaseMethods;
   private FloatingActionButton butNewMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intFireBase();
        initWidgets();
        setProfile();
        setChats();




    }




    private void initWidgets()
    {

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        butNewMessage=findViewById(R.id.butNewMessage);


    }
    private void setProfile()
    {
        if(fireBaseMethods.onChangeState())
        {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getUsername());
                    if (user.getImageURL().equals("default")){
                        profile_image.setImageResource(R.mipmap.ic_launcher);
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


                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                    tabLayout.setupWithViewPager(viewPager);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }





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
}
