package com.example.mobilehealthcareapp_java;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MessagingTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> loginActivityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    private String patientEmail;
    private String professionalEmail;
    private final String password = "password123";
    private final String patientName = "Test Patient Message";
    private final String professionalName = "Dr. TestMsgProf";

    // Helper method to click on a child view within a RecyclerView item
    public static ViewAction clickOnViewChild(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null; // Or suitable constraints like: Matchers.allOf(isDisplayed(), isAssignableFrom(View.class));
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }


    @Before
    public void setupTestEnvironmentAndBookAppointment() throws InterruptedException {
        // 1. Sign out
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1200);

        // 2. Register User A (Patient)
        patientEmail = "patient_msg_test_" + System.currentTimeMillis() + "@example.com";
        onView(withId(R.id.textViewRegisterLink)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.editTextEmailRegister)).perform(typeText(patientEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.editTextConfirmPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonRegister)).perform(click());
        Thread.sleep(2500);

        // 3. Login User A (Patient)
        onView(withId(R.id.editTextEmailLogin)).perform(typeText(patientEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordLogin)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonLogin)).perform(click());
        Thread.sleep(2500);

        // 4. Create Patient Profile for User A
        onView(withId(R.id.buttonGoToProfile)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.editTextPatientName)).perform(typeText(patientName), closeSoftKeyboard());
        onView(withId(R.id.editTextPatientDOB)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(1990, 1, 1));
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.buttonSavePatientProfile)).perform(click());
        Thread.sleep(1500);
        onView(withId(R.id.editTextPatientName)).perform(click()); // Dismiss Toast

        // 5. Logout User A
        loginActivityRule.getScenario().onActivity(activity -> activity.onBackPressed()); // Back to Main
        Thread.sleep(500);
        onView(withId(R.id.buttonLogout)).perform(click());
        Thread.sleep(1200);

        // 6. Register User B (Professional)
        professionalEmail = "prof_msg_test_" + System.currentTimeMillis() + "@example.com";
        onView(withId(R.id.textViewRegisterLink)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.editTextEmailRegister)).perform(typeText(professionalEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.editTextConfirmPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonRegister)).perform(click());
        Thread.sleep(2500);

        // 7. Login User B (Professional)
        onView(withId(R.id.editTextEmailLogin)).perform(typeText(professionalEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordLogin)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonLogin)).perform(click());
        Thread.sleep(2500);

        // 8. Create Professional Profile for User B
        onView(withId(R.id.buttonGoToProfessionalProfile)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.editTextProfessionalName)).perform(typeText(professionalName), closeSoftKeyboard());
        onView(withId(R.id.editTextQualifications)).perform(typeText("MD Messaging"), closeSoftKeyboard());
        onView(withId(R.id.editTextSpecialties)).perform(typeText("Telepathy"), closeSoftKeyboard());
        onView(withId(R.id.editTextAvailability)).perform(typeText("Always Available for Test Messages"), closeSoftKeyboard());
        onView(withId(R.id.buttonSaveProfessionalProfile)).perform(click());
        Thread.sleep(1500);
        onView(withId(R.id.editTextProfessionalName)).perform(click()); // Dismiss Toast

        // 9. Logout User B
        loginActivityRule.getScenario().onActivity(activity -> activity.onBackPressed()); // Back to Main
        Thread.sleep(500);
        onView(withId(R.id.buttonLogout)).perform(click());
        Thread.sleep(1200);

        // 10. Login User A (Patient) again
        onView(withId(R.id.editTextEmailLogin)).perform(typeText(patientEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordLogin)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonLogin)).perform(click());
        Thread.sleep(2500);

        // 11. Navigate to ListProfessionalsActivity
        onView(withId(R.id.buttonGoToProfile)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.buttonBookAppointment)).perform(click());
        Thread.sleep(2000); // Wait for professionals list to load

        // 12. Select User B (Professional)
        onView(withId(R.id.recyclerViewProfessionals))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(1000);

        // 13. Book an appointment
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7); // One week from now
        onView(withId(R.id.editTextAppointmentDate)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.editTextAppointmentTime)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(14, 0)); // 2:00 PM
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.editTextAppointmentReason)).perform(typeText("Appointment for messaging test"), closeSoftKeyboard());
        onView(withId(R.id.buttonConfirmAppointment)).perform(click());
        Thread.sleep(3000); // Wait for booking and navigation back to ListProfessionals

        // 14. Navigate to ViewAppointmentsActivity (as Patient)
        // Back from ScheduleAppointment to ListProfessionals, then back to PatientProfile
        loginActivityRule.getScenario().onActivity(activity -> activity.onBackPressed()); // Back to PatientProfile
        Thread.sleep(500);
        onView(withId(R.id.buttonViewPatientAppointments)).perform(click());
        Thread.sleep(2000); // Wait for appointments to load
    }

    @Test
    public void testSendMessageAndVerifyReceipt() throws InterruptedException {
        // Assumes @Before has run, and ViewAppointmentsActivity is open for User A (Patient)
        // and an appointment with User B (Professional) is at position 0.

        // Click "Chat" button on the first appointment
        onView(withId(R.id.recyclerViewAppointments))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.buttonChat)));
        Thread.sleep(1500); // Wait for ChatActivity to open

        // Now in ChatActivity
        String testMessage = "Hello, Dr. " + professionalName + "! This is a test message from " + patientName + ".";

        // Type the message
        onView(withId(R.id.editTextMessage)).perform(typeText(testMessage), closeSoftKeyboard());
        // Check if send button is enabled (it should be after typing)
        onView(withId(R.id.buttonSendMessage)).check(matches(isDisplayed())); // or matches(isEnabled())

        // Click send
        onView(withId(R.id.buttonSendMessage)).perform(click());

        // Verify the message appears in the recyclerViewMessages
        Thread.sleep(2000); // Allow time for Firestore round trip and UI update from listener
        onView(withId(R.id.recyclerViewMessages)).check(matches(hasDescendant(withText(testMessage))));

        // Optional: Log out to clean up state for next potential manual run or other tests
        // loginActivityRule.getScenario().onActivity(activity -> activity.onBackPressed()); // Back to ViewAppointments
        // Thread.sleep(500);
        // loginActivityRule.getScenario().onActivity(activity -> activity.onBackPressed()); // Back to PatientProfile
        // Thread.sleep(500);
        // loginActivityRule.getScenario().onActivity(activity -> activity.onBackPressed()); // Back to Main
        // Thread.sleep(500);
        // onView(withId(R.id.buttonLogout)).perform(click());
        // Thread.sleep(1200);
    }
}
