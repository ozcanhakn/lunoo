package com.lumoo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonalInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalInfoFragment extends Fragment {
    TextView txtInterest1, txtInterest2, txtInterest3, txtAge, txtHoroscope, txtPostCount,txtUsername;
    TextView txtInvitationCode, txtCredit;
    RelativeLayout btnCreditBuy;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PersonalInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PersonalInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PersonalInfoFragment newInstance(String param1, String param2) {
        PersonalInfoFragment fragment = new PersonalInfoFragment();
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_personal_info, container, false);


        txtHoroscope = view.findViewById(R.id.txtHoroscope);
        txtUsername = view.findViewById(R.id.txtUsername);
        txtCredit = view.findViewById(R.id.txtCredit);
        txtInvitationCode = view.findViewById(R.id.txtInvitationCode);
        btnCreditBuy = view.findViewById(R.id.btnCreditBuy);

        init();




        //Veri çekme
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Kullanıcılar").child(uid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("kullanıcıAdı").getValue(String.class);
                String credit = snapshot.child("credit").getValue(String.class);
                long postCount = 0; // Varsayılan olarak 0

                //Kaç tane post gönderdiğinin hesabı burada yapılıyor. İleride lazım olursa diye silmiyorum.
                /*if (snapshot.hasChild("Post")) { // "Post" düğümü var mı kontrol et
                    postCount = snapshot.child("Post").getChildrenCount();
                    String posts = String.valueOf(postCount);
                    txtPostCount.setText(posts);
                }*/


                txtCredit.setText(credit);

                txtUsername.setText("Kullanıcı Adı: @"+username);






            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        return view;
    }
    private void init(){
        btnCreditBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BuyCreditActivity.class);
                startActivity(intent);
            }
        });

    }
}