package com.app.superapp;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class AboutWindow {
    Stage stage = new Stage();
    TextArea areaAbout = new TextArea("""
            Файловый менеджер.
            Программа написана на java.
            """);
    TextArea areaHotkeys = new TextArea("""
            CTRL+C - Копировать
            CTRL+V - Вставить
            ALT+R - Переименовать
            DEL - Удалить в корзину
            SHIFT+DEL - Перманентно удалить
            """);
    public void create(String title){
        areaAbout.setEditable(false);
        areaHotkeys.setEditable(false);
        areaAbout.setWrapText(true);
        areaHotkeys.setWrapText(true);
        areaAbout.setFont(new Font(16));
        areaHotkeys.setFont(new Font(16));
        HBox hBox = new HBox();
        if(title.equals("О программе"))
            hBox.getChildren().add(areaAbout);
        else hBox.getChildren().add(areaHotkeys);
        Scene scene = new Scene(hBox, 300, 200);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }
}
