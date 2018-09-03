package com.example.asus.final_two.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.asus.final_two.helperclasses.MessageClass;
import com.example.asus.final_two.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ListAdapterClass extends BaseAdapter {
    Context c;
    ArrayList<MessageClass> list;

    public ListAdapterClass(Context c, ArrayList<MessageClass> list) {
        this.c = c;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    class holder {
        TextView t1, t2, t3;
        ImageView imageView;

        public holder(View view) {
            t1 = view.findViewById(R.id.chatName);
            t2 = view.findViewById(R.id.chatMessage);
            t3 = view.findViewById(R.id.chatTime);
            imageView = view.findViewById(R.id.chatImage);
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View row = view;
        holder h = null;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                row = inflater.inflate(R.layout.listlement, viewGroup, false);
            }
            h = new holder(row);
            row.setTag(h);
        } else {
            h = (holder) row.getTag();
        }

        h.t1.setText(((list.get(i)).name));
        String s = "hh:mm aaa";
        Calendar calander = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(s);
        String time = simpleDateFormat.format(calander.getTime());
        Log.e("listTag", time);
        h.t3.setText(time);
        String temp = list.get(i).imageUri;
        boolean isPhoto = !temp.equals("nothing");
        if (isPhoto) {
            Uri imgUri = Uri.parse((list.get(i).imageUri));
            Log.e("listTag", imgUri.toString());
            h.imageView.setVisibility(View.VISIBLE);
            h.t2.setVisibility(View.GONE);
            Glide.with(c).load(temp).apply(new RequestOptions().placeholder(R.drawable.image_holder)).into(h.imageView);
        } else {
            h.imageView.setVisibility(View.GONE);
            h.t2.setVisibility(View.VISIBLE);
            h.t2.setText(((list.get(i)).message));
        }


        return row;
    }
}

