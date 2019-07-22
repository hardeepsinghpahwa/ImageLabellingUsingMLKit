package com.example.imagelabellingproject;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ViewHolderClass extends RecyclerView.ViewHolder {

    ImageView imageView;
    ProgressBar progressBar;
    public ViewHolderClass(@NonNull View itemView) {
        super(itemView);

        imageView=itemView.findViewById(R.id.image);
        progressBar=itemView.findViewById(R.id.pbar);
    }

    public void setImageView(String image) {

        Picasso.get().load(image).resize(300,283).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
}
