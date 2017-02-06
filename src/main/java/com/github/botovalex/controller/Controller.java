package com.github.botovalex.controller;

import com.github.botovalex.model.Model;
import com.github.botovalex.view.View;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.util.List;

public class Controller {
    private View view;
    private Model model;

    public Controller() {
        view = new View(this);
        model = new Model();
    }

    public void init() {
        view.init(File.listRoots());
    }

    public TableModel getTableModel(File... files) {
        return model.getTableModel(files);
    }

    public void enterToTheFolder(JTable table) {
        model.enterToTheFolder(table);
    }

    public void upOneLevel(JTable table) {
        model.upOneLevel(table);
    }

    public String getStringCurrentDirectory(JTable table) {
        return model.getStringCurrentDirectory(table);
    }

    public String createFolder(JTable table, String folderName) {
        try {
            return model.createFolder(table, folderName);
        } catch (IllegalArgumentException e ) {
            throw new IllegalArgumentException(e);
        }
    }

    public String copyOrMoveFiles(JTable sourceTable, JTable destTable, boolean isMove, boolean isReplaceExisting) {
        try {
            return model.copyOrMoveFiles(sourceTable, destTable, isMove, isReplaceExisting);
        } catch (IllegalArgumentException e ) {
            throw new IllegalArgumentException(e);
        }
    }

    public String deleteFiles(JTable table) {
        try {
            return model.deleteFiles(table);
        } catch (IllegalArgumentException e ) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<String> findFiles(JTable table, String namePart, boolean isSearchInSubfolders) {
        try {
            return model.findFiles(table, namePart, isSearchInSubfolders);
        } catch (IllegalArgumentException e ) {
            throw new IllegalArgumentException(e);
        }
    }
}
