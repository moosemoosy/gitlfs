package com.airzac.inspire;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.RelativeLayout;
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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PendingArticlesActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference personalityRef, usersRef, postsRef, userTypeRef, likeRef, commentRef, subscribeRef;
    private FirebaseStorage personalityimageRef;
    //private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName, subscribetext;
    private ImageButton addNewPostButton;
    private Button homesubscribebutton, pendingbtn , approvedbtn, rejectedbtn;
    String currentUserID, usertype;
    boolean likechecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_articles);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        commentRef = FirebaseDatabase.getInstance().getReference().child("Comment");
        personalityRef = FirebaseDatabase.getInstance().getReference().child("Personality");
        subscribeRef = FirebaseDatabase.getInstance().getReference().child("Subscriptions");
        personalityimageRef = FirebaseStorage.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Approve Pending Articles");

        pendingbtn = (Button) findViewById(R.id.pending_button);
        approvedbtn = (Button) findViewById(R.id.approved_button);
        rejectedbtn = (Button) findViewById(R.id.rejected_button);


        if (mAuth.getCurrentUser() != null) {
            currentUserID = mAuth.getCurrentUser().getUid();
            userTypeRef = usersRef.child(currentUserID).child("usertype");
        }
        // addNewPostButton = (ImageButton) findViewById(R.id.add_new_post);

        userTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usertype = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        postList = (RecyclerView) findViewById(R.id.all_user_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        //fetch();
        postList.setLayoutManager(linearLayoutManager);


        //displayAllPosts();
        //String articletitle = postsRef.child("personalityname").toString();
        // Toast.makeText(this, "title is: "+articletitle, Toast.LENGTH_LONG).show();

        pendingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pendingbtn.setBackgroundColor(Color.WHITE);
                approvedbtn.setBackgroundColor(Color.parseColor("#32CD32"));
                rejectedbtn.setBackgroundColor(Color.parseColor("#f44336"));
                Query postQuery = postsRef.orderByChild("date");
                FirebaseRecyclerOptions<Posts> options =
                        new FirebaseRecyclerOptions.Builder<Posts>()
                                .setQuery(postQuery, Posts.class).build();

                FirebaseRecyclerAdapter<Posts, PendingArticlesActivity.PostViewHolder> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<Posts, PendingArticlesActivity.PostViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(final @NonNull PendingArticlesActivity.PostViewHolder holder, int position, @NonNull Posts model) {

                                final String PostKey = getRef(position).getKey();
                                holder.personalityname.setText(model.getPersonalityname());
                                holder.authorname.setText("-" + model.getAuthorname());
                                holder.date.setText(model.getDate());
                                holder.articletitle.setText(model.getArticleTitle());
                                final String htmlshow = model.getArticlehtml();
                                final String personalityid = model.getPersonalityid();
                                if (model.getPublished()==0) {

                                    holder.statusText.setTextColor(Color.parseColor("#FFA500"));
                                    holder.approvebtn.setVisibility(View.VISIBLE);
                                    holder.statusText.setText("status: pending");

                                }
                                //Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                                // Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                                //Picasso.get().load(model.getPersonalityImage()).into(holder.personalityImage);
                                //StorageReference filePath = personalityimageRef.child(personalityid + ".jpeg");
                                //String link=filePath.toString();
                                //Toast.makeText(MainActivity.this, link, Toast.LENGTH_SHORT).show();
                                else if (model.getPublished()==1) {
                                    PostViewHolder.Layout_hide();
                                    //PendingArticlesActivity.PostViewHolder.Layout_hide();
                                    holder.statusText.setTextColor(Color.parseColor("#32CD32"));
                                    holder.statusText.setText("status: published");
                                    holder.approvebtn.setVisibility(View.GONE);

                                }

                                else if (model.getPublished()==99)
                                {
                                    PostViewHolder.Layout_hide();
                                    holder.statusText.setTextColor(Color.parseColor("#ff0000"));
                                    holder.statusText.setText("status: rejected");
                                    holder.approvebtn.setVisibility(View.GONE);
                                    holder.rejectbtn.setVisibility(View.GONE);
                                }

                                StorageReference gsReference =
                                        personalityimageRef.getReferenceFromUrl("gs://inspire18-a6804.appspot.com/Personality Images").child(personalityid + ".jpeg");

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

                                holder.approvebtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        postsRef.child(PostKey).child("published").setValue(1);
                                    }
                                });

                                holder.rejectbtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        postsRef.child(PostKey).child("published").setValue(99);
                                    }
                                });
                                //Picasso.get().load(downloadurl).into(holder.personalityImage);
                                //Uri downloadUrl = filePath.getDownloadUrl();

                                //Toast.makeText(MainActivity.this, downloadurl, Toast.LENGTH_SHORT).show();

                                //Picasso.get().load(""+downloadurl).into(holder.personalityImage);
                                holder.root.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Intent viewArticleIntent = new Intent(PendingArticlesActivity.this, ViewArticleActivity.class);
                                        //String articlehtml = htmlshow;
                                        viewArticleIntent.putExtra("artihtml", htmlshow);
                                        startActivity(viewArticleIntent);

                                        //String showhtml = holder.articlehtml.get
                                        //Toast.makeText(MainActivity.this, "html :"+htmlshow, Toast.LENGTH_SHORT).show();
                                        // Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }


                            @NonNull
                            @Override
                            public PendingArticlesActivity.PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.edit_articles_layout, viewGroup, false);
                                PendingArticlesActivity.PostViewHolder viewHolder = new PendingArticlesActivity.PostViewHolder(view);
                                return viewHolder;
                            }

                        };


                postList.setAdapter(firebaseRecyclerAdapter);

                firebaseRecyclerAdapter.startListening();
            }
        });

        approvedbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pendingbtn.setBackgroundColor(Color.parseColor("#FFA500"));
                approvedbtn.setBackgroundColor(Color.WHITE);
                rejectedbtn.setBackgroundColor(Color.parseColor("#f44336"));
                Query postQuery = postsRef.orderByChild("date");
                FirebaseRecyclerOptions<Posts> options =
                        new FirebaseRecyclerOptions.Builder<Posts>()
                                .setQuery(postQuery, Posts.class).build();

                FirebaseRecyclerAdapter<Posts, PendingArticlesActivity.PostViewHolder> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<Posts, PendingArticlesActivity.PostViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(final @NonNull PendingArticlesActivity.PostViewHolder holder, int position, @NonNull Posts model) {

                                final String PostKey = getRef(position).getKey();
                                holder.personalityname.setText(model.getPersonalityname());
                                holder.authorname.setText("-" + model.getAuthorname());
                                holder.date.setText(model.getDate());
                                holder.articletitle.setText(model.getArticleTitle());
                                final String htmlshow = model.getArticlehtml();
                                final String personalityid = model.getPersonalityid();
                                if (model.getPublished()==0) {
                                    PostViewHolder.Layout_hide();
                                    holder.statusText.setTextColor(Color.parseColor("#FFA500"));
                                    holder.approvebtn.setVisibility(View.VISIBLE);
                                    holder.statusText.setText("status: pending");

                                }
                                //Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                                // Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                                //Picasso.get().load(model.getPersonalityImage()).into(holder.personalityImage);
                                //StorageReference filePath = personalityimageRef.child(personalityid + ".jpeg");
                                //String link=filePath.toString();
                                //Toast.makeText(MainActivity.this, link, Toast.LENGTH_SHORT).show();
                                else if (model.getPublished()==1) {
                                    //PendingArticlesActivity.PostViewHolder.Layout_hide();
                                    holder.statusText.setTextColor(Color.parseColor("#32CD32"));
                                    holder.statusText.setText("status: published");
                                    holder.approvebtn.setVisibility(View.GONE);

                                }
                                else if (model.getPublished()==99)
                                {
                                    PostViewHolder.Layout_hide();
                                    holder.statusText.setTextColor(Color.parseColor("#ff0000"));
                                    holder.statusText.setText("status: rejected");
                                    holder.approvebtn.setVisibility(View.GONE);
                                    holder.rejectbtn.setVisibility(View.GONE);
                                }

                                StorageReference gsReference =
                                        personalityimageRef.getReferenceFromUrl("gs://inspire18-a6804.appspot.com/Personality Images").child(personalityid + ".jpeg");

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

                                holder.approvebtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        postsRef.child(PostKey).child("published").setValue(1);
                                    }
                                });

                                holder.rejectbtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        postsRef.child(PostKey).child("published").setValue(99);
                                    }
                                });
                                //Picasso.get().load(downloadurl).into(holder.personalityImage);
                                //Uri downloadUrl = filePath.getDownloadUrl();

                                //Toast.makeText(MainActivity.this, downloadurl, Toast.LENGTH_SHORT).show();

                                //Picasso.get().load(""+downloadurl).into(holder.personalityImage);
                                holder.root.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Intent viewArticleIntent = new Intent(PendingArticlesActivity.this, ViewArticleActivity.class);
                                        //String articlehtml = htmlshow;
                                        viewArticleIntent.putExtra("artihtml", htmlshow);
                                        startActivity(viewArticleIntent);

                                        //String showhtml = holder.articlehtml.get
                                        //Toast.makeText(MainActivity.this, "html :"+htmlshow, Toast.LENGTH_SHORT).show();
                                        // Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }


                            @NonNull
                            @Override
                            public PendingArticlesActivity.PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.edit_articles_layout, viewGroup, false);
                                PendingArticlesActivity.PostViewHolder viewHolder = new PendingArticlesActivity.PostViewHolder(view);
                                return viewHolder;
                            }

                        };


                postList.setAdapter(firebaseRecyclerAdapter);

                firebaseRecyclerAdapter.startListening();
            }
        });

        rejectedbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pendingbtn.setBackgroundColor(Color.parseColor("#FFA500"));
                approvedbtn.setBackgroundColor(Color.parseColor("#32CD32"));
                rejectedbtn.setBackgroundColor(Color.WHITE);
                Query postQuery = postsRef.orderByChild("date");
                FirebaseRecyclerOptions<Posts> options =
                        new FirebaseRecyclerOptions.Builder<Posts>()
                                .setQuery(postQuery, Posts.class).build();

                FirebaseRecyclerAdapter<Posts, PendingArticlesActivity.PostViewHolder> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<Posts, PendingArticlesActivity.PostViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(final @NonNull PendingArticlesActivity.PostViewHolder holder, int position, @NonNull Posts model) {

                                final String PostKey = getRef(position).getKey();
                                holder.personalityname.setText(model.getPersonalityname());
                                holder.authorname.setText("-" + model.getAuthorname());
                                holder.date.setText(model.getDate());
                                holder.articletitle.setText(model.getArticleTitle());
                                final String htmlshow = model.getArticlehtml();
                                final String personalityid = model.getPersonalityid();
                                if (model.getPublished()==0) {
                                    PostViewHolder.Layout_hide();
                                    holder.statusText.setTextColor(Color.parseColor("#FFA500"));
                                    holder.approvebtn.setVisibility(View.VISIBLE);
                                    holder.statusText.setText("status: pending");

                                }
                                //Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                                // Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                                //Picasso.get().load(model.getPersonalityImage()).into(holder.personalityImage);
                                //StorageReference filePath = personalityimageRef.child(personalityid + ".jpeg");
                                //String link=filePath.toString();
                                //Toast.makeText(MainActivity.this, link, Toast.LENGTH_SHORT).show();
                                else if (model.getPublished()==1) {
                                    PostViewHolder.Layout_hide();
                                    //PendingArticlesActivity.PostViewHolder.Layout_hide();
                                    holder.statusText.setTextColor(Color.parseColor("#32CD32"));
                                    holder.statusText.setText("status: published");
                                    holder.approvebtn.setVisibility(View.GONE);

                                }

                                else if (model.getPublished()==99)
                                {
                                    holder.statusText.setTextColor(Color.parseColor("#ff0000"));
                                    holder.statusText.setText("status: rejected");
                                    holder.approvebtn.setVisibility(View.GONE);
                                    holder.rejectbtn.setVisibility(View.GONE);
                                }

                                StorageReference gsReference =
                                        personalityimageRef.getReferenceFromUrl("gs://inspire18-a6804.appspot.com/Personality Images").child(personalityid + ".jpeg");

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

                                holder.approvebtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        postsRef.child(PostKey).child("published").setValue(1);
                                    }
                                });

                                holder.rejectbtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        postsRef.child(PostKey).child("published").setValue(99);
                                    }
                                });
                                //Picasso.get().load(downloadurl).into(holder.personalityImage);
                                //Uri downloadUrl = filePath.getDownloadUrl();

                                //Toast.makeText(MainActivity.this, downloadurl, Toast.LENGTH_SHORT).show();

                                //Picasso.get().load(""+downloadurl).into(holder.personalityImage);
                                holder.root.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Intent viewArticleIntent = new Intent(PendingArticlesActivity.this, ViewArticleActivity.class);
                                        //String articlehtml = htmlshow;
                                        viewArticleIntent.putExtra("artihtml", htmlshow);
                                        startActivity(viewArticleIntent);

                                        //String showhtml = holder.articlehtml.get
                                        //Toast.makeText(MainActivity.this, "html :"+htmlshow, Toast.LENGTH_SHORT).show();
                                        // Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }


                            @NonNull
                            @Override
                            public PendingArticlesActivity.PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.edit_articles_layout, viewGroup, false);
                                PendingArticlesActivity.PostViewHolder viewHolder = new PendingArticlesActivity.PostViewHolder(view);
                                return viewHolder;
                            }

                        };


                postList.setAdapter(firebaseRecyclerAdapter);

                firebaseRecyclerAdapter.startListening();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder {
        public static LinearLayout root;
        static RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ImageView likebutton, commentbutton;
        TextView authorname, personalityname, date, time, articletitle, likecount, commentcount, articlehtml, statusText;
        CircleImageView personalityImage;
        int counLikes;
        ImageButton approvebtn, rejectbtn, editpencilbtn;
        String currentUserId;
        DatabaseReference likesRef;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            authorname = itemView.findViewById(R.id.post_author_name);
            personalityname = itemView.findViewById(R.id.post_personality_name);
            date = itemView.findViewById(R.id.post_date_time);
            personalityImage = itemView.findViewById(R.id.post_profile_image);
            articletitle = itemView.findViewById(R.id.post_article_title);
            root = itemView.findViewById(R.id.list_root);
            approvebtn = (ImageButton) itemView.findViewById(R.id.approve_button);
            rejectbtn = (ImageButton) itemView.findViewById(R.id.reject_button);
            editpencilbtn = (ImageButton) itemView.findViewById(R.id.edit_pencil_button);
            editpencilbtn.setVisibility(View.GONE);
            statusText = (TextView) itemView.findViewById(R.id.status_text);
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        public static void Layout_hide() {
            params.height = 0;
            //itemView.setLayoutParams(params); //This One.
            root.setLayoutParams(params);   //Or This one.
        }

    }
}
