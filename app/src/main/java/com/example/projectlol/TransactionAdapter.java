package com.example.projectlol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectlol.R;
import com.example.projectlol.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.senderNameTextView.setText(transaction.getSenderName());
        holder.amountTextView.setText(String.format("Сумма: ₽%.2f", transaction.getAmount()));
        holder.messageTextView.setText(transaction.getMessage());
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView, amountTextView, messageTextView, statusTextView;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            senderNameTextView = itemView.findViewById(R.id.senderNameTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }
    }
}
