//******************************************************************************
//A Topdata Sistemas de Automação Ltda não se responsabiliza por qualquer
//tipo de dano que este software possa causar, este exemplo deve ser utilizado
//apenas para demonstrar a comunicação com os equipamentos da linha Inner.
//
//Exemplo Biometria
//Desenvolvido em Java.
//                                           Topdata Sistemas de Automação Ltda.
//******************************************************************************
package com.topdata.easyInner.ui;

import com.topdata.easyInner.controller.EasyInnerCadastroController;
import com.topdata.easyInner.dao.Catraca;
import com.topdata.easyInner.dao.DAO;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class JFRCadastro extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private final EasyInnerCadastroController cadastroController = new EasyInnerCadastroController(this);
    private final Boolean configurado = false;
    private final Catraca catraca = new Catraca();

    public JFRCadastro() {
        super.setExtendedState(JFrame.NORMAL);
        //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        //super.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        super.setLocationRelativeTo(null);
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jbSolicitarDigitais = new javax.swing.JButton();
        textCodigo = new javax.swing.JTextField();
        lblCodigo = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SDK EasyInner 2.09");

        jbSolicitarDigitais.setText("SOLICITAR DIGITAIS");
        jbSolicitarDigitais.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSolicitarDigitaisActionPerformed(evt);
            }
        });

        textCodigo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textCodigoKeyPressed(evt);
            }
        });

        lblCodigo.setText("CÓDIGO DA PESSOA");
        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jbSolicitarDigitais, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textCodigo)
                    .addComponent(lblCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(lblCodigo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jbSolicitarDigitais, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbSolicitarDigitaisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSolicitarDigitaisActionPerformed
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    jbSolicitarDigitais.setEnabled(false);
                    textCodigo.setEnabled(false);
                    
                    catraca.load_lista_catraca();
                    
                    if (!catraca.getBiometrico()) {
                        JOptionPane.showMessageDialog(rootPane, "CATRACA NÃO ESTA CONFIGURADA COM LEITOR BIOMÉTRICO");
                        limpar();
                        return;
                    }

                    if (textCodigo.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(rootPane, "DIGITE UM CÓDIGO PARA A PESSOA");
                        limpar();
                        return;
                    }

                    DAO dao = new DAO();
                    Integer codigo;
                    ResultSet rs;
                    try {
                        codigo = Integer.valueOf(textCodigo.getText());
                        rs = dao.query("SELECT id_pessoa FROM pes_biometria WHERE id_pessoa = " + codigo);
                        if (rs.next()) {
                            JOptionPane.showMessageDialog(null, "DIGITAIS JÁ EXISTEM!");
                            limpar();
                            return;
                        }
                    } catch (SQLException | HeadlessException ex) {
                        ex.getMessage();
                    }

                    try {
                        codigo = Integer.valueOf(textCodigo.getText());
                        rs = dao.query("SELECT ds_nome FROM pes_pessoa WHERE id = " + codigo);
                        if (rs.next()) {
                            String nome_usuario = rs.getString("ds_nome");
                            if (JOptionPane.showConfirmDialog(null, "Capturar digital de " + nome_usuario, "WARNING",
                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                                jbSolicitarDigitais.setText("CONFIGURANDO CATRACA");

                                if (catraca.getLeitor_biometrico_externo())
                                    cadastroController.carregarHamster();
                                else
                                    cadastroController.configurarInner();
                                
                                jbSolicitarDigitais.setText("CARREGANDO BIOMETRIA");
                                Thread.sleep(1000);

                                if (catraca.getLeitor_biometrico_externo())
                                    cadastroController.cadastrarDigitalHamster();
                                else
                                    cadastroController.solicitarDigitalLeitorInner();
                            } else {
                                limpar();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "CÓDIGO NÃO ENCONTRADO!");
                            limpar();
                        }
                    } catch (SQLException | HeadlessException ex) {
                        ex.getMessage();
                        JOptionPane.showMessageDialog(null, "CÓDIGO NÃO ENCONTRADO!");
                        limpar();
                    }

                } catch (HeadlessException | InterruptedException ex) {

                }
            }
        }).start();
    }//GEN-LAST:event_jbSolicitarDigitaisActionPerformed

    private void textCodigoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textCodigoKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            jbSolicitarDigitaisActionPerformed(null);
        }
    }//GEN-LAST:event_textCodigoKeyPressed

    private void limpar() {
        textCodigo.setText("");
        jbSolicitarDigitais.setEnabled(true);
        textCodigo.setEnabled(true);
    }

    private void Help() throws IOException {
        String dir = System.getProperty("user.dir");
        String ArquivoHelp = "SDK_EasyInner_-_Manual_de_Desenvolvimento.chm";
        Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + dir + "\\" + ArquivoHelp);

    }

    public static void main(String args[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JFRCadastro().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFrame jFrame1;
    public javax.swing.JButton jbSolicitarDigitais;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JMenuBar menuBar;
    public javax.swing.JTextField textCodigo;
    // End of variables declaration//GEN-END:variables

}
