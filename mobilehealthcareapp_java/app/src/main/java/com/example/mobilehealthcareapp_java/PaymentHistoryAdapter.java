package com.example.mobilehealthcareapp_java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilehealthcareapp_java.models.Payment; // Ensure this model exists
import com.google.firebase.firestore.FirebaseFirestore; // Not directly used for now, but could be for fetching more details

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder> {

    private Context context;
    private List<Payment> paymentList;
    // private FirebaseFirestore db; // If needed for fetching related data like appointment details from ID

    public PaymentHistoryAdapter(Context context, List<Payment> paymentList) {
        this.context = context;
        this.paymentList = paymentList;
        // this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_history, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);

        // For now, just display Appointment ID. In a real app, you might fetch appointment details.
        holder.textViewPaymentItemAppointmentInfo.setText("Appointment ID: " + payment.getAppointmentId());
        holder.textViewPaymentItemAmount.setText(String.format(Locale.US, "Amount: $%.2f", payment.getAmount()));

        if (payment.getPaymentTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.US);
            holder.textViewPaymentItemDate.setText("Date: " + sdf.format(payment.getPaymentTimestamp().toDate()));
        } else {
            holder.textViewPaymentItemDate.setText("Date: Not available");
        }

        holder.textViewPaymentItemStatus.setText("Status: " + payment.getStatus());
        holder.textViewPaymentItemMethod.setText("Method: " + payment.getPaymentMethodDetails());
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPaymentItemAppointmentInfo;
        TextView textViewPaymentItemAmount;
        TextView textViewPaymentItemDate;
        TextView textViewPaymentItemStatus;
        TextView textViewPaymentItemMethod;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPaymentItemAppointmentInfo = itemView.findViewById(R.id.textViewPaymentItemAppointmentInfo);
            textViewPaymentItemAmount = itemView.findViewById(R.id.textViewPaymentItemAmount);
            textViewPaymentItemDate = itemView.findViewById(R.id.textViewPaymentItemDate);
            textViewPaymentItemStatus = itemView.findViewById(R.id.textViewPaymentItemStatus);
            textViewPaymentItemMethod = itemView.findViewById(R.id.textViewPaymentItemMethod);
        }
    }

    public void updatePaymentHistory(List<Payment> newPayments) {
        this.paymentList.clear();
        this.paymentList.addAll(newPayments);
        notifyDataSetChanged();
    }
}
