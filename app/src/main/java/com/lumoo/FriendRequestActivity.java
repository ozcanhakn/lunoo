package com.lumoo;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lumoo.Model.FriendRequest;
import com.lumoo.Model.MessageUser;
import com.lumoo.ViewHolder.FriendRequestViewHolder;
import com.lumoo.ViewHolder.MessageFragmentViewHolder;
import com.lumoo.util.GlideUtil;

public class FriendRequestActivity extends AppCompatActivity {
    RecyclerView recyclerRequestFriends;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friend_request);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerRequestFriends = findViewById(R.id.recyclerRequestFriends);
        recyclerRequestFriends.setHasFixedSize(true);
        recyclerRequestFriends.setLayoutManager(new LinearLayoutManager(FriendRequestActivity.this));




        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Kullanıcılar").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("FriendRequests");


        FirebaseRecyclerOptions<FriendRequest> options1 =
                new FirebaseRecyclerOptions.Builder<FriendRequest>()
                        .setQuery(reference,FriendRequest.class)
                        .build();

        FirebaseRecyclerAdapter<FriendRequest, FriendRequestViewHolder> firebaseRecyclerAdapter1 =
                new FirebaseRecyclerAdapter<FriendRequest, FriendRequestViewHolder>(options1) {
                    @Override
                    protected void onBindViewHolder(FriendRequestViewHolder holder, final int position, FriendRequest model) {

                        holder.txtFriendRequestName.setText(model.getUsername());
                        Log.d("prof", "onBindViewHolder: "+ model.getUsername()+model.getProfileImage());

                        String imageUrl = model.getProfileImage();
                        GlideUtil.loadOriginalImage(getApplicationContext(), imageUrl, holder.userImageRequest);

                        String uid = model.getUid();
                        String url = model.getProfileImage();
                        String userName = model.getUsername();



                        holder.cardViewApprove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Mevcut kullanıcının UID'si
                                String friendUid = model.getUid(); // Arkadaşlık isteği gönderenin UID'si
                                String friendUsername = model.getUsername(); // Arkadaşın kullanıcı adı
                                String friendProfilePhoto = model.getProfileImage(); // Arkadaşın profil fotoğrafı

                                DatabaseReference myFriendsRef = database1.getReference("Kullanıcılar").child(myUid).child("Friends").child(friendUid);
                                DatabaseReference friendFriendsRef = database1.getReference("Kullanıcılar").child(friendUid).child("Friends").child(myUid);
                                DatabaseReference requestRef = database1.getReference("Kullanıcılar").child(myUid).child("FriendRequests").child(friendUid);

                                // Kendi Friends düğümüne arkadaş ekleme
                                myFriendsRef.child("uid").setValue(friendUid);
                                myFriendsRef.child("username").setValue(friendUsername);
                                myFriendsRef.child("profileImage").setValue(friendProfilePhoto);

                                // Arkadaşın Friends düğümüne kendimizi ekleme
                                friendFriendsRef.child("uid").setValue(myUid);
                                friendFriendsRef.child("username").setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

                                // FriendRequests düğümünden isteği kaldırma
                                requestRef.removeValue();
                            }
                        });

                        holder.cardViewReject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                String friendUid = model.getUid();

                                DatabaseReference requestRef = database1.getReference("Kullanıcılar").child(myUid).child("FriendRequests").child(friendUid);

                                // Reddedilen isteği FriendRequests düğümünden kaldır
                                requestRef.removeValue();
                            }
                        });

                    }

                    @Override
                    public FriendRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.friend_request_layout, parent, false);




                        return new FriendRequestViewHolder(view);
                    }
                };
        firebaseRecyclerAdapter1.startListening();
        recyclerRequestFriends.setAdapter(firebaseRecyclerAdapter1);

    }
}