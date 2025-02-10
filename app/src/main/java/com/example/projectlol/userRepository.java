package com.example.projectlol;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class userRepository {
    private static final String TAG = "UserRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface UserDataCallback {
        void onSuccess(userModel user);
        void onFailure(String error);
    }

    public void getUserData(UserDataCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.e("REPO", "Пользователь не аутентифицирован");
            callback.onFailure("User not authenticated");
            return;
        }

        Log.d("REPO", "Загрузка данных для UID: " + user.getUid());

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    Log.d("REPO", "Документ существует: " + document.exists());
                    if (document.exists()) {
                        userModel model = document.toObject(userModel.class);
                        if (model != null) {
                            Log.d("REPO", "Данные пользователя получены: " + model.getName());
                            callback.onSuccess(model);
                        }
                    } else {
                        Log.d("REPO", "Создание нового пользователя");
                        createNewUser(user, callback);
                    }
                });
    }

    private void createNewUser(FirebaseUser firebaseUser, UserDataCallback callback) {
        userModel newUser = new  userModel(
                firebaseUser.getUid(),
                firebaseUser.getEmail(), // Используем email как имя
                0.0
        );

        db.collection("users").document(firebaseUser.getUid())
                .set(newUser)
                .addOnSuccessListener(aVoid -> callback.onSuccess(newUser))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user", e);
                    callback.onFailure("User creation failed: " + e.getMessage());
                });
    }
}