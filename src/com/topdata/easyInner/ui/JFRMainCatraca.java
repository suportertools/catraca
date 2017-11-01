package com.topdata.easyInner.ui;

import com.topdata.easyInner.controller.EasyInnerCatracaController;
import com.topdata.easyInner.utils.EnviaAtualizacao;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.GregorianCalendar;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public final class JFRMainCatraca extends JFrame implements ActionListener{

    private Timer timer = null;
    
    private JLabel lblRelogio = new JLabel();
    
    private JLabel lblStatus = new JLabel("Iniciando Projeto");
    
    private ActionEvent ae = null;
    
    public JFRMainCatraca() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        
        panel.add(lblRelogio);
        panel.add(lblStatus);
    
        disparaRelogio();
        
        criar_SystemTray();
        
        lblStatus.setText("Projeto em Execução");
    }
    
    
    public void criar_SystemTray(){
        try {
            SystemTray tray = SystemTray.getSystemTray();
            TrayIcon trayIcon = new TrayIcon(new ImageIcon("C:/rtools/catraca/catraca-icon.png").getImage(), "Catraca v5");

            trayIcon.addActionListener(Action_Tray());

            tray.add(trayIcon);
        } catch (HeadlessException | AWTException e) {
            e.getMessage();
        }
    }
    
    public ActionListener Action_Tray() {
        ActionListener action_tray = (ActionEvent e) -> {
            JFrame frame = new JFrame();
            frame.setTitle("Catraca v5");
            
            frame.setAutoRequestFocus(true);
            frame.setLayout(null);
            frame.setBounds(0, 0, 450, 180);
            frame.setResizable(false);
            
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);

            JButton button_esconder = new JButton("Esconder");
            button_esconder.addActionListener(Action_Esconder(frame));
            button_esconder.setBounds(10, 100, 200, 30);

            JButton button_sair = new JButton("Sair do Sistema");
            button_sair.addActionListener(Action_Sair());
            button_sair.setBounds(220, 100, 200, 30);
    
            lblRelogio.setBounds(10, 10, 500, 50);
            lblRelogio.setFont(new Font(null, Font.PLAIN, 20));
            
            lblStatus.setBounds(10, 50, 500, 50);
            lblStatus.setFont(new Font(null, Font.PLAIN, 20));

            frame.add(button_esconder);
            frame.add(button_sair);
            frame.add(lblRelogio);
            frame.add(lblStatus);
            
            
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    EnviaAtualizacao.inativar_todas_catracas();
                }
            });
            
            frame.setVisible(true);
            
        };
        return action_tray;
    }
    
    public ActionListener Action_Sair() {
        ActionListener action_sair = (ActionEvent e) -> {
            System.exit(0);
        };
        return action_sair;
    }

    public ActionListener Action_Esconder(JFrame frame) {
        ActionListener action_esconder = (ActionEvent e) -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        };
        return action_esconder;
    }
    
    public static void main(String[] args) {
        EasyInnerCatracaController innerCatracaController = new EasyInnerCatracaController(new JFRMainCatraca());
        innerCatracaController.run();
    }

    public void disparaRelogio() {
        if (timer == null) {
            timer = new Timer(1000, this);
            timer.setInitialDelay(0);
            timer.start();

            actionPerformed(ae);
        } else if (!timer.isRunning()) {
            timer.restart();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GregorianCalendar calendario = new GregorianCalendar();
        int h = calendario.get(GregorianCalendar.HOUR_OF_DAY);
        int m = calendario.get(GregorianCalendar.MINUTE);
        int s = calendario.get(GregorianCalendar.SECOND);
        String hora
                = ((h < 10) ? "0" : "")
                + h
                + ":"
                + ((m < 10) ? "0" : "")
                + m
                + ":"
                + ((s < 10) ? "0" : "")
                + s;
        getLblRelogio().setText(hora);
    }

    public JLabel getLblRelogio() {
        return lblRelogio;
    }

    public void setLblRelogio(JLabel lblRelogio) {
        this.lblRelogio = lblRelogio;
    }

    public JLabel getLblStatus() {
        return lblStatus;
    }

    public void setLblStatus(JLabel lblStatus) {
        this.lblStatus = lblStatus;
    }
}
