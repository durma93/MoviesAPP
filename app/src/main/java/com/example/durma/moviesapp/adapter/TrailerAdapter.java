package com.example.durma.moviesapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.durma.moviesapp.R;
import com.example.durma.moviesapp.model.Trailer;

import java.util.List;

/**
 * Created by durma on 24.1.18..
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHoler> {
    Context context;
    List<Trailer> trailerList;

    public TrailerAdapter(Context context, List<Trailer> trailerList) {
        this.context = context;
        this.trailerList = trailerList;
    }

    @Override
    public ViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_card, parent, false);

        return new ViewHoler(view);
    }

    @Override
    public void onBindViewHolder(ViewHoler holder, int position) {
        holder.title.setText(trailerList.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return trailerList.size();
    }

    public class ViewHoler extends RecyclerView.ViewHolder {
        TextView title;
        ImageView imageView;
        public ViewHoler(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.title);
            imageView = (ImageView)itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        Trailer clicketDataItem = trailerList.get(position);
                        String videoId= trailerList.get(position).getKey();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v="+videoId));
                        intent.putExtra("VIDEO_ID", videoId);
                        context.startActivity(intent);
                        Toast.makeText(view.getContext(), "You clicked " + clicketDataItem.getName(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
