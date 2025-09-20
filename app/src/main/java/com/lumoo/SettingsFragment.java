package com.lumoo;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    ConstraintLayout btnShare, btnPassword, btnPrivacy, btnLogout, btnDelete, btnRate, btnFrames;

    private FirebaseAuth mAuth;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        btnShare = view.findViewById(R.id.btnShareApp);
        btnDelete = view.findViewById(R.id.btnDeleteAccount);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnRate = view.findViewById(R.id.btnRateThisApp);
        btnPrivacy = view.findViewById(R.id.btnShield);
        btnPassword = view.findViewById(R.id.btnPassword);
        btnFrames = view.findViewById(R.id.btnFramesSettings);


        mAuth = FirebaseAuth.getInstance();

        init();






        return view;
    }
    public void init(){

        btnFrames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FramesChooseActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                // Bu satırı ekleyin, böylece kullanıcı çıkış yaparken mevcut aktiviteyi kapatır.

                Toast.makeText(getContext(), "Çıkış Yapıldı", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DeleteAccountActivity.class);
                startActivity(intent);
            }
        });
        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RateThisApp.openRateDialog(getContext());
            }
        });

        btnShare.setOnClickListener(view -> {
            String shareText = "Bu harika uygulamayı denemelisin! 📱\n\nİndirmek için tıkla: https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(shareIntent, "Uygulamayı Paylaş"));
        });

        btnPassword.setOnClickListener(view -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            if (user == null) {
                Toast.makeText(requireContext(), "Kullanıcı bulunamadı!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Yeni şifreyi almak için bir AlertDialog oluştur
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Şifre Değiştir");

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

            builder.setPositiveButton("Değiştir", (dialog, which) -> {
                String newPassword = input.getText().toString().trim();

                if (newPassword.length() < 6) {
                    Toast.makeText(requireContext(), "Şifre en az 6 karakter olmalıdır!", Toast.LENGTH_SHORT).show();
                    return;
                }

                user.updatePassword(newPassword).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Şifre başarıyla değiştirildi.", Toast.LENGTH_SHORT).show();

                        // Kullanıcıyı çıkış yaptır ve giriş ekranına yönlendir
                        auth.signOut();
                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        Toast.makeText(requireContext(), "Şifre değiştirilemedi! Tekrar giriş yapıp deneyin.", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
            builder.show();
        });

    }
}