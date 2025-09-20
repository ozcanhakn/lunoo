package com.lumoo;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.lumoo.Model.Post;
import com.lumoo.ViewHolder.PostViewHolder;

import io.reactivex.rxjava3.annotations.NonNull;

public class SharesFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    RecyclerView recyclerShares;
    LottieAnimationView lottieGhostShare;
    private String mParam1;
    private String mParam2;
    String url, uid;

    public SharesFragment() {
        // Required empty public constructor
    }

    public static PostFragment newInstance(String param1, String param2) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
             uid = getArguments().getString("uid");
             url = getArguments().getString("url");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shares, container, false);

        recyclerShares = view.findViewById(R.id.recyclerShares);
        lottieGhostShare = view.findViewById(R.id.lottieGhostShare);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Kullanıcılar").child(uid).child("Post");

        // FirebaseRecyclerAdapter ile bu veriyi alıyoruz
        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(reference, Post.class)
                .build();

        FirebaseRecyclerAdapter<Post, PostViewHolder> adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@io.reactivex.rxjava3.annotations.NonNull PostViewHolder holder, int position, @io.reactivex.rxjava3.annotations.NonNull Post model) {
                //holder.imgPost.setImageBitmap(decodeBase64(model.getImage()));
                holder.txtPostDesc.setText(model.getDescription());
            }

            @io.reactivex.rxjava3.annotations.NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_item, parent, false);
                return new PostViewHolder(view);
            }

            // Bu metot veri geldiğinde veya değiştiğinde çağrılır
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                // Eğer veri yoksa Lottie animasyonunu göster
                if (getItemCount() == 0) {
                    recyclerShares.setVisibility(View.GONE); // RecyclerView'ı gizle
                    lottieGhostShare.setVisibility(View.VISIBLE); // Lottie'yi göster
                } else {
                    recyclerShares.setVisibility(View.VISIBLE); // RecyclerView'ı göster
                    lottieGhostShare.setVisibility(View.GONE); // Lottie'yi gizle
                }
            }
        };

        // RecyclerView'a adapter'ı set ediyoruz
        recyclerShares.setAdapter(adapter);
        adapter.startListening(); // Dinlemeye başla

        return view;
    }
}
