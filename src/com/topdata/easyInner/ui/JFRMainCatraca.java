package com.topdata.easyInner.ui;

import com.topdata.easyInner.controller.EasyInnerCatracaController;
import com.topdata.easyInner.dao.DAO;
import com.topdata.easyInner.utils.Block;
import com.topdata.easyInner.utils.BlockInterface;
import com.topdata.easyInner.utils.EnviaAtualizacao;
import com.topdata.easyInner.utils.Mac;
import com.topdata.easyInner.utils.Path;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public final class JFRMainCatraca extends JFrame implements ActionListener {

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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DAO dao = new DAO();
                String mac = Mac.getInstance();
                if (dao.isActive()) {
                    dao.query_execute("DELETE FROM soc_catraca_monitora WHERE id_catraca IN(SELECT id FROM soc_catraca WHERE ds_mac = '" + mac + "')");
                }
                try {
                    new DAO().getConnection().close();
                } catch (SQLException ex) {
                    Logger.getLogger(JFRMainCatraca.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(0);
            }
        });

        lblStatus.setText("Projeto em Execução");
    }

    public void criar_SystemTray() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            String path = Path.getRealPath();
            TrayIcon trayIcon = new TrayIcon(new ImageIcon(path + File.separator + "images" + File.separator + "catraca-icon.png").getImage(), "Catraca v5");

            trayIcon.addActionListener(Action_Tray());

            tray.add(trayIcon);
        } catch (HeadlessException | AWTException e) {
            e.getMessage();
        }
    }

    public ActionListener Action_Tray() {

        ActionListener action_tray = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        };

        return action_tray;
    }
    //        ActionListener action_tray = (ActionEvent e) -> {
    //            JFrame frame = new JFrame();
    //            frame.setTitle("Catraca v5");
    //
    //            frame.setAutoRequestFocus(true);
    //            frame.setLayout(null);
    //            frame.setBounds(0, 0, 450, 180);
    //            frame.setResizable(false);
    //
    //            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    //            frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
    //
    //            JButton button_esconder = new JButton("Esconder");
    //            button_esconder.addActionListener(Action_Esconder(frame));
    //            button_esconder.setBounds(10, 100, 200, 30);
    //
    //            JButton button_sair = new JButton("Sair do Sistema");
    //            button_sair.addActionListener(Action_Sair());
    //            button_sair.setBounds(220, 100, 200, 30);
    //
    //            lblRelogio.setBounds(10, 10, 500, 50);
    //            lblRelogio.setFont(new Font(null, Font.PLAIN, 20));
    //
    //            lblStatus.setBounds(10, 50, 500, 50);
    //            lblStatus.setFont(new Font(null, Font.PLAIN, 20));
    //
    //            frame.add(button_esconder);
    //            frame.add(button_sair);
    //            frame.add(lblRelogio);
    //            frame.add(lblStatus);
    //
    //            addWindowListener(new java.awt.event.WindowAdapter() {
    //                @Override
    //                public void windowClosing(WindowEvent windowEvent) {
    //                    EnviaAtualizacao.inativar_todas_catracas();
    //                }
    //            });
    //
    //            frame.setVisible(true);
    //
    //        };

    // }
    public ActionListener Action_Sair() {
        ActionListener action_sair = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DAO dao = new DAO();
                String mac = Mac.getInstance();
                if (dao.isActive()) {
                    dao.query_execute("DELETE FROM soc_catraca_monitora WHERE id_catraca IN(SELECT id FROM soc_catraca WHERE ds_mac = '" + mac + "')");
                }
                try {
                    new DAO().getConnection().close();
                } catch (SQLException ex) {
                    Logger.getLogger(JFRMainCatraca.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(0);
            }
        };
//        ActionListener action_sair = (ActionEvent e) -> {
//            DAO dao = new DAO();
//            String mac = Mac.getInstance();
//            if (dao.getConectado()) {
//                dao.query("DELETE FROM soc_catraca_monitora WHERE id_catraca IN(SELECT id FROM soc_catraca WHERE ds_mac = '" + mac + "')");
//            }
//            System.exit(0);
//        };
        return action_sair;
    }

    public ActionListener Action_Esconder(final JFrame frame) {
        ActionListener action_esconder = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        };
//        ActionListener action_esconder = (ActionEvent e) -> {
//            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
//        };
        return action_esconder;
    }

    public static void main(String[] args) {
        Block.TYPE = "servico_catraca";
        if (!Block.registerInstance()) {
            // instance already running.
            System.out.println("Another instance of this application is already running.  Exiting.");
            JOptionPane.showMessageDialog(null, "Aplicação já esta em execução");
            System.exit(0);
        }
        Block.setBlockInterface(new BlockInterface() {
            @Override
            public void newInstanceCreated() {
                System.out.println("New instance detected...");
                // this is where your handler code goes...
            }
        });
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
