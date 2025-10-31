package com.app.superapp.Processes;

import com.app.superapp.HelloApplication;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.time.Instant;

public class Controller {
    @FXML
    private Label labelStartTime, labelThreads, labelWorkTime, labelSelProc;
    @FXML
    private ListView<ProcStruct> listView;
    ProcStruct selectedProc;
    final File SYSTEM = new File(URLDecoder.decode(HelloApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath(), StandardCharsets.UTF_8)).toPath().getParent().toFile();
    String workTime = "";
    String[] Info = new String[3];
    String[] someInfo = new String[] {"123 proc1", "4312 proc2", "5323 proc3"};
    String split = SharedMemory.getSplit();
    String pidSplit = SharedMemory.getPidSplit();
    StringBuilder logger = new StringBuilder();
    @FXML
    void initialize() {
        Thread mainThread = Thread.currentThread();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Runnable updater = new Runnable() {
                    @Override
                    public void run() {
                        update();
                    }
                };

                while (!mainThread.isInterrupted()) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    Platform.runLater(updater);
                }
            }

        });
        thread.start();
    }
    void update() {
        String received = SharedMemory.receive();
        String[] pidsAndData = received.split(pidSplit);
        String[] pids = pidsAndData[0].split(split);
        String[] data = pidsAndData[1].split(split);

        labelStartTime.setText(data[1]); //Время старта Супер апп (час: мин: сек)
        labelThreads.setText(data[0]); //Количество потоков Супер апп

        logger.append(LocalTime.now()).append(" - Время старта Супер апп (час: мин: сек) -- ").append(labelStartTime.getText());
        logger.append(LocalTime.now()).append(" - Количество потоков Супер апп -- ").append(labelThreads.getText());

        listView.getItems().clear();
        for (String pid : pids) {
            listView.getItems().add(new ProcStruct(Long.parseLong(pid)));
        }
        if (!Objects.isNull(selectedProc)) {
            labelSelProc.setText(selectedProc.getName());
            Optional<ProcessHandle> processHandle = ProcessHandle.of(selectedProc.getPid());
            if (processHandle.isPresent()) {
                ProcessHandle.Info processInfo = processHandle.get().info();

                Duration duration = Duration.ofMillis(Instant.now().toEpochMilli() - processInfo.startInstant().get().toEpochMilli());

                labelWorkTime.setText(formatDuration(duration)); //Время работы выбранного процесса
                logger.append(LocalTime.now()).append(" - Время работы выбранного процесса ").append(selectedProc.getName()).append(" -- ").append(labelWorkTime.getText());
            }
        }
    }
    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
    @FXML
    void OnClick(MouseEvent event) {
        selectedProc = listView.getSelectionModel().getSelectedItem();
    }
    @FXML
    void OnSaveLogs(ActionEvent event) throws IOException {
        File log = new File(SYSTEM+"/proccesses-log-"+LocalTime.now()+".txt");
        log.createNewFile();
        FileWriter fileWriter = new FileWriter(log);
        fileWriter.write(logger.toString());
        fileWriter.close();
    }
}
class ProcStruct{
    long pid;
    String name;
    public ProcStruct(long pid){
        this.pid = pid;
        try {
            Process process = Runtime.getRuntime().exec("jps -l");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(Long.toString(pid))) {
                    String[] parts = line.split(" ");
                    name = new File(parts[1]).getName();
                }
            }

            reader.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public String getName() {
        return name;
    }

    public long getPid() {
        return pid;
    }

    @Override
    public String toString(){
        return name;
    }
}
