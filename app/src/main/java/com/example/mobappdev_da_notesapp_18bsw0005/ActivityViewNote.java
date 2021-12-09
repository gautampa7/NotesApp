package com.example.mobappdev_da_notesapp_18bsw0005;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

public class ActivityViewNote extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference user_col = db.collection("users");

    TextView text_title,text_note_content;
    Button delete;
    String document_id,email,title,content="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        Intent in = getIntent();
        email = in.getStringExtra("email");
        document_id = in.getStringExtra("document_id");

        delete = (Button) findViewById(R.id.deltenotebtn);
        text_title = (TextView) findViewById(R.id.etnotetitle);
        text_note_content = (TextView) findViewById(R.id.etnotecontent);

        user_col.document("user_"+email).collection("notes").document(document_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    title = document.getString("title_key");
                    content = document.getString("note_content_key");
                    text_title.setText(title);
                    text_note_content.setText(content);
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_col.document("user_"+email).collection("notes").document(document_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Note Deleted Successfully", Toast.LENGTH_SHORT).show();
                        Intent back = new Intent(getApplicationContext(),ActivityHomePage.class);
                        back.putExtra("email",email);
                        startActivity(back);
                        finish();
                    }
                });
            }
        });

    }
}