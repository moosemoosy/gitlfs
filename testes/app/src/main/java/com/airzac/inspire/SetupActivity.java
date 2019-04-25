package com.airzac.inspire;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName;
    private Button SaveInfoButton;
    private CircleImageView ProfileImage;
    private ProgressDialog LoadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    private StorageReference UserProfileImageRef;
    private CountryCodePicker ccp;
    String currentUserID;
    final static int Gallery_PICK=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        ccp = (CountryCodePicker) findViewById(R.id.setup_country_name);
        SaveInfoButton = (Button) findViewById(R.id.setup_info_button);
        ProfileImage = (CircleImageView)findViewById(R.id.setup_profile_image);
        LoadingBar = new ProgressDialog(this);


        SaveInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAccountSetupInfo();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_PICK);
            }
        });

        UserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists())
                    {
                        if (dataSnapshot.hasChild("profileimage"))
                        {
                            String image = dataSnapshot.child("profileimage").getValue().toString();
                            Picasso.get().load(image).placeholder(R.drawable.profile2).into(ProfileImage);
                        }
                        else
                        {
                            Toast.makeText(SetupActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                        }
                        if (dataSnapshot.hasChild("username"))
                        {
                            String username = dataSnapshot.child("username").getValue().toString();
                            UserName.setText(username);
                        }
                        if (dataSnapshot.hasChild("fullname"))
                        {
                            String username = dataSnapshot.child("fullname").getValue().toString();
                            FullName.setText(username);
                        }
                        if (dataSnapshot.hasChild("country"))
                        {
                            String countryname = dataSnapshot.child("country").getValue().toString();
                        }

                    }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



}




   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode, resultCode, data);



        if(requestCode == Gallery_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();


            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        // crop button clicked
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                LoadingBar.setTitle("Profile Image");
                LoadingBar.setMessage("Uploading profile image, please wait...");
                LoadingBar.show();
                LoadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {

                            Toast.makeText(SetupActivity.this, "profile image stored successfully...", Toast.LENGTH_SHORT).show();

                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();

                                    UserRef.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                        startActivity(selfIntent);

                                                        Toast.makeText(SetupActivity.this, "profile image uploaded.", Toast.LENGTH_SHORT).show();
                                                        LoadingBar.dismiss();
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                        LoadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            }
            else {
                Toast.makeText(SetupActivity.this, "Error: Image can not be cropped. Try Again..", Toast.LENGTH_SHORT).show();
                LoadingBar.dismiss();
            }
        }
    }


    private void saveAccountSetupInfo() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = ccp.getSelectedCountryName();

        if (TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "please enter desired username.", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "please enter your full name.", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "please enter your country", Toast.LENGTH_SHORT).show();
        }
        else{

            LoadingBar.setTitle("Saving Info");
            LoadingBar.setMessage("Please wait, while your details are being saved...");
            LoadingBar.show();
            LoadingBar.setCanceledOnTouchOutside(true);

            final HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", country);
            userMap.put("status", "getting inspired.....using inspire app");
            userMap.put("gender", "none");
            userMap.put("dob", "");


            UserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("usertype")) {
                            String usertype = dataSnapshot.child("usertype").getValue().toString();
                            userMap.put("usertype", usertype);
                        }
                        else
                        {
                            //Toast.makeText(SetupActivity.this, "node does not exist", Toast.LENGTH_SHORT).show();
                            UserRef.child("usertype").setValue("viewer");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {
                        Toast.makeText(SetupActivity.this, "account details saved successfully", Toast.LENGTH_LONG).show();
                        SendUserToMainActivity();
                        LoadingBar.dismiss();
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "error: "+message, Toast.LENGTH_SHORT).show();
                        LoadingBar.dismiss();
                    }

                }
            });

    }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
