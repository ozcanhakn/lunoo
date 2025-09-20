package com.lumoo.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lumoo.R;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;

public class CountrySpinnerAdapter extends ArrayAdapter<CountryItem> {

    private LayoutInflater layoutInflater;
    private int dropDownResource;

    public CountrySpinnerAdapter(@NonNull Context context, @NonNull List<CountryItem> countryList) {
        super(context, 0, countryList);
        this.layoutInflater = LayoutInflater.from(context);
        this.dropDownResource = R.layout.spinner_country_item;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent, R.layout.spinner_country_item);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent, dropDownResource);
    }

    private View initView(int position, View convertView, ViewGroup parent, int layoutResource) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(layoutResource, parent, false);
        }

        CountryItem currentItem = getItem(position);
        if (currentItem != null) {
            ImageView imageViewFlag = view.findViewById(R.id.image_flag);
            TextView textViewName = view.findViewById(R.id.text_country_name);

            if (textViewName != null) {
                textViewName.setText(currentItem.getCountryName());
                textViewName.setTextColor(Color.WHITE);
            }

            if (imageViewFlag != null) {
                // Drawable'dan bayrak çek
                int resId = getContext().getResources().getIdentifier(
                        currentItem.getCountryCode(), "drawable", getContext().getPackageName());
                if (resId != 0) {
                    imageViewFlag.setImageResource(resId);
                } else {
                    // Varsayılan bayrak ikonu
                    imageViewFlag.setImageResource(R.drawable.ic_launcher_background);
                }
            }
        }

        return view;
    }

    @Override
    public void setDropDownViewResource(int resource) {
        this.dropDownResource = resource;
    }
}