package com.example.techst;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class user_activity extends AppCompatActivity implements View.OnClickListener {

    private CoordinatorLayout user_coordinatorlayout;
    private AppBarLayout user_appbarlayout;
    private CollapsingToolbarLayout user_collapsetoolbarlayout;
    private Toolbar user_toolbar;
    private NestedScrollView user_nestedscrollview;
    private FloatingActionButton user_fabadd;
    private ImageView imageView;
    private ProgressBar user_progressbar;
    private Button user_searchbutton;
    private EditText user_searchtext;
    RecyclerView recyclerView;
    BottomNavigationView navbar;


    private FirebaseAuth mAuth;
    private FirebaseUser user;
    FirebaseRecyclerOptions<User_Image> options;
    FirebaseRecyclerAdapter<User_Image, MyViewHolder> adapter;

    Uri galleryUri;
    Uri cameraUri;
    Bitmap rotbit;

    boolean isGalleryAdded = false;
    boolean isCameraAdded = false;


    DatabaseReference DataRef;
    StorageReference StorageRef;


    private String currentPhotoPath;
    private String scannedtext = "Scan to view text";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        //UI connection
        UI_init();

        //set toolbar as actionbar
        setSupportActionBar(user_toolbar);

        //set recyclerview
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);

        //set onClicks
        user_fabadd.setOnClickListener(this);

        //Database initialization
        user = FirebaseAuth.getInstance().getCurrentUser();
        DataRef = FirebaseDatabase.getInstance().getReference("Users/"+user.getUid()).child("ImageLink");
        mAuth = FirebaseAuth.getInstance();
        String username = user.getDisplayName();
        StorageRef = FirebaseStorage.getInstance().getReference().child(username).child("Folder 1");
        LoadData("");

        user_searchbutton.setOnClickListener(this);

        navbar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navbar_home:
                        break;
//                    case R.id.navbar_folder:
//                        startActivity(new Intent(user_activity.this, fileviewer_activity.class));
//                        break;
                }
                return false;
            }
        });

    }



    private void LoadData(String data) {

        Query query = DataRef.orderByChild("scannedtext").startAt(data).endAt(data+"\uf8ff");

        options = new FirebaseRecyclerOptions.Builder<User_Image>().setQuery(query, User_Image.class).build();
        adapter = new FirebaseRecyclerAdapter<User_Image, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull User_Image model) {
                holder.recyclerview_imagename.setText(model.getImagename());
                holder.getRecyclerview_scannedtext.setText(model.getScannedtext());
                if(model.imagename.startsWith("Camera"))
                {
                    Picasso.get().load(model.getUri()).fit().centerInside().rotate(90).into(holder.recyclerview_image);
                }
                else{
                    Picasso.get().load(model.getUri()).into(holder.recyclerview_image);
                }

                holder.v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(user_activity.this, imageinfo_activity.class);
                        intent.putExtra("ImageKey", getRef(position).getKey());
                        startActivity(intent);
                    }

                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_entry, parent, false);
                return new MyViewHolder(v);
            }
        };
        adapter.startListening();

        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.user_logout:
                //user_signout();
                signout_dialog();
                break;
            case R.id.user_deleteaccount:
                //user_deleteaccount();
                delete_dialog();
                break;

        }
        return true;
    }

    private void user_deleteaccount() {
        user_progressbar.setVisibility(View.VISIBLE);
        if(user!= null) {
            user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(@NonNull Void unused) {
                    Intent intent = new Intent(user_activity.this, login_activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }
    }

    private void user_signout() {
        user_progressbar.setVisibility(View.VISIBLE);
        mAuth.signOut();
        Toast.makeText(user_activity.this, "Signing Out", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(user_activity.this, login_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void UI_init() {
        user_coordinatorlayout = findViewById(R.id.user_coordinatorlayout);
        user_appbarlayout = findViewById(R.id.user_appbarlayout);
        user_collapsetoolbarlayout = findViewById(R.id.user_collapsetoolbarlayout);
        user_toolbar = findViewById(R.id.user_toolbar);
        user_nestedscrollview = findViewById(R.id.user_nestedscrollview);
        user_fabadd = findViewById(R.id.user_fabadd);
        user_progressbar = findViewById(R.id.user_progressbar);
        user_searchtext = findViewById(R.id.user_searchtext);
        user_searchbutton = findViewById(R.id.user_searchbutton);
        recyclerView = findViewById(R.id.user_recyclerview);
        navbar = findViewById(R.id.user_bottomnavbar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.user_fabadd:
                image_dialog();
                break;

            case R.id.user_searchbutton:
            LoadData(user_searchtext.getText().toString());
                break;
        }
    }

    private void signout_dialog(){
        new AlertDialog.Builder(user_activity.this)
                .setTitle("Sign Out")
                .setMessage(Html.fromHtml("<font color='#B52400'>Are you sure you want to sign out?</font>"))
                .setPositiveButton(Html.fromHtml("<font color='#B52400'>Yes</font>"), (dialog, which) -> user_signout())
                .setNegativeButton(Html.fromHtml("<font color='#B52400'>No</font>"), null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    private void delete_dialog(){
        new AlertDialog.Builder(user_activity.this)
                .setTitle("Delete")
                .setMessage(Html.fromHtml("<font color='#B52400'>Are you sure you want to delete?</font>"))
                .setPositiveButton(Html.fromHtml("<font color='#B52400'>Yes</font>"), (dialog, which) -> user_deleteaccount())
                .setNegativeButton(Html.fromHtml("<font color='#B52400'>No</font>"), null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void image_dialog() {

        final Dialog dialog = new Dialog(user_activity.this);
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.setTitle("IMAGE");
        //The user will be able to cancel the dialog by clicking anywhere outside the dialog.
        dialog.setCancelable(true);
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.image_dialog);

        //Initializing the views of the dialog.
        Button dialog_camera = dialog.findViewById(R.id.dialog_camera);
        Button dialog_gallery = dialog.findViewById(R.id.dialog_gallery);

        dialog.show();

        dialog_camera.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 user_progressbar.setVisibility(View.VISIBLE);
                 createImageFromCamera();
                 dialog.dismiss();
             }
         });

    dialog_gallery.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            user_progressbar.setVisibility(View.VISIBLE);
            mGetGallery.launch("image/*");
            dialog.dismiss();
        }
    });
    }

    private void createImageFromCamera() {
        File imageFile;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mGetCamera.launch(intent);
        Random rand = new Random();
        int x = rand.nextInt(1000);
        int y = rand.nextInt(10000);
        final String filename = "LocalImage-"+x+y;
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            imageFile = File.createTempFile(filename, ".jpg", storageDirectory );
            currentPhotoPath = imageFile.getAbsolutePath();
            cameraUri= FileProvider.getUriForFile(user_activity.this, "com.example.techst.fileprovider", imageFile);
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            mGetCamera.launch(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadimage(final String imagename, Uri uri) {

        final String key = DataRef.push().getKey();
        StorageRef.child(key+".jpg").putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                StorageRef.child(key+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageLink imagelink = new ImageLink(imagename, uri.toString(), scannedtext);
                        DataRef.child(key).setValue(imagelink).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(user_activity.this, "Data Successfully uploaded", Toast.LENGTH_LONG).show();
                                user_progressbar.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        });
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    ActivityResultLauncher<Intent> mGetCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            rotbit = RotateBitmap(bitmap, 90);
            Random rand = new Random();
            int x = rand.nextInt(1000);
            final String imagename = "Camera-Image-" + x;
            if (imagename != null) {
                uploadimage(imagename, cameraUri);
            }
        }
    });

    ActivityResultLauncher<String> mGetGallery = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if(result!=null)
            {
                galleryUri = result;
                isGalleryAdded = true;
                Random rand = new Random();
                int x = rand.nextInt(1000);
                final String imagename = "Gallery-Image-"+x;
                if(isGalleryAdded != false && imagename != null)
                {
                    uploadimage(imagename, galleryUri);
                }
            }
        }
    });

    @Override
    public void onBackPressed() {
        Toast.makeText(user_activity.this, "Please sign out", Toast.LENGTH_LONG).show();
        //super.onBackPressed();
    }



}

