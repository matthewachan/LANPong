/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lanpong;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 *
 * @author Matt
 */
public class LANPong extends JFrame {
    JFrame frame_config;
    JLabel label_ip;
    JLabel label_port;
    JButton btn_connect;
    JButton btn_server;
    JPanel panel_config;
    JTextField field_ip;
    JTextField field_port;
    String ip_hint = "localhost";
    String ip_address;
    String port_hint = "5000";
    int port;
    static Game frame_game;
    

    public LANPong() {
        // Create config JFrame and JPanel here
        frame_config = new JFrame();
        panel_config = new JPanel();
        panel_config.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel_config.setPreferredSize(new Dimension(400,80));
        
        createWidgets();
        
        // Settings for config JFrame
        frame_config.add(panel_config);
        frame_config.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame_config.pack();
        frame_config.setLocation(500, 300);
        frame_config.setVisible(true);
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new LANPong();
    }
    
    public void createWidgets () {
        // Add widgets here
        label_ip = new JLabel("Host IP Address (ignore if creating server)");
        panel_config.add(label_ip);
        
        field_ip = new JTextField(ip_hint);
        field_ip.setBorder(BorderFactory.createCompoundBorder(field_ip.getBorder(), BorderFactory.createEmptyBorder(5,5,5,5)));
        field_ip.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                field_ip.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field_ip.getText().length() < 1)
                    field_ip.setText(ip_hint);
            }
            
        });
        panel_config.add(field_ip);
        
        label_port = new JLabel("Port");
        panel_config.add(label_port);
        
        
        field_port = new JTextField(port_hint);
        field_port.setBorder(BorderFactory.createCompoundBorder(field_port.getBorder(), BorderFactory.createEmptyBorder(5,5,5,5)));
        field_port.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                field_port.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field_port.getText().length() < 1)
                    field_port.setText(port_hint);
            }
            
        });
        panel_config.add(field_port);

        btn_connect = new JButton("Connect");
        btn_connect.setLocation(btn_connect.getLocation().x, 200);
        btn_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame_config.setVisible(false);
                ip_address = field_ip.getText();
                port = Integer.parseInt(field_port.getText());
                frame_game = new Game(ip_address, port);
            }
        });
        panel_config.add(btn_connect);
        
        btn_server = new JButton("Create Server");
        btn_server.setLocation(btn_server.getLocation().x, 200);
        btn_server.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame_config.setVisible(false);
                port = Integer.parseInt(field_port.getText());
                frame_game = new Game(port);
            }
        });
        panel_config.add(btn_server);
    }
    
 
    
}


