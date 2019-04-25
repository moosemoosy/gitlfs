package com.airzac.inspire;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MySubscriptionsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private DatabaseReference subscriptionRef, personalityRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage personalityimageRef;
    private FirebaseRecyclerAdapter adapter;
    private RecyclerView mRecyclerView;

    String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_subscriptions);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        personalityimageRef = FirebaseStorage.getInstance();
        subscriptionRef = FirebaseDatabase.getInstance().getReference().child("Subscriptions");
        personalityRef = FirebaseDatabase.getInstance().getReference().child("Personality");
        mRecyclerView = (RecyclerView) findViewById(R.id.my_subscription_list);
        mRecyclerView.hasFixedSize();
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Subscriptions");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //Query subQuery = subscriptionRef.orderByChild(current_user_id);

       /* subQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null)
                {
                    Toast.makeText(MySubscriptionsActivity.this, "you haven't subscribed to any personality yet!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        displaySubList();
    }


    private void displaySubList() {

        final ArrayList<String> subscribelist = new ArrayList<>();
        subscriptionRef.orderByChild(current_user_id).equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        subscribelist.add(childSnapshot.getKey());
                        //Toast.makeText(MySubscriptionsActivity.this, childSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                    }
                    //Toast.makeText(MySubscriptionsActivity.this, "ds size: "+subscribelist.size(), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MySubscriptionsActivity.this, model.getId(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MySubscriptionsActivity.this, "no subscriptions", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<Personality> options =
                new FirebaseRecyclerOptions.Builder<Personality>()
                        .setQuery(personalityRef, Personality.class).build();

        FirebaseRecyclerAdapter<Personality, MySubscriptionsActivity.PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Personality, MySubscriptionsActivity.PostViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final MySubscriptionsActivity.PostViewHolder holder, int position, @NonNull final Personality model) {

                        final  String PostKey = getRef(position).getKey();
                        final String personalitykey = getRef(position).getKey();
                        final String personalityid = model.getId();

                        if (!subscribelist.contains(personalityid))
                        {
                            PostViewHolder.Layout_hide();
                        }

                        holder.personalityname.setText(model.getPersonalityname());
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

                        holder.unsubscribebtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                subscriptionRef.child(personalitykey).child(current_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful())
                                        {
                                            Toast.makeText(MySubscriptionsActivity.this, "unsubscribed!", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            Toast.makeText(MySubscriptionsActivity.this, "already unsubscribed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                holder.unsubscribebtn.setBackgroundResource(R.drawable.gradient2);
                                holder.unsubscribebtn.setText("unsubscribed");
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public MySubscriptionsActivity.PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mysubscriptions, viewGroup, false);
                        MySubscriptionsActivity.PostViewHolder viewHolder = new MySubscriptionsActivity.PostViewHolder(view);
                        return viewHolder;
                    }
                };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }


    public static class PostViewHolder extends RecyclerView.ViewHolder
    {
        public static LinearLayout root;
        static RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView personalityname;
        Button unsubscribebtn;
        CircleImageView personalityImage;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            personalityImage = itemView.findViewById(R.id.sub_personality_image);
            personalityname = itemView.findViewById(R.id.post_personality_name);
            unsubscribebtn = itemView.findViewById(R.id.unsubscribe_button);
            root = itemView.findViewById(R.id.list_root);

        }

        public static void Layout_hide() {
            params.height = 0;
            //itemView.setLayoutParams(params); //This One.
            root.setLayoutParams(params);   //Or This one.
        }
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
        Intent homeIntent = new Intent(MySubscriptionsActivity.this, MainActivity.class);
        startActivity(homeIntent);
    }
}
