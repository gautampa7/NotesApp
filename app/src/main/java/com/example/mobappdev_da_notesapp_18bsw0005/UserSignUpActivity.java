package com.example.mobappdev_da_notesapp_18bsw0005;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.hash.Hashing;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UserSignUpActivity extends AppCompatActivity {

    EditText edit_name,edit_email,edit_password,edit_conf_passwd;
    String name,email,password,conf_passwd,encrypted_password="";
    Button signup_btn;
    boolean form_validated = false;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference user_col = db.collection("users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);

        edit_name = (EditText) findViewById(R.id.etusername);
        edit_email = (EditText) findViewById(R.id.etemail);
        edit_password = (EditText) findViewById(R.id.etpassword);
        edit_conf_passwd = (EditText) findViewById(R.id.etconfpassword);

        signup_btn = (Button) findViewById(R.id.btnsignupbtn);

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = edit_name.getText().toString();
                email = edit_email.getText().toString().toLowerCase();
                password = edit_password.getText().toString();
                conf_passwd = edit_conf_passwd.getText().toString();


                form_validated = form_validation();
                if(form_validated){
                    //isExistingUser();
                    DocumentReference user_doc = user_col.document("user_"+email);
                    user_doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    edit_email.setError("User with same email id already exits! Try logging into existing account or use different email id.");
                                    Toast.makeText(getApplicationContext(), "User Already Exits!", Toast.LENGTH_SHORT).show();
                                } else {

                                    try {
                                        encrypted_password = encrypt_passwd(password);
                                    } catch (Error e) {
                                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

                                    }

                                    Map<String,Object> user_sign_up_details = new Hashtable<>();
                                    user_sign_up_details.put("name_key",name);
                                    user_sign_up_details.put("email_key",email);
                                    user_sign_up_details.put("password_key",encrypted_password);
                                    user_col.document("user_"+email).set(user_sign_up_details);
                                    Toast.makeText(getApplicationContext(), "User Created Successfully!", Toast.LENGTH_SHORT).show();

                                    Intent login_page = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(login_page);
                                    finish();

                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), task.getException().toString()+" FireBase Connection ERROR!", Toast.LENGTH_LONG).show();
                                }
                        }
                    });

                }else {
                    Toast.makeText(getApplicationContext(), "Enter Valid Details!", Toast.LENGTH_SHORT).show();
                }

           }
        });

    }

    public String encrypt_passwd(String passwd) {

        String encrypted_passwd="";

        encrypted_passwd = Hashing.sha256()
                .hashString(passwd, StandardCharsets.UTF_8)
                .toString();

        return encrypted_passwd;
    }

    public boolean form_validation(){
        Pattern name_regex = Pattern.compile("[a-zA-Z.]{3,}");
        Pattern email_regex = Patterns.EMAIL_ADDRESS;
        Pattern password_regex = Pattern.compile("^(?=.*[0-9]+)(?=.*[a-z]+)(?=.*[A-Z]+)(?=.*[!@#$%^&*+=-]+).{8,32}$");

        Matcher name_match = name_regex.matcher(name);
        Matcher email_match = email_regex.matcher(email);
        Matcher password_match = password_regex.matcher(password);

        boolean form_valid=true;

        if(!name_match.matches()) {
            edit_name.setError("Please enter a valid name, no spaces");
            form_valid=false;
        }

        if(!email_match.matches()) {
            edit_email.setError("Please enter a valid email id");
            form_valid=false;
        }
        if(!password_match.matches()){
            edit_password.setError("Please enter a valid password containing at least 1 lower case, 1 upper case, 1 number and 1 special character [!@#$%^&*+=-]. Minimum Length must be 8 characters");
            form_valid=false;
        }
        if(!(password.equals(conf_passwd))){
            edit_conf_passwd.setError("Password doesn't match!");
            form_valid=false;
        }

        return form_valid;
    }
}