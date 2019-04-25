package com.airzac.inspire;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SubscribeActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private DatabaseReference usersRef, personalityRef, subscribeRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage personalityimageRef;
    private FirebaseRecyclerAdapter adapter;
    private Button subscribeButton;
    private Toolbar mToolbar;
    boolean subscribe = false;

    String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        personalityimageRef = FirebaseStorage.getInstance();
        subscribeRef =  FirebaseDatabase.getInstance().getReference().child("Subscriptions");
        mRecyclerView = (RecyclerView) findViewById(R.id.all_author_article_list);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(0);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        SnapHelper helper = new LinearSnapHelper();


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Subscribe");

        mRecyclerView.setLayoutManager(linearLayoutManager);
        helper.attachToRecyclerView(mRecyclerView);

        if (mAuth.getCurrentUser()!=null) {
            currentUserID = mAuth.getCurrentUser().getUid();
        }

        personalityRef = FirebaseDatabase.getInstance().getReference().child("Personality");
        displayAllPersonalities();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SubscribeActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }

    private void displayAllPersonalities() {
        //Query personalityQuery = personalityRef.orderByChild("uid").equalTo(currentUserID);

        FirebaseRecyclerOptions<Personality> options =
                new FirebaseRecyclerOptions.Builder<Personality>()
                        .setQuery(personalityRef, Personality.class).build();

        FirebaseRecyclerAdapter<Personality, SubscribeActivity.PostViewHolder> firebaseRecyclerAdapter =
               new FirebaseRecyclerAdapter<Personality, SubscribeActivity.PostViewHolder>(options) {
                   @Override
                   protected void onBindViewHolder(@NonNull final SubscribeActivity.PostViewHolder holder, int position, @NonNull Personality model) {

                       final  String PostKey = getRef(position).getKey();
                       final String personalitykey = getRef(position).getKey();
                       final String personalityid = model.getId();
                       holder.personalityname.setText(model.getPersonalityname());
                       holder.description.setText(model.getDescription());
                       holder.setSubscribeButtonStatus(PostKey);
                       StorageReference gsReference =
                               personalityimageRef.getReferenceFromUrl("gs://inspire18-a6804.appspot.com/Personality Images").child(personalityid+".jpeg");

                       gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                           private String downloadurl;

                           @Override
                           public void onSuccess(Uri uri) {
                               //Log.e("Tuts+", "uri: " + uri.toString());
                               this.downloadurl = uri.toString();
                               //Handle whatever you're going to do with the URL here
                               Picasso.get().load(downloadurl).into(holder.personalityImage);
                               //Toast.makeText(MainActivity.this, downloadurl, Toast.LENGTH_SHORT).show();
                           }

                       });

                       holder.subscribeButton.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                               subscribe = true;
                               subscribeRef.addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       if (subscribe==true)
                                       {
                                           if (dataSnapshot.child(PostKey).hasChild(currentUserID))
                                           {
                                               subscribeRef.child(PostKey).child(currentUserID).removeValue();
                                               subscribe = false;
                                           }
                                           else
                                           {
                                               subscribeRef.child(PostKey).child(currentUserID).setValue(true);
                                               subscribe = false;
                                           }
                                       }
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });
                           }
                       });

                   }

                   @NonNull
                   @Override
                   public SubscribeActivity.PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                       View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subscribe_personalty, viewGroup, false);
                       subscribeButton = (Button)view.findViewById(R.id.subscribe_button);
                       SubscribeActivity.PostViewHolder viewHolder = new SubscribeActivity.PostViewHolder(view);
                       return viewHolder;
                   }
               };



        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder
    {
        public LinearLayout root;
        TextView personalityname, description;
        Button subscribeButton;
        CircleImageView personalityImage;
        DatabaseReference subscribeRef;
        String currentUserId;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            personalityImage =  itemView.findViewById(R.id.personality_image);
            personalityname = itemView.findViewById(R.id.personality_name);
            subscribeButton = (Button) itemView.findViewById(R.id.subscribe_button);
            description = itemView.findViewById(R.id.personality_description);
            root = itemView.findViewById(R.id.list_root);
            root.setOrientation(LinearLayout.HORIZONTAL);
            subscribeRef = FirebaseDatabase.getInstance().getReference().child("Subscriptions");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setSubscribeButtonStatus(final String postKey) {
            subscribeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(postKey).hasChild(currentUserId)) {
                        subscribeButton.setBackgroundResource(R.drawable.gradient2);
                        subscribeButton.setText("Unsubscribe");
                    } else {
                        subscribeButton.setBackgroundResource(R.drawable.gradient1);
                        subscribeButton.setText("Subscribe");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(SubscribeActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
