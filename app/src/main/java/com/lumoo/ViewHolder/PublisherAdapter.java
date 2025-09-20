package com.lumoo.ViewHolder;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.BroadcastActivity;
import com.lumoo.Model.Publisher;
import com.lumoo.R;
import com.lumoo.ViewerActivity;
import com.lumoo.util.GlideUtil;

import java.util.List;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import io.reactivex.rxjava3.annotations.NonNull;

public class PublisherAdapter extends RecyclerView.Adapter<PublisherAdapter.PublisherViewHolder> {

    private List<Publisher> publisherList;
    private Context context;
    String userID, userName;
    private ActivityResultLauncher<String> activityResultLauncher;

    public PublisherAdapter(List<Publisher> publisherList, Context context, ActivityResultLauncher<String> activityResultLauncher) {
        this.publisherList = publisherList;
        this.context = context;
        this.activityResultLauncher = activityResultLauncher;
    }

    @NonNull
    @Override
    public PublisherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_publisher, parent, false);
        return new PublisherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublisherViewHolder holder, int position) {
        Publisher publisher = publisherList.get(position);
        holder.userName.setText(publisher.getUserName());

        // GlideUtil ile görsel yükleme
        GlideUtil.loadOriginalImage(context, publisher.getStreamerPhoto(), holder.userPhoto);

        // Firebase veritabanına yayıncı bilgilerini ekle
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference("Kullanıcılar").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                userName = snapshot.child("kullanıcıAdı").getValue(String.class);
                userID = snapshot.child("ad").getValue(String.class);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
            }
        });

        // Tıklandığında kullanıcıyı yayına al
        holder.itemView.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(android.Manifest.permission.RECORD_AUDIO);
            } else if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(android.Manifest.permission.CAMERA);
            } else {
                Intent intent = new Intent(context, ViewerActivity.class);
                intent.putExtra("roomID", publisher.getRoomId());
                intent.putExtra("isHost", false);
                intent.putExtra("userID", userID);
                intent.putExtra("userName", userName);
                context.startActivity(intent);
            }

            ZegoEngineProfile profile = new ZegoEngineProfile();
            profile.appID = 1771662561;
            profile.appSign = "83ab84a07b219d583fee959f8fa5f913fd213c96b601f891af3ea0a3ea8deb40";
            profile.scenario = ZegoScenario.BROADCAST;
            profile.application = (Application) context.getApplicationContext();
            ZegoExpressEngine.createEngine(profile, null);
            Log.d("123", "onBindViewHolder: " + publisher.getRoomId() + "/" + false + "/" + userID + "/" + userName);
        });
    }

    @Override
    public int getItemCount() {
        return publisherList.size();
    }

    public class PublisherViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView userPhoto;

        public PublisherViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.publisherName);
            userPhoto = itemView.findViewById(R.id.imagePublisher);
        }
    }
}