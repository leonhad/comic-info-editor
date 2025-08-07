package com.github.leonhad.components;

import lombok.experimental.UtilityClass;

import javax.swing.*;

@UtilityClass
public final class StatusBar {

    private static JLabel status;
    private static JLabel imageStatus;
    private static JLabel fileStatus;

    public static void setStatusBar(JLabel status, JLabel fileStatus, JLabel imageStatus) {
        StatusBar.status = status;
        StatusBar.fileStatus = fileStatus;
        StatusBar.imageStatus = imageStatus;
    }

    public static void setStatus(String status) {
        StatusBar.status.setText(status);
    }

    public static void setImageStatus(String imageStatus) {
        StatusBar.imageStatus.setText(imageStatus);
    }

    public static void setFileStatus(String fileStatus) {
        StatusBar.fileStatus.setText(fileStatus);
    }
}
