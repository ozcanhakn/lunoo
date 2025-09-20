package com.lumoo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.ViewHolder.MessageUserAdapter;
import com.lumoo.Model.MessageUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageFragment extends Fragment {
    RecyclerView recyclerView;
    FirebaseDatabase database;
    DatabaseReference reference;
    MessageUserAdapter adapter;
    List<MessageUser> messageUserList;
    String currentuid;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance(String param1, String param2) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return view;

        currentuid = user.getUid();
        database = FirebaseDatabase.getInstance();

        recyclerView = view.findViewById(R.id.recyclerChat);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        messageUserList = new ArrayList<>();
        adapter = new MessageUserAdapter(getContext(), messageUserList);
        recyclerView.setAdapter(adapter);

        loadChatUsers();

        return view;
    }

    private void loadChatUsers() {
        reference = database.getReference("Message").child(currentuid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageUserList.clear();

                if (snapshot.exists()) {
                    // Her mesajlaşılan kullanıcı için
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String otherUserUid = userSnapshot.getKey();

                        Log.d("MessageFragment", "Found chat with user: " + otherUserUid);

                        // Kendi kendine mesaj kontrolü değil - gerçek bir mesaj var mı kontrolü
                        boolean hasValidMessages = false;
                        for (DataSnapshot messageSnapshot : userSnapshot.getChildren()) {
                            String senderUid = messageSnapshot.child("senderuid").getValue(String.class);
                            String receiverUid = messageSnapshot.child("receiveruid").getValue(String.class);
                            String message = messageSnapshot.child("message").getValue(String.class);

                            // Mesajda geçerli sender ve receiver varsa ve mesaj varsa - geçerli sohbet
                            if (senderUid != null && receiverUid != null && message != null && !message.trim().isEmpty()) {
                                hasValidMessages = true;
                                break;
                            }
                        }

                        if (hasValidMessages) {
                            Log.d("MessageFragment", "Processing valid chat with user: " + otherUserUid);
                            processUserMessages(otherUserUid, userSnapshot);
                        } else {
                            Log.d("MessageFragment", "Skipping invalid chat: " + otherUserUid);
                        }
                    }
                } else {
                    Log.d("MessageFragment", "No messages found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MessageFragment", "Database error: " + error.getMessage());
            }
        });
    }

    private void processUserMessages(String otherUserUid, DataSnapshot userMessagesSnapshot) {
        // Son mesajı ve timestamp'i bul
        String lastMessage = "Henüz mesaj yok";
        long lastMessageTimestamp = 0;
        String lastMessageType = "text";

        // Timestamp'li mesajları kontrol et
        for (DataSnapshot messageSnapshot : userMessagesSnapshot.getChildren()) {
            Long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);
            if (timestamp != null && timestamp > lastMessageTimestamp) {
                lastMessageTimestamp = timestamp;
                lastMessage = messageSnapshot.child("message").getValue(String.class);
                lastMessageType = messageSnapshot.child("type").getValue(String.class);
            }
        }

        // Eğer timestamp'li mesaj yoksa, en son eklenen mesajı al
        if (lastMessageTimestamp == 0) {
            Log.d("MessageFragment", "No timestamp found, using last message for: " + otherUserUid);
            DataSnapshot lastSnapshot = null;
            for (DataSnapshot messageSnapshot : userMessagesSnapshot.getChildren()) {
                lastSnapshot = messageSnapshot; // En son iterasyondaki mesaj
            }
            if (lastSnapshot != null) {
                lastMessage = lastSnapshot.child("message").getValue(String.class);
                lastMessageType = lastSnapshot.child("type").getValue(String.class);
                // Timestamp yoksa date ve time'dan yaklaşık bir değer oluştur
                String date = lastSnapshot.child("date").getValue(String.class);
                String time = lastSnapshot.child("time").getValue(String.class);
                if (date != null && time != null) {
                    // Bu sadece sıralama için - gerçek timestamp değil
                    lastMessageTimestamp = System.currentTimeMillis() - 86400000; // 1 gün öncesi gibi varsay
                }
            }
        }

        // Mesaj tipine göre gösterimi ayarla
        if ("iv".equals(lastMessageType)) {
            lastMessage = "📷 Fotoğraf";
        } else if ("audio".equals(lastMessageType)) {
            lastMessage = "🎵 Ses kaydı";
        } else if (lastMessage != null && lastMessage.length() > 30) {
            lastMessage = lastMessage.substring(0, 30) + "...";
        }

        // Okunmamış mesaj sayısını hesapla - sadece karşı taraftan gelen okunmamış mesajlar
        int unreadCount = 0;
        for (DataSnapshot messageSnapshot : userMessagesSnapshot.getChildren()) {
            Boolean read = messageSnapshot.child("read").getValue(Boolean.class);
            String senderUid = messageSnapshot.child("senderuid").getValue(String.class);
            String receiverUid = messageSnapshot.child("receiveruid").getValue(String.class);

            // Sadece bana gelen ve okunmamış mesajları say
            // senderUid = otherUser (mesajı gönderen), receiverUid = currentuid (ben - alan)
            if ((read == null || !read) &&
                    otherUserUid.equals(senderUid) &&
                    currentuid.equals(receiverUid)) {
                unreadCount++;
            }
        }

        Log.d("MessageFragment", "User: " + otherUserUid + ", Last: " + lastMessage + ", Unread: " + unreadCount);

        // Kullanıcı bilgilerini Firebase'den al
        loadUserInfo(otherUserUid, lastMessage, lastMessageTimestamp, unreadCount);
    }

    private void loadUserInfo(String otherUserUid, String lastMessage,
                              long lastMessageTimestamp, int unreadCount) {
        Log.d("MessageFragment", "Loading user info for: " + otherUserUid);

        DatabaseReference userRef = database.getReference("Kullanıcılar").child(otherUserUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                Log.d("MessageFragment", "User snapshot exists: " + userSnapshot.exists() + " for " + otherUserUid);
                if (userSnapshot.exists()) {
                    // Firebase yapısına uygun field'ları al
                    String username = userSnapshot.child("name").getValue(String.class);
                    String profileImage = userSnapshot.child("profileImage").getValue(String.class);
                    String kullaniciAdi = userSnapshot.child("kullanıcıAdı").getValue(String.class);
                    String frame = userSnapshot.child("frame").getValue(String.class);
                    Boolean hasFrame = userSnapshot.child("hasFrame").getValue(Boolean.class);

                    Log.d("MessageFragment", "User data - Name: " + username + ", KullanıcıAdı: " + kullaniciAdi +
                            ", ProfileImage: " + (profileImage != null && !profileImage.isEmpty() ? "exists" : "null") +
                            " for " + otherUserUid);

                    // Kullanıcı adını belirle - önce name, yoksa kullanıcıAdı
                    String displayName = username;
                    if (displayName == null || displayName.trim().isEmpty()) {
                        displayName = kullaniciAdi;
                    }
                    if (displayName == null || displayName.trim().isEmpty()) {
                        displayName = "Bilinmeyen Kullanıcı";
                    }

                    // Profil resmi kontrolü
                    if (profileImage == null || profileImage.trim().isEmpty()) {
                        profileImage = ""; // Boş string - ViewHolder'da varsayılan resim gösterilecek
                    }

                    MessageUser messageUser = new MessageUser(
                            displayName,
                            profileImage,
                            otherUserUid,
                            lastMessage,
                            "",
                            frame,
                            unreadCount,
                            hasFrame != null ? hasFrame : false,
                            lastMessageTimestamp
                    );

                    updateMessageUserList(messageUser);
                    Log.d("MessageFragment", "Successfully added user: " + displayName + " to list. Total users: " + messageUserList.size());
                } else {
                    // Kullanıcı Firebase'de yoksa da göster - silinmiş hesap olabilir
                    MessageUser messageUser = new MessageUser(
                            "Silinmiş Hesap",
                            "",
                            otherUserUid,
                            lastMessage,
                            "",
                            null,
                            unreadCount,
                            false,
                            lastMessageTimestamp
                    );
                    updateMessageUserList(messageUser);
                    Log.d("MessageFragment", "Added deleted user for: " + otherUserUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MessageFragment", "User data error for " + otherUserUid + ": " + error.getMessage());
                // Hata durumunda da göster
                MessageUser messageUser = new MessageUser(
                        "Kullanıcı",
                        "",
                        otherUserUid,
                        lastMessage,
                        "",
                        null,
                        unreadCount,
                        false,
                        lastMessageTimestamp
                );
                updateMessageUserList(messageUser);
            }
        });
    }

    private void updateMessageUserList(MessageUser newUser) {
        // Aynı kullanıcı varsa güncelle, yoksa ekle
        boolean found = false;
        for (int i = 0; i < messageUserList.size(); i++) {
            if (messageUserList.get(i).getUid().equals(newUser.getUid())) {
                messageUserList.set(i, newUser);
                found = true;
                break;
            }
        }

        if (!found) {
            messageUserList.add(newUser);
        }

        // Son mesaj zamanına göre sırala (en yeni üstte)
        Collections.sort(messageUserList, new Comparator<MessageUser>() {
            @Override
            public int compare(MessageUser u1, MessageUser u2) {
                return Long.compare(u2.getLastMessageTimestamp(), u1.getLastMessageTimestamp());
            }
        });

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        Log.d("MessageFragment", "Updated list, total users: " + messageUserList.size());
    }

    @Override
    public void onStart() {
        super.onStart();
        // onCreateView'da zaten loadChatUsers() çağırıyoruz
    }
}