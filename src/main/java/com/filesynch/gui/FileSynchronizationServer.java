package com.filesynch.gui;

import com.filesynch.Main;
import com.filesynch.dto.ServerStatus;
import com.filesynch.entity.ClientInfo;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileSynchronizationServer {
    @Getter
    private JPanel jPanelServer;
    private JTabbedPane tabbedPane1;
    private JTable table2;
    private JTable table3;
    private JPanel jPanelMain;
    private JPanel jPanelTextMessage;
    private JPanel jPanelLog;
    private JPanel jPanelFile;
    private JPanel jPanelCommand;
    private JTextField jTextFieldTextMessage;
    private JButton jButtonTextMessage;
    private JLabel jLabelTextMessage;
    private JTextField jTextFieldFile;
    private JButton jButtonSendFile;
    private JTextField jTextFieldCommand;
    private JButton jButtonSendCommand;
    private JLabel jLabelFileTitle;
    private JLabel jLabelFile;
    private JLabel jLabelCommand;
    @Getter
    private JProgressBar jProgressBarFile;
    @Getter
    private JTextArea jTextAreaLog;
    private JButton jButtonStartServer;
    private JButton jButtonStopServer;
    @Getter
    private JList jListQueueSending;
    @Getter
    @Setter
    private JList jListClientList;
    private JPanel jPanelClientInfo;
    private JLabel jLabelClientStatus;
    private JLabel jLabelClientInfo;
    private JLabel jLabelPCModel;
    private JLabel jLabelIP;
    private JLabel jLabelPCName;
    private JLabel jLabelClientStatusValue;
    private JLabel jLabelIPValue;
    private JLabel jLabelPCNameValue;
    private JLabel jLabelPCModelValue;
    private JLabel jLabelServerInfo;
    private JPanel jPanelServerInfo;
    private JLabel jLabelServerStatus;
    @Getter
    private JLabel jLabelServerInfoValue;
    private JLabel jLabelServerStatusValue;
    private JLabel jLabelLog;
    private JScrollPane jScrollPaneLog;
    private JScrollPane jScrollPaneQueueSending;
    private JScrollPane jScrollPaneClientList;
    private JButton jButtonSendAllFiles;
    private JScrollPane jScrollPaneQueueReceiving;
    @Getter
    private JList jListQueueReceiving;
    private JButton jButtonSendAllFilesFast;
    private JButton jButtonSendFileFast;

    public FileSynchronizationServer() {
        jButtonStopServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.stopServer(20);
                Main.server.setServerStatus(ServerStatus.SERVER_STANDBY_FULL);
                jLabelServerStatusValue.setText(ServerStatus.SERVER_STANDBY_FULL.getStatus());
                jLabelServerStatusValue.setForeground(Color.RED);
            }
        });
        jButtonStartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.startServer(20);
                Main.server.setServerStatus(ServerStatus.SERVER_WORK);
                jLabelServerStatusValue.setText(ServerStatus.SERVER_WORK.getStatus());
                jLabelServerStatusValue.setForeground(Color.GREEN);
            }
        });
        jListClientList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ClientInfo clientInfo =
                        Main.server.getClientInfoRepository()
                                .findByLogin(jListClientList.getSelectedValue().toString());
                jLabelClientStatusValue.setText(clientInfo.getStatus().getStatus());
                jLabelIPValue.setText(clientInfo.getIpAddress());
                jLabelPCNameValue.setText(clientInfo.getPcName());
                jLabelPCModelValue.setText(clientInfo.getPcModel());
            }
        });
        jButtonTextMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    Main.sendMessage(jListClientList.getSelectedValue().toString(), jTextFieldTextMessage.getText());
                }).start();
            }
        });
        jButtonSendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    Main.sendFile(jListClientList.getSelectedValue().toString(), jTextFieldFile.getText());
                }).start();
            }
        });
        jButtonSendAllFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    Main.sendAllFiles(jListClientList.getSelectedValue().toString());
                }).start();
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("File Synchronization Client");
        frame.setContentPane(new FileSynchronizationServer().jPanelServer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        jPanelServer = new JPanel();
        jPanelServer.setLayout(new GridLayoutManager(1, 1, new Insets(10, 20, 10, 20), -1, -1));
        jPanelServer.setPreferredSize(new Dimension(950, 800));
        tabbedPane1 = new JTabbedPane();
        jPanelServer.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        jPanelMain = new JPanel();
        jPanelMain.setLayout(new GridLayoutManager(7, 5, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Main", jPanelMain);
        jPanelTextMessage = new JPanel();
        jPanelTextMessage.setLayout(new GridLayoutManager(2, 2, new Insets(5, 5, 5, 5), -1, -1));
        jPanelMain.add(jPanelTextMessage, new GridConstraints(3, 2, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(-1, 30), null, 0, false));
        jPanelTextMessage.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-10385191)), null));
        jLabelTextMessage = new JLabel();
        jLabelTextMessage.setText("Text Message:");
        jPanelTextMessage.add(jLabelTextMessage, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jTextFieldTextMessage = new JTextField();
        jPanelTextMessage.add(jTextFieldTextMessage, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        jButtonTextMessage = new JButton();
        jButtonTextMessage.setText("Send Message");
        jPanelTextMessage.add(jButtonTextMessage, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jPanelFile = new JPanel();
        jPanelFile.setLayout(new GridLayoutManager(4, 6, new Insets(5, 5, 5, 5), -1, -1));
        jPanelMain.add(jPanelFile, new GridConstraints(4, 2, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        jPanelFile.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-7939681)), null));
        jLabelFileTitle = new JLabel();
        jLabelFileTitle.setText("File:");
        jPanelFile.add(jLabelFileTitle, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelFile = new JLabel();
        jLabelFile.setText("File Name:");
        jPanelFile.add(jLabelFile, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jTextFieldFile = new JTextField();
        jPanelFile.add(jTextFieldFile, new GridConstraints(1, 1, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        jProgressBarFile = new JProgressBar();
        jPanelFile.add(jProgressBarFile, new GridConstraints(3, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jButtonSendAllFiles = new JButton();
        jButtonSendAllFiles.setText("Send All Files");
        jPanelFile.add(jButtonSendAllFiles, new GridConstraints(1, 3, 2, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jButtonSendFile = new JButton();
        jButtonSendFile.setText("Send File");
        jPanelFile.add(jButtonSendFile, new GridConstraints(1, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jPanelCommand = new JPanel();
        jPanelCommand.setLayout(new GridLayoutManager(2, 2, new Insets(5, 5, 5, 5), -1, -1));
        jPanelMain.add(jPanelCommand, new GridConstraints(5, 2, 2, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        jPanelCommand.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-2525260)), null));
        jLabelCommand = new JLabel();
        jLabelCommand.setText("Command:");
        jPanelCommand.add(jLabelCommand, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jTextFieldCommand = new JTextField();
        jPanelCommand.add(jTextFieldCommand, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        jButtonSendCommand = new JButton();
        jButtonSendCommand.setText("Send Command");
        jPanelCommand.add(jButtonSendCommand, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jPanelClientInfo = new JPanel();
        jPanelClientInfo.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        jPanelMain.add(jPanelClientInfo, new GridConstraints(2, 2, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        jLabelClientStatus = new JLabel();
        jLabelClientStatus.setText("Client Status:");
        jPanelClientInfo.add(jLabelClientStatus, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelClientInfo = new JLabel();
        jLabelClientInfo.setText("Client Info:");
        jPanelClientInfo.add(jLabelClientInfo, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelIP = new JLabel();
        jLabelIP.setText("IP Address:");
        jPanelClientInfo.add(jLabelIP, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelPCName = new JLabel();
        jLabelPCName.setText("PC Name:");
        jPanelClientInfo.add(jLabelPCName, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelClientStatusValue = new JLabel();
        jLabelClientStatusValue.setText("");
        jPanelClientInfo.add(jLabelClientStatusValue, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelIPValue = new JLabel();
        jLabelIPValue.setText("");
        jPanelClientInfo.add(jLabelIPValue, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelPCNameValue = new JLabel();
        jLabelPCNameValue.setText("");
        jPanelClientInfo.add(jLabelPCNameValue, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelPCModel = new JLabel();
        jLabelPCModel.setText("PC Model:");
        jPanelClientInfo.add(jLabelPCModel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelPCModelValue = new JLabel();
        jLabelPCModelValue.setText("");
        jPanelClientInfo.add(jLabelPCModelValue, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jPanelServerInfo = new JPanel();
        jPanelServerInfo.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        jPanelMain.add(jPanelServerInfo, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        jLabelServerInfo = new JLabel();
        jLabelServerInfo.setText("Server Info:");
        jPanelServerInfo.add(jLabelServerInfo, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelServerStatus = new JLabel();
        jLabelServerStatus.setText("Server Status:");
        jPanelServerInfo.add(jLabelServerStatus, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jButtonStopServer = new JButton();
        jButtonStopServer.setText("Stop Server");
        jPanelServerInfo.add(jButtonStopServer, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelServerInfoValue = new JLabel();
        jLabelServerInfoValue.setText("127.0.0.1:1099/fs");
        jPanelServerInfo.add(jLabelServerInfoValue, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jLabelServerStatusValue = new JLabel();
        jLabelServerStatusValue.setForeground(new Color(-2537940));
        jLabelServerStatusValue.setText("SERVER_STANDBY_FULL");
        jPanelServerInfo.add(jLabelServerStatusValue, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jButtonStartServer = new JButton();
        jButtonStartServer.setText("Start Server");
        jPanelServerInfo.add(jButtonStartServer, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jPanelLog = new JPanel();
        jPanelLog.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        jPanelLog.setDoubleBuffered(true);
        jPanelMain.add(jPanelLog, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(100, 200), null, 0, false));
        jPanelLog.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-13224394)), null));
        jLabelLog = new JLabel();
        jLabelLog.setText("Log:");
        jPanelLog.add(jLabelLog, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jScrollPaneLog = new JScrollPane();
        jPanelLog.add(jScrollPaneLog, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        jTextAreaLog = new JTextArea();
        jScrollPaneLog.setViewportView(jTextAreaLog);
        jScrollPaneQueueSending = new JScrollPane();
        jPanelMain.add(jScrollPaneQueueSending, new GridConstraints(4, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        jListQueueSending = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        jListQueueSending.setModel(defaultListModel1);
        jScrollPaneQueueSending.setViewportView(jListQueueSending);
        jScrollPaneClientList = new JScrollPane();
        jPanelMain.add(jScrollPaneClientList, new GridConstraints(2, 0, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        jListClientList = new JList();
        final DefaultListModel defaultListModel2 = new DefaultListModel();
        jListClientList.setModel(defaultListModel2);
        jScrollPaneClientList.setViewportView(jListClientList);
        jScrollPaneQueueReceiving = new JScrollPane();
        jPanelMain.add(jScrollPaneQueueReceiving, new GridConstraints(4, 1, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        jListQueueReceiving = new JList();
        final DefaultListModel defaultListModel3 = new DefaultListModel();
        jListQueueReceiving.setModel(defaultListModel3);
        jScrollPaneQueueReceiving.setViewportView(jListQueueReceiving);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Edit", panel1);
        table2 = new JTable();
        panel1.add(table2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Info", panel2);
        table3 = new JTable();
        panel2.add(table3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel2.add(spacer4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jPanelServer;
    }

}
