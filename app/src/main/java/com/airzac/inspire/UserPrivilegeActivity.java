package com.airzac.inspire;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

public class UserPrivilegeActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private Toolbar mToolbar;
    private DatabaseReference usersRef, postsRef, userTypeRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage personalityimageRef;
    private FirebaseRecyclerAdapter adapter;
    private ImageButton homeButton, editButton;
    private EditText searchbar;
    String currentUserID;
    ArrayList<Users> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_privilege);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar = (Toolbar) findViewById(R.id.user_list_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        usersList = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.all_user_list);
        searchbar = (EditText) findViewById(R.id.filter_bar) ;
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        if (mAuth.getCurrentUser()!=null) {
            currentUserID = mAuth.getCurrentUser().getUid();

        }
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        displayAllUsers();


    }



    private void displayAllUsers() {
        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(usersRef, Users.class).build();

        FirebaseRecyclerAdapter<Users, UserPrivilegeActivity.UserViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Users model) {
                        holder.email.setText(getRef(position).getKey());
                        holder.username.setText(model.getUsername());
                        holder.userfullname.setText(model.getFullname());
                    }

                    @NonNull
                    @Override
                    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_layout, viewGroup, false);
                        UserPrivilegeActivity.UserViewHolder viewHolder = new UserPrivilegeActivity.UserViewHolder(view);
                        return viewHolder;
                    }
                };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();

    }

    public static class UserViewHolder extends RecyclerView.ViewHolder
    {
        public LinearLayout root;
        TextView email, username, userfullname;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            email = itemView.findViewById(R.id.user_email);
            username = itemView.findViewById(R.id.user_name);
            userfullname = itemView.findViewById(R.id.user_full_name);
            root = itemView.findViewById(R.id.list_root);

        }

        //public void setAuthorname(String string) {
          //  authorname.setText(string);
        //}


    }
}
