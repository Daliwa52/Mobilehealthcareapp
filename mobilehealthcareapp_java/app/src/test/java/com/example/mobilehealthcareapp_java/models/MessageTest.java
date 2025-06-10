package com.example.mobilehealthcareapp_java.models;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.firebase.Timestamp;
import java.util.Date;

public class MessageTest {

    @Test
    public void message_DefaultConstructor() {
        Message message = new Message();
        assertNull("Default constructor messageId should be null", message.getMessageId());
        assertNull("Default constructor senderId should be null", message.getSenderId());
        assertNull("Default constructor receiverId should be null", message.getReceiverId());
        assertNull("Default constructor messageText should be null", message.getMessageText());
        assertNull("Default constructor timestamp should be null", message.getTimestamp());
    }

    @Test
    public void message_ParameterizedConstructorAndGetters() {
        String senderId = "user1";
        String receiverId = "user2";
        String text = "Hello there!";
        // Timestamp is usually set by @ServerTimestamp or manually before saving, not in this constructor.

        Message message = new Message(senderId, receiverId, text);

        assertEquals("SenderId should match constructor argument", senderId, message.getSenderId());
        assertEquals("ReceiverId should match constructor argument", receiverId, message.getReceiverId());
        assertEquals("MessageText should match constructor argument", text, message.getMessageText());
    }

    @Test
    public void message_Id_GetterSetter() {
        Message message = new Message();
        String testId = "msg123";
        message.setMessageId(testId);
        assertEquals("MessageId getter/setter failed", testId, message.getMessageId());
    }

    @Test
    public void message_SenderId_GetterSetter() {
        Message message = new Message();
        String testSenderId = "senderABC";
        message.setSenderId(testSenderId);
        assertEquals("SenderId getter/setter failed", testSenderId, message.getSenderId());
    }

    @Test
    public void message_ReceiverId_GetterSetter() {
        Message message = new Message();
        String testReceiverId = "receiverXYZ";
        message.setReceiverId(testReceiverId);
        assertEquals("ReceiverId getter/setter failed", testReceiverId, message.getReceiverId());
    }

    @Test
    public void message_MessageText_GetterSetter() {
        Message message = new Message();
        String testText = "How are you?";
        message.setMessageText(testText);
        assertEquals("MessageText getter/setter failed", testText, message.getMessageText());
    }

    @Test
    public void message_Timestamp_GetterSetter() {
        Message message = new Message();
        Timestamp testTimestamp = new Timestamp(new Date());
        message.setTimestamp(testTimestamp);
        assertEquals("Timestamp getter/setter failed", testTimestamp, message.getTimestamp());
        // Note: If @ServerTimestamp is active, the actual value might be different when retrieved from Firestore.
        // This test only checks local getter/setter.
    }
}
