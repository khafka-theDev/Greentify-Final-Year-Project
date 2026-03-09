package org.oss.greentify;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.oss.greentify.Home.AboutActivity;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText email, password, passwordConfirm, username;
    Button register;
    TextView loginCta;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        passwordConfirm = findViewById(R.id.editTextPasswordConfirm);
        username = findViewById(R.id.editTextUsername);

        register = findViewById(R.id.buttonRegister);
        register.setOnClickListener(view -> registerUser(
                email.getText().toString().trim(),
                password.getText().toString().trim(),
                passwordConfirm.getText().toString().trim(),
                username.getText().toString().trim()
        ));

        loginCta = findViewById(R.id.loginCta);
        loginCta.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    void registerUser(String userEmail, String userPassword, String userPasswordConfirm, String userUsername) {
        if (userEmail.isEmpty() || userPassword.isEmpty() || userPasswordConfirm.isEmpty() || userUsername.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!userPassword.equals(userPasswordConfirm)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", userEmail);
                            userData.put("username", userUsername);
                            userData.put("points", 0);
                            userData.put("profilePictureUrl", "");
                            userData.put("role", "user");

                            db.collection("users").document(uid).set(userData)
                                    .addOnSuccessListener(unused -> {
                                        Dialog successDialog = new Dialog(RegisterActivity.this);
                                        successDialog.setContentView(R.layout.dialog_register_success);
                                        successDialog.setCancelable(false);

                                        LottieAnimationView animationView = successDialog.findViewById(R.id.lottieSuccess);
                                        animationView.playAnimation();

                                        animationView.addAnimatorListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                successDialog.dismiss();

                                                // ✅ Save first-time walkthrough flag
                                                SharedPreferences prefs = getSharedPreferences("GreentifyPrefs", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = prefs.edit();
                                                editor.putBoolean("isFirstTimeUser", true);
                                                editor.putBoolean("hasSeenMainTutorial", false); // Reset this too
                                                editor.apply();

                                                // ✅ Go to AboutActivity
                                                Intent intent = new Intent(RegisterActivity.this, AboutActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 💥 This line is important
                                                startActivity(intent);

                                            }

                                            @Override public void onAnimationStart(Animator animation) {}
                                            @Override public void onAnimationCancel(Animator animation) {}
                                            @Override public void onAnimationRepeat(Animator animation) {}
                                        });

                                        successDialog.show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Registration failed (Firestore)", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
