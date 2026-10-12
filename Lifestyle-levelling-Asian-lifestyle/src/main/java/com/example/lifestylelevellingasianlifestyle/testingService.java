package com.example.lifestylelevellingasianlifestyle;

/**
 * Testing service to ensure all features are functional when launching the application
 */
public class testingService {
    private final AuthenticationService authenticationService = new AuthenticationService();

    int emailTestCases = 0;
    int signUpTestCases = 0;
    int loginTestCases = 0;
    int emailnewTestCases = 0;
    int passwordTestCases = 0;

    int LogintestsPassed = 0;
    int LogintestsFailed = 0;

    int EmailtestsPassed = 0;
    int EmailtestsFailed = 0;

    int PasswordtestsPassed = 0;
    int PasswordtestsFailed = 0;

    int SignuptestsPassed = 0;
    int SignuptestsFailed = 0;

    /**
     * Tests the signup feature
     */
    public void testSignup() {
        // removing the previous testing account if one exists
        authenticationService.removeAccount("will@testing.com", "Testing");

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

        System.out.println("\nSignup test summery: \n" + SignuptestsPassed + " passed \n" + SignuptestsFailed + " failed \n Total: " + signUpTestCases);
    }

    /**
     * Tests the login feature
     */
    public void testLogin() {

        // case 1, loging into an existing account
        // test should pass
        int userId = authenticationService.login("will@testing.com", "Testing");
        handleLoginResults(userId, 0);

        // case 2, loging into an non-existing account
        // test should fail
        int case2 = authenticationService.login("jack@testing.com", "Testing123");
        handleLoginResults(case2, -1);

        // case 3, loging into an existing account with an incorrect password
        // test should fail
        int case3 = authenticationService.login("will@testing.com", "Testing123");
        handleLoginResults(case3, -1);

        // case 4, loging in without an email
        // test should fail
        int case4 = authenticationService.login("", "Testing123");
        handleLoginResults(case4, -1);

        System.out.println("\nLogin test summery: \n" + LogintestsPassed + " passed \n" + LogintestsFailed + " failed \n Total: " + loginTestCases);

    }

    /**
     * Tests the regex and email validitiy check
     */
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

    /**
     * Tests check for new email
     */
    public void testNewValidEmail() {

        // case 1, trying a valid email
        // test should pass
        boolean case1 = isValidEmailNew("will@testing.com");
        handleEmailResults(case1, true);

        // case 2, trying an invalid email (no local part)
        // test should fail
        boolean case2 = isValidEmailNew("will.testing.com");
        handleEmailResults(case2, false);

        // case 3, trying no/empty email
        // test should fail
        boolean case3 = isValidEmailNew("");
        handleEmailResults(case3, false);

        // case 4, trying no TLD
        // test should fail
        boolean case4 = isValidEmailNew("alice@testing");
        handleEmailResults(case4, false);

        // case 5, Invalid Domain
        // test should fail
        boolean case5 = isValidEmailNew("alice@.com");
        handleEmailResults(case5, false);

        System.out.println("\nEmail test summery: \n" + EmailtestsPassed + " passed \n" + EmailtestsFailed + " failed \n Total: " + emailTestCases);
    }

    /**
     * Tests password strength check
     */
    public void testValidPasswords() {

        // case 1, trying an invalid password
        // test should fail
        boolean case1 = passwordHasGoodStrenght("Password1");
        handlePasswordResults(case1, true);

        // case 2, trying a valid password
        // test should pass
        boolean case2 = passwordHasGoodStrenght("Password1!");
        handlePasswordResults(case2, false);

        // case 3, trying no/empty password
        // test should fail
        boolean case3 = passwordHasGoodStrenght("");
        handlePasswordResults(case3, false);

        // case 4, Trying a password with a whitespace
        // test should fail
        boolean case4 = passwordHasGoodStrenght("Password 1!");
        handlePasswordResults(case4, false);

        // case 5, Trying a password that is too long > 20 characters
        // test should fail
        boolean case5 = passwordHasGoodStrenght("This!1sArea11yL0ngPa33word!!!0000");
        handlePasswordResults(case5, false);

        System.out.println("\nPassword test summery: \n" + PasswordtestsPassed + " passed \n" + PasswordtestsFailed + " failed \n Total: " + passwordTestCases);

    }

    /**
     * Email regex to identify valid email
     * @param email email given by user
     * @return true if email is a valid email
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * Email regex to identify valid email
     * @param email email given by user
     * @return true if email is a valid email
     */
    private boolean isValidEmailNew(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Checks if password strength is good
     * @param password password set by the user
     * @return true if password is strong enough
     */
    private boolean passwordHasGoodStrenght(String password) {
        // 4 possible errors
        // 1: not enough length, length should be at least 8 characters long, with a max of 20 characters
        // 2: no capital letter
        // 3: no number
        // 4: no symbol/special character
        // 5: should not have any whitespace
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&\\-+=()])(?=\\S+$).{8,20}$");
    }

    /**
     * Handles test cases from email checks
     * @param i test number
     * @param expected value
     */
    private void handleEmailResults(boolean i, boolean expected) {
        // takes in the test number, the value and the expected value
        emailTestCases++;
        System.out.println("\nCase[" + emailTestCases + "]: ");
        if (i) {
            System.out.print("Email Valid");
            //return true;
        } else {
            System.out.print("Email invalid");
            //return false;
        }
        if (i == expected) {
            System.out.print("\nTest Passed: Expected " + expected + " got " + i);
            EmailtestsPassed++;
        } else {
            System.out.print("\nTest Failed Expected " + expected + " got " + i);
            EmailtestsFailed++;
        }
    }

    /**
     * Handles test cases from login checks
     * @param i test number
     * @param expected value
     */
    private void handleLoginResults(int i, int expected) {
        // takes in the test number, the value and the expected value
        loginTestCases++;
        System.out.println("\nCase[" + emailTestCases + "]: ");
        if (i != -1) {
            System.out.println("Login successful");
        } else {
            System.out.println("Login failed");
        }
        if (i == expected) {
            System.out.print("\nTest Passed: Expected " + expected + " got " + i);
            LogintestsPassed++;
        } else {
            System.out.print("\nTest Failed Expected " + expected + " got " + i);
            LogintestsFailed++;
        }
    }

    /**
     * Handles test cases from password checks
     * @param i test number
     * @param expected value
     */
    private void handlePasswordResults(boolean i, boolean expected) {
        // takes in the test number, the value and the expected value
        passwordTestCases++;
        System.out.println("\nCase[" + emailTestCases + "]: ");
        if (i) {
            System.out.print("Password Valid");
            //return true;
        } else {
            System.out.print("Password invalid");
            //return false;
        }
        if (i == expected) {
            System.out.print("\nTest Passed: Expected " + expected + " got " + i);
            PasswordtestsPassed++;
        } else {
            System.out.print("\nTest Failed Expected " + expected + " got " + i);
            PasswordtestsFailed++;
        }
    }

    /**
     * Handles test cases from signup checks
     * @param i test number
     * @param expected value
     */
    private void handleSignupResults(boolean i, boolean expected) {
        // takes in the test number, the value and the expected value
        signUpTestCases++;
        System.out.println("\nCase[" + emailTestCases + "]: ");
        if (i) {
            System.out.print("Signup Successful");
            //return true;
        } else {
            System.out.print("Signup Failed");
            //return false;
        }
        if (i == expected) {
            System.out.print("\nTest Passed: Expected " + expected + " got " + i);
            SignuptestsPassed++;
        } else {
            System.out.print("\nTest Failed Expected " + expected + " got " + i);
            SignuptestsFailed++;
        }
    }
}