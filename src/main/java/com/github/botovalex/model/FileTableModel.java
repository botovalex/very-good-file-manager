package com.github.botovalex.model;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileTableModel extends AbstractTableModel {
    private List<FileBean> beans;
    private File lastDirectory = File.listRoots()[0];
    private String currentDirectory;

    public FileTableModel() {
        this(File.listRoots());
    }

    public FileTableModel(File... files) {
        beans = new ArrayList<>();
        setFileList(files);
    }

    public File getLastDirectory() {
        return lastDirectory;
    }

    public void setFileList(File... files) {
        if (files == null) return;
        if (beans.size() > 0 && files.length > 0) {
            lastDirectory = beans.get(0).getCurrentDirectory();
        }

        beans.clear();

        FileBean topLevelBean;
        if (files.length > 0) {
            if (!files[0].equals(File.listRoots()[0])) {
                topLevelBean = new FileBean(files[0].getParentFile());
                topLevelBean.setName("..");
                beans.add(topLevelBean);
            }
            for (File file : files) {
                beans.add(new FileBean(file));
            }
        } else {
            topLevelBean = new FileBean(lastDirectory);
            topLevelBean.setName("..");
            beans.add(topLevelBean);
        }

        if (beans != null && beans.size() > 0 && beans.get(0).getName().equals("..")) {
            currentDirectory = beans.get(0).getCurrentDirectory().getAbsolutePath();
        } else {
            currentDirectory = "root";
        }
        fireTableDataChanged();

    }

    public void setFileList(int selectedRow) {
        if (beans == null || selectedRow < 0 || selectedRow >= beans.size()) return;
        FileBean bean = beans.get(selectedRow);
        if (bean.getName().equals("..")) {
            upOneLevel();
        } else {
            if (bean.getListFiles() == null || bean.getListFiles().length == 0) {
                lastDirectory = bean.getCurrentDirectory();
            }
            setFileList(bean.getListFiles());
        }
    }

    public void upOneLevel() {
        if (beans != null && beans.size() > 0 && beans.get(0).getCurrentDirectory().getParentFile() != null) {
            setFileList(beans.get(0).getCurrentDirectory().getParentFile().listFiles());
        } else {
            setFileList(File.listRoots());
        }
    }

    public String getStringCurrentDirectory() {
        return currentDirectory != null ? currentDirectory : "root";
    }

    @Override
    public int getRowCount() {
        return beans.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Имя";
            case 1:
                return "Размер";
            case 2:
                return "Тип";
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return FileBean.FileBeanSize.class;
            case 2:
                return String.class;
            case 3:
                return File.class;
            default:
                return Object.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        //TODO
//        System.out.println("beans size = " + beans.size() + " row " + rowIndex + " column " + columnIndex);
        if (beans == null || beans.size() <= 0) return "";
        FileBean bean = beans.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return bean.getName();
            case 1:
                return bean.getSize();
            case 2:
                return bean.getType();
            case 3:
                return bean.getFile();
        }
        return "";
    }
}
