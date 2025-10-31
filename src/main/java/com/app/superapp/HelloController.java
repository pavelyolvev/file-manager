package com.app.superapp;

import com.app.superapp.Processes.SharedMemory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.*;

public class HelloController {
    @FXML
    private FlowPane flowPane, flowPaneDrives;
    @FXML
    private AnchorPane anchorPaneRoot;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button buttonRefresh;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label labelPath, labelSel, labelCopy, selectedPathLabel;
    @FXML
    private TextField textField, searchTextField;
    @FXML
    private ChoiceBox<Utils> choiceBox;
    @FXML
    private MenuItem itemCopy, itemCopyPath, itemCrFile, itemCrFolder, itemDel, itemPaste, itemPermDel, itemRename;
    final File ROOT = new File(URLDecoder.decode(HelloApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath(), StandardCharsets.UTF_8)).toPath().getParent().getParent().toFile();
    final File ROOTDRIVES = new File("/media/" + System.getProperty("user.name")+"/");
    File current = ROOT;
    File copyBuffer;
    File selected = ROOT;
    File renaming;
    Label selVboxLabel;
    ContextMenu contextMenu = menuFabric();
    Long startTime;
    StringBuilder logger = new StringBuilder();

    @FXML
    void initialize() throws IOException {
        flowPaneAutoSize();
        addHotKeys();
        loadFolder();
        fillChoiceBox();
        startTime = LocalTime.now().toNanoOfDay();
        logger.append(LocalTime.now()).append(" - запущено приложение Супер апп с PID - ").append(ProcessHandle.current().pid()).append("\n");
    }


    @FXML
    void OnContextMReqflowPane(ContextMenuEvent event) {
        contextMenu.show(flowPane, event.getScreenX(), event.getScreenY());
    }

    @FXML
    void OnMouseClickedflowPane(MouseEvent event) {
        contextMenu.hide();
        selected = current;
        selectionReset();
        updateLabels();
    }
    @FXML
    void AddFile(ActionEvent event) throws IOException {
        loadFolder();
    }

    @FXML
    void Copy(ActionEvent event) {
        if(!protection(selected))
        {
            alertCreate("Нельзя скопировать файл\n" + selected);
            return;
        }
        copyBuffer = selected;
        updateLabels();
    }

    @FXML
    void Paste(ActionEvent event) {
        if(!protection(selected))
        {
            alertCreate("Нельзя вставить файл в этот каталог\n" + selected);
            return;
        }
        try {
            FileManagment.copy(copyBuffer.toPath(), selected.toPath());
            loadFolder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @FXML
    void Rename(ActionEvent event){
        if(!protection(selected))
        {
            alertCreate("Нельзя переименовать\n" + selected);
            return;
        }
        renaming = selected;
        renameLabel(selVboxLabel);
        updateLabels();
    }
    void renameLabel(Label label){
        textField.setText(label.getText());
        textField.setVisible(true);
        textField.requestFocus();
    }
    @FXML
    void RenameApply(KeyEvent event) throws IOException {
        if(event.getCode().equals(KeyCode.ENTER)) {
            FileManagment.rename(renaming, textField.getText());
            textField.setVisible(false);
            loadFolder();
        } else if(event.getCode().equals(KeyCode.ESCAPE)) {
            textField.setVisible(false);
            loadFolder();
        }

    }
    @FXML
    void CopyPath(ActionEvent event) {
        String text = selected.toString();

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(text);
        clipboard.setContent(clipboardContent);
    }

    @FXML
    void CreateFile(ActionEvent event) throws IOException {
        if(!protection(selected))
        {
            alertCreate("Нельзя создать файл в этой директории\n" + selected);
            return;
        }
        File file = new File(selected + "/новый файл");
        file.createNewFile();
        Label label = labelFabric(file.getName());
        flowPaneItemFabric(file.toPath(), label, new ImageView(HelloController.class.getResource("File.png").toString()));
        selected = file;
        loadFolder();
        renameLabel(label);
    }

    @FXML
    void CreateFolder(ActionEvent event) throws IOException {
        if(!protection(selected))
        {
            alertCreate("Нельзя создать каталог в этой директории\n" + selected);
            return;
        }
        File file = new File(selected + "/новая папка");
        file.mkdir();
        Label label = labelFabric(file.getName());
        flowPaneItemFabric(file.toPath(), label, new ImageView(HelloController.class.getResource("Folder.png").toString()));
        selected = file;
        loadFolder();
        renameLabel(label);
    }

    @FXML
    void Delete(ActionEvent event){
        if(!protection(selected))
        {
            alertCreate("Нельзя удалить\n" + selected);
            return;
        }
        FileManagment.move(selected.toPath(), Path.of(ROOT + "/Trash/"));
        try {
            loadFolder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void OnRoot(ActionEvent event) throws IOException {
        current = ROOT;
        loadFolder();
    }


    @FXML
    void PermDelete(ActionEvent event){
        if(!protection(selected))
        {
            alertCreate("Нельзя удалить\n" + selected);
            return;
        }
        FileManagment.delete(selected);
        try {
            loadFolder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void OnStartUtil(ActionEvent event) throws IOException {
        String util = choiceBox.getValue().getExecName();
        ProcessBuilder pb = new ProcessBuilder(util);
        Process process = pb.start();
        logger.append(LocalTime.now()).append(" - запущено приложение ").append(util).append("с PID ").append(process.pid()).append("\n");
    }
    @FXML
    void OnTerminal(ActionEvent event) throws IOException {
        new Terminal().start();
    }
    @FXML
    void OnSaveLogs(ActionEvent event) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Введите имя лог-файла");

        inputDialog.showAndWait();
        if(Objects.isNull(inputDialog.getResult()))
            return;
        String enteredText = inputDialog.getResult();
        File log = new File(ROOT+"/System/" + enteredText + ".txt");
        try {
            FileWriter fileWriter = new FileWriter(log);
            fileWriter.write(logger.toString());
            fileWriter.close();
        } catch (IOException e) {
                    throw new RuntimeException(e);
        }
    }


    @FXML
    void OnProcesses(ActionEvent event) throws IOException {
        //new ProcessViewer().start(new Stage());
        ProcessBuilder functional = new ProcessBuilder("java",
                "--module-path", ROOT.toString()+"/System/lib/",
                "--add-modules", "javafx.controls,javafx.fxml",
                "-cp", ROOT.toString()+"/System/superapp.jar",
                "com.app.superapp.ProcessViewer");
        Process process = functional.start();
        logger.append(LocalTime.now()).append(" - запущено приложение Процессы").append("с PID ").append(process.pid()).append("\n");


        //Время старта

        LocalTime localTime = LocalTime.ofNanoOfDay(startTime);

        // Форматируем время в формат "час:минута:секунда"
        String formattedTime = localTime.toString();

        // Если время имеет формат "час:минута:секунда.миллисекунды", уберем миллисекунды
        if (formattedTime.contains(".")) {
            formattedTime = formattedTime.substring(0, formattedTime.indexOf('.'));
        }
        String finalFormattedTime = formattedTime;

        //System.out.println(finalFormattedTime);
        //всего потоков:
        Thread thread = new Thread(() -> {

            while (!Thread.interrupted()) {
                int count = 0;
                for (Thread x : Thread.getAllStackTraces().keySet()) {
                    count++;
                }
                //Процессы
                ArrayList<Long> pids = new ArrayList<>();
                pids.add(ProcessHandle.current().pid());
                ProcessHandle.current().children().forEach(processHandle -> pids.add(processHandle.pid()));
                SharedMemory.send(pids, count, finalFormattedTime);
                //System.out.println(SharedMemory.receive());

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        thread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(thread::interrupt));
    }
    @FXML
    void OnOpenAbout(ActionEvent event) {
        new AboutWindow().create("О программе");
    }
    @FXML
    void OnOpenHotKeys(ActionEvent event) {
        new AboutWindow().create("Горячие клавиши");
    }
    private void fillChoiceBox() {
        choiceBox.getItems().addAll(new Utils("Терминал", "x-terminal-emulator"),
                new Utils("Минер", "gnome-mines"),
                new Utils("Калькулятор", "gnome-calculator"),
                new Utils("Текстовый редактор", "gnome-text-editor"),
                new Utils("Центр управления", "gnome-control-center"));
    }
    void updateLabels(){
        if(!Objects.isNull(selected)){
            labelSel.setText("Выбран: " + selected.getName());
            selectedPathLabel.setText(selected.toString());
        }

        if(!Objects.isNull(copyBuffer))
            labelCopy.setText("Копируется: " + copyBuffer.getName());
    }
    void alertCreate(String txt){
        Alert alert = new Alert(Alert.AlertType.NONE, txt, ButtonType.OK);
        alert.setTitle("Ошибка");
        alert.showAndWait();
    }
    boolean protection(File file){
        return !file.toPath().startsWith(ROOT + "/System") && !file.toPath().equals(Path.of(ROOT + "/Trash")) && !file.toPath().equals(Path.of(ROOT + "/Work")) && !file.toPath().equals(ROOTDRIVES.toPath());
    }
    void flowPaneAutoSize(){

        ChangeListener<Number> sizeListener = new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                flowPane.setMinHeight(0);
                double sumWidth = 0;
                double maxHeigth = 0;
                int rows = 1;
                for (Node node : flowPane.getChildren()) {
                    VBox vBox = (VBox) node;
                    if((sumWidth += vBox.getWidth()) >= flowPane.getWidth())
                    {
                        rows++;
                        sumWidth = 0;
                    }
                    if(maxHeigth<vBox.getHeight())
                        maxHeigth = vBox.getHeight();
                }
                if(sumWidth != 0) rows++; //дополнительная строка в том случае, если последняя строка заполнена не полностью
                flowPane.setMinHeight(maxHeigth * rows);
            }
        };
        flowPane.heightProperty().addListener(sizeListener);
        flowPane.widthProperty().addListener(sizeListener);
    }
    void loadFolder() throws IOException {

        labelPath.setText("Путь: /" + ROOT.toPath().relativize(current.toPath()));
        flowPane.getChildren().clear();


        if(searchTextField.getText().isEmpty()){
            if(!current.equals(ROOT)){
                Label backlabel = labelFabric("Назад");
                ImageView backImg = new ImageView(HelloController.class.getResource("Back.png").toString());
                flowPane.getChildren().add(flowPaneItemFabric(current.toPath().getParent(), backlabel, backImg));
            }
            if(current.equals(ROOT)){
                Label label = labelFabric("Съемные носители");
                ImageView Img = new ImageView(HelloController.class.getResource("Folder.png").toString());
                flowPane.getChildren().add(flowPaneItemFabric(ROOTDRIVES.toPath(), label, Img));
            }
            if (current.equals(ROOTDRIVES)){
                labelPath.setText("Путь: Съемные носители/" + ROOTDRIVES.toPath().relativize(current.toPath()));
                flowPane.getChildren().clear();
                Label backlabel = labelFabric("Назад");
                ImageView backImg = new ImageView(HelloController.class.getResource("Back.png").toString());
                flowPane.getChildren().add(flowPaneItemFabric(ROOT.toPath(), backlabel, backImg));
            }
            Files.walk(current.toPath(), 1).filter(path -> !path.equals(current.toPath())).forEach(file ->{
                ImageView imgView;
                if(file.toFile().isFile())
                    imgView = new ImageView(HelloController.class.getResource("File.png").toString());
                else imgView = new ImageView(HelloController.class.getResource("Folder.png").toString());
                Label label = labelFabric(file.toFile().getName());

                flowPane.getChildren().add(flowPaneItemFabric(file, label, imgView));
            });
        }

        else Files.walk(current.toPath()).filter(path -> !path.equals(current.toPath()) && path.toFile().getName().toLowerCase().startsWith(searchTextField.getText().toLowerCase())).forEach(file ->{
            ImageView imgView;
            if(file.toFile().isFile())
                imgView = new ImageView(HelloController.class.getResource("File.png").toString());
            else imgView = new ImageView(HelloController.class.getResource("Folder.png").toString());
            Label label = labelFabric(file.toFile().getName());
            Tooltip tooltip = new Tooltip(file.toString());
            label.setTooltip(tooltip);



            flowPane.getChildren().add(flowPaneItemFabric(file, label, imgView));
            });
        updateLabels();
    }
    void selectionReset(){
        flowPane.getChildren().forEach(node -> {
            if (node instanceof VBox vbox) {
                vbox.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                vbox.getChildren().filtered(Vboxsnode -> Vboxsnode instanceof Label).forEach(VBoxsnode -> {
                    Label vBoxsnode = (Label) VBoxsnode;
                    vBoxsnode.setMaxHeight(60);
                });
            }
        });
    }
    VBox flowPaneItemFabric(Path file, Label label, ImageView imgView){
        VBox vBox = new VBox(imgView, label);

        vBox.setOnMouseClicked(mouseEvent -> {

            selectionReset();
            vBox.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
            label.setMaxHeight(200);
            selected = file.toFile();
            selVboxLabel = label;

            if(mouseEvent.getClickCount() == 2)
            {
                if (selected.isFile())
                {
                    ProcessBuilder processBuilder = new ProcessBuilder("xdg-open", selected.toString());
                    try {
                        Process process = processBuilder.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    current = selected;
                    searchTextField.setText("");

                    try {
                        loadFolder();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

            updateLabels();
            mouseEvent.consume();
        });
        vBox.setOnDragDetected(event -> {
                Dragboard db = vBox.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                List<File> files = new ArrayList<>();
            try {
                files.add(new File(new String(file.toAbsolutePath().normalize().toString().getBytes(), System.getProperty("file.encoding"))));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            content.putFiles(files);
                db.setContent(content);
                event.consume();

        });

        vBox.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();

            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                event.consume();
            }
        });

        vBox.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                File source = db.getFiles().get(db.getFiles().size()-1);

                File dest;
                dest = file.toFile();
                FileManagment.move(source.toPath(), dest.toPath());
                event.setDropCompleted(true);
                event.consume();
                try {
                    loadFolder();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        vBox.setAlignment(Pos.TOP_CENTER);
        return vBox;
    }
    Label labelFabric(String text){
        Label label = new Label(text);
        Tooltip tooltip = new Tooltip();

        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setMaxWidth(50);
        label.setMaxHeight(60);
        tooltip.textProperty().bind(label.textProperty()); // Привязываем текст Label к тексту подсказки
        label.setTooltip(tooltip);
        return label;
    }
    ContextMenu menuFabric(){
        ContextMenu menu = new ContextMenu();
        MenuItem copy = new MenuItem("Копировать");
        MenuItem paste = new MenuItem("Вставить");
        MenuItem rename = new MenuItem("Переименовать");
        MenuItem delete = new MenuItem("Удалить");
        MenuItem fullDelete = new MenuItem("Перманентно удалить");
        MenuItem copyPath = new MenuItem("Копировать путь");
        menu.getItems().addAll(copy, paste, rename, delete, fullDelete, copyPath);

        copy.setOnAction(this::Copy);
        paste.setOnAction(this::Paste);
        rename.setOnAction(this::Rename);
        delete.setOnAction(this::Delete);
        fullDelete.setOnAction(this::PermDelete);
        copyPath.setOnAction(this::CopyPath);

        return menu;
    }
    void addHotKeys(){
        itemCopy.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        itemPaste.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        itemRename.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN));
        itemDel.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        itemPermDel.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN));
    }
}