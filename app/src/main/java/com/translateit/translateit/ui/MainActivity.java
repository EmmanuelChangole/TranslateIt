package com.translateit.translateit.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.translateit.translateit.R;
import com.translateit.translateit.fragments.ChatsFragment;
import com.translateit.translateit.fragments.TranslateFragment;
import com.translateit.translateit.models.Chat;
import com.translateit.translateit.models.User;
import com.translateit.translateit.notifictions.Data;
import com.translateit.translateit.utils.FireBaseMethods;
import com.translateit.translateit.utils.NetworkCheck;
import com.translateit.translateit.utils.QueryUtils;
import com.translateit.translateit.utils.ViewPagerAdapter;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.translateit.translateit.utils.GlobalVars.BASE_REQ_URL;
import static com.translateit.translateit.utils.GlobalVars.DEFAULT_LANG_POS;
import static com.translateit.translateit.utils.GlobalVars.LANGUAGE_CODES;

public class MainActivity extends AppCompatActivity
{

   private CircleImageView profile_image;
   private  Spinner spinner;
   private TextView username,tvError;
   private FirebaseUser firebaseUser;
   private DatabaseReference reference;
   private String language;
   private TabLayout tabLayout;
   private ViewPager viewPager;
   private FireBaseMethods fireBaseMethods;
   private FloatingActionButton butNewMessage;
   volatile boolean activityRunning;
   private CircleImageView pro_imageView;
    private static final int IMAGE_REQUEST = 1;
   private ImageView ic_more;
    private Uri imageUri;
    private StorageTask uploadTask;
    StorageReference storageReference;
   private FirebaseUser fuser;
   private final String SAMPLE_CROPPED_IMAGE="SampleCropImage";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        intFireBase();
        initWidgets();
        setChats();
        setProfile();
        setOnclick();


    }

    private void setOnclick()
    {

        pro_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Profile image", Toast.LENGTH_SHORT).show();
            }
        });

        ic_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             setView();
            }
        });
    }

    private void setView()
    {
        LayoutInflater myLayout = LayoutInflater.from(this);
        final View dialogView = myLayout.inflate(R.layout.profile_managment, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        spinner=alertDialog.findViewById(R.id.spLanguage);
        final EditText edUsername=alertDialog.findViewById(R.id.editUserName);
        Button butSubmit=alertDialog.findViewById(R.id.editSubmit);
        Button butCancel=alertDialog.findViewById(R.id.editCancel);
        final CircleImageView imgProfile=alertDialog.findViewById(R.id.editProfileImage);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user.getImageURL().equals("default")){
                    imgProfile.setImageResource(R.drawable.ic_users);
                } else {

                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(imgProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
          checkNetwork();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                language=LANGUAGE_CODES.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        butSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username=edUsername.getText().toString();
                updateProfile(language,username);
                Toast.makeText(MainActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });

        butCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });









    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK)
        {
            if(requestCode==IMAGE_REQUEST)
            {
                final Uri selectedUri=data.getData();
                if(selectedUri!=null)
                {
                    startCrop(selectedUri);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Error cannot open ucrop", Toast.LENGTH_SHORT).show();
                }

            }
            else if(requestCode==UCrop.REQUEST_CROP)
            {
                handleCropResult(data);

            }
        }
        if(requestCode==UCrop.RESULT_ERROR)
        {
            handleCropError(data);
        }



        /*if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)
        {
              imageUri = data.getData();


            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(this, "Upload in preogress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }*/
    }

    private void handleCropError(Intent data)
    {

    }

    private void handleCropResult(Intent data)
    {
        Uri imageURI=UCrop.getOutput(data);
        imageUri=imageURI;
        if(imageUri!=null)
        {
            Toast.makeText(getApplicationContext(), "Uploading", Toast.LENGTH_SHORT).show();
            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(this, "Upload in preogress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }

        }
        else
        {
            Toast.makeText(getApplicationContext(), "Error Uploading", Toast.LENGTH_SHORT).show();
        }

    }

    private void startCrop(Uri selectedUri)
    {
        String destinationFileName=SAMPLE_CROPPED_IMAGE;
        destinationFileName+=".jpg";
        UCrop uCrop=UCrop.of(selectedUri,Uri.fromFile(new File(getCacheDir(),destinationFileName)));
        uCrop.withAspectRatio(1,1);
       /* uCrop.withAspectRatio(3,4);
        uCrop.useSourceImageAspectRatio();
        uCrop.withAspectRatio(2,3);*/
       uCrop.withMaxResultSize(450,450);
       uCrop.withOptions(getCropOptions());
       uCrop.start(MainActivity.this);






    }

    private UCrop.Options getCropOptions()
    {
        UCrop.Options options=new UCrop.Options();
        options.setCompressionQuality(70);
        /*options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);*/
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true);
        options.setStatusBarColor(getResources().getColor(R.color.colorCustomGrey));
        options.setToolbarColor(getResources().getColor(R.color.colorBlue));
        options.setToolbarTitle("Crop image");
         return options;
    }


    private void updateProfile(final String language, final String username)
    {
        String userId=FirebaseAuth.getInstance().getUid();
         final DatabaseReference mReference=FirebaseDatabase.getInstance().
                 getReference(getString(R.string.dbName_users)).
                 child(userId);

         ValueEventListener valueEventListener=new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user= dataSnapshot.getValue(User.class);
                    mReference.child("username").setValue(username);
                    mReference.child("language").setValue(language);

             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         };

         mReference.addListenerForSingleValueEvent(valueEventListener);


    }
    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }

                    return  fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", ""+mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    private void initWidgets()
    {
          activityRunning=true;
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        butNewMessage=findViewById(R.id.butNewMessage);
        tvError=findViewById(R.id.tvError);
        tvError.setVisibility(View.GONE);
        pro_imageView=(CircleImageView)findViewById(R.id.profile_image);
        ic_more=(ImageView)findViewById(R.id.ic_more);
        fuser=FirebaseAuth.getInstance().getCurrentUser();




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
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

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
    private void checkNetwork()
    {
        NetworkCheck net = new NetworkCheck();

        if (net.isNetworkAvailable(MainActivity.this))
        {
            new GetLanguages().execute();



        } else
        {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();

        }

    }

    private class GetLanguages extends AsyncTask<Void,Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            Uri baseUri = Uri.parse(BASE_REQ_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendPath("getLangs")
                    .appendQueryParameter("key",getString(R.string.Yandex_Api))
                    .appendQueryParameter("ui","en");
            Log.e("String Url ---->",uriBuilder.toString());
            return QueryUtils.fetchLanguages(uriBuilder.toString());
        }
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (activityRunning) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, result);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                //  SET DEFAULT LANGUAGE SELECTIONS
                spinner.setSelection(DEFAULT_LANG_POS);

            }
        }
    }

}
