package com.example.techst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class imageinfo_activity extends AppCompatActivity {

    private CoordinatorLayout imageinfo_coordinatorlayout;
    private AppBarLayout imageinfo_appbarlayout;
    private CollapsingToolbarLayout imageinfo_collapsetoolbarlayout;
    private Toolbar imageinfo_toolbar;
    private NestedScrollView imageinfo_nestedscrollview;
    private ProgressBar imageinfo_progressbar;
    private FirebaseAuth mAuth;
    private ImageView imageinfo_image;
    private TextView imageinfo_imagename;
    private TextView imageinfo_scannedtext;
    private Button imageinfo_scanbutton;
    private Button imageinfo_deletebutton;
    private Button imageinfo_copyclipboardbutton;

    DatabaseReference ref, Dataref;
    StorageReference StorageRef;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageinfo);

        //UI connecttion
        UI_init();

        //set toolbar as actionbar
        setSupportActionBar(imageinfo_toolbar);

        //FIrebase instances
        user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference("Users/"+user.getUid()).child("ImageLink");

        String ImageKey = getIntent().getStringExtra("ImageKey");
        Dataref = ref.child(ImageKey);
        StorageRef = FirebaseStorage.getInstance().getReference().child("Images").child(ImageKey+".jpg");


        ref.child(ImageKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String imagename = snapshot.child("imagename").getValue().toString();
                    String uri = snapshot.child("uri").getValue().toString();
                    String scannedtext = snapshot.child("scannedtext").getValue().toString();

                    if(imagename.startsWith("Camera")){
                        Picasso.get().load(uri).fit().centerInside().rotate(90).into(imageinfo_image);

                    }
                    else {
                        Picasso.get().load(uri).into(imageinfo_image);
                    }
                    imageinfo_imagename.setText(imagename);
                    imageinfo_scannedtext.setText(scannedtext);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imageinfo_deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Dataref.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                startActivity(new Intent(imageinfo_activity.this, user_activity.class));

                            }
                        });
                    }
                });
            }
        });

        imageinfo_scanbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mltext(imageinfo_image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        imageinfo_copyclipboardbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Scanned Text", imageinfo_scannedtext.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(imageinfo_activity.this, "Copied to clipboard", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void mltext(ImageView v) throws IOException {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Bitmap bm=((BitmapDrawable)v.getDrawable()).getBitmap();
        InputImage image = InputImage.fromBitmap(bm, 0);
            Task<Text> result =
                    recognizer.process(image)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text visionText) {
                                    imageinfo_scannedtext.setText(visionText.getText());
                                    Dataref.child("scannedtext").setValue(visionText.getText());
                                   if(visionText.toString().isEmpty()) {
                                       imageinfo_scannedtext.setText("No text found :(");
                                       Dataref.child("scannedtext").setValue("No text found :(");
                                   }
                                }}).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
    }



    private void UI_init() {
        imageinfo_appbarlayout = findViewById(R.id.imageinfo_appbarlayout);
        imageinfo_collapsetoolbarlayout = findViewById(R.id.imageinfo_collapsetoolbarlayout);
        imageinfo_coordinatorlayout = findViewById(R.id.imageinfo_coordinatorlayout);
        imageinfo_nestedscrollview = findViewById(R.id.imageinfo_nestedscrollview);
        imageinfo_progressbar = findViewById(R.id.imageinfo_progressbar);
        imageinfo_toolbar = findViewById(R.id.imageinfo_toolbar);
        imageinfo_image = findViewById(R.id.imageinfo_image);
        imageinfo_imagename = findViewById(R.id.imageinfo_imagename);
        imageinfo_scannedtext = findViewById(R.id.imageinfo_scannedtext);
        imageinfo_scanbutton = findViewById(R.id.imageinfo_scanbutton);
        imageinfo_deletebutton = findViewById(R.id.imageinfo_deletebutton);
        imageinfo_copyclipboardbutton = findViewById(R.id.imageinfo_copyclipboardbutton);
    }
    private void user_signout() {
        imageinfo_progressbar.setVisibility(View.VISIBLE);
        mAuth.signOut();
        Toast.makeText(imageinfo_activity.this, "Signing Out", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(imageinfo_activity.this, login_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
                signout_dialog();
                //user_signout();
                break;

            case R.id.user_deleteaccount:
                delete_dialog();
                //user_deleteaccount();
                break;
        }
        return true;
    }

    private void signout_dialog(){
        new AlertDialog.Builder(imageinfo_activity.this)
                .setTitle(Html.fromHtml("<font color='#B52400'>Sign Out</font>"))
                .setMessage(Html.fromHtml("<font color='#B52400'>Are you sure you want to sign out?</font>"))
                .setPositiveButton(Html.fromHtml("<font color='#B52400'>Yes</font>"), (dialog, which) -> user_signout())
                .setNegativeButton(Html.fromHtml("<font color='#B52400'>No</font>"), null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    private void delete_dialog(){
        new AlertDialog.Builder(imageinfo_activity.this)
                .setTitle(Html.fromHtml("<font color='#B52400'>Delete</font>"))
                .setMessage(Html.fromHtml("<font color='#B52400'>Are you sure you want to delete?</font>"))
                .setPositiveButton(Html.fromHtml("<font color='#B52400'>Yes</font>"), (dialog, which) -> user_deleteaccount())
                .setNegativeButton(Html.fromHtml("<font color='#B52400'>No</font>"), null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void user_deleteaccount() {

        
        imageinfo_progressbar.setVisibility(View.VISIBLE);
        if(user!= null) {
            user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(@NonNull Void unused) {
                    Intent intent = new Intent(imageinfo_activity.this, login_activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(imageinfo_activity.this, user_activity.class));
    }
}