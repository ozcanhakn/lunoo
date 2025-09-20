package com.lumoo.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lumoo.Model.HoroscopeItem;
import com.lumoo.R;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;

public class HoroscopeSpinnerAdapter extends ArrayAdapter<HoroscopeItem> {

    private LayoutInflater layoutInflater;
    private int dropDownResource;

    public HoroscopeSpinnerAdapter(@NonNull Context context, @NonNull List<HoroscopeItem> horoscopeItems) {
        super(context, 0, horoscopeItems);
        this.layoutInflater = LayoutInflater.from(context);
        this.dropDownResource = R.layout.spinner_horoscope_item;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent, R.layout.spinner_horoscope_item);
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

        HoroscopeItem currentItem = getItem(position);
        if (currentItem != null) {
            ImageView imageViewHoroscope = view.findViewById(R.id.image_horoscope);
            TextView textViewName = view.findViewById(R.id.text_horoscope_name);

            if (textViewName != null) {
                textViewName.setText(currentItem.getName());
                textViewName.setTextColor(Color.WHITE);
            }

            if (imageViewHoroscope != null) {
                // Drawable'dan burç ikonu çek
                int resId = getContext().getResources().getIdentifier(
                        currentItem.getCode(), "drawable", getContext().getPackageName());
                if (resId != 0) {
                    imageViewHoroscope.setImageResource(resId);
                } else {
                    // Varsayılan burç ikonu
                    imageViewHoroscope.setImageResource(R.drawable.ic_launcher_background);
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