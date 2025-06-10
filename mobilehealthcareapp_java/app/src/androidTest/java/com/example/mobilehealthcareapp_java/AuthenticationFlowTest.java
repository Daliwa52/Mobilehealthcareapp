package com.example.mobilehealthcareapp_java;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AuthenticationFlowTest {

    // Rule to launch LoginActivity before each test method that needs it.
    // If a test starts with registration, it will navigate from LoginActivity.
    @Rule
    public ActivityScenarioRule<LoginActivity> loginActivityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    // CountingIdlingResource for managing Firebase asynchronous operations in tests.
    // Not fully implemented here for brevity, but this is where you'd integrate it.
    // For now, Thread.sleep will be used, but this is the better practice.
    private CountingIdlingResource firebaseIdlingResource;

    @Before
    public void signOutCurrentUser() {
        // Initialize Idling Resource - for more complex scenarios
        // firebaseIdlingResource = new CountingIdlingResource("FirebaseLoading");
        // IdlingRegistry.getInstance().register(firebaseIdlingResource);

        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            // Ignore if no user is signed in or other issues during signout attempt for test setup
        }
        // A short delay to allow Firebase to process the sign-out.
        // Replace with IdlingResource for robust tests.
        try {
            Thread.sleep(1500); // Increased sleep time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @After
    public void unregisterIdlingResource() {
        // if (firebaseIdlingResource != null) {
        //     IdlingRegistry.getInstance().unregister(firebaseIdlingResource);
        // }
    }

    @Test
    public void testUserRegistration_Success_NavigatesToLogin() throws InterruptedException {
        // Navigate from Login to Register screen
        onView(withId(R.id.textViewRegisterLink)).perform(click());

        // Generate a unique email for each test run
        String email = "testuser_" + System.currentTimeMillis() + "@example.com";
        String password = "password123";

        // Perform registration actions
        onView(withId(R.id.editTextEmailRegister)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.editTextConfirmPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonRegister)).perform(click());

        // Wait for Firebase operation and navigation
        // Replace with IdlingResource for robust tests
        Thread.sleep(3000); // Increased sleep time for Firebase operations

        // Assert that it navigates back to LoginActivity after successful registration
        // (as per current RegisterActivity implementation)
        onView(withId(R.id.editTextEmailLogin)).check(matches(isDisplayed()));
    }


    @Test
    public void testUserLogin_Success_andLogout() throws InterruptedException {
        // This test combines registration, login, and logout for a complete flow test
        // within a single test method to manage user creation and cleanup simply for this context.

        String email = "logintest_" + System.currentTimeMillis() + "@example.com";
        String password = "password123";

        // 1. Go to Register screen from Login screen
        onView(withId(R.id.textViewRegisterLink)).perform(click());

        // 2. Register the user
        onView(withId(R.id.editTextEmailRegister)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.editTextConfirmPasswordRegister)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonRegister)).perform(click());
        Thread.sleep(3000); // Wait for registration and navigation to LoginActivity

        // Current RegisterActivity navigates to LoginActivity on success.
        // 3. Perform Login
        onView(withId(R.id.editTextEmailLogin)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordLogin)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonLogin)).perform(click());
        Thread.sleep(3000); // Wait for login to complete and navigate to MainActivity

        // 4. Assert navigation to MainActivity (e.g., check for a view unique to MainActivity)
        onView(withId(R.id.buttonLogout)).check(matches(isDisplayed())); // Assuming buttonLogout is in MainActivity

        // 5. Clean up: Logout
        onView(withId(R.id.buttonLogout)).perform(click());
        Thread.sleep(1500); // Wait for logout and navigation back to LoginActivity

        // 6. Assert back on Login screen
        onView(withId(R.id.editTextEmailLogin)).check(matches(isDisplayed()));
    }

    // The testUserRegistration_thenLogout() as a separate test is tricky if it depends
    // on state from another test. The combined test above is more self-contained.
    // If RegisterActivity were to auto-login and go to MainActivity, that specific flow would be:
    // @Test
    // public void testUserRegistration_AutoLogin_thenLogout() throws InterruptedException {
    //     // Navigate from Login to Register screen
    //     onView(withId(R.id.textViewRegisterLink)).perform(click());
    //     String email = "autologin_test_" + System.currentTimeMillis() + "@example.com";
    //     // ... perform registration ...
    //     // onView(withId(R.id.buttonRegister)).perform(click());
    //     // Thread.sleep(3000); // Wait for auto-login to MainActivity
    //     // onView(withId(R.id.buttonLogout)).check(matches(isDisplayed())); // Now in MainActivity
    //     // onView(withId(R.id.buttonLogout)).perform(click());
    //     // Thread.sleep(1500);
    //     // onView(withId(R.id.editTextEmailLogin)).check(matches(isDisplayed())); // Back on Login screen
    // }

}
