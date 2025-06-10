package com.example.mobilehealthcareapp_java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilehealthcareapp_java.models.MedicalDocument;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MedicalDocumentAdapter extends RecyclerView.Adapter<MedicalDocumentAdapter.DocumentViewHolder> {

    private Context context;
    private List<MedicalDocument> documentList;
    private OnDocumentActionListener listener;

    public interface OnDocumentActionListener {
        void onViewDocument(MedicalDocument document);
        void onDeleteDocument(MedicalDocument document, int position);
    }

    public MedicalDocumentAdapter(Context context, List<MedicalDocument> documentList, OnDocumentActionListener listener) {
        this.context = context;
        this.documentList = documentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medical_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        MedicalDocument document = documentList.get(position);

        holder.textViewDocumentName.setText(document.getFileName());

        if (document.getUploadedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            holder.textViewDocumentUploadDate.setText("Uploaded: " + sdf.format(document.getUploadedAt().toDate()));
        } else {
            holder.textViewDocumentUploadDate.setText("Uploaded: Date N/A");
        }

        // Set icons based on file type (basic example)
        if (document.getFileType() != null) {
            if (document.getFileType().startsWith("image/")) {
                holder.imageViewDocumentIcon.setImageResource(R.drawable.ic_image_placeholder); // Create this drawable
            } else if (document.getFileType().equals("application/pdf")) {
                holder.imageViewDocumentIcon.setImageResource(R.drawable.ic_pdf_placeholder); // Create this drawable
            } else {
                holder.imageViewDocumentIcon.setImageResource(R.drawable.ic_document_placeholder); // Default
            }
        } else {
            holder.imageViewDocumentIcon.setImageResource(R.drawable.ic_document_placeholder);
        }


        holder.buttonViewDocument.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDocument(document);
            }
        });

        holder.buttonDeleteDocument.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteDocument(document, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewDocumentIcon;
        TextView textViewDocumentName;
        TextView textViewDocumentUploadDate;
        ImageButton buttonViewDocument;
        ImageButton buttonDeleteDocument;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewDocumentIcon = itemView.findViewById(R.id.imageViewDocumentIcon);
            textViewDocumentName = itemView.findViewById(R.id.textViewDocumentName);
            textViewDocumentUploadDate = itemView.findViewById(R.id.textViewDocumentUploadDate);
            buttonViewDocument = itemView.findViewById(R.id.buttonViewDocument);
            buttonDeleteDocument = itemView.findViewById(R.id.buttonDeleteDocument);
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < documentList.size()) {
            documentList.remove(position);
            notifyItemRemoved(position);
            // notifyItemRangeChanged(position, documentList.size()); // Optional: if positions change
        }
    }

    public void addDocument(MedicalDocument document) {
        documentList.add(0, document); // Add to the top
        notifyItemInserted(0);
    }

    public void setDocuments(List<MedicalDocument> documents) {
        this.documentList.clear();
        this.documentList.addAll(documents);
        notifyDataSetChanged();
    }
}
