package com.precious.apps.cameraapplication;

import java.util.UUID;

public class Utils {

    /**
     * Generates a unique random id, this should be unique.
     * @return
     */
    public static String generateUniqueUserID(){
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }
}
