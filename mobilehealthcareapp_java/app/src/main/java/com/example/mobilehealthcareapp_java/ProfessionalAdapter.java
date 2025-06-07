package com.example.mobilehealthcareapp_java;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilehealthcareapp_java.models.HealthcareProfessional; // Ensure this model is created

import java.util.List;

public class ProfessionalAdapter extends RecyclerView.Adapter<ProfessionalAdapter.ProfessionalViewHolder> {

    private Context context;
    private List<HealthcareProfessional> professionalList;

    public ProfessionalAdapter(Context context, List<HealthcareProfessional> professionalList) {
        this.context = context;
        this.professionalList = professionalList;
    }

    @NonNull
    @Override
    public ProfessionalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_professional, parent, false);
        return new ProfessionalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfessionalViewHolder holder, int position) {
        HealthcareProfessional professional = professionalList.get(position);
        holder.textViewProfessionalNameItem.setText(professional.getName());

        if (professional.getSpecialties() != null && !professional.getSpecialties().isEmpty()) {
            holder.textViewProfessionalSpecialtyItem.setText("Specialties: " + TextUtils.join(", ", professional.getSpecialties()));
        } else {
            holder.textViewProfessionalSpecialtyItem.setText("Specialties: Not specified");
        }

        holder.textViewProfessionalAvailabilityItem.setText("Availability: " + (TextUtils.isEmpty(professional.getAvailability()) ? "Not specified" : professional.getAvailability()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ScheduleAppointmentActivity.class);
            intent.putExtra("PROFESSIONAL_ID", professional.getFirebaseId()); // Assuming HealthcareProfessional has getFirebaseId()
            intent.putExtra("PROFESSIONAL_NAME", professional.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return professionalList.size();
    }

    static class ProfessionalViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProfessionalNameItem;
        TextView textViewProfessionalSpecialtyItem;
        TextView textViewProfessionalAvailabilityItem;

        public ProfessionalViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProfessionalNameItem = itemView.findViewById(R.id.textViewProfessionalNameItem);
            textViewProfessionalSpecialtyItem = itemView.findViewById(R.id.textViewProfessionalSpecialtyItem);
            textViewProfessionalAvailabilityItem = itemView.findViewById(R.id.textViewProfessionalAvailabilityItem);
        }
    }

    // Helper method to update data
    public void setProfessionals(List<HealthcareProfessional> professionals) {
        this.professionalList = professionals;
        notifyDataSetChanged();
    }
}
