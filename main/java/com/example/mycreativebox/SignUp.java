package com.example.mycreativebox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {


    public static final String TAG = "TAG";
    TextView toLoginBtn;
    EditText r_name, r_email, r_password, r_phoneNo;
    Button register;
    String userID;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);


        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();


        //hooks
        r_name = findViewById(R.id.PersonName);
        r_email = findViewById(R.id.reg_email);
        r_phoneNo = findViewById(R.id.reg_phone);
        r_password = findViewById(R.id.reg_password);
        register = findViewById(R.id.reg_button);
        toLoginBtn = findViewById(R.id.reg_login);
        progressBar = findViewById(R.id.progress_bar);



        toLoginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            }
        });



        //check if user is already registered
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), BluetoothConnect.class));
            finish();
        }

        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String email = r_email.getText().toString().trim();
                String password = r_password.getText().toString().trim();
                String fullName = r_name.getText().toString();
                String phone = r_phoneNo.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    r_email.setError("Email can't be empty");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    r_password.setError("Password can't be empty");
                    return;
                }

                if(password.length() < 7){
                    r_password.setError("Password should be more than seven characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);


                //register user in firebase
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            //send verification link
                            FirebaseUser fuser = mAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SignUp.this,"Verification email has been sent",Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"onFailure: email not sent" + e.getMessage());

                                }
                            });


                            Toast.makeText(SignUp.this,"User Created Successfully",Toast.LENGTH_LONG).show();
                            userID = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("fName",fullName);
                            user.put("email",email);
                            user.put("phone",phone);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG,"onSuccess: user profile is created for" + userID);
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"onFailure: " + e.toString());

                                }
                            });
                            startActivity(new Intent(getApplicationContext(), BluetoothConnect.class));
                        }
                        else{
                            Toast.makeText(SignUp.this,"Error: " + task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });


    };
}



