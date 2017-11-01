package com.topdata.easyInner.ui;

import com.topdata.easyInner.controller.EasyInnerCatracaController;
import com.topdata.easyInner.utils.EnviaAtualizacao;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

public final class JFRMainCatraca_copia extends JFrame implements ActionListener, KeyListener {

    private static List<JFRMainCatraca_copia> listaForm = new ArrayList();
    private Timer timer = null;
    private ActionEvent ae = null;
    private JTextField txtLeitora = new JTextField();
    private String leitora = "";

    private JLabel lblRelogio = new JLabel();
    private JPanel colorPanel = new JPanel();
    private JLabel lblMensagem = new JLabel("");

    //private ImageIcon imageIcon;
    private JLabel lblNome = new JLabel("");

    private final JLabel labelCodigo = new JLabel("CÓDIGO");
    private JLabel lblCodigo = new JLabel("");

    private JLabel lblStatus = new JLabel("Carregando Projeto ...");
    private final JLabel labelNumero = new JLabel("N°");
    private JLabel lblNumero = new JLabel("");

    private JLabel lblImage = new JLabel();

    public JFRMainCatraca_copia(GraphicsConfiguration gc, int j) {
        setLayout(null);
        //setLayout(new FlowLayout());
        //setLayout(new GridLayout(2,6,2,2));
        setBounds(gc.getBounds());
        setTitle("R'Tools Desenvolvimentos - Catraca v4.4");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createForm();

        disparaRelogio();
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    public void createForm() {
        // LABEL MENSAGEM
        lblMensagem.setBounds(5, 20, 930, 60);
        lblMensagem.setFont(new Font("Lucida Sans", Font.BOLD, 35));
        //lblMensagem.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        add(lblMensagem);

        // LABEL RELOGIO
        lblRelogio.setBounds(960, 20, 320, 60);
        lblRelogio.setFont(new Font("Lucida Sans", Font.BOLD, 70));
        //lblRelogio.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        add(lblRelogio);

        // IMAGEM DA PESSOA
        //imageIcon = new ImageIcon("C:/Catraca/Fotos/russel.jpg");
        ImageIcon imageIcon = new ImageIcon("C:/Catraca/Fotos/sem_imagem.png");
        lblImage = new JLabel(imageIcon);
        lblImage.setBounds(5, 100, 930, 550);
        //lblImage.setHorizontalAlignment(SwingConstants.LEFT); // ALINHAR IMAGEM A ESQUERDA DA LABEL
        lblImage.setBorder(BorderFactory.createLineBorder(Color.WHITE, 10));
        add(lblImage);

        // NOME DA PESSOA
        lblNome.setBounds(5, 710, 930, 60);
        lblNome.setFont(new Font("Lucida Sans", Font.PLAIN, 60));
        //lblNome.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        add(lblNome);

        // LABEL CODIGO
        labelCodigo.setBounds(5, 650, 210, 60);
        labelCodigo.setFont(new Font("Lucida Sans", Font.PLAIN, 50));
        labelCodigo.setOpaque(true);
        labelCodigo.setBackground(Color.DARK_GRAY);
        labelCodigo.setForeground(Color.WHITE);
        //labelCodigo.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        add(labelCodigo);

        // CODIGO DA PESSOA
        lblCodigo.setBounds(220, 650, 450, 60);
        lblCodigo.setFont(new Font("Lucida Sans", Font.BOLD, 50));
        //lblCodigo.setForeground(Color.GREEN);
        //lblCodigo.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        add(lblCodigo);

        // STATUS CATRACA
        lblStatus.setBounds(970, 650, 300, 60);
        lblStatus.setFont(new Font("Lucida Sans", Font.BOLD, 20));
        //lblStatus.setForeground(Color.WHITE);
        //lblStatus.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        add(lblStatus);

        // LABEL NUMERO CATRACA
        labelNumero.setBounds(970, 710, 70, 60);
        labelNumero.setFont(new Font("Lucida Sans", Font.BOLD, 50));
        //labelNumero.setForeground(Color.WHITE);
        //labelNumero.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        add(labelNumero);

        // NUMERO CATRACA
        lblNumero.setBounds(1050, 710, 100, 60);
        lblNumero.setFont(new Font("Lucida Sans", Font.BOLD, 50));
        //labelNumero.setForeground(Color.WHITE);
        //lblNumero.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        add(lblNumero);

        // PAINEL DE COR (AGUARDANDO/LIBERADO/BLOQUEADO)
        colorPanel.setBounds(960, 100, 400, 550);
        colorPanel.setBackground(Color.WHITE);
        add(colorPanel);

        // INPUT DE LEITURA DO TECLADO OU LEITORA
        txtLeitora.setBounds(640, 400, 0, 12);
        txtLeitora.addKeyListener(this);
        add(txtLeitora);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                EnviaAtualizacao.inativar_todas_catracas();
            }
        });
    }

    public static void main(String[] args) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();

        for (int j = 0; j < gs.length; j++) {
            GraphicsDevice gd = gs[j];
            GraphicsConfiguration[] gc = gd.getConfigurations();
            JFRMainCatraca_copia formTest = new JFRMainCatraca_copia(gc[0], j);

            listaForm.add(formTest);
        }

        //EasyInnerCatracaController innerCatracaController = new EasyInnerCatracaController(listaForm);
        EasyInnerCatracaController innerCatracaController = new EasyInnerCatracaController(null);

        innerCatracaController.run();

        // -- CRIAR DUAS TELAS PARA DUAS CATRACAS ------------------------------
        //NewInner criar = new NewInner();
        //criar.setNewForm(listaForm);
        //criar.run();
        // ---------------------------------------------------------------------
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

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        JComponent c = (JComponent) e.getSource();
        if (c == txtLeitora) {
            //int xxx = e.getKeyCode();
            if (e.getKeyCode() == 10) {
                if (!txtLeitora.getText().isEmpty()) {
                    setLeitora(getTxtLeitora().getText());
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public JTextField getTxtLeitora() {
        return txtLeitora;
    }

    public void setTxtLeitora(JTextField txtLeitora) {
        this.txtLeitora = txtLeitora;
    }

    public JLabel getLblRelogio() {
        return lblRelogio;
    }

    public void setLblRelogio(JLabel lblRelogio) {
        this.lblRelogio = lblRelogio;
    }

    public static List<JFRMainCatraca_copia> getListaForm() {
        return listaForm;
    }

    public static void setListaForm(List<JFRMainCatraca_copia> aListaForm) {
        listaForm = aListaForm;
    }

    public JPanel getColorPanel() {
        return colorPanel;
    }

    public void setColorPanel(JPanel colorPanel) {
        this.colorPanel = colorPanel;
    }

    public JLabel getLblMensagem() {
        return lblMensagem;
    }

    public void setLblMensagem(JLabel lblMensagem) {
        this.lblMensagem = lblMensagem;
    }

    public JLabel getLblNome() {
        return lblNome;
    }

    public void setLblNome(JLabel lblNome) {
        this.lblNome = lblNome;
    }

    public JLabel getLblCodigo() {
        return lblCodigo;
    }

    public void setLblCodigo(JLabel lblCodigo) {
        this.lblCodigo = lblCodigo;
    }

    public JLabel getLblStatus() {
        return lblStatus;
    }

    public void setLblStatus(JLabel lblStatus) {
        this.lblStatus = lblStatus;
    }

    public JLabel getLblNumero() {
        return lblNumero;
    }

    public void setLblNumero(JLabel lblNumero) {
        this.lblNumero = lblNumero;
    }

    public JLabel getLblImage() {
        return lblImage;
    }

    public void setLblImage(JLabel lblImage) {
        this.lblImage = lblImage;
    }

    public String getLeitora() {
        return leitora;
    }

    public void setLeitora(String leitora) {
        this.leitora = leitora;
    }
}
