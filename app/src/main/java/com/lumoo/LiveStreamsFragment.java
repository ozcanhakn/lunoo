package com.lumoo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.Model.Publisher;
import com.lumoo.ViewHolder.PublisherAdapter;
import com.lumoo.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

public class LiveStreamsFragment extends Fragment {
    private RecyclerView recyclerView;
    private PublisherAdapter publisherAdapter;
    private List<Publisher> publisherList = new ArrayList<>();

    LottieAnimationView lottieAstronaut;
    TextView lottieText;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public LiveStreamsFragment() {
        // Required empty public constructor
    }

    public static LiveStreamsFragment newInstance(String param1, String param2) {
        LiveStreamsFragment fragment = new LiveStreamsFragment();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_streams, container, false);

        recyclerView = view.findViewById(R.id.recyclerLiveStream);
        lottieAstronaut = view.findViewById(R.id.lottieAstronaut);
        lottieText = view.findViewById(R.id.lottieText);

        // RecyclerView optimizasyonları
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());

        // Firebase'den yayıncıları çekme
        loadPublishersFromFirebase();

        return view;
    }

    private void loadPublishersFromFirebase() {
        DatabaseReference publishersRef = FirebaseDatabase.getInstance().getReference("Publishers");
        publishersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                publisherList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        Publisher publisher = dataSnapshot.getValue(Publisher.class);
                        if (publisher != null) {
                            publisherList.add(publisher);
                        }
                    } catch (Exception e) {
                        Log.e("LiveStreamsFragment", "Error processing publisher: " + e.getMessage());
                    }
                }

                // Adaptörü güncelle
                try {
                    if (isAdded() && getContext() != null) {
                        publisherAdapter = new PublisherAdapter(publisherList, getContext(), activityResultLauncher);
                        recyclerView.setAdapter(publisherAdapter);

                        updateUIVisibility();
                    }
                } catch (Exception e) {
                    Log.e("LiveStreamsFragment", "Error updating adapter: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LiveStreamsFragment", "Firebase error: " + error.getMessage());
                if (isAdded()) {
                    updateUIVisibility();
                }
            }
        });
    }

    private void updateUIVisibility() {
        if (!isAdded() || getView() == null) return;

        try {
            if (publisherList.isEmpty()) {
                Log.d("LiveStreamsFragment", "Liste boş - Lottie gösteriliyor");
                lottieAstronaut.setVisibility(View.VISIBLE);
                lottieText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                Log.d("LiveStreamsFragment", "Liste dolu - RecyclerView gösteriliyor");
                lottieAstronaut.setVisibility(View.GONE);
                lottieText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e("LiveStreamsFragment", "Error updating UI visibility: " + e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Memory leak'leri önlemek için temizlik
        if (publisherAdapter != null) {
            publisherAdapter = null;
        }

        if (publisherList != null) {
            publisherList.clear();
        }

        // Garbage collection tetikle
        System.gc();
    }

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    if (isGranted) {
                        Log.d("Permission", "İzin verildi!");
                    } else {
                        Log.d("Permission", "İzin reddedildi!");
                    }
                }
            });
}