package com.example.publicsafetycomission.Helpers;

public interface ApiCallback {
    void responseCallback(boolean success, String message, String s);
    void responseCallback(int success, String message, String s);
}
