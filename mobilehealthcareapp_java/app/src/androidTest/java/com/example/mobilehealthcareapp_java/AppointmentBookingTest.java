package com.example.mobilehealthcareapp_java;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText; // For Toast, though not directly used here

import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.contrib.RecyclerViewActions; // For RecyclerView
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppointmentBookingTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> loginActivityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    private String patientEmail;
    private String professionalEmail;
    private final String password = "password123";
    private final String professionalName = "Dr. TestProf";

    @Before
    public void setupTestEnvironment() throws InterruptedException {
        // 1. Sign out any previous user
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1200);

        // --- Create User A (Patient) ---
        patientEmail = "patient_appt_test_" + System.currentTimeMillis() + "@example.com";
        // Navigate to Register
        onView(withId(R.id.textViewRegisterLink)).perform(click());
        Thread.sleep(500);
        // Register Patient
        onView(withId(R.id.editTextEmailRegister)).perform(typeText(patientEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.editTextConfirmPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonRegister)).perform(click());
        Thread.sleep(2500); // Wait for registration & navigation to Login

        // Login Patient
        onView(withId(R.id.editTextEmailLogin)).perform(typeText(patientEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordLogin)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonLogin)).perform(click());
        Thread.sleep(2500); // Wait for login & navigation to Main

        // Create Patient Profile (minimal)
        onView(withId(R.id.buttonGoToProfile)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.editTextPatientName)).perform(typeText("Test Patient Appt"), closeSoftKeyboard());
        onView(withId(R.id.editTextPatientDOB)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(1990, 1, 1));
        onView(withId(android.R.id.button1)).perform(click()); // OK on DatePicker
        Thread.sleep(500);
        onView(withId(R.id.buttonSavePatientProfile)).perform(click());
        Thread.sleep(1500); // Wait for save
        onView(withId(R.id.editTextPatientName)).perform(click()); // Just to ensure previous Toast is gone

        // Navigate back to MainActivity and Logout User A (Patient)
        loginActivityRule.getScenario().onActivity(activity -> activity.onBackPressed()); // Back to Main
        Thread.sleep(500);
        onView(withId(R.id.buttonLogout)).perform(click());
        Thread.sleep(1200); // Wait for logout & navigation to Login

        // --- Create User B (Professional) ---
        professionalEmail = "prof_appt_test_" + System.currentTimeMillis() + "@example.com";
        // Navigate to Register
        onView(withId(R.id.textViewRegisterLink)).perform(click());
        Thread.sleep(500);
        // Register Professional
        onView(withId(R.id.editTextEmailRegister)).perform(typeText(professionalEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.editTextConfirmPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonRegister)).perform(click());
        Thread.sleep(2500); // Wait for registration & navigation to Login

        // Login Professional
        onView(withId(R.id.editTextEmailLogin)).perform(typeText(professionalEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordLogin)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonLogin)).perform(click());
        Thread.sleep(2500); // Wait for login & navigation to Main

        // Create Professional Profile (minimal)
        onView(withId(R.id.buttonGoToProfessionalProfile)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.editTextProfessionalName)).perform(typeText(professionalName), closeSoftKeyboard());
        onView(withId(R.id.editTextQualifications)).perform(typeText("MD, PhD"), closeSoftKeyboard());
        onView(withId(R.id.editTextSpecialties)).perform(typeText("General Practice, Testing"), closeSoftKeyboard());
        onView(withId(R.id.editTextAvailability)).perform(typeText("Mon-Fri, 9am-5pm for testing"), closeSoftKeyboard());
        onView(withId(R.id.buttonSaveProfessionalProfile)).perform(click());
        Thread.sleep(1500); // Wait for save
        onView(withId(R.id.editTextProfessionalName)).perform(click()); // Just to ensure previous Toast is gone

        // Navigate back to MainActivity and Logout User B (Professional)
        loginActivityRule.getScenario().onActivity(activity -> activity.onBackPressed()); // Back to Main
        Thread.sleep(500);
        onView(withId(R.id.buttonLogout)).perform(click());
        Thread.sleep(1200); // Wait for logout & navigation to Login

        // --- Login User A (Patient) again to perform the test ---
        onView(withId(R.id.editTextEmailLogin)).perform(typeText(patientEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordLogin)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonLogin)).perform(click());
        Thread.sleep(2500); // Wait for login & navigation to Main

        // Navigate to ListProfessionalsActivity
        onView(withId(R.id.buttonGoToProfile)).perform(click()); // To Patient Profile
        Thread.sleep(1000);
        onView(withId(R.id.buttonBookAppointment)).perform(click()); // To ListProfessionals
        Thread.sleep(2000); // Wait for professionals list to load
    }

    @Test
    public void testBookAppointmentAndVerify() throws InterruptedException {
        // Assumes @Before has successfully run and ListProfessionalsActivity is open
        // and contains at least one professional (User B created in @Before).

        // Select the first professional from the list
        onView(withId(R.id.recyclerViewProfessionals))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(1000); // Wait for ScheduleAppointmentActivity to open

        // In ScheduleAppointmentActivity:
        // Verify professional name is displayed (optional, but good sanity check)
        onView(withId(R.id.textViewSelectedProfessionalName)).check(matches(withText(professionalName)));

        // Select Date
        onView(withId(R.id.editTextAppointmentDate)).perform(click());
        // Get a date in the future to avoid issues with minDate
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 5); // 5 days from now
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // Picker month is 1-12
        int day = cal.get(Calendar.DAY_OF_MONTH);
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, month, day));
        onView(withId(android.R.id.button1)).perform(click()); // OK
        Thread.sleep(500);

        // Select Time
        onView(withId(R.id.editTextAppointmentTime)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(10, 30));
        onView(withId(android.R.id.button1)).perform(click()); // OK
        Thread.sleep(500);

        // Type a reason
        onView(withId(R.id.editTextAppointmentReason)).perform(typeText("Espresso Test appointment reason"), closeSoftKeyboard());

        // Confirm Appointment
        onView(withId(R.id.buttonConfirmAppointment)).perform(click());

        // Wait for appointment booking and navigation back (ScheduleAppointmentActivity finishes)
        Thread.sleep(3000); // Increased wait for Firestore operations

        // Assert that we are back on ListProfessionalsActivity
        onView(withId(R.id.recyclerViewProfessionals)).check(matches(isDisplayed()));

        // More robust verification could involve navigating to "View My Appointments"
        // and checking if the appointment details appear there. This requires User A to have a profile
        // and then navigating: ListProf -> (back) -> PatientProfile -> ViewAppointments
        // For this subtask, checking navigation back to ListProfessionalsActivity is the primary verification.
    }
}
