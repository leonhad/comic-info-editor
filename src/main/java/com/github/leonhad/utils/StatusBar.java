package com.github.leonhad.utils;

import javax.swing.*;

public final class StatusBar {

    private JLabel status;
    private JLabel imageStatus;
    private JLabel fileStatus;

    private static final StatusBar INSTANCE = new StatusBar();

    public static StatusBar getInstance() {
        return INSTANCE;
    }

    public void setStatusBar(JLabel status, JLabel fileStatus, JLabel imageStatus) {
        this.status = status;
        this.fileStatus = fileStatus;
        this.imageStatus = imageStatus;
    }

    public static void setStatus(String status) {
        INSTANCE.status.setText(status);
    }

    public static void setImageStatus(String imageStatus) {
        INSTANCE.imageStatus.setText(imageStatus);
    }

    public static void setFileStatus(String fileStatus) {
        INSTANCE.fileStatus.setText(fileStatus);
    }
}
