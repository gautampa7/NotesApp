package com.example.mobappdev_da_notesapp_18bsw0005;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.hash.Hashing;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    String email,password,encrypted_password,db_password;
    Button sigin,signup;
    EditText edit_username,edit_password;
    Boolean form_validated = false;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference user_col = db.collection("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        edit_username = (EditText) findViewById(R.id.editTextTextEmailAddress);
        edit_password = (EditText) findViewById(R.id.editTextTextPassword);

        sigin = (Button) findViewById(R.id.loginbtn);
        signup = (Button) findViewById(R.id.signupbtn);

        sigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email= edit_username.getText().toString().toLowerCase();
                password = edit_password.getText().toString();

                form_validated=form_validation();

                if(form_validated){

                    try {
                        encrypted_password = encrypt_passwd(password);
                    } catch (Error e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }

                    DocumentReference user_doc = user_col.document("user_"+email);
                    user_doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                if (document.exists()) {

                                    db_password = document.getData().get("password_key").toString();

                                    if(db_password.matches(encrypted_password)) {
                                        Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                                        Intent home = new Intent(getApplicationContext(),ActivityHomePage.class);
                                        home.putExtra("email",email);
                                        startActivity(home);

                                    }else{

                                        edit_password.setError("Invalid Credentials! Try Again or reset password!.");
                                    }
                                } else {

                                    edit_username.setError("User doesn't exits! Sign up or retry with registered email id.");
                                    Toast.makeText(getApplicationContext(), "User Doesn't Exits!", Toast.LENGTH_SHORT).show();

                                }
                            } else {
                                Toast.makeText(getApplicationContext()," FireBase Connection ERROR!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }else
                {
                    Toast.makeText(getApplicationContext(), "Enter Valid Details!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent next = new Intent(getApplicationContext(),UserSignUpActivity.class);
                startActivity(next);
            }
        });

    }

    public boolean form_validation(){

        Pattern email_regex = Patterns.EMAIL_ADDRESS;
        Pattern password_regex = Pattern.compile("^(?=.*[0-9]+)(?=.*[a-z]+)(?=.*[A-Z]+)(?=.*[!@#$%^&*+=-]+).{8,32}$");

        Matcher email_match = email_regex.matcher(email);
        Matcher password_match = password_regex.matcher(password);

        boolean form_valid=true;

        if(!email_match.matches()) {
            edit_username.setError("Please enter a valid email id");
            form_valid=false;
        }
        if(!password_match.matches()){
            edit_password.setError("Please enter a valid password containing at least 1 lower case, 1 upper case, 1 number and 1 special character [!@#$%^&*+=-]. Minimum Length must be 8 characters");
            form_valid=false;
        }

        return form_valid;
    }

    public String encrypt_passwd(String passwd) {

        String encrypted_passwd="";

        encrypted_passwd = Hashing.sha256()
                .hashString(passwd, StandardCharsets.UTF_8)
                .toString();

        return encrypted_passwd;
    }


}