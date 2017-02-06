package com.github.botovalex.model;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Model {

    public TableModel getTableModel(File... files) {
        return new FileTableModel(files);
    }

    public void enterToTheFolder(JTable table) {
        if (table.getModel() instanceof FileTableModel) {
            FileTableModel tableModel = (FileTableModel) table.getModel();
            tableModel.setFileList(table.convertRowIndexToModel(table.getSelectedRow()));
        } else {
            System.err.println("enterToTheFolder: tableModel not instanceof FileTableModel");
        }
    }

    public void upOneLevel(JTable table) {
        if (table.getModel() instanceof FileTableModel) {
            FileTableModel tableModel = (FileTableModel) table.getModel();
            tableModel.upOneLevel();
        } else {
            System.err.println("upOneLevel: tableModel not instanceof FileTableModel");
        }
    }

    public String getStringCurrentDirectory(JTable table) {
        if (table.getModel() instanceof FileTableModel) {
            return ((FileTableModel) table.getModel()).getStringCurrentDirectory();
        } else {
            System.err.println("getStringCurrentDirectory: tableModel not instanceof FileTableModel");
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    public String createFolder(JTable table, String folderName) {
        if (table.getModel() instanceof FileTableModel) {
            FileTableModel tableModel = (FileTableModel) table.getModel();
            String currentDirectory = tableModel.getStringCurrentDirectory();
            if (currentDirectory.equals("root")) return "error_Директория не может быть создана в корне файловой системы";
            File folder = new File(currentDirectory, folderName);
            boolean result = folder.mkdir();
            if (result) tableModel.setFileList(new File(currentDirectory).listFiles());
            return result ? "Директория успешно создана" : "error_Директория не может быть создана";
        } else {
            System.err.println("createFolder: tableModel not instanceof FileTableModel");
        }

        throw new IllegalArgumentException();
    }

    @SuppressWarnings("unchecked")
    public List<String> findFiles(JTable table, String namePart, boolean isSearchInSubfolders) {
        if (table.getModel() instanceof FileTableModel) {
            FileTableModel tableModel = (FileTableModel) table.getModel();
            String currentDirectory = tableModel.getStringCurrentDirectory();

            ArrayList<String> result = new ArrayList<>();
            if (currentDirectory.equals("root")) result.addAll(findFiles(File.listRoots(), namePart, isSearchInSubfolders));
            else result.addAll(findFiles(new File(currentDirectory).listFiles(), namePart, isSearchInSubfolders));

            return result;
        } else {
            System.err.println("createFolder: tableModel not instanceof FileTableModel");
        }

        throw new IllegalArgumentException();
    }

    private List<String> findFiles(File[] files, String namePart, boolean isSearchInSubfolders) {
        ArrayList<String> result = new ArrayList<>();
        if (files == null || files.length == 0) return result;

        for (File file : files) {
            if (file.exists()) {
                if (file.getName().matches("^.*" + namePart + ".*$")) result.add(file.getAbsolutePath());
                if (isSearchInSubfolders && file.isDirectory()) result.addAll(findFiles(file.listFiles(), namePart, isSearchInSubfolders));
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public String deleteFiles(JTable table) {
        if (table.getModel() instanceof FileTableModel) {
            FileTableModel tableModel = (FileTableModel) table.getModel();
            String currentDirectory = tableModel.getStringCurrentDirectory();
            if (currentDirectory.equals("root")) return "Корневой диск не может быть удалён";

            int[] selectedRows = table.getSelectedRows();
            for (int i = 0; i < selectedRows.length; i++) {
                selectedRows[i] = table.convertRowIndexToModel(selectedRows[i]);
            }

            ArrayList<File> sourceFiles = new ArrayList<>();
            for (int row : selectedRows) {
                if (!tableModel.getValueAt(row, 0).equals(".."))
                    sourceFiles.add((File) tableModel.getValueAt(row, 3));
            }

            int countOfNotDeletedFiles = deleteFiles(sourceFiles.toArray(new File[sourceFiles.size()]));
            tableModel.setFileList(new File(currentDirectory).listFiles());

            if (countOfNotDeletedFiles == 0) return "Все файлы или директории успешно удалены";
            else return String.format("Не удалось удалить %d файлов или директорий", countOfNotDeletedFiles);

        } else {
            System.err.println("deleteFiles: tableModel not instanceof FileTableModel");
        }

        throw new IllegalArgumentException();
    }

    @SuppressWarnings("unchecked")
    public String copyOrMoveFiles(JTable sourceTable, JTable destTable, boolean isMove, boolean isReplaceExisting) {
        if (sourceTable.getModel() instanceof FileTableModel || destTable.getModel() instanceof FileTableModel) {
            FileTableModel sourceTableModel = (FileTableModel) sourceTable.getModel();
            FileTableModel destTableModel = (FileTableModel) destTable.getModel();
            if (sourceTableModel.getStringCurrentDirectory().equals("root")) {
                return "Корневой диск не может быть " + (isMove ? "перемещён" : "скопирован");
            }

            int[] selectedRows = sourceTable.getSelectedRows();
            for (int i = 0; i < selectedRows.length; i++) {
                selectedRows[i] = sourceTable.convertRowIndexToModel(selectedRows[i]);
            }

            ArrayList<File> sourceFiles = new ArrayList<>();
            for (int row : selectedRows) {
                if (!sourceTableModel.getValueAt(row, 0).equals(".."))
                    sourceFiles.add((File) sourceTableModel.getValueAt(row, 3));
            }

            File destFolder = new File(destTableModel.getStringCurrentDirectory());
            int countOfNotCopiedFiles = copyOrMoveFiles(sourceFiles.toArray(new File[sourceFiles.size()]), destFolder, destFolder, isMove, isReplaceExisting);

            destTableModel.setFileList(destFolder.listFiles());
            if (isMove) sourceTableModel.setFileList(new File(sourceTableModel.getStringCurrentDirectory()).listFiles());

            if (countOfNotCopiedFiles == 0) return isMove ? "Все файлы перемещены" : "Все файлы скопированы";
            else {
                String temp = isMove ? "переместить" : "скопировать";
                return String.format("Не удалось %s %d файлов", temp, countOfNotCopiedFiles);
            }


        } else {
            System.err.println("copyOrMoveFiles: tableModel not instanceof FileTableModel");
        }
        throw new IllegalArgumentException();
    }

    private int copyOrMoveFiles(File[] sourceFiles, File destFolder, File realDestFolderToAvoidInfiniteLoop, boolean isMove, boolean isReplaceExisting) {
        int countOfUnsuccessfullyProcessedFiles = 0;
        for (File sourceFile : sourceFiles) {
            if (sourceFile.equals(realDestFolderToAvoidInfiniteLoop)) continue;
            if (sourceFile.isDirectory()) {
                File folder = new File(destFolder, sourceFile.getName());
                try {
                    Files.createDirectories(folder.toPath());
                } catch (IOException ignored) {
                }
                countOfUnsuccessfullyProcessedFiles += copyOrMoveFiles(sourceFile.listFiles(), folder, realDestFolderToAvoidInfiniteLoop, isMove, isReplaceExisting);
                if (isMove) sourceFile.delete();
            } else {
                try {
                    if (isMove) {
                        if (isReplaceExisting) Files.move(sourceFile.toPath(), new File(destFolder, sourceFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        else Files.move(sourceFile.toPath(), new File(destFolder, sourceFile.getName()).toPath());
                    } else {
                        if (isReplaceExisting) Files.copy(sourceFile.toPath(), new File(destFolder, sourceFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        else Files.copy(sourceFile.toPath(), new File(destFolder, sourceFile.getName()).toPath());
                    }
                } catch (IOException e) {
                    countOfUnsuccessfullyProcessedFiles++;
                }
            }
        }

        return countOfUnsuccessfullyProcessedFiles;
    }

    private int deleteFiles (File[] files) {
        int countOfNotDeletedFiles = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                countOfNotDeletedFiles = deleteFiles(file.listFiles());
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    countOfNotDeletedFiles++;
                }
            } else {
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    countOfNotDeletedFiles++;
                }
            }
        }
        return countOfNotDeletedFiles;
    }
}
