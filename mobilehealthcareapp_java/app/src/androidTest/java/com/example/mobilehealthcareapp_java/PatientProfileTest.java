package com.example.mobilehealthcareapp_java;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.widget.DatePicker;
import androidx.test.espresso.contrib.PickerActions; // For DatePickerDialog
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PatientProfileTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> loginActivityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    private String uniqueEmail;
    private final String password = "password123";

    @Before
    public void setupTestEnvironment() throws InterruptedException {
        // Sign out any previous user
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1200); // Slightly longer pause for Firebase operations

        // Register a new unique user
        uniqueEmail = "patient_profile_test_" + System.currentTimeMillis() + "@example.com";

        onView(withId(R.id.textViewRegisterLink)).perform(click()); // Navigate to Register
        Thread.sleep(500);
        onView(withId(R.id.editTextEmailRegister)).perform(typeText(uniqueEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.editTextConfirmPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonRegister)).perform(click());
        Thread.sleep(2500); // Wait for registration and navigation to LoginActivity

        // Login the new user
        onView(withId(R.id.editTextEmailLogin)).perform(typeText(uniqueEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordLogin)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonLogin)).perform(click());
        Thread.sleep(2500); // Wait for login and navigation to MainActivity

        // Navigate to PatientProfileActivity from MainActivity
        onView(withId(R.id.buttonGoToProfile)).check(matches(isDisplayed())); // Ensure button is there
        onView(withId(R.id.buttonGoToProfile)).perform(click());
        Thread.sleep(1500); // Wait for PatientProfileActivity to load
    }

    @Test
    public void testCreateAndVerifyPatientProfile() throws InterruptedException {
        String testPatientName = "Test Patient Name";
        String testDOBYear = "2000";
        String testDOBMonth = "01"; // January (month is 1-12 for PickerActions, but 0-11 for Calendar)
        String testDOBDay = "15";
        String expectedDOBText = "2000-01-15"; // Format set by DatePickerDialog in app

        String testMedicalHistory = "Seasonal allergies, mild asthma.";
        String testAllergies = "Pollen, Dust";
        String testMedications = "Ventolin Inhaler (as needed), Loratadine (daily)";

        // Input data into fields
        onView(withId(R.id.editTextPatientName)).perform(typeText(testPatientName), closeSoftKeyboard());

        // Interact with DatePickerDialog for DOB
        onView(withId(R.id.editTextPatientDOB)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(Integer.parseInt(testDOBYear), Integer.parseInt(testDOBMonth), Integer.parseInt(testDOBDay)));
        onView(withId(android.R.id.button1)).perform(click()); // Click "OK" on DatePickerDialog
        Thread.sleep(500); // Allow dialog to close and text to update

        onView(withId(R.id.editTextMedicalHistory)).perform(typeText(testMedicalHistory), closeSoftKeyboard());
        onView(withId(R.id.editTextAllergies)).perform(typeText(testAllergies), closeSoftKeyboard());
        onView(withId(R.id.editTextMedications)).perform(typeText(testMedications), closeSoftKeyboard());

        // Save the profile
        onView(withId(R.id.buttonSavePatientProfile)).perform(click());
        Thread.sleep(2500); // Wait for Firestore save operation and Toast

        // Verify Toast message (optional, but good for quick feedback check)
        // onView(withText(R.string.profile_save_success)).inRoot(new ToastMatcher())
        // .check(matches(isDisplayed())); // ToastMatcher is a custom Espresso utility

        // Re-enter activity to force reload from Firestore and verify persistence
        onView(isRoot()).perform(pressBack()); // Back to MainActivity
        Thread.sleep(500);
        onView(withId(R.id.buttonGoToProfile)).perform(click()); // Re-open PatientProfileActivity
        Thread.sleep(2500); // Wait for data to load from Firestore

        // Assert that the fields are populated with the saved data
        onView(withId(R.id.editTextPatientName)).check(matches(withText(testPatientName)));
        onView(withId(R.id.editTextPatientDOB)).check(matches(withText(expectedDOBText)));
        onView(withId(R.id.editTextMedicalHistory)).check(matches(withText(testMedicalHistory)));
        onView(withId(R.id.editTextAllergies)).check(matches(withText(testAllergies))); // Assumes comma-separated input is stored and reloaded as is
        onView(withId(R.id.editTextMedications)).check(matches(withText(testMedications)));

        // Clean up: Sign out the user (optional, as @Before handles it for the next test)
        // onView(isRoot()).perform(pressBack()); // Back to MainActivity
        // onView(withId(R.id.buttonLogout)).perform(click());
        // Thread.sleep(1000);
    }
}
