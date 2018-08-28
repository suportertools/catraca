package com.topdata.easyInner.ui;

import com.topdata.easyInner.controller.EasyInnerCatracaController;
import com.topdata.easyInner.dao.Catraca;
import com.topdata.easyInner.dao.Conf_Cliente;
import com.topdata.easyInner.dao.DAO;
import com.topdata.easyInner.utils.Biometria;
import com.topdata.easyInner.utils.Block;
import com.topdata.easyInner.utils.BlockInterface;
import com.topdata.easyInner.utils.Debugs;
import com.topdata.easyInner.utils.EnviaAtualizacao;
import com.topdata.easyInner.utils.Mac;
import com.topdata.easyInner.utils.Path;
import com.topdata.easyInner.utils.Ping;
import com.topdata.easyInner.utils.Preloader;
import com.topdata.easyInner.utils.RetornoJson;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import org.json.JSONObject;

public final class JFRMainCatraca extends JFrame implements ActionListener {

    private Timer timer = null;
    private JLabel lbl_relogio = new JLabel();
    private JLabel lbl_status = new JLabel("Iniciando Projeto");
    private ActionEvent ae = null;
    private final Preloader preloader;
    private JButton btn_debug;
    private JButton btn_codigo;
    private JButton btn_cartao;
    private JButton btn_codigo_externo;
    private static List<Catraca> list_catraca;
    static EasyInnerCatracaController innerCatracaController;
    static Thread biometria;
    private Conf_Cliente conf_Cliente = new Conf_Cliente();
    private Socket socket;

    public static void main(String[] args) {
        Ping.execute();
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
        innerCatracaController = new EasyInnerCatracaController(new JFRMainCatraca());
        innerCatracaController.run();
    }

    public JFRMainCatraca() {
        conf_Cliente.loadJson();
        preloader = new Preloader();
        preloader.setAppTitle("Dispostivo - " + "Catraca");
        preloader.setAppStatus("Iniciando...");
        preloader.setShowIcon(true);
        preloader.setWaitingStarted(true);
        preloader.show();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                System.exit(0);
            }
        });
        preloader.reloadStatus("Verificando se computador é registrado...");

        JPanel panel = new JPanel(new GridLayout(0, 1));

        panel.add(lbl_relogio);
        panel.add(lbl_status);

        disparaRelogio();

        criar_SystemTray();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DAO dao = new DAO();
                String mac = Mac.getInstance();
                if (dao.isActive()) {
                    // dao.query_execute("DELETE FROM soc_catraca_monitora WHERE id_catraca IN(SELECT id FROM soc_catraca WHERE ds_mac = '" + mac + "')");
                    dao.query("SELECT func_catraca_monitora(false, null, '" + mac + "')");
                }
                try {
                    new DAO().getConnection().close();
                } catch (SQLException ex) {
                    Logger.getLogger(JFRMainCatraca.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(0);
            }
        });
        preloader.reloadStatus("Aplicação em execução na bandeja do windows.");
        lbl_status.setText("Projeto em Execução");
        preloader.hide();
        if (conf_Cliente.getSocket_catraca()) {
            if (conf_Cliente.getSocket_catraca_porta() == null) {
                System.err.println("Informar a porta do socket!");
            } else {
                biometria = new Thread(waitCodigoBiometria);
                biometria.start();
            }
        }
    }

    public void criar_SystemTray() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            String path = Path.getRealPath();
//            TrayIcon trayIcon = new TrayIcon(new ImageIcon(path + File.separator + "images" + File.separator + "catraca-icon.png").getImage(), "Catraca v5");
            TrayIcon trayIcon = new TrayIcon(new ImageIcon(Path.getRealPath() + File.separator + "src" + File.separator + "resources" + File.separator + "images" + File.separator + "frame_icon.png").getImage(), "Catraca v5");

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
                frame.setIconImage(new ImageIcon(Path.getRealPath() + File.separator + "src" + File.separator + "resources" + File.separator + "images" + File.separator + "frame_icon.png").getImage());
                frame.setTitle("Catraca v5");

                frame.setAutoRequestFocus(true);
                frame.setLayout(null);
                frame.setBounds(0, 0, 450, 260);
                frame.setResizable(false);

                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);

                JButton button_esconder = new JButton("Esconder");
                button_esconder.addActionListener(Action_Esconder(frame));
                button_esconder.setBounds(10, 100, 200, 30);

                JButton button_sair = new JButton("Sair da Aplicação");
                button_sair.addActionListener(Action_Sair());
                button_sair.setBounds(220, 100, 200, 30);

                setBtn_debug(new JButton("Depurar"));
                getBtn_debug().addActionListener(Action_Debug(frame));
                getBtn_debug().setBounds(10, 150, 80, 30);

                setBtn_cartao(new JButton("Cartão"));
                getBtn_cartao().addActionListener(Action_Cartao(frame));
                getBtn_cartao().setBounds(180, 150, 80, 30);
                getBtn_cartao().setEnabled(false);

                setBtn_codigo(new JButton("Código"));
                getBtn_codigo().addActionListener(Action_Codigo(frame));
                getBtn_codigo().setBounds(260, 150, 80, 30);

                setBtn_codigo_externo(new JButton("Externo"));
                getBtn_codigo_externo().addActionListener(Action_Codigo_Externo(frame));
                getBtn_codigo_externo().setBounds(340, 150, 80, 30);

                getLbl_relogio().setBounds(10, 10, 500, 50);
                getLbl_relogio().setFont(new Font(null, Font.PLAIN, 20));

                getLbl_status().setBounds(10, 50, 500, 50);
                getLbl_status().setFont(new Font(null, Font.PLAIN, 20));
                frame.add(button_esconder);
                frame.add(button_sair);
                frame.add(new JSeparator(SwingConstants.HORIZONTAL));
                frame.add(getBtn_debug());
                frame.add(getBtn_cartao());
                frame.add(getBtn_codigo());
                frame.add(getBtn_codigo_externo());
                frame.add(getLbl_relogio());
                frame.add(getLbl_status());

                addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        EnviaAtualizacao.inativar_todas_catracas();
                    }
                });

                getBtn_cartao().setEnabled(Debugs.ON);
                getBtn_codigo().setEnabled(Debugs.ON);
                getBtn_codigo_externo().setEnabled(Debugs.ON);

                frame.setVisible(true);
            }
        };

        return action_tray;
    }

    public ActionListener Action_Sair() {
        ActionListener action_sair = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DAO dao = new DAO();
                String mac = Mac.getInstance();
                if (dao.isActive()) {
                    dao.query("SELECT func_catraca_monitora(false, null, '" + mac + "')");
                }
                try {
                    new DAO().getConnection().close();
                } catch (SQLException ex) {
                    Logger.getLogger(JFRMainCatraca.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(0);
            }
        };
        return action_sair;
    }

    public ActionListener Action_Esconder(final JFrame frame) {
        ActionListener action_esconder = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        };
        return action_esconder;
    }

    public ActionListener Action_Debug(final JFrame frame) {
        ActionListener action_esconder = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                list_catraca = new ArrayList();
                Debugs.ON = !Debugs.ON;
                if (Debugs.ON) {
                    list_catraca = innerCatracaController.getLista_catraca();
                }
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        };
        return action_esconder;
    }

    public ActionListener Action_Cartao(final JFrame frame) {
        ActionListener action_esconder = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Debugs.ON) {
                    JOptionPane.showMessageDialog(null, "Habiltiar o modo de depuração - Cartão");
                    return;
                }
                String depto = "12";
                String catraca = "1";
                String servidor = "1";
                String catraca_id = "1";
                String deptos = "";
                Boolean leitorExterno = false;
                String ip = "";
                try {
                    depto = JOptionPane.showInputDialog(null, "Depto", depto);
                } catch (Exception e2) {

                }
                try {
                    catraca = JOptionPane.showInputDialog(null, "Catraca Inner", catraca);
                    for (int i = 0; i < list_catraca.size(); i++) {
                        if (list_catraca.get(i).getNumero() == Integer.parseInt(catraca) && list_catraca.get(i).getDepartamento() == Integer.parseInt(depto)) {
                            catraca_id = list_catraca.get(i).getId() + "";
                            servidor = list_catraca.get(i).getServidor() + "";
                            if (list_catraca.get(i).getLeitor_biometrico_externo()) {
                                leitorExterno = true;
                                ip = list_catraca.get(i).getIP();
                            }
                            break;
                        }
                    }
                } catch (Exception e2) {

                }
                String response = JOptionPane.showInputDialog(null, "Código de Barras do Cartão");
                Conf_Cliente cc = new Conf_Cliente();
                cc.loadJson();
                try {
                    RetornoJson json = EnviaAtualizacao.webservice(response, Integer.parseInt(depto), Integer.parseInt(catraca));
                    if (json != null) {
                        if (json.getLiberado()) {
                            if (json.getLiberado()) {
                                EnviaAtualizacao.atualiza_tela(cc.getPostgres_cliente(), Integer.parseInt(servidor), Integer.parseInt(catraca), Integer.parseInt(catraca_id), json, true);
                            }
                        }
                    }
                } catch (Exception e3) {

                }
            }
        };
//        ActionListener action_esconder = (ActionEvent e) -> {
//            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
//        };
        return action_esconder;
    }

    public ActionListener Action_Codigo(final JFrame frame) {
        ActionListener action_esconder = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Debugs.ON) {
                    JOptionPane.showMessageDialog(null, "Habiltiar o modo de depuração - Código");
                    return;
                }
                String depto = "12";
                String catraca = "1";
                String servidor = "1";
                String catraca_id = "1";
                String deptos = "";
                Boolean leitorExterno = false;
                String ip = "";
                try {
                    depto = JOptionPane.showInputDialog(null, "Depto", depto);
                } catch (Exception e2) {

                }
                try {
                    catraca = JOptionPane.showInputDialog(null, "Catraca Inner", catraca);
                    for (int i = 0; i < list_catraca.size(); i++) {
                        if (list_catraca.get(i).getNumero() == Integer.parseInt(catraca) && list_catraca.get(i).getDepartamento() == Integer.parseInt(depto)) {
                            catraca_id = list_catraca.get(i).getId() + "";
                            servidor = list_catraca.get(i).getServidor() + "";
                            if (list_catraca.get(i).getLeitor_biometrico_externo()) {
                                leitorExterno = true;
                                ip = list_catraca.get(i).getIP();
                            }
                            break;
                        }
                    }
                } catch (Exception e2) {

                }
                String response = JOptionPane.showInputDialog(null, "Código");
                Conf_Cliente cc = new Conf_Cliente();
                cc.loadJson();
                try {
                    RetornoJson json = EnviaAtualizacao.webservice(Integer.parseInt(response), Integer.parseInt(depto), Integer.parseInt(catraca));
                    if (json != null) {
                        if (json.getLiberado()) {
                            EnviaAtualizacao.atualiza_tela(cc.getPostgres_cliente(), Integer.parseInt(servidor), Integer.parseInt(catraca), Integer.parseInt(catraca_id), json, true);
                        }
                    }
                } catch (Exception e2) {

                }
            }
        };
//        ActionListener action_esconder = (ActionEvent e) -> {
//            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
//        };
        return action_esconder;
    }

    public ActionListener Action_Codigo_Externo(final JFrame frame) {
        ActionListener action_esconder = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Debugs.ON) {
                    JOptionPane.showMessageDialog(null, "Habiltiar o modo de depuração - Código");
                    return;
                }
                String depto = "12";
                String catraca = "1";
                String servidor = "1";
                String catraca_id = "1";
                String deptos = "";
                Boolean leitorExterno = false;
                String ip = "";
                try {
                    depto = JOptionPane.showInputDialog(null, "Depto", depto);
                } catch (Exception e2) {

                }
                try {
                    catraca = JOptionPane.showInputDialog(null, "Catraca Inner", catraca);
                    for (int i = 0; i < list_catraca.size(); i++) {
                        if (list_catraca.get(i).getNumero() == Integer.parseInt(catraca) && list_catraca.get(i).getDepartamento() == Integer.parseInt(depto)) {
                            catraca_id = list_catraca.get(i).getId() + "";
                            servidor = list_catraca.get(i).getServidor() + "";
                            if (list_catraca.get(i).getLeitor_biometrico_externo()) {
                                leitorExterno = true;
                                ip = list_catraca.get(i).getIP();
                            }
                            break;
                        }
                    }
                } catch (Exception e2) {

                }
                String response = JOptionPane.showInputDialog(null, "Código da Pessoa");
                Conf_Cliente cc = new Conf_Cliente();
                cc.loadJson();
                if (!leitorExterno) {
                    Debugs.breakPoint("NÃO HÁ BIOMETRIA EXTERNA CONFIGURADA VÍNCULADA A ESTA CATRACA!");
                    return;
                }
                try {
                    Debugs.breakPoint("TESTE DA BIOMETRIA DE LEITURA EXTERNA (BIOMETRIA -> biometria_catraca)");
                    ResultSet rs_insert = new DAO().query("SELECT func_biometria_catraca_insert('" + ip + "', " + Integer.parseInt(response) + ") AS retorno");
                    rs_insert.next();
                } catch (NumberFormatException | SQLException e2) {

                }
            }
        };
//        ActionListener action_esconder = (ActionEvent e) -> {
//            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
//        };
        return action_esconder;
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
        getLbl_relogio().setText(hora);
    }

    private final Runnable waitCodigoBiometria = new Runnable() {
        @Override
        public void run() {

            try {
                ServerSocket serverSocket = new ServerSocket(conf_Cliente.getSocket_catraca_porta());
                System.out.println("Server Started and listening to the port " + conf_Cliente.getSocket_catraca_porta());

                //Server is running always. This is done using this while(true) loop
                while (true) {
                    //Reading the message from the client
                    socket = serverSocket.accept();
                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String fromClient = br.readLine();
                    System.out.println("Message received from client is " + fromClient);
                    JSONObject jSONObject = new JSONObject(fromClient);
                    String ip = "";
                    String codigo = null;
                    try {
                        ip = jSONObject.getString("ds_ip");
                    } catch (Exception e) {

                    }
                    try {
                        codigo = jSONObject.getString("id_pessoa");
                    } catch (Exception e) {

                    }
                    if (codigo == null || codigo.equals("null") || codigo.equals("-1") || codigo.isEmpty()) {
                        Biometria.RECEBIDA.put(ip, null);
                    } else {
                        Biometria.RECEBIDA.put(ip, Integer.parseInt(codigo));
                    }

                    //Multiplying the number by 2 and forming the return message
                    String returnMessage;
                    try {
                        returnMessage = "200";
                    } catch (NumberFormatException e) {
                        //Input was not a number. Sending proper message back to client.
                        returnMessage = "200";
                    }

                    //Sending the response back to the client.
                    OutputStream os = socket.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    BufferedWriter bw = new BufferedWriter(osw);
                    bw.write(returnMessage);
                    System.out.println("Message sent to the client is " + returnMessage);
                    bw.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (Exception e) {
                }
            }
        }

    };

    public JLabel getLbl_relogio() {
        return lbl_relogio;
    }

    public void setLbl_relogio(JLabel lbl_relogio) {
        this.lbl_relogio = lbl_relogio;
    }

    public JLabel getLbl_status() {
        return lbl_status;
    }

    public void setLbl_status(JLabel lbl_status) {
        this.lbl_status = lbl_status;
    }

    public JButton getBtn_debug() {
        return btn_debug;
    }

    public void setBtn_debug(JButton btn_debug) {
        this.btn_debug = btn_debug;
    }

    public JButton getBtn_codigo() {
        return btn_codigo;
    }

    public void setBtn_codigo(JButton btn_codigo) {
        this.btn_codigo = btn_codigo;
    }

    public JButton getBtn_cartao() {
        return btn_cartao;
    }

    public void setBtn_cartao(JButton btn_cartao) {
        this.btn_cartao = btn_cartao;
    }

    public JButton getBtn_codigo_externo() {
        return btn_codigo_externo;
    }

    public void setBtn_codigo_externo(JButton btn_codigo_externo) {
        this.btn_codigo_externo = btn_codigo_externo;
    }

}
