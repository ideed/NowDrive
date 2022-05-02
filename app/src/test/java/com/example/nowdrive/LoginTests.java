package com.example.nowdrive;

import static org.junit.Assert.*;

import android.util.Patterns;

import junit.framework.TestCase;

import org.junit.Test;

public class LoginTests {
    String inputEmail, inputPass;

    @Test
    public void userLogin_isCorrect() {
        inputEmail = "testEmail@gmail.com";
        inputPass = "1234A@";

        if(inputEmail.isEmpty()){
            fail();
        }

        else if(inputPass.isEmpty()){
            fail();
        }

        else if(inputPass.length()<6){
            fail();
        }

        else {
            assertTrue(true);
        }
    }

    @Test
    public void userLogin_passFail(){
        inputEmail = "testEmail@gmail.com";
        inputPass = "";

        if(inputEmail.isEmpty()){
            fail();
        }

        else if(inputPass.isEmpty()){
            assertTrue(true);
        }

        else if(inputPass.length()<6){
            assertTrue(true);
        }

        else {
            fail();
        }
    }

    @Test
    public void userLogin_emailFail(){
        inputEmail = "";
        inputPass = "1234A@";

        if(inputEmail.isEmpty()){
            assertTrue(true);
        }

        else if(inputPass.isEmpty()){
            fail();
        }

        else if(inputPass.length()<6){
            fail();
        }

        else {
            fail();
        }
    }

}