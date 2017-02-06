package com.github.botovalex.view;


import com.github.botovalex.controller.Controller;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class View {
    private final Controller controller;
    private final JFrame mainFrame;
    private JTable leftJTable;
    private JTable rightJTable;

    private boolean isLeftTableHasLastFocus = true;
    private boolean isReplaceInCopy = false;
    private boolean isReplaceInMove = false;

    private static final int INITIAL_WIDTH = 1000;
    private static final int INITIAL_HEIGHT = 500;

    public View(Controller controller) {
        this.controller = controller;
        mainFrame = new JFrame("Very Good File Manager");
    }

    public void init(final File... files) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createGUI(files);
            }
        });
    }

    private void createGUI(File... files) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mainFrame.setJMenuBar(createJMenuBar());

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel tablePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        leftJTable = createJTable(files);
        JScrollPane scrollPaneLeft = new JScrollPane(leftJTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneLeft.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder()));
        scrollPaneLeft.getVerticalScrollBar().setFocusable(false);
        addToTitleBorderTableModelListener(scrollPaneLeft, leftJTable);
        leftPanel.add(scrollPaneLeft);

        rightJTable = createJTable(files);
        JScrollPane scrollPaneRight = new JScrollPane(rightJTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneRight.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder()));
        scrollPaneRight.getVerticalScrollBar().setFocusable(false);
        addToTitleBorderTableModelListener(scrollPaneRight, rightJTable);

        rightPanel.add(scrollPaneRight);

        tablePanel.add(leftPanel);
        tablePanel.add(rightPanel);

        mainFrame.add(mainPanel);

        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");
        UIManager.put("OptionPane.okButtonText", "Готово");

        mainFrame.setPreferredSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setLocationRelativeTo(null);
        leftJTable.requestFocus();
    }

    private void addToTitleBorderTableModelListener(final JComponent component, final JTable table) {
        if (component.getBorder() instanceof TitledBorder) {
            final TitledBorder titledBorder = (TitledBorder) component.getBorder();
            titledBorder.setTitle(controller.getStringCurrentDirectory(table));

            table.getModel().addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    titledBorder.setTitle(controller.getStringCurrentDirectory(table));
                    component.repaint();
                }
            });
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalGlue());

        JButton copyButton = new JButton("Копировать (F5)");
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isLeftTableHasLastFocus) copyOrMoveFiles(leftJTable, rightJTable, false, isReplaceInCopy);
                else copyOrMoveFiles(rightJTable, leftJTable, false, isReplaceInCopy);
            }
        });
        copyButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        copyButton.setFocusable(false);
        panel.add(copyButton);
        panel.add(Box.createHorizontalStrut(40));

        JButton moveButton = new JButton("Переместить (F6)");
        moveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isLeftTableHasLastFocus) copyOrMoveFiles(leftJTable, rightJTable, true, isReplaceInMove);
                else copyOrMoveFiles(rightJTable, leftJTable, true, isReplaceInMove);
            }
        });
        moveButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(moveButton);
        panel.add(Box.createHorizontalStrut(40));

        JButton newFolderButton = new JButton("Создать папку (F7)");
        newFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isLeftTableHasLastFocus) createFolder(leftJTable);
                else createFolder(rightJTable);
            }
        });
        newFolderButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(newFolderButton);
        panel.add(Box.createHorizontalStrut(40));

        JButton deleteButton = new JButton("Удалить (F8)");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isLeftTableHasLastFocus) deleteFiles(leftJTable);
                else deleteFiles(rightJTable);
            }
        });
        deleteButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(deleteButton);
        panel.add(Box.createHorizontalStrut(40));

        JButton findButton = new JButton("Поиск файлов (ctrl+F)");
        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isLeftTableHasLastFocus) findFiles(leftJTable);
                else findFiles(rightJTable);
            }
        });
        findButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(findButton);
        panel.add(Box.createHorizontalGlue());


        return panel;
    }

    private JMenuBar createJMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        Font font = new Font("Verdana", Font.PLAIN, 11);
        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setFont(font);

        JCheckBoxMenuItem isReplaceInCopyMenuItem = new JCheckBoxMenuItem("Копировать с заменой");
        isReplaceInCopyMenuItem.setFont(font);
        isReplaceInCopyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isReplaceInCopy = ((JCheckBoxMenuItem)e.getSource()).isSelected();
            }
        });
        fileMenu.add(isReplaceInCopyMenuItem);

        JCheckBoxMenuItem isReplaceInMoveMenuItem = new JCheckBoxMenuItem("Переместить с заменой");
        isReplaceInMoveMenuItem.setFont(font);
        isReplaceInMoveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isReplaceInMove = ((JCheckBoxMenuItem)e.getSource()).isSelected();
            }
        });

        fileMenu.add(isReplaceInMoveMenuItem);

        fileMenu.addSeparator();
        JMenuItem exitMenuItem = new JMenuItem("Выход");
        exitMenuItem.setFont(font);
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        JMenu aboutMenu = new JMenu("Справка");
        aboutMenu.setFont(font);
        JMenuItem aboutMenuItem = new JMenuItem("О программе");
        aboutMenuItem.setFont(font);
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutWindows();
            }
        });
        aboutMenu.add(aboutMenuItem);
        menuBar.add(aboutMenu);

        return menuBar;
    }

    private void createFolder(JTable table) {
        String folderName = JOptionPane.showInputDialog(mainFrame, "Введите имя директории: ", "Введите имя директории: ", JOptionPane.QUESTION_MESSAGE);
        String message;
        try {
            message = controller.createFolder(table, folderName);
        } catch (IllegalArgumentException e) {
            message = "error_Внутренняя ошибка приложения";
        }

        if (message.startsWith("error_")) {
            JOptionPane.showMessageDialog(mainFrame, message.substring(6), "Ошибка создания директории", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTable createJTable(File... files) {
        final JTable table = new JTable(controller.getTableModel(files));

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        controller.enterToTheFolder(table);
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        controller.upOneLevel(table);
                        break;
                    case KeyEvent.VK_TAB:
                        if (e.getSource() == leftJTable) rightJTable.requestFocus();
                        else leftJTable.requestFocus();
                        break;
                    case KeyEvent.VK_F5:
                        if (e.getSource() == leftJTable) copyOrMoveFiles(leftJTable, rightJTable, false, isReplaceInCopy);
                        else copyOrMoveFiles(rightJTable, leftJTable, false, isReplaceInCopy);
                        break;
                    case KeyEvent.VK_F6:
                        if (e.getSource() == leftJTable) copyOrMoveFiles(leftJTable, rightJTable, true, isReplaceInMove);
                        else copyOrMoveFiles(rightJTable, leftJTable, true, isReplaceInMove);
                        break;
                    case KeyEvent.VK_F7:
                        createFolder(table);
                        break;
                    case KeyEvent.VK_F8:
                        deleteFiles(table);
                        break;
                    case KeyEvent.VK_F:
                        if (e.isControlDown()) findFiles(table);
                        break;
                }
            }
        });

        table.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                isLeftTableHasLastFocus = e.getSource() == leftJTable;
            }
        });

        table.setAutoCreateRowSorter(true);

        return table;
    }

    private void copyOrMoveFiles(final JTable sourceTable, final JTable destTable, final boolean isMove, final boolean isReplaceExisting) {
//        int cancel = JOptionPane.showConfirmDialog(window, "Выполняется операция над файлами. Отменить?", "Операция над файлами", JOptionPane.CANCEL_OPTION);
        SwingWorker<String, Void> swingWorker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    return controller.copyOrMoveFiles(sourceTable, destTable, isMove, isReplaceExisting);
                } catch (IllegalArgumentException e) {
                    return "Внутренняя ошибка приложения";
                }
            }

            @Override
            protected void done() {
                String message;
                try {
                    message = get();
                    JOptionPane.showMessageDialog(mainFrame, message, "Результат", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException e) {
                    JOptionPane.showMessageDialog(mainFrame, "Операция прервана", "Операция прервана", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException e) {
                    JOptionPane.showMessageDialog(mainFrame, "Ошибка работы с файловой системой", "Ошибка работы с файловой системой", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        swingWorker.execute();
    }

    private void deleteFiles(final JTable table) {
//        int cancel = JOptionPane.showConfirmDialog(window, "Выполняется операция над файлами. Отменить?", "Операция над файлами", JOptionPane.CANCEL_OPTION)
        SwingWorker<String, Void> swingWorker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    return controller.deleteFiles(table);
                } catch (IllegalArgumentException e) {
                    return "Внутренняя ошибка приложения";
                }
            }

            @Override
            protected void done() {
                String message;
                try {
                    message = get();
                    JOptionPane.showMessageDialog(mainFrame, message, "Результат", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException e) {
                    JOptionPane.showMessageDialog(mainFrame, "Операция прервана", "Операция прервана", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException e) {
                    JOptionPane.showMessageDialog(mainFrame, "Ошибка удаления файлов", "Ошибка удаления файлов", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        swingWorker.execute();
    }

    private void findFiles(final JTable table) {
        final JFrame findFilesFrame = new JFrame("Поиск файлов");
        findFilesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel findFilesPanel = new JPanel(new BorderLayout());
        findFilesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(),
                "Поиск файлов в директории: " + controller.getStringCurrentDirectory(table)));

        JPanel topPanel = new JPanel();
        JLabel findTextLabel = new JLabel("Поиск файлов: ");
        final DefaultListModel<String> dlm = new DefaultListModel<>();
        final JList<String> resultList = new JList<>(dlm);
        final JCheckBox checkBox = new JCheckBox("Искать во вложенных папках");

        final JTextField textField = new JTextField();
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    fildFilesStart(table, resultList, textField, checkBox.isSelected());
                }
            }
        });

        JButton findButton = new JButton("Поиск");
        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fildFilesStart(table, resultList, textField, checkBox.isSelected());
            }
        });

        GroupLayout layout = new GroupLayout(topPanel);
        topPanel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(findTextLabel).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(textField).addComponent(checkBox)).addComponent(findButton));
        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(findTextLabel).addComponent(textField).addComponent(findButton))
                .addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(checkBox))));

        findFilesPanel.add(topPanel, BorderLayout.NORTH);
        findFilesPanel.add(new JScrollPane(resultList));
        findFilesFrame.add(findFilesPanel);

        findFilesFrame.setPreferredSize(new Dimension(500, 300));
        findFilesFrame.setMinimumSize(new Dimension(500, 300));
        findFilesFrame.pack();
        findFilesFrame.setVisible(true);
        findFilesFrame.setLocationRelativeTo(null);
    }

    private void fildFilesStart(final JTable table, final JList<String> resultList, final JTextField textField, final boolean isSearchInSubfolders) {
        SwingWorker<ArrayList<String>, Void> swingWorker = new SwingWorker<ArrayList<String>, Void>() {
            @Override
            protected ArrayList<String> doInBackground() throws Exception {
                ArrayList<String> result = new ArrayList<>();
                try {
                    result.addAll(controller.findFiles(table, textField.getText(), isSearchInSubfolders));

                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(resultList.getParent(), "Внутренняя ошибка приложения", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
                return result;
            }

            @Override
            protected void done() {
                ArrayList<String> result;
                try {
                    result = get();
                    ((DefaultListModel<String>) resultList.getModel()).removeAllElements();
                    if (result.isEmpty()) {
                        ((DefaultListModel<String>) resultList.getModel()).addElement("Совпадений не найдено");
                    } else {
                        Collections.sort(result);
                        for (String s : result) {
                            ((DefaultListModel<String>) resultList.getModel()).addElement(s);
                        }
                    }

                } catch (InterruptedException e) {
                    JOptionPane.showMessageDialog(resultList.getParent(), "Операция прервана", "Операция прервана", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException e) {
                    JOptionPane.showMessageDialog(resultList.getParent(), "Ошибка поиска файлов", "Ошибка поиска файлов", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        if (textField.getText().isEmpty())
            JOptionPane.showMessageDialog(resultList.getParent(), "Введите текст для поиска", "Введите текст", JOptionPane.ERROR_MESSAGE);
        else {
            swingWorker.execute();
        }
    }

    private void showAboutWindows() {
        final JFrame aboutFrame = new JFrame("О программе");
        aboutFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        String text = "<html><h2>Very Good File Manager, ver 1.4</h2>" +
                "<font face=’verdana’ size = 2>" +
                " ©Александр Ботов, 2017<br>" +
                " Все права защищены.<br>" +
                " <br>" +
                " Благодарим за использование программы,<br>" +
                " надеемся, она вам понравится.<br>" +
                " <br>" +
                " С автором можно связать по e-mail<br>" +
                " botovalex@gmail.com</html>";
        JLabel htmlLabel = new JLabel(text);
        aboutFrame.add(htmlLabel, BorderLayout.CENTER);

        aboutFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                aboutFrame.setShape(new RoundRectangle2D.Double(0, 0, aboutFrame.getWidth(), aboutFrame.getHeight(), 20, 20));
            }
        });
        aboutFrame.setPreferredSize(new Dimension(300, 220));
        aboutFrame.setMinimumSize(new Dimension(300, 220));
        aboutFrame.setUndecorated(true);
        aboutFrame.setOpacity(0.85F);
        aboutFrame.setResizable(false);
        aboutFrame.pack();
        aboutFrame.setVisible(true);
        aboutFrame.setLocationRelativeTo(null);
    }

    public static void main(String[] args) throws InterruptedException {
        Controller controller = new Controller();
        controller.init();

    }
}
