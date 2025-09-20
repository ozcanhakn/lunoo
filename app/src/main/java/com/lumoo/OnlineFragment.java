package com.lumoo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.Model.AllUser;
import com.lumoo.ViewHolder.UserAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OnlineFragment extends Fragment {
    RecyclerView recyclerView;
    EditText searchEditText;
    TextView txtOnlineCount;
    ProgressBar progressBar;
    UserAdapter adapter;
    ArrayList<AllUser> allUsersList = new ArrayList<>();
    ArrayList<AllUser> filteredUsersList = new ArrayList<>();

    private int onlineUserCount = 0;
    private DatabaseReference databaseReference;
    private ValueEventListener usersEventListener;

    private static final int MAX_USERS_TO_LOAD = 30;
    private static final int DEFAULT_DISPLAY_LIMIT = 10;
    private static final int SEARCH_RESULT_LIMIT = 5;

    public OnlineFragment() {
    }

    public static OnlineFragment newInstance() {
        return new OnlineFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSearchFunctionality();
        loadUsersFromFirebase();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeFirebaseListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeFirebaseListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (allUsersList.isEmpty()) {
            loadUsersFromFirebase();
        }
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recOnline);
        searchEditText = view.findViewById(R.id.searchEditText);
        txtOnlineCount = view.findViewById(R.id.txtOnlineCount);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new UserAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void removeFirebaseListeners() {
        if (databaseReference != null && usersEventListener != null) {
            databaseReference.removeEventListener(usersEventListener);
        }
    }

    private void loadUsersFromFirebase() {
        showLoadingState();

        Query usersQuery = FirebaseDatabase.getInstance()
                .getReference("Kullanıcılar")
                .limitToFirst(MAX_USERS_TO_LOAD);

        usersEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                processUsersData(snapshot);
                hideLoadingState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OnlineFragment", "Database error: " + error.getMessage());
                hideLoadingState();
            }
        };

        // addListenerForSingleValueEvent yerine addValueEventListener kullan
        usersQuery.addValueEventListener(usersEventListener);
    }

    private void processUsersData(DataSnapshot snapshot) {
        allUsersList.clear();
        onlineUserCount = 0;

        String currentUserId = getCurrentUserId();

        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            if (allUsersList.size() >= MAX_USERS_TO_LOAD) break;

            // Current user'ı atla (isteğe bağlı)
            //if (dataSnapshot.getKey().equals(currentUserId)) continue;

            AllUser user = dataSnapshot.getValue(AllUser.class);
            if (user != null) {
                user.setUserId(dataSnapshot.getKey());

                if ("online".equals(user.getOnline())) {
                    onlineUserCount++;
                }
                allUsersList.add(user);
            }
        }

        sortUsersByOnlineStatus();
        updateOnlineCountDisplay();
        applyDefaultFilter();

        // Adapter'a veri güncellemesini bildir
        adapter.setUserList(filteredUsersList);
    }
    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
    }

    private void sortUsersByOnlineStatus() {
        Collections.sort(allUsersList, new Comparator<AllUser>() {
            @Override
            public int compare(AllUser user1, AllUser user2) {
                boolean user1Online = "online".equals(user1.getOnline());
                boolean user2Online = "online".equals(user2.getOnline());

                if (user1Online && !user2Online) return -1;
                if (!user1Online && user2Online) return 1;
                return 0;
            }
        });
    }

    private void filterUsers(String searchText) {
        filteredUsersList.clear();

        if (searchText.isEmpty()) {
            applyDefaultFilter();
        } else {
            applySearchFilter(searchText);
        }

        adapter.setUserList(filteredUsersList);
        // Adapter'a veri değiştiğini bildir
        adapter.notifyDataSetChanged();
    }

    private void applyDefaultFilter() {
        filteredUsersList.clear(); // ÖNEMLİ: Önce listeyi temizle
        int limit = Math.min(allUsersList.size(), DEFAULT_DISPLAY_LIMIT);
        filteredUsersList.addAll(allUsersList.subList(0, limit));
    }

    private void applySearchFilter(String searchText) {
        ArrayList<AllUser> searchResults = new ArrayList<>();
        String searchTextLower = searchText.toLowerCase();

        for (AllUser user : allUsersList) {
            if (searchResults.size() >= SEARCH_RESULT_LIMIT) break;

            String userName = user.getKullanıcıAdı();
            if (userName != null && userName.toLowerCase().contains(searchTextLower)) {
                searchResults.add(user);
            }
        }

        filteredUsersList.addAll(searchResults);
    }

    private void showLoadingState() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
    }

    private void updateOnlineCountDisplay() {
        if (txtOnlineCount != null) {
            String countText = onlineUserCount + " Online";
            txtOnlineCount.setText(countText);

            txtOnlineCount.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction(() ->
                            txtOnlineCount.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(150)
                    );
        }
    }
}