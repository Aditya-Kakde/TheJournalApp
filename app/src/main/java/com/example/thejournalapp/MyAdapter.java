package com.example.thejournalapp;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

    private Context context;
    private List<Journal> journalList;
    public MyAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }
    public void updateJournalList(List<Journal> newJournalList) {
        journalList.clear();
        journalList.addAll(newJournalList);
        notifyDataSetChanged();  // Notify the adapter to update the view
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.journal_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Journal currentJournal = journalList.get(position);

        holder.title.setText(currentJournal.getTitle());
        holder.thoughts.setText(currentJournal.getThoughts());
        holder.name.setText(currentJournal.getUsername());

        String imageUrl = currentJournal.getImageUrl();

        if (currentJournal.getTimestamp() != null) {
            String timeAgo = (String) DateUtils.getRelativeTimeSpanString(
                    currentJournal.getTimestamp().getSeconds() * 1000);
            holder.dateAdded.setText(timeAgo);
        } else {
            holder.dateAdded.setText("No date available");
        }
        Glide.with(context)
                .load(imageUrl)
                .fitCenter()
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView title , thoughts, dateAdded, name;
        public ImageView image, shareButton;
        public String userId, userame;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.journal_title_list);
            thoughts = itemView.findViewById(R.id.journal_thought_list);
            dateAdded = itemView.findViewById(R.id.journal_timestamp_list);

            image = itemView.findViewById(R.id.journal_image_list);
            name = itemView.findViewById(R.id.journal_row_username);

            shareButton = itemView.findViewById(R.id.journal_row_share_button);

            shareButton.setOnClickListener(v -> {

            });
        }
    }

}
