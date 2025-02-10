package com.example.projectlol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class profile extends AppCompatActivity {

    private TextView tvName, tvBalance;
    private userViewModel userViewModel;
    private ImageButton logout;
    private ProgressBar progressBar;
    private Button donateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        syncReceivedFunds();
        tvName = findViewById(R.id.tvUserName);
        tvBalance = findViewById(R.id.tvBalance);
        logout = findViewById(R.id.signOutButton);
        progressBar = findViewById(R.id.progressBarProfile);
        donateButton = findViewById(R.id.makeNewDonation);
        userViewModel = new ViewModelProvider(this).get(userViewModel.class);
        userViewModel.loadUserData();

        userViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                tvName.setText(user.getName());
                tvBalance.setText(String.format("Баланс: ₽%.2f", user.getBalance()));
            }
        });

        logout.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            performLogout();
        });


        donateButton.setOnClickListener(v -> {
            startActivity(new Intent(profile.this, donation.class));
        });

        userViewModel.getErrorLiveData().observe(this, error -> {

            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                performLogout();
            }

            findViewById(R.id.signOutButton).setOnClickListener(v ->
            {
                FirebaseAuth.getInstance().signOut();
                recreate();
            });
        });
    }

    private void performLogout() {
        // Выход из Firebase
        FirebaseAuth.getInstance().signOut();

        // Переход на экран входа
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        progressBar.setVisibility(View.GONE);
    }

    private void syncReceivedFunds() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String recipientId = auth.getCurrentUser().getUid();

        db.collection("transactions")
                .whereEqualTo("recipientId", recipientId) // Получаем все транзакции для пользователя
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Ошибка синхронизации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        return;
                    }

                    List<Transaction> transactions = new ArrayList<>();
                    for (DocumentSnapshot transactionDoc : snapshots.getDocuments()) {
                        double amount = transactionDoc.getDouble("amount");
                        boolean isProcessed = transactionDoc.getBoolean("processed");
                        String senderId = transactionDoc.getString("senderId");
                        String message = transactionDoc.getString("message");

                        // Добавляем транзакцию в список для отображения
                        transactions.add(new Transaction(senderId, amount, message, isProcessed ? "Обработано" : "Необработано"));

                        // Если транзакция необработанная, увеличиваем баланс
                        if (!isProcessed) {
                            DocumentReference recipientRef = db.collection("users").document(recipientId);

                            // Увеличиваем баланс получателя
                            recipientRef.update("balance", FieldValue.increment(amount))
                                    .addOnSuccessListener(aVoid -> {
                                        // Помечаем транзакцию как обработанную
                                        transactionDoc.getReference().update("processed", true);
                                    })
                                    .addOnFailureListener(err -> {
                                        Toast.makeText(this, "Ошибка обновления баланса: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                    // Обновляем RecyclerView с транзакциями
                    updateRecyclerView(transactions);
                });
    }



    private void updateRecyclerView(List<Transaction> transactions) {
        RecyclerView recyclerView = findViewById(R.id.transactionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TransactionAdapter adapter = new TransactionAdapter(transactions);
        recyclerView.setAdapter(adapter);
    }
}