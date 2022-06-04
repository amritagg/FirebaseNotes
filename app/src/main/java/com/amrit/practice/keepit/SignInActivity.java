package com.amrit.practice.keepit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private static final String LOG_TAG = SignInActivity.class.getSimpleName();
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mAuth = FirebaseAuth.getInstance();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Button to start in
        findViewById(R.id.sign_in).setOnClickListener(view -> signIn());
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        signInActivityLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> signInActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.e(LOG_TAG, result.getResultCode() + "");
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.e(LOG_TAG, "firebaseAuthWithGoogle: " + account.getId());
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.e(LOG_TAG, "Google sign in failed", e);
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        Log.e(LOG_TAG, "firebase auth " + idToken);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        findViewById(R.id.sign_in).setVisibility(View.GONE);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.e(LOG_TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                        if (user != null) {
                            final DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference()
                                    .child(Constants.FIRE_USER).child(user.getUid());

                            mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        String email = user.getEmail();
                                        assert email != null;
                                        String userName = email.substring(0, email .indexOf("@"));
                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put(Constants.FIRE_EMAIL, email);
                                        userMap.put(Constants.FIRE_NAME, user.getDisplayName());
                                        userMap.put(Constants.FIRE_USERNAME, userName);
                                        if(user.getPhotoUrl() != null){
                                            userMap.put(Constants.FIRE_PHOTO, Objects.requireNonNull(user.getPhotoUrl()).toString());
                                        }
                                        mUserDb.updateChildren(userMap);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }

                            });

                        }
                        updateUI(user);

                    } else {
                        Log.w(LOG_TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });

        mAuth.signInWithCredential(credential)
                .addOnFailureListener(this, e -> {
                    String msg = e.getMessage();
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    updateUI(null);
                });

    }

    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            findViewById(R.id.sign_in).setVisibility(View.VISIBLE);
            findViewById(R.id.progress).setVisibility(View.GONE);
        }
    }

}