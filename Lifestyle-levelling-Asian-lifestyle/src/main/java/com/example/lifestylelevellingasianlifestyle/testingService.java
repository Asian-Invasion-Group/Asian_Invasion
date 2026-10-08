package com.example.lifestylelevellingasianlifestyle;

public class testingService {

    // testing service callable by involking
    // testingService test = new testingService();
    // test.testSignup();
    // test.testLogin();
    // test.testValidEmail();

    // signup should always be involked first, as it creates the accounts!!!
    // currently has 11 tests related to signup / login functions

    private final AuthenticationService authenticationService = new AuthenticationService();

    int emailTestCases = 0;
    int signUpTestCases = 0;
    int loginTestCases = 0;
    public void testSignup() {

        // case 1, signing up with a non-existant email
        // test should pass
        boolean signUp = authenticationService.signUp("will@testing.com", "Testing");
        handleSignupResults(signUp, true);

        // case 2, signing up with an existing email
        // test should fail
        boolean case2 = authenticationService.signUp("will@testing.com", "Testing");
        handleSignupResults(case2, false);

        // case 3, signing up with an empty email field
        // test should fail
        boolean case3 = authenticationService.signUp("", "Testing");
        handleSignupResults(case3, false);

        // case 4, signing up with an empty email field
        // test should fail
        boolean case4 = authenticationService.signUp("john@testing.com", "");
        handleSignupResults(case4, false);
    }

    public void testLogin() {

        // case 1, loging into an existing account
        // test should pass
        int userId = authenticationService.login("will@testing.com", "Testing");

        if (userId != -1) {
            System.out.println("Login successful");
        } else {
            System.out.println("Login failed");
        }

        // case 2, loging into an non-existing account
        // test should fail
        int case2 = authenticationService.login("jack@testing.com", "Testing123");

        if (case2 != -1) {
            System.out.println("Login successful");
        } else {
            System.out.println("Login failed");
        }

        // case 3, loging into an existing account with an incorrect password
        // test should fail
        int case3 = authenticationService.login("will@testing.com", "Testing123");

        if (case3 != -1) {
            System.out.println("Login successful");
        } else {
            System.out.println("Login failed");
        }

        // case 4, loging in without an email
        // test should fail
        int case4 = authenticationService.login("", "Testing123");

        if (case4 != -1) {
            System.out.println("Login successful");
        } else {
            System.out.println("Login failed");
        }

    }

    public void testValidEmail() {

        // case 1, trying a valid email
        // test should pass
        boolean case1 = isValidEmail("will@testing.com");
        handleEmailResults(case1, true);

        // case 2, trying an invalid email (no local part)
        // test should fail
        boolean case2 = isValidEmail("will.testing.com");
        handleEmailResults(case2, false);

        // case 3, trying no/empty email
        // test should fail
        boolean case3 = isValidEmail("");
        handleEmailResults(case3, false);

        // case 4, trying no TLD
        // test should fail
        boolean case4 = isValidEmail("alice@testing");
        handleEmailResults(case4, false);

        // case 5, Invalid Domain
        // test should fail
        boolean case5 = isValidEmail("alice@.com");
        handleEmailResults(case5, false);

    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
    private void handleEmailResults(boolean i, boolean expected) {
        // takes in the test number, the value and the expected value
        emailTestCases++;
        System.out.println("Case[" + emailTestCases + "]: ");
        if (i) {
            System.out.print("Email Valid");
            //return true;
        } else {
            System.out.print("Email invalid");
            //return false;
        }
        if (i == expected) {
            System.out.print("\nTest Passed");
        } else {
            System.out.print("\nTest Failed");
        }
    }

    private void handleSignupResults(boolean i, boolean expected) {
        // takes in the test number, the value and the expected value
        signUpTestCases++;
        System.out.println("Case[" + emailTestCases + "]: ");
        if (i) {
            System.out.print("Signup Successful");
            //return true;
        } else {
            System.out.print("Signup Failed");
            //return false;
        }
        if (i == expected) {
            System.out.print("\nTest Passed");
        } else {
            System.out.print("\nTest Failed");
        }
    }
}

