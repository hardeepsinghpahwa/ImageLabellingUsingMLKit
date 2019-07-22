package com.example.imagelabellingproject;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.List;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static final int RESULT_OK = 1;
    private Bitmap mBitmap;
    private ImageView mImageView;
    private TextView mTextView;
    Uri dataUri;
    String uri;
    public static final int RC_SELECT_PICTURE = 103;
    public File imageFile;
    Button b,save;
    StorageReference storageReference;
    DatabaseReference databaseReference;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);


        FirebaseApp.initializeApp(getActivity());
        mTextView = v.findViewById(R.id.textView);
        mImageView = v.findViewById(R.id.imageView);
        b = v.findViewById(R.id.selectimage);
        save=v.findViewById(R.id.save);

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Saved");
        storageReference=FirebaseStorage.getInstance().getReference();

        v.findViewById(R.id.btn_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog p=new ProgressDialog(getActivity());
                p.setMessage("Analyzing");
                p.setTitle("Please Wait");
                p.show();
                if (mBitmap != null) {
                    FirebaseVisionLabelDetectorOptions options = new FirebaseVisionLabelDetectorOptions.Builder()
                            .setConfidenceThreshold(0.7f)
                            .build();
                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);
                    FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector(options);
                    detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionLabel> labels) {
                            p.dismiss();
                            for (FirebaseVisionLabel label : labels) {
                                mTextView.append(label.getLabel() + "\n");
                                mTextView.append(label.getConfidence() + "\n\n");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            p.dismiss();
                            mTextView.setText(e.getMessage());
                        }
                    });
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {

            String id= UUID.randomUUID().toString();

            @Override
            public void onClick(View v) {

                final ProgressDialog p=new ProgressDialog(getActivity());
                p.setMessage("Saving");
                p.setTitle("Please Wait");
                p.setCancelable(false);
                Log.i("uri", dataUri.toString());
                Log.i("text", mTextView.getText().toString());

                        if(dataUri!=null && mTextView.getText().toString()!=null)
                        {                p.show();
                            storageReference=storageReference.child("Saved Images/"+id+".jpg");
                            UploadTask uploadTask = storageReference.putFile(dataUri);
                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }

                                    // Continue with the task to get the download URL
                                    return storageReference.getDownloadUrl();}
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful())
                                    {
                                        uri=task.getResult().toString();
                                        databaseReference.child(id).child("image").setValue(uri);
                                        databaseReference.child(id).child("prediction").setValue(mTextView.getText().toString());
                                        p.dismiss();
                                        Toast.makeText(getActivity(),"Successfully Saved",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else {
                            if(dataUri==null)
                            {
                                Toast.makeText(getActivity(),"No Image",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                if(mTextView.getText().toString()==null)
                                {
                                    Toast.makeText(getActivity(),"First Analyze The Image",Toast.LENGTH_SHORT).show();
                                }
                            }}
                    }
                });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkWriteExternalPermission())
                {            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},1);

                }
                else {
                 selectPicture();
                }

            }
        });

        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
                     dataUri = data.getData();
                    String path = MyHelper.getPath(getActivity(), dataUri);
                    if (path == null) {
                        mBitmap = MyHelper.resizeImage(imageFile, getActivity(), dataUri, mImageView);
                    } else {
                        mBitmap = MyHelper.resizeImage(imageFile, path, mImageView);
                    }
                    if (mBitmap != null) {
                        mTextView.setText(null);
                        mImageView.setImageBitmap(mBitmap);
                    }
        }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseApp.initializeApp(getActivity());

    }


        private boolean checkWriteExternalPermission()
        {
            String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
            int res = getActivity().checkCallingOrSelfPermission(permission);
            return (res == PackageManager.PERMISSION_GRANTED);
        }



    private void selectPicture() {
        imageFile = MyHelper.createTempFile(imageFile);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_SELECT_PICTURE);
    }

}
