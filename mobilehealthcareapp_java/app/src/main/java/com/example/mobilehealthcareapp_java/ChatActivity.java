package com.example.mobilehealthcareapp_java;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.mobilehealthcareapp_java.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    public static final String EXTRA_RECEIVER_ID = "RECEIVER_ID";
    public static final String EXTRA_RECEIVER_NAME = "RECEIVER_NAME"; // Optional: for display in toolbar

    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText editTextMessage;
    private ImageButton buttonSendMessage;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private String senderId;
    private String receiverId;
    private String chatRoomId;
    private ListenerRegistration messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to chat.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        senderId = currentUser.getUid();

        receiverId = getIntent().getStringExtra(EXTRA_RECEIVER_ID);
        String receiverName = getIntent().getStringExtra(EXTRA_RECEIVER_NAME); // Use this for the ActionBar title if needed

        if (receiverId == null) {
            Toast.makeText(this, "Receiver not specified.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null && !TextUtils.isEmpty(receiverName)) {
            getSupportActionBar().setTitle("Chat with " + receiverName);
        } else if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat");
        }


        chatRoomId = getChatRoomId(senderId, receiverId);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // To show latest messages at the bottom
        recyclerViewMessages.setLayoutManager(linearLayoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);

        buttonSendMessage.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private String getChatRoomId(String userId1, String userId2) {
        // Sort IDs alphabetically to ensure consistency
        List<String> ids = Arrays.asList(userId1, userId2);
        Collections.sort(ids);
        return ids.get(0) + "_" + ids.get(1);
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        Message message = new Message(senderId, receiverId, messageText);
        // Timestamp will be set by @ServerTimestamp

        db.collection("chats").document(chatRoomId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    editTextMessage.setText("");
                    Log.d(TAG, "Message sent successfully: " + documentReference.getId());
                    // RecyclerView should scroll down automatically due to stackFromEnd and adapter notification
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error sending message", e);
                });
    }

    private void loadMessages() {
        CollectionReference messagesRef = db.collection("chats").document(chatRoomId).collection("messages");
        messagesListener = messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Toast.makeText(ChatActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        // Using a loop through document changes for more efficient updates
                        // than clearing and re-adding the whole list on every snapshot.
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Message newMessage = dc.getDocument().toObject(Message.class);
                                    newMessage.setMessageId(dc.getDocument().getId());
                                    messageAdapter.addMessage(newMessage);
                                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                                    break;
                                // Handle MODIFIED and REMOVED if necessary for your app logic
                                // case MODIFIED:
                                // Log.d(TAG, "Modified message: " + dc.getDocument().getData());
                                // break;
                                // case REMOVED:
                                // Log.d(TAG, "Removed message: " + dc.getDocument().getData());
                                // break;
                            }
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove(); // Stop listening to prevent memory leaks
        }
    }
}
