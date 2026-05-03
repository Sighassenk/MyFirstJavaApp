package com.example.myapplication.repositories;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.myapplication.models.User;

public class AuthRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void register(String name, String email, String password,
                         String role, OnCompleteListener<Void> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful() && authTask.getResult().getUser() != null) {
                        String uid = authTask.getResult().getUser().getUid();
                        User user = new User();
                        user.setId(uid);
                        user.setName(name);
                        user.setEmail(email);
                        user.setRole(role);

                        db.collection("users").document(uid).set(user)
                                .addOnCompleteListener(listener);
                    } else {
                        if (listener != null) {
                            listener.onComplete((com.google.android.gms.tasks.Task<Void>) (com.google.android.gms.tasks.Task<?>) authTask);
                        }
                    }
                });
    }

    public void login(String email, String password,
                      OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void getCurrentUser(OnSuccessListener<User> listener) {
        if (auth.getCurrentUser() == null) {
            listener.onSuccess(null);
            return;
        }
        String uid = auth.getCurrentUser().getUid();
        
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        listener.onSuccess(doc.toObject(User.class));
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onSuccess(null);
                });
    }

    public void logout() {
        auth.signOut();
    }
}
