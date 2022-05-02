package com.example.nowdrive;

import static org.junit.Assert.*;

import junit.framework.TestCase;

import org.junit.Test;

public class RegistrationTests {
    String inputEmail, inputPass, inputPassTwo;

    @Test
    public void userReg_isCorrect() {
        inputPass="1234A@";
        inputPassTwo="1234A@";
        inputEmail="testEmail@gmail.com";

        if(inputEmail.isEmpty()){
            fail();
        }

        else if(inputPass.isEmpty()){
            fail();
        }

        else if(inputPass.length()<6){
            fail();
        }

        else if(!inputPass.equals(inputPassTwo)){
            fail();
        }

        else {
            assertTrue(true);
        }
    }

    @Test
    public void userReg_passNoMatch() {
        inputPass="1234A@";
        inputPassTwo="1234";
        inputEmail="testEmail@email.com";

        if(inputEmail.isEmpty()){
            fail();
        }

        else if(inputPass.isEmpty()){
            fail();
        }

        else if(inputPass.length()<6){
            fail();
        }

        else if(!inputPass.equals(inputPassTwo)){
            assertTrue(true);
        }

        else {
            fail();
        }
    }

    @Test
    public void userReg_passFail() {
        inputPass="123@";
        inputPassTwo="123@";
        inputEmail="testEmail@gmail.com";

        if(inputEmail.isEmpty()){
            fail();
        }

        else if(inputPass.isEmpty()){
            fail();
        }

        else if(inputPass.length()<6){
            assertTrue(true);
        }

        else if(!inputPass.equals(inputPassTwo)){
            fail();
        }

        else {
            fail();
        }
    }

    @Test
    public void userReg_emailFail() {
        inputPass="1234A@";
        inputPassTwo="1234A@";
        inputEmail="";

        if(inputEmail.isEmpty()){
            assertTrue(true);
        }

        else if(inputPass.isEmpty()){
            fail();
        }

        else if(inputPass.length()<6){
            fail();
        }

        else if(!inputPass.equals(inputPassTwo)){
            fail();
        }

        else {
            fail();
        }
    }
}