package com.example.snehit.abcd;


import android.database.Cursor;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.content.ContentResolver;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.support.v7.appcompat.R.id.text;


public class MainActivity extends AppCompatActivity implements View.OnClickListener /*  implementing click listener */ {
    //a constant to track the file chooser intent
    private static final int PICK_IMAGE_REQUEST = 1;
    private DatabaseReference mDatabase;


    //Buttons
    private Button buttonChoose;
    private Button buttonUpload;
    private Button button2;
    //ImageView
    private ImageView imageView;
    private StorageReference mStorageRef;

    //a Uri object to store file path
    private Uri filePath;
    private String fileName;
    private ListView mlistview;
    private String dfilename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //getting views from layout
        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        button2 = (Button) findViewById(R.id.button2);







        //attaching listener
        button2.setOnClickListener(this);
        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        button2.setOnLongClickListener(new Button.OnLongClickListener() {
            public boolean onLongClick(View v) {

                TextView text = (TextView) findViewById(R.id.text);
                text.setText("you suck more");
                return true;
            }
        });

    }



    public ArrayList<String> arr;
    public ArrayAdapter adapter;

    @Override
    protected void onStart() {
        super.onStart();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        arr = new ArrayList<>();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                map2list((Map) dataSnapshot.getValue());
                //formats the datasnapshot entries to strings
                adapter.notifyDataSetChanged();
                //makes the ListView realtime
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                System.out.println(databaseError.toException());
                // ...
            }
        };
        mDatabase.addValueEventListener(listener);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, arr);

        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                dfilename = ((TextView)view).getText().toString();

                Toast.makeText(getBaseContext(), dfilename, Toast.LENGTH_LONG).show();

            }
        });




    }

    public void map2list(Map<String,Long> map) {

        arr.clear();
        for (Map.Entry<String, Long> entry : map.entrySet()) {


            String d = entry.getKey();

            arr.add(d.replace(",","."));
        }
    }


    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select file"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();


            //TextView text = (TextView) findViewById(R.id.text);
            File myFile = new File(filePath.getPath());
            Toast.makeText(getApplicationContext(), " " + filePath.toString(), Toast.LENGTH_LONG).show();
            //text.setText(filePath.toString());
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            ContentResolver cr = getApplicationContext().getContentResolver();

            Cursor metaCursor = cr.query(filePath, projection, null, null, null);
            if (metaCursor != null) {
                try {
                    if (metaCursor.moveToFirst()) {
                        fileName = metaCursor.getString(0);
                    }
                } finally {
                    metaCursor.close();
                }
            }
            //text.setText(filePath.toString());
          /*  try {
                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    private void changetext() {
        //mStorageRef.child("files/");

        StorageReference islandRef = mStorageRef.child("files/"+dfilename);

        File rootPath = new File(Environment.getExternalStorageDirectory(), "firebase");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, dfilename);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("downloading");
        progressDialog.show();

        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
                progressDialog.hide();

                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "failure", Toast.LENGTH_LONG).show();

            }
        });
    }

    //this method will upload the file
    private void uploadFile() {
        //if there is a file to upload
        if (filePath != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            mDatabase.child(fileName.replace(".", ",")).setValue("files/" + fileName.replace(".", ","));


            StorageReference riversRef = mStorageRef.child("files/" + fileName);
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        //if there is not any file
        else {
            //you can display an error toast
        }
    }

    public void onClick(View view) {
        //if the clicked button is choose
        if (view == buttonChoose) {
            showFileChooser();
        }
        //if the clicked button is upload
        else if (view == buttonUpload) {
            uploadFile();
        } else if (view == button2) {
            changetext();
        }
    }

}
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */





