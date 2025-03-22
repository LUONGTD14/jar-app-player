package org.example;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class AppMain {
    private JPanel mainPanel;
    private JButton buttonSelect;
    private JButton buttonStart;
    private JButton buttonPause;
    private JButton buttonStop;
    private JButton buttonClear;
    private JProgressBar progressBar;
    private JEditorPane editorPane;
    private JComboBox<String> comboBoxTime;
    private JComboBox<String> comboBoxRepeat;
    private JLabel jLabelPath;

    private MediaPlayer mediaPlayer;
    private boolean isPaused = false;
    private Timer progressTimer;
    private int repeatCount;
    private int currentRepeat;
    private int delayTime;

    public AppMain() {
        new JFXPanel();

        comboBoxTime.addItem("0 second");
        comboBoxTime.addItem("3 second");
        comboBoxTime.addItem("5 second");
        comboBoxTime.addItem("10 second");
        comboBoxTime.addItem("30 second");
        comboBoxTime.addItem("1 minute");
        comboBoxTime.addItem("5 minute");

        comboBoxRepeat.addItem("1");
        comboBoxRepeat.addItem("3");
        comboBoxRepeat.addItem("5");
        comboBoxRepeat.addItem("10");
        comboBoxRepeat.addItem("30");

        buttonSelect.addActionListener(e -> chooseFile());

        buttonStart.addActionListener(e -> startPlayback());

        buttonPause.addActionListener(e -> togglePause());

        buttonStop.addActionListener(e -> stopPlayback());

        buttonClear.addActionListener(e -> editorPane.setText(""));

        comboBoxRepeat.addActionListener(e -> repeatCount = Integer.parseInt(comboBoxRepeat.getSelectedItem().toString()));
        comboBoxTime.addActionListener(e -> delayTime = getDelayTime(comboBoxTime.getSelectedItem().toString()));
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Media Files", "mp3", "wav", "mp4", "m4a"));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            jLabelPath.setText(selectedFile.getAbsolutePath());
        }
    }

    private void startPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        File file = new File(jLabelPath.getText());
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "File not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentRepeat = 0;

        playMedia(file);
    }

    private void playMedia(File file) {
        Platform.runLater(() -> {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                progressBar.setMaximum((int) mediaPlayer.getTotalDuration().toMillis());
                startProgressTimer();
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                stopProgressTimer();
                if (currentRepeat < repeatCount - 1) {
                    currentRepeat++;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            playMedia(file);
                        }
                    }, delayTime);
                }
            });

            mediaPlayer.play();
        });
    }

    private void togglePause() {
        if (mediaPlayer != null) {
            if (isPaused) {
                mediaPlayer.play();
                buttonPause.setText("Pause");
            } else {
                mediaPlayer.pause();
                buttonPause.setText("Resume");
            }
            isPaused = !isPaused;
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            stopProgressTimer();
            progressBar.setValue(0);
        }
    }

    private void startProgressTimer() {
        progressTimer = new Timer();
        progressTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    Platform.runLater(() -> progressBar.setValue((int) mediaPlayer.getCurrentTime().toMillis()));
                }
            }
        }, 0, 1000);
    }

    private void stopProgressTimer() {
        if (progressTimer != null) {
            progressTimer.cancel();
        }
    }

    private int getDelayTime(String delay) {
        switch (delay) {
            case "3 second":
                return 3000;
            case "5 second":
                return 5000;
            case "10 second":
                return 10000;
            case "30 second":
                return 30000;
            case "1 minute":
                return 60000;
            case "5 minute":
                return 300000;
            default:
                return 0;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Music Player");
        frame.setContentPane(new AppMain().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setVisible(true);
    }
}
