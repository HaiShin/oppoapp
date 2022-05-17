package com.example.oppoapp;

import android.app.Application;

public class GlobalApp extends Application {

    private TransferLearningModelWrapper tlModel;

    public TransferLearningModelWrapper getTlModel() {
        return tlModel;
    }

    public void setTlModel(TransferLearningModelWrapper tlModel) {
        this.tlModel = tlModel;
    }

    public boolean isNull() {
        if (tlModel == null) {
            return true;
        } else {
            return false;
        }
    }
}
