package com.example.projectlol;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class donation extends AppCompatActivity {

    private EditText etRecipientId, etAmount, etMessage;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        etRecipientId = findViewById(R.id.etRecipientId);
        etAmount = findViewById(R.id.etAmount);
        etMessage = findViewById(R.id.etMessage);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnDonate).setOnClickListener(v -> processDonation());
    }

    private void processDonation() {
        String recipientId = etRecipientId.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (validateInput(recipientId, amountStr)) {
            progressBar.setVisibility(View.VISIBLE);
            double amount = Double.parseDouble(amountStr);

            if (auth.getCurrentUser().getUid().equals(recipientId)) {
                showError("Нельзя отправлять себе");
                progressBar.setVisibility(View.GONE);
                return;
            }

            DocumentReference recipientRef = db.collection("users").document(recipientId);
            DocumentReference senderRef = db.collection("users").document(auth.getUid());

            db.runTransaction(transaction -> {
                DocumentSnapshot senderSnapshot = transaction.get(senderRef);
                double senderBalance = senderSnapshot.getDouble("balance");
                DocumentSnapshot recipientSnapshot = transaction.get(recipientRef);
                double recipientBalance = recipientSnapshot.getDouble("balance");
                if (senderBalance < amount) {
                    try {
                        throw new Exception("Недостаточно средств");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                transaction.update(senderRef, "balance", senderBalance - amount);
               // transaction.update(recipientRef, "balance", recipientBalance + amount);
                Map<String, Object> transactionData = new HashMap<>();
                transactionData.put("senderId", auth.getUid());
                transactionData.put("recipientId", recipientId);
                transactionData.put("amount", amount);
                transactionData.put("message", message);
                transactionData.put("processed", false);
                transactionData.put("timestamp", FieldValue.serverTimestamp());

                db.collection("transactions").add(transactionData);

                return null;
            }).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Перевод успешно выполнен!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = "Ошибка: " + task.getException().getMessage();
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                }
            });
            recipientRef.get().addOnSuccessListener(recipientDoc -> {
                if (recipientDoc.exists()) {
                    Log.d("Firestore", "Получатель найден: " + recipientDoc.getData());
                } else {
                    Log.e("Firestore", "Документ получателя не найден");
                }
            }).addOnFailureListener(e -> {
                Log.e("Firestore", "Ошибка доступа к получателю", e);
            });
        }
    }

    private boolean validateInput(String recipientId, String amountStr) {
        if (recipientId.isEmpty()) {
            showError("Введите ID получателя");
            return false;
        }

        if (amountStr.isEmpty()) {
            showError("Введите сумму");
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                showError("Сумма должна быть больше нуля");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Неверный формат суммы");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}