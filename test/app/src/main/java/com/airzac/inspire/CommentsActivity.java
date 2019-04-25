package com.airzac.inspire;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
    private ImageView commentButton;
    private EditText commentInput;
    private RecyclerView commentList;
    private DatabaseReference usersRef, postsRef;
    private FirebaseAuth mAuth;
    private String Post_Key, current_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key = getIntent().getExtras().get("postkey").toString();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");
        commentList = (RecyclerView) findViewById(R.id.comment_list);
        commentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentList.setLayoutManager(linearLayoutManager);
        commentInput = (EditText) findViewById(R.id.user_comment_box);
        commentButton = (ImageView) findViewById(R.id.post_comment_button);

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            String userName = dataSnapshot.child("username").getValue().toString();
                            validateComment(userName);

                            commentInput.setText("");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query commentQuery = postsRef.orderByChild("date");
        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(commentQuery, Comments.class).build();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>
                (options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {
                holder.setUsername(model.getUsername());
                holder.setComment(model.getComment());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
            }
            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_comments_layout, viewGroup, false);
                CommentsActivity.CommentsViewHolder viewHolder = new CommentsActivity.CommentsViewHolder(view);
                return viewHolder;
            }

        };

        commentList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static  class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username)
        {
            TextView Commentusername = (TextView) mView.findViewById(R.id.comment_user_name);
            Commentusername.setText("@"+username+ "  ");
        }
        public void setComment(String comment) {
            TextView Commentext = (TextView) mView.findViewById(R.id.comment_text);
            Commentext.setText(comment);
        }
        public void setDate(String date) {
            TextView commentdate = (TextView) mView.findViewById(R.id.comment_date);
            commentdate.setText("  Date: "+date);
        }
        public void setTime(String time) {
            TextView commenttime = (TextView) mView.findViewById(R.id.comment_time);
            commenttime.setText("  Time: "+time);
        }

    }

    private void validateComment(String userName) {
        String commentText = commentInput.getText().toString();
        if (TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "please write a comment first!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat getcurrentDate = new SimpleDateFormat("dd-MM-yyyy");
            final String currentDate = getcurrentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat getcurrentTime = new SimpleDateFormat("HH:mm");
            final String currentTime = getcurrentTime.format(calFordTime.getTime());

            final String randomKey = current_user_id+currentDate+currentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", current_user_id);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", currentDate);
            commentsMap.put("time", currentTime);
            commentsMap.put("username", userName);
            postsRef.child(randomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                   if (task.isSuccessful())
                   {
                       Toast.makeText(CommentsActivity.this, "comment posted successfully.", Toast.LENGTH_SHORT).show();
                   }
                   else
                   {
                       Toast.makeText(CommentsActivity.this, "error occured, try again", Toast.LENGTH_SHORT).show();
                   }
                }
            });

        }
    }
}
