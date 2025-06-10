package com.example.mobilehealthcareapp_java;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable; // For TextWatcher
import android.text.TextUtils;
import android.text.TextWatcher; // For TextWatcher
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
    public static final String EXTRA_RECEIVER_NAME = "RECEIVER_NAME";

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
        String receiverName = getIntent().getStringExtra(EXTRA_RECEIVER_NAME);

        if (receiverId == null) {
            Toast.makeText(this, "Receiver not specified. Cannot open chat.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null && !TextUtils.isEmpty(receiverName)) {
            getSupportActionBar().setTitle("Chat with " + receiverName);
        } else if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_chat); // Use string resource
        }


        chatRoomId = getChatRoomId(senderId, receiverId);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(linearLayoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);

        // Initially disable send button if message is empty
        buttonSendMessage.setEnabled(false);
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSendMessage.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonSendMessage.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private String getChatRoomId(String userId1, String userId2) {
        List<String> ids = Arrays.asList(userId1, userId2);
        Collections.sort(ids);
        return ids.get(0) + "_" + ids.get(1);
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        // Check is already handled by TextWatcher enabling/disabling the button
        if (TextUtils.isEmpty(messageText)) {
            // Toast.makeText(this, "Cannot send an empty message.", Toast.LENGTH_SHORT).show(); // Optional: if button could still be clicked
            return;
        }

        Message message = new Message(senderId, receiverId, messageText);
        // Timestamp will be set by @ServerTimestamp in Message model

        // Show progress indicator if any
        buttonSendMessage.setEnabled(false); // Temporarily disable during send

        db.collection("chats").document(chatRoomId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    editTextMessage.setText(""); // Clear input field
                    // buttonSendMessage remains disabled until text is entered due to TextWatcher
                    Log.d(TAG, "Message sent successfully: " + documentReference.getId());
                    // RecyclerView should scroll down automatically if new items are added at the end
                    // and stackFromEnd is true or you manually scroll.
                })
                .addOnFailureListener(e -> {
                    buttonSendMessage.setEnabled(true); // Re-enable on failure
                    Toast.makeText(ChatActivity.this, "Error sending message. Please try again.", Toast.LENGTH_SHORT).show();
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
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Message newMessage = dc.getDocument().toObject(Message.class);
                                newMessage.setMessageId(dc.getDocument().getId());
                                messageAdapter.addMessage(newMessage); // Adapter handles adding and notifying
                                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                            }
                            // Handle MODIFIED or REMOVED if needed
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}
