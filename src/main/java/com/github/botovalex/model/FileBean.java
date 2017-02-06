package com.github.botovalex.model;

import java.io.File;
import java.util.Locale;

public class FileBean {
    //TODO имплементировать сериализацию, добавить конструктор без параметров

    private final File file;
    private String name;

    public FileBean(File file) {
        this.file = file;
        if (file == null) name = "";
        else name = file.getParent() != null ? file.getName() : file.toString();
    }

    public File[] getListFiles() {
        if (file == null) return File.listRoots();
        return file.isDirectory() ? file.listFiles() : file.getParentFile().listFiles();
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public void setName(String name) {
        if (name == null) this.name = "";
        this.name = name;
    }

    public FileBeanSize getSize() {
        if (file == null || file.isDirectory()) return new FileBeanSize(0L);
        return new FileBeanSize(file.length());
    }

    public String getType() {
        if (file == null || !file.exists()) return "";
        if (file.isDirectory()) return "directory";
        return file.getName().contains(".") ? file.getName().substring(file.getName().lastIndexOf('.') + 1) : "file";
    }

    public File getCurrentDirectory() {
        if (file == null) return File.listRoots()[0];
        if (file.isDirectory()) return file;
        else if (file.isFile()) return file.getParentFile() != null ? file.getParentFile() : File.listRoots()[0];
        return File.listRoots()[0];
    }

    class FileBeanSize implements Comparable<FileBeanSize> {
        private Long size;

        public FileBeanSize(Long size) {
            this.size = size;
        }

        public Long getSize() {
            return size;
        }

        @Override
        public String toString() {
            if (size == 0) return "";

            double dSize = (double) size;
            int count = 0;
            while (dSize >= 1024) {
                dSize /= 1024;
                count++;
            }
            String format = count > 0 ? "%.2f" : "%.0f";
            String result = String.format(Locale.US, format, dSize);
            switch (count) {
                case 0:
                    return result + " Байт";
                case 1:
                    return result + " КБ";
                case 2:
                    return result + " МБ";
                case 3:
                    return result + " ГБ";
                case 4:
                    return result + " ТБ";
                default:
                    return result;
            }
        }

        @Override
        public int compareTo(FileBeanSize o) {
            return size.compareTo(o.getSize());
        }
    }
}
