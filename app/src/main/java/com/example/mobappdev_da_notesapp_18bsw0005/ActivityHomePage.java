package com.example.mobappdev_da_notesapp_18bsw0005;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ActivityHomePage extends AppCompatActivity implements NotesAdapter.ItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference user_col = db.collection("users");
    String cur_title,cur_note_content,email,name="";
    String doc_tile,doc_id="";
    Button add_note;
    EditText edit_title, edit_note;
    ArrayList<String> notes_document_id,notes_title;
    TextView txt_name;
    RecyclerView notes_recycler;
    public NotesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        notes_document_id= new ArrayList<>();
        notes_title = new ArrayList<>();

        email = getIntent().getStringExtra("email");
        edit_title = (EditText) findViewById(R.id.et_add_note_title);
        edit_note = (EditText) findViewById(R.id.et_add_note_content);
        add_note = (Button)findViewById(R.id.button2);
        txt_name = (TextView) findViewById(R.id.textView6);

        user_col.document("user_"+email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    name=document.getString("name_key");
                    txt_name.setText("Hi,"+name);
                }
            }
        });


        notes_document_id.clear();
        notes_title.clear();
        notes_recycler = (RecyclerView)findViewById(R.id.notesrecyler);
        adapter = new NotesAdapter(getApplicationContext(), notes_title);

        add_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cur_title = edit_title.getText().toString();
                cur_note_content = edit_note.getText().toString();

                Map<String,Object> new_note = new HashMap<>();
                new_note.put("title_key",cur_title);
                new_note.put("note_content_key",cur_note_content);

                user_col.document("user_"+email).collection("notes").document().set(new_note).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Note Added Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "FireBase Connection Error!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        user_col.document("user_"+email).collection("notes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot document: task.getResult()){
                        doc_id=document.getId();
                        doc_tile=document.getString("title_key");
                        notes_document_id.add(doc_id);
                        notes_title.add(doc_tile);
                        adapter = new NotesAdapter(getApplicationContext(), notes_title);
                        adapter.notifyDataSetChanged();
                    }

                }else{
                    Toast.makeText(getApplicationContext(),"FireBase Connection Error!", Toast.LENGTH_LONG).show();
                }
            }
        });
        notes_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new NotesAdapter(getApplicationContext(), notes_title);
        adapter.setClickListener(this::onItemClick);
        notes_recycler.setAdapter(adapter);


        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT ) {

            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red))
                        .addSwipeLeftLabel("Delete Note")
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green))
                        .addSwipeRightLabel("Open Note")
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

                @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Toast.makeText(getApplicationContext(), "on Move", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if(swipeDir==4) {
                    //Delete Note if Swiped Left
                    int position = viewHolder.getAdapterPosition();
                    Toast.makeText(getApplicationContext(), "Deleting Note... "+(position+1), Toast.LENGTH_SHORT).show();
                    notes_title.remove(position);
                    adapter.notifyDataSetChanged();
                    //Deleting Note from Database
                    String document_id=notes_document_id.get(position);
                    user_col.document("user_"+email).collection("notes").document(document_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getApplicationContext(), "Note Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else if(swipeDir==8){
                    //Open Note if Swiped Right
                    int position = viewHolder.getAdapterPosition();
                    Toast.makeText(getApplicationContext(), "Opening Note " + (position+1), Toast.LENGTH_SHORT).show();
                    Intent next = new Intent(getApplicationContext(),ActivityViewNote.class);
                    next.putExtra("document_id",notes_document_id.get(position));
                    next.putExtra("email",email);
                    startActivity(next);
                    finish();
                }

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(notes_recycler);
    }


    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + (position+1), Toast.LENGTH_SHORT).show();
        Intent next = new Intent(getApplicationContext(),ActivityViewNote.class);
        next.putExtra("document_id",notes_document_id.get(position));
        next.putExtra("email",email);
        startActivity(next);
        finish();
    }
}