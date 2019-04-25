package com.airzac.inspire;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.support.v7.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

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
    private TextView NavProfileUserName, subscribetext, noposts;
    private ImageButton addNewPostButton;
    private Button homesubscribebutton;
    String currentUserID, usertype;
    boolean likechecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef =  FirebaseDatabase.getInstance().getReference().child("Likes");
        commentRef =  FirebaseDatabase.getInstance().getReference().child("Comment");
        personalityRef = FirebaseDatabase.getInstance().getReference().child("Personality");
        subscribeRef = FirebaseDatabase.getInstance().getReference().child("Subscriptions");
        personalityimageRef = FirebaseStorage.getInstance();

        if (mAuth.getCurrentUser()!=null) {
            currentUserID = mAuth.getCurrentUser().getUid();
            userTypeRef = usersRef.child(currentUserID).child("usertype");

        }
        // addNewPostButton = (ImageButton) findViewById(R.id.add_new_post);
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");


        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        addNewPostButton = (ImageButton) findViewById(R.id.add_new_post);
        addNewPostButton.setVisibility(View.GONE);
        subscribetext = (TextView) findViewById(R.id.not_subscribed_text);
        subscribetext.setVisibility(View.GONE);
        noposts =(TextView) findViewById(R.id.no_posts);
        noposts.setVisibility(View.GONE);
        homesubscribebutton = (Button) findViewById(R.id.home_subscribe_button);
        homesubscribebutton.setVisibility(View.GONE);
        homesubscribebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent subscribeIntent = new Intent(MainActivity.this, SubscribeActivity.class);
                startActivity(subscribeIntent);
            }
        });


        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        final Menu nav_menu = navigationView.getMenu();

        userTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    usertype = dataSnapshot.getValue(String.class);

                    if (usertype.equals("author")) {
                        addNewPostButton.setVisibility(View.VISIBLE);
                        nav_menu.findItem(R.id.my_articles).setVisible(true);
                        nav_menu.findItem(R.id.admin_panel).setVisible(false);
                        addNewPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //sendUserToPostActivity();
                                sendUserToPostDetailsActivity();
                            }
                        });
                    } else if (usertype.equals("viewer")) {
                        addNewPostButton.setVisibility(View.GONE);
                        nav_menu.findItem(R.id.my_articles).setVisible(false);
                        nav_menu.findItem(R.id.admin_panel).setVisible(false);
                        //Toast.makeText(MainActivity.this, "usertype:" +usertype, Toast.LENGTH_SHORT).show();
                    } else if (usertype.equals("admin")) {
                        addNewPostButton.setVisibility(View.VISIBLE);
                        nav_menu.findItem(R.id.my_articles).setVisible(true);
                        addNewPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //sendUserToPostActivity();
                                sendUserToPostDetailsActivity();
                            }
                        });
                        //Intent AdminIntent = new Intent(MainActivity.this, AdminPanelActivity.class);
                        //startActivity(AdminIntent);
                        //Toast.makeText(MainActivity.this, "usertype:" +usertype, Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postList = (RecyclerView) findViewById(R.id.all_user_post_list);
        postList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        //fetch();
        postList.setLayoutManager(linearLayoutManager);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_username);

        if (mAuth.getCurrentUser()!=null) {

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(currentUserID))
                    {
                        SendUserToSetupActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("fullname")) {
                            String fullname = dataSnapshot.child("fullname").getValue().toString();
                            NavProfileUserName.setText(fullname);
                        }
                        if (dataSnapshot.hasChild("profileimage")) {
                            String image = dataSnapshot.child("profileimage").getValue().toString();
                            Picasso.get().load(image).placeholder(R.drawable.profile2).into(NavProfileImage);
                        } else {
                            Toast.makeText(MainActivity.this, "Please upload your profile image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });

        displayAllPosts();
        //String articletitle = postsRef.child("personalityname").toString();
        // Toast.makeText(this, "title is: "+articletitle, Toast.LENGTH_LONG).show();
    }

    private void sendUserToPostDetailsActivity() {
        Intent postDetails = new Intent(MainActivity.this, PostFormActivity.class);
        startActivity(postDetails);
    }

    private void displayAllPosts() {

        final ArrayList<String> subscribelist = new ArrayList<>();
        //Toast.makeText(this, currentUserID, Toast.LENGTH_LONG).show();
        subscribeRef.orderByChild(currentUserID).equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                        //Toast.makeText(MainActivity.this, "ds size: "+childSnapshot.getChildrenCount(), Toast.LENGTH_SHORT).show();
                        subscribelist.add(childSnapshot.getKey());
                    }
                }
                else
                {
                    //Toast.makeText(MainActivity.this, "feels empty in here...", Toast.LENGTH_SHORT).show();
                    subscribetext.setVisibility(View.VISIBLE);
                    homesubscribebutton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query postQuery = postsRef.orderByChild("date");
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(postQuery, Posts.class).build();


        FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(final @NonNull PostViewHolder holder, int position, @NonNull final Posts model) {

                        final  String PostKey = getRef(position).getKey();
                        holder.personalityname.setText(model.getPersonalityname());
                        holder.authorname.setText("-"+model.getAuthorname());
                        holder.date.setText(model.getDate());
                        holder.articletitle.setText(model.getArticleTitle());
                        final String htmlshow = model.getArticlehtml();
                        final String personalityid = model.getPersonalityid();

                        //Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                        holder.setLikeButtonStatus(PostKey);
                        // Toast.makeText(MainActivity.this, personalityid, Toast.LENGTH_SHORT).show();
                        //Picasso.get().load(model.getPersonalityImage()).into(holder.personalityImage);
                        //StorageReference filePath = personalityimageRef.child(personalityid + ".jpeg");
                        //String link=filePath.toString();
                        //Toast.makeText(MainActivity.this, link, Toast.LENGTH_SHORT).show();

                        if (!subscribelist.contains(model.getPersonalityid()))
                        {
                            PostViewHolder.Layout_hide();
                        }
                        if(model.getPublished()==0 || model.getPublished()==99)
                        {
                            PostViewHolder.Layout_hide();
                        }

                        //else nopost = 1;
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
                        holder.root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent viewArticleIntent=new Intent(MainActivity.this, ViewArticleActivity.class);
                                //String articlehtml = htmlshow;
                                viewArticleIntent.putExtra("artihtml", htmlshow);
                                viewArticleIntent.putExtra("encrypted", model.getEncrypted());
                                viewArticleIntent.putExtra("decryptkey", model.getDecryptkey());
                                startActivity(viewArticleIntent);

                                //String showhtml = holder.articlehtml.get
                                //Toast.makeText(MainActivity.this, "html :"+htmlshow, Toast.LENGTH_SHORT).show();
                                // Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                            }
                        });

                        holder.commentbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent viewCommentIntent=new Intent(MainActivity.this, CommentsActivity.class);
                                viewCommentIntent.putExtra("postkey", PostKey);
                                startActivity(viewCommentIntent);
                            }
                        });

                        postsRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(PostKey).hasChild("Comments"))
                                {
                                    //Toast.makeText(MainActivity.this, "comment present", Toast.LENGTH_SHORT).show();
                                    int comcount = (int) dataSnapshot.child(PostKey).child("Comments").getChildrenCount();
                                    holder.commentcount.setText(Integer.toString(comcount));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        holder.likebutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                likechecker = true;
                                likeRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (likechecker==true)
                                        {
                                            if (dataSnapshot.child(PostKey).hasChild(currentUserID))
                                            {
                                                likeRef.child(PostKey).child(currentUserID).removeValue();
                                                likechecker = false;
                                            }
                                            else
                                            {
                                                likeRef.child(PostKey).child(currentUserID).setValue(true);
                                                likechecker = false;
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
                    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_layout, viewGroup, false);
                        PostViewHolder viewHolder = new PostViewHolder(view);
                        return viewHolder;
                    }
                };



        postList.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder
    {
        public static LinearLayout root;
        static RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        ImageView likebutton, commentbutton;
        TextView authorname, personalityname, date, time, articletitle, likecount, commentcount, articlehtml;
        CircleImageView personalityImage;
        int counLikes, countComments;
        String currentUserId;
        DatabaseReference likesRef, commentsRef;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            authorname = itemView.findViewById(R.id.post_author_name);
            personalityname = itemView.findViewById(R.id.post_personality_name);
            date = itemView.findViewById(R.id.post_date_time);
            personalityImage = itemView.findViewById(R.id.post_profile_image);
            articletitle = itemView.findViewById(R.id.post_article_title);
            root = itemView.findViewById(R.id.list_root);
            likebutton = (ImageView) itemView.findViewById(R.id.like_button);
            commentbutton = (ImageView) itemView.findViewById(R.id.comment_button);
            likecount = (TextView) itemView.findViewById(R.id.like_count_text);
            commentcount = (TextView) itemView.findViewById(R.id.comment_count_text);
            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            commentsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        public static void Layout_hide() {
            params.height = 0;
            //itemView.setLayoutParams(params); //This One.
            root.setLayoutParams(params);   //Or This one.
        }


        public void setLikeButtonStatus(final String PostKey) {
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(PostKey).hasChild(currentUserId)) {
                        counLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likebutton.setImageResource(R.drawable.like_1);
                        likecount.setText(Integer.toString(counLikes));
                    } else {
                        counLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likebutton.setImageResource(R.drawable.like_0);
                        likecount.setText(Integer.toString(counLikes));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    }


    private void sendUserToPostActivity() {
        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(postIntent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        else {
            CheckUserExistence();
        }
    }

    private void CheckUserExistence() {

    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void UserMenuSelector(MenuItem item)
    {
        switch (item.getItemId())
        {
                /*case R.id.nav_post:
                    SendUserToPostActivity();
                    break;
                    */
            case R.id.nav_profile:
                Intent profileIntent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(profileIntent);
                break;

            case R.id.nav_home:
                Intent homeIntent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(homeIntent);
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.admin_panel:
                Intent adminIntent = new Intent(MainActivity.this, AdminPanelActivity.class);
                startActivity(adminIntent);
                break;
            case R.id.nav_notification:
                Intent notificationIntent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(notificationIntent);
                Toast.makeText(this, "notifications", Toast.LENGTH_SHORT).show();
                break;

            case R.id.my_articles:
                Intent myArticleIntent = new Intent(MainActivity.this, MyArticlesActivity.class);
                startActivity(myArticleIntent);
                break;

            case R.id.nav_subscription:
                Intent mySubIntent = new Intent(MainActivity.this, MySubscriptionsActivity.class);
                startActivity(mySubIntent);
                Toast.makeText(this, "subscription", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_subscribe:
                Toast.makeText(this, "subscribe", Toast.LENGTH_SHORT).show();
                Intent subscribeIntent = new Intent(MainActivity.this, SubscribeActivity.class);
                startActivity(subscribeIntent);
                break;

            case R.id.nav_settings:
                Intent forgotpassword = new Intent(MainActivity.this, ResetPasswordActivity.class);
                startActivity(forgotpassword);
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }


}
