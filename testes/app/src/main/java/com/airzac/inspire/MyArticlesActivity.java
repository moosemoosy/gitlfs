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

public class MyArticlesActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private Toolbar mToolbar;
    private DatabaseReference usersRef, postsRef, userTypeRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage personalityimageRef;
    private FirebaseRecyclerAdapter adapter;
    private ImageButton homeButton, editButton;
    private Button rejected, approved, pending;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_articles);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        personalityimageRef = FirebaseStorage.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Articles");
        mRecyclerView = (RecyclerView) findViewById(R.id.all_author_article_list);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        pending = (Button) findViewById(R.id.pending_button);
        approved = (Button) findViewById(R.id.approved_button);
        rejected = (Button) findViewById(R.id.rejected_button);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        if (mAuth.getCurrentUser()!=null) {
            currentUserID = mAuth.getCurrentUser().getUid();

        }
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

       // displayAllArticles();

        pending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Query postQuery = postsRef.orderByChild("uid").equalTo(currentUserID);

                postQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue()==null)
                        {
                            Toast.makeText(MyArticlesActivity.this, "you haven't written any articles yet!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                FirebaseRecyclerOptions<Posts> options =
                        new FirebaseRecyclerOptions.Builder<Posts>()
                                .setQuery(postQuery, Posts.class).build();

                FirebaseRecyclerAdapter<Posts, MyArticlesActivity.PostViewHolder> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<Posts, MyArticlesActivity.PostViewHolder>(options)
                        {
                            @Override

                            protected void onBindViewHolder(final @NonNull MyArticlesActivity.PostViewHolder holder, int position, @NonNull Posts model) {

                                final String PostKey = getRef(position).getKey();
                                final int status = model.getPublished();
                                if (status==1)
                                {
                                    PostViewHolder.Layout_hide();
                                }
                                holder.personalityname.setText(model.getPersonalityname());
                                holder.authorname.setText("-"+model.getAuthorname());
                                holder.date.setText(model.getDate());
                                holder.articletitle.setText(model.getArticleTitle());
                                if (status==1) {
                                    holder.status.setTextColor(Color.parseColor("#32CD32"));
                                    holder.status.setText("published");
                                }
                                else if(status==0){
                                    holder.status.setTextColor(Color.parseColor("#FFA500"));
                                    holder.status.setText("pending");
                                }
                                else if (status==99)
                                {
                                    holder.status.setTextColor(Color.RED);
                                    holder.status.setText("rejected");
                                }
                                else
                                {
                                    holder.status.setTextColor(Color.GRAY);
                                    holder.status.setText("unknown");
                                }

                                final String htmlshow = model.getArticlehtml();
                                final String articletitle = model.getArticleTitle();
                                final String personalityid = model.getPersonalityid();
                                final String articleid = articletitle+personalityid;
                                // Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                                //Picasso.get().load(model.getPersonalityImage()).into(holder.personalityImage);
                                //StorageReference filePath = personalityimageRef.child(personalityid + ".jpeg");
                                //String link=filePath.toString();
                                //Toast.makeText(MainActivity.this, link, Toast.LENGTH_SHORT).show();

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

                                //Picasso.get().load(downloadurl).into(holder.personalityImage);
                                //Uri downloadUrl = filePath.getDownloadUrl();

                                //Toast.makeText(MainActivity.this, downloadurl, Toast.LENGTH_SHORT).show();

                                //Picasso.get().load(""+downloadurl).into(holder.personalityImage);

                                editButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Toast.makeText(MyArticlesActivity.this, "edit button clicked", Toast.LENGTH_SHORT).show();
                                        Intent editArticleIntent=new Intent(MyArticlesActivity.this, EditArticleActivity.class);
                                        //String articlehtml = htmlshow;
                                        editArticleIntent.putExtra("articleid", articleid);
                                        editArticleIntent.putExtra("artihtml", htmlshow);
                                        editArticleIntent.putExtra("identifier", PostKey);
                                        startActivity(editArticleIntent);

                                    }
                                });

                                holder.root.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Intent viewArticleIntent=new Intent(MyArticlesActivity.this, ViewArticleActivity.class);
                                        //String articlehtml = htmlshow;
                                        viewArticleIntent.putExtra("artihtml", htmlshow);
                                        viewArticleIntent.putExtra("articletitle", articletitle);
                                        startActivity(viewArticleIntent);

                                        //String showhtml = holder.articlehtml.get
                                        //Toast.makeText(MainActivity.this, "html :"+htmlshow, Toast.LENGTH_SHORT).show();
                                        // Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }


                            @NonNull
                            @Override
                            public MyArticlesActivity.PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.edit_articles_layout, viewGroup, false);
                                editButton = (ImageButton)view.findViewById(R.id.edit_pencil_button);
                                MyArticlesActivity.PostViewHolder viewHolder = new MyArticlesActivity.PostViewHolder(view);
                                return viewHolder;
                            }
                        };



                mRecyclerView.setAdapter(firebaseRecyclerAdapter);

                firebaseRecyclerAdapter.startListening();

            }
        });

        approved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Query postQuery = postsRef.orderByChild("uid").equalTo(currentUserID);

                postQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue()==null)
                        {
                            Toast.makeText(MyArticlesActivity.this, "you haven't written any articles yet!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                FirebaseRecyclerOptions<Posts> options =
                        new FirebaseRecyclerOptions.Builder<Posts>()
                                .setQuery(postQuery, Posts.class).build();

                FirebaseRecyclerAdapter<Posts, MyArticlesActivity.PostViewHolder> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<Posts, MyArticlesActivity.PostViewHolder>(options)
                        {
                            @Override

                            protected void onBindViewHolder(final @NonNull MyArticlesActivity.PostViewHolder holder, int position, @NonNull Posts model) {

                                final String PostKey = getRef(position).getKey();
                                final int status = model.getPublished();
                                if (status==0)
                                {
                                    PostViewHolder.Layout_hide();
                                }
                                holder.personalityname.setText(model.getPersonalityname());
                                holder.authorname.setText("-"+model.getAuthorname());
                                holder.date.setText(model.getDate());
                                holder.articletitle.setText(model.getArticleTitle());
                                if (status==1) {
                                    holder.status.setTextColor(Color.parseColor("#32CD32"));
                                    holder.status.setText("published");
                                }
                                else if(status==0){
                                    PostViewHolder.Layout_hide();
                                    holder.status.setTextColor(Color.parseColor("#FFA500"));
                                    holder.status.setText("pending");
                                }
                                else if (status==99)
                                {
                                    PostViewHolder.Layout_hide();
                                    holder.status.setTextColor(Color.RED);
                                    holder.status.setText("rejected");
                                }
                                else
                                {
                                    holder.status.setTextColor(Color.GRAY);
                                    holder.status.setText("unknown");
                                }

                                final String htmlshow = model.getArticlehtml();
                                final String articletitle = model.getArticleTitle();
                                final String personalityid = model.getPersonalityid();
                                final String articleid = articletitle+personalityid;
                                // Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                                //Picasso.get().load(model.getPersonalityImage()).into(holder.personalityImage);
                                //StorageReference filePath = personalityimageRef.child(personalityid + ".jpeg");
                                //String link=filePath.toString();
                                //Toast.makeText(MainActivity.this, link, Toast.LENGTH_SHORT).show();

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

                                //Picasso.get().load(downloadurl).into(holder.personalityImage);
                                //Uri downloadUrl = filePath.getDownloadUrl();

                                //Toast.makeText(MainActivity.this, downloadurl, Toast.LENGTH_SHORT).show();

                                //Picasso.get().load(""+downloadurl).into(holder.personalityImage);

                                editButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Toast.makeText(MyArticlesActivity.this, "edit button clicked", Toast.LENGTH_SHORT).show();
                                        Intent editArticleIntent=new Intent(MyArticlesActivity.this, EditArticleActivity.class);
                                        //String articlehtml = htmlshow;
                                        editArticleIntent.putExtra("articleid", articleid);
                                        editArticleIntent.putExtra("artihtml", htmlshow);
                                        editArticleIntent.putExtra("identifier", PostKey);
                                        startActivity(editArticleIntent);

                                    }
                                });

                                holder.root.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Intent viewArticleIntent=new Intent(MyArticlesActivity.this, ViewArticleActivity.class);
                                        //String articlehtml = htmlshow;
                                        viewArticleIntent.putExtra("artihtml", htmlshow);
                                        viewArticleIntent.putExtra("articletitle", articletitle);
                                        startActivity(viewArticleIntent);

                                        //String showhtml = holder.articlehtml.get
                                        //Toast.makeText(MainActivity.this, "html :"+htmlshow, Toast.LENGTH_SHORT).show();
                                        // Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }


                            @NonNull
                            @Override
                            public MyArticlesActivity.PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.edit_articles_layout, viewGroup, false);
                                editButton = (ImageButton)view.findViewById(R.id.edit_pencil_button);
                                MyArticlesActivity.PostViewHolder viewHolder = new MyArticlesActivity.PostViewHolder(view);
                                return viewHolder;
                            }
                        };



                mRecyclerView.setAdapter(firebaseRecyclerAdapter);

                firebaseRecyclerAdapter.startListening();

            }
        });

        pending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Query postQuery = postsRef.orderByChild("uid").equalTo(currentUserID);

                postQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue()==null)
                        {
                            Toast.makeText(MyArticlesActivity.this, "you haven't written any articles yet!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                FirebaseRecyclerOptions<Posts> options =
                        new FirebaseRecyclerOptions.Builder<Posts>()
                                .setQuery(postQuery, Posts.class).build();

                FirebaseRecyclerAdapter<Posts, MyArticlesActivity.PostViewHolder> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<Posts, MyArticlesActivity.PostViewHolder>(options)
                        {
                            @Override

                            protected void onBindViewHolder(final @NonNull MyArticlesActivity.PostViewHolder holder, int position, @NonNull Posts model) {

                                final String PostKey = getRef(position).getKey();
                                final int status = model.getPublished();
                                if (status==1)
                                {
                                    PostViewHolder.Layout_hide();
                                }
                                holder.personalityname.setText(model.getPersonalityname());
                                holder.authorname.setText("-"+model.getAuthorname());
                                holder.date.setText(model.getDate());
                                holder.articletitle.setText(model.getArticleTitle());
                                if (model.getPublished()==1) {
                                    PostViewHolder.Layout_hide();
                                    holder.status.setTextColor(Color.parseColor("#32CD32"));
                                    holder.status.setText("published");
                                }
                                else if(model.getPublished()==0){
                                    holder.status.setTextColor(Color.parseColor("#FFA500"));
                                    holder.status.setText("pending");
                                }
                                else if (model.getPublished()==99)
                                {
                                    PostViewHolder.Layout_hide();
                                    holder.status.setTextColor(Color.RED);
                                    holder.status.setText("rejected");
                                }
                                else
                                {
                                    holder.status.setTextColor(Color.GRAY);
                                    holder.status.setText("unknown");
                                }

                                final String htmlshow = model.getArticlehtml();
                                final String articletitle = model.getArticleTitle();
                                final String personalityid = model.getPersonalityid();
                                final String articleid = articletitle+personalityid;
                                // Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                                //Picasso.get().load(model.getPersonalityImage()).into(holder.personalityImage);
                                //StorageReference filePath = personalityimageRef.child(personalityid + ".jpeg");
                                //String link=filePath.toString();
                                //Toast.makeText(MainActivity.this, link, Toast.LENGTH_SHORT).show();

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

                                //Picasso.get().load(downloadurl).into(holder.personalityImage);
                                //Uri downloadUrl = filePath.getDownloadUrl();

                                //Toast.makeText(MainActivity.this, downloadurl, Toast.LENGTH_SHORT).show();

                                //Picasso.get().load(""+downloadurl).into(holder.personalityImage);

                                editButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Toast.makeText(MyArticlesActivity.this, "edit button clicked", Toast.LENGTH_SHORT).show();
                                        Intent editArticleIntent=new Intent(MyArticlesActivity.this, EditArticleActivity.class);
                                        //String articlehtml = htmlshow;
                                        editArticleIntent.putExtra("articleid", articleid);
                                        editArticleIntent.putExtra("artihtml", htmlshow);
                                        editArticleIntent.putExtra("identifier", PostKey);
                                        startActivity(editArticleIntent);

                                    }
                                });

                                holder.root.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Intent viewArticleIntent=new Intent(MyArticlesActivity.this, ViewArticleActivity.class);
                                        //String articlehtml = htmlshow;
                                        viewArticleIntent.putExtra("artihtml", htmlshow);
                                        viewArticleIntent.putExtra("articletitle", articletitle);
                                        startActivity(viewArticleIntent);

                                        //String showhtml = holder.articlehtml.get
                                        //Toast.makeText(MainActivity.this, "html :"+htmlshow, Toast.LENGTH_SHORT).show();
                                        // Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }


                            @NonNull
                            @Override
                            public MyArticlesActivity.PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.edit_articles_layout, viewGroup, false);
                                editButton = (ImageButton)view.findViewById(R.id.edit_pencil_button);
                                MyArticlesActivity.PostViewHolder viewHolder = new MyArticlesActivity.PostViewHolder(view);
                                return viewHolder;
                            }
                        };



                mRecyclerView.setAdapter(firebaseRecyclerAdapter);

                firebaseRecyclerAdapter.startListening();

            }
        });

        rejected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Query postQuery = postsRef.orderByChild("uid").equalTo(currentUserID);

                postQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue()==null)
                        {
                            Toast.makeText(MyArticlesActivity.this, "you haven't written any articles yet!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                FirebaseRecyclerOptions<Posts> options =
                        new FirebaseRecyclerOptions.Builder<Posts>()
                                .setQuery(postQuery, Posts.class).build();

                FirebaseRecyclerAdapter<Posts, MyArticlesActivity.PostViewHolder> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<Posts, MyArticlesActivity.PostViewHolder>(options)
                        {
                            @Override

                            protected void onBindViewHolder(final @NonNull MyArticlesActivity.PostViewHolder holder, int position, @NonNull Posts model) {

                                final String PostKey = getRef(position).getKey();
                                final int status = model.getPublished();
                                if (status==1 || status==0)
                                {
                                    PostViewHolder.Layout_hide();
                                }
                                holder.personalityname.setText(model.getPersonalityname());
                                holder.authorname.setText("-"+model.getAuthorname());
                                holder.date.setText(model.getDate());
                                holder.articletitle.setText(model.getArticleTitle());
                                if (model.getPublished()==1) {
                                    holder.status.setTextColor(Color.parseColor("#32CD32"));
                                    holder.status.setText("published");
                                    PostViewHolder.Layout_hide();
                                }
                                else if(model.getPublished()==0){
                                    holder.status.setTextColor(Color.parseColor("#FFA500"));
                                    holder.status.setText("pending");
                                    PostViewHolder.Layout_hide();
                                }
                                else if (model.getPublished()==99)
                                {
                                    holder.status.setTextColor(Color.RED);
                                    holder.status.setText("rejected");
                                }
                                else
                                {
                                    holder.status.setTextColor(Color.GRAY);
                                    holder.status.setText("unknown");
                                }

                                final String htmlshow = model.getArticlehtml();
                                final String articletitle = model.getArticleTitle();
                                final String personalityid = model.getPersonalityid();
                                final String articleid = articletitle+personalityid;
                                // Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                                //Picasso.get().load(model.getPersonalityImage()).into(holder.personalityImage);
                                //StorageReference filePath = personalityimageRef.child(personalityid + ".jpeg");
                                //String link=filePath.toString();
                                //Toast.makeText(MainActivity.this, link, Toast.LENGTH_SHORT).show();

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

                                //Picasso.get().load(downloadurl).into(holder.personalityImage);
                                //Uri downloadUrl = filePath.getDownloadUrl();

                                //Toast.makeText(MainActivity.this, downloadurl, Toast.LENGTH_SHORT).show();

                                //Picasso.get().load(""+downloadurl).into(holder.personalityImage);

                                editButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Toast.makeText(MyArticlesActivity.this, "edit button clicked", Toast.LENGTH_SHORT).show();
                                        Intent editArticleIntent=new Intent(MyArticlesActivity.this, EditArticleActivity.class);
                                        //String articlehtml = htmlshow;
                                        editArticleIntent.putExtra("articleid", articleid);
                                        editArticleIntent.putExtra("artihtml", htmlshow);
                                        editArticleIntent.putExtra("identifier", PostKey);
                                        startActivity(editArticleIntent);

                                    }
                                });

                                holder.root.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Intent viewArticleIntent=new Intent(MyArticlesActivity.this, ViewArticleActivity.class);
                                        //String articlehtml = htmlshow;
                                        viewArticleIntent.putExtra("artihtml", htmlshow);
                                        viewArticleIntent.putExtra("articletitle", articletitle);
                                        startActivity(viewArticleIntent);

                                        //String showhtml = holder.articlehtml.get
                                        //Toast.makeText(MainActivity.this, "html :"+htmlshow, Toast.LENGTH_SHORT).show();
                                        // Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }


                            @NonNull
                            @Override
                            public MyArticlesActivity.PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.edit_articles_layout, viewGroup, false);
                                editButton = (ImageButton)view.findViewById(R.id.edit_pencil_button);
                                MyArticlesActivity.PostViewHolder viewHolder = new MyArticlesActivity.PostViewHolder(view);
                                return viewHolder;
                            }
                        };



                mRecyclerView.setAdapter(firebaseRecyclerAdapter);

                firebaseRecyclerAdapter.startListening();

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    //here it begins

    public static class PostViewHolder extends RecyclerView.ViewHolder
    {
        public static LinearLayout root;
        static RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView authorname, personalityname, date, status, articletitle, articlehtml;
        ImageButton approvebtn, rejectbtn;
        CircleImageView personalityImage;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            authorname = itemView.findViewById(R.id.post_author_name);
            personalityname = itemView.findViewById(R.id.post_personality_name);
            date = itemView.findViewById(R.id.post_date_time);
            personalityImage = itemView.findViewById(R.id.post_profile_image);
            articletitle = itemView.findViewById(R.id.post_article_title);
            status = itemView.findViewById(R.id.status_text);
            approvebtn = itemView.findViewById(R.id.approve_button);
            approvebtn.setVisibility(View.GONE);
            rejectbtn = itemView.findViewById(R.id.reject_button);
            rejectbtn.setVisibility(View.GONE);
            root = itemView.findViewById(R.id.list_root);

        }
        public static void Layout_hide() {
            params.height = 0;
            //itemView.setLayoutParams(params); //This One.
            root.setLayoutParams(params);   //Or This one.
        }

        public void setAuthorname(String string) {
            authorname.setText(string);
        }

    }

    private void sendUserToMainActivity() {
        Intent homeIntent = new Intent(MyArticlesActivity.this, MainActivity.class);
        startActivity(homeIntent);
    }
}
