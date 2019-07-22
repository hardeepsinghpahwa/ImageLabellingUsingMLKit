package com.example.imagelabellingproject;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class Saved extends Fragment {

    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<itemdetail,ViewHolderClass> firebaseRecyclerAdapter;
    DatabaseReference dataref;
    ProgressBar p;

    public Saved() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v= inflater.inflate(R.layout.fragment_saved, container, false);

        p=v.findViewById(R.id.recpbar);
        dataref= FirebaseDatabase.getInstance().getReference().child("Saved");
        recyclerView=v.findViewById(R.id.savedrecyclerview);


        firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<itemdetail, ViewHolderClass>
                (itemdetail.class,R.layout.itemlayout,ViewHolderClass.class,dataref) {

            @Override
            public void onViewAttachedToWindow(@NonNull ViewHolderClass holder) {
                super.onViewAttachedToWindow(holder);
                p.setVisibility(View.INVISIBLE);
            }

            @Override
            protected void populateViewHolder(final ViewHolderClass viewHolder, itemdetail model, final int position) {
                viewHolder.setImageView(model.getImage());

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i=new Intent(getActivity(),ViewSavedItem.class);

                        String s=getRef(position).getKey();
                        i.putExtra("key",s);
                        startActivity(i);
                    }
                });
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

        return v;
    }

}
