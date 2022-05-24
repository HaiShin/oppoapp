package com.example.oppoapp;

import android.app.Application;

public class GlobalApp extends Application {

    private TransferLearningModelWrapper tlModel;
    private String device_number;

    public TransferLearningModelWrapper getTlModel() {
        return tlModel;
    }

    public void setTlModel(TransferLearningModelWrapper tlModel) {
        this.tlModel = tlModel;
    }

    public void setDeviceNumber(String device_number){
        this.device_number = device_number;
    }
    public String getDeviceNumber(){
        return device_number;
    }

    public boolean isNull() {
        if (tlModel == null) {
            return true;
        } else {
            return false;
        }
    }
}
