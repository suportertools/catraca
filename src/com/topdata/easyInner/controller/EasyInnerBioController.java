//******************************************************************************
//A Topdata Sistemas de Automação Ltda não se responsabiliza por qualquer
//tipo de dano que este software possa causar, este exemplo deve ser utilizado
//apenas para demonstrar a comunicação com os equipamentos da linha
//inner e não deve ser alterado, por este motivo ele não deve ser incluso em
//suas aplicações comerciais.
//
//Exemplo Biometria
//Desenvolvido em Java.
//                                           Topdata Sistemas de Automação Ltda.
//******************************************************************************.
package com.topdata.easyInner.controller;

import com.topdata.easyInner.service.EasyInnerBioService;
import com.topdata.easyInner.ui.JIFEasyInnerBio;
import com.topdata.easyInner.dao.ArquivosDAO;
import com.topdata.EasyInner;
import com.topdata.easyInner.utils.EasyInnerUtils;
import java.awt.HeadlessException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import static com.topdata.easyInner.enumeradores.Enumeradores.*;

/**
 *
 * @author juliano.ezequiel
 */
public class EasyInnerBioController {

    private final JIFEasyInnerBio uiBio;
    private final EasyInner easyInner;
    private final EasyInnerBioService bioService;
    private final ArquivosDAO arquivosDAO;

    public EasyInnerBioController(JIFEasyInnerBio innerBio) {
        this.uiBio = innerBio;
        easyInner = new EasyInner();
        bioService = new EasyInnerBioService(easyInner);
        arquivosDAO = new ArquivosDAO();
    }

    /**
     * CONECTAR Rotina responsável por efetuar a conexão com o Inner
     *
     * @return
     */
    public boolean isConectado() {

        boolean isConectado = false;

        try {
            uiBio.LblStatus.setText("Conectando ao Inner...");
            uiBio.jTxaManutencao.setText("Conectando ao Inner...");
            easyInner.FecharPortaComunicacao();
            easyInner.DefinirTipoConexao(uiBio.jCboTipoConexao.getSelectedIndex());
            isConectado = bioService.isConectado(Integer.parseInt(uiBio.jTxtNumInner.getText()),
                    Integer.parseInt(uiBio.jTxtPorta.getText()),
                    uiBio.jCboTipoConexao.getSelectedIndex());

            //Caso o retorno seja OK.. volta a função chamadora..
            if (isConectado) {
                uiBio.LblStatus.setText("Conectou ao Inner!");
                uiBio.jTxaManutencao.setText("Conectou ao Inner!");
            } else {
                //Exibe mensagem de erro para o Usuário..
                uiBio.LblStatus.setText("Não conectou ao Inner!");
                uiBio.jTxaManutencao.setText("Não conectou ao Inner!");
            }

        } catch (NumberFormatException | InterruptedException | HeadlessException ex) {
            System.out.println(ex.getMessage());
        }
        return isConectado;
    }

    //<editor-fold defaultstate="collapsed" desc="Configurações Inner">
    /**
     * Envia as configurações para o Inner
     *
     * @throws InterruptedException
     */
    public void configurarInner() throws InterruptedException {
        int ret;

        //Obrigatório
        if (uiBio.jCboTipoLeitor.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Favor selecionar um tipo de leitor !");
            return;
        }

        //Mensagem Status
        uiBio.LblStatus.setText("Enviando Configuração...");

        //Chama rotina que realiza a conexão
        //   if (isConectado() == 0) {
        //****************************
        //Configurar INNER
        ret = MontarConfiguracao();

        //****************************
        //Configurar INNER BIO
        ret = ConfigurarInnerBio();

        //Se Configuração ok
        if (ret == 0) {
            JOptionPane.showMessageDialog(null, "Configuração enviada com sucesso!");
            uiBio.LblStatus.setText("Configuração OK..");
        } else {
            //Se erro de Configuração
            if (ret == 1) {
                JOptionPane.showMessageDialog(null, "Erro ao configurar o Inner!");
                uiBio.LblStatus.setText("Erro ao configurar inner..");
            } else {
                //Se erro de Configuração da Biometria
                JOptionPane.showMessageDialog(null, "Erro ao configurar o Inner bio!");
                uiBio.LblStatus.setText("Erro ao configurar inner bio..");
            }
        }
        //  }
        //Fecha Porta Comunicação
        easyInner.FecharPortaComunicacao();
    }

    /**
     * MONTAR CONFIGURAÇÃO Esta rotina monta o buffer para enviar a configuração
     * do Inner
     *
     * @return
     */
    private int MontarConfiguracao() throws InterruptedException {

        int Retorno = -1;

        //Definição da EasyInner
        easyInner.DefinirPadraoCartao(uiBio.jCboPadraoCartao.getSelectedIndex());
        easyInner.DefinirQuantidadeDigitosCartao(Integer.parseInt(uiBio.jTxtQtdeDigitos.getText()));
        easyInner.ConfigurarInnerOffLine();
        easyInner.HabilitarTeclado(1, 0);
        easyInner.DefinirQuantidadeDigitosCartao(Integer.parseInt(uiBio.jTxtQtdeDigitos.getText()));
        easyInner.ConfigurarTipoLeitor(uiBio.jCboTipoLeitor.getSelectedIndex());
        easyInner.ConfigurarLeitor1(3);
        easyInner.ConfigurarAcionamento1(1, 5);

        easyInner.ConfigurarBioVariavel(uiBio.jChkHabilitaMaior10Digitos.isSelected() ? 1 : 0);

        HashMap<String, Object> infoInner = bioService.getInfoInner(
                Integer.parseInt(uiBio.jTxtNumInner.getText()),
                Integer.parseInt(uiBio.jTxtPorta.getText()),
                uiBio.jCboTipoConexao.getSelectedIndex());

        //Configura o tipo de registro que será associado a uma marcação, quando for
        //inserido o dedo no Inner bio sem que o usuário tenha definido se é uma entrada,
        //saída, função...
        if (infoInner.get("LinhaInner").toString().equals("Inner Bio")
                || (infoInner.get("LinhaInner").toString().equals("Inner Acesso"))) //Entrada
        {
            easyInner.DefinirFuncaoDefaultSensorBiometria(10);
        } else //Desativa
        {
            easyInner.DefinirFuncaoDefaultSensorBiometria(0);
        }

        Retorno = easyInner.EnviarConfiguracoes(Integer.parseInt(uiBio.jTxtNumInner.getText()));

        return Retorno;
    }

    /**
     * MONTAR CONFIGURAÇÃO BIO Envia as configurações da Inner Bio
     *
     * @return
     */
    private int ConfigurarInnerBio() throws InterruptedException {

        int Ret = -1;

        //Mensagem Status
        uiBio.LblStatus.setText("Enviando Configuração Bio...");

        //Verifica se estão checados validação e Identificação..
        int Identificacao = (uiBio.chkHabIdentificacao.isSelected() ? 1 : 0);
        int Verificacao = (uiBio.chkHabVerificacao.isSelected() ? 1 : 0);

        if (Identificacao == 1 || Verificacao == 1) {
            //Envia comando de Configuração..
            Ret = easyInner.ConfigurarBio(Integer.parseInt(uiBio.jTxtNumInner.getText()), Identificacao, Verificacao);

            //Testa retorno do comando..
            if (Ret == RET_COMANDO_OK) {
                EasyInnerUtils.setarTimeoutBio();

                //Espera resposta do comando..
                do {
                    Ret = easyInner.ResultadoConfiguracaoBio(Integer.parseInt(uiBio.jTxtNumInner.getText()), 0);
                    Thread.sleep(1);
                } while (EasyInnerUtils.isEsperaRespostaBio(Ret));
            }

            if (Ret != RET_COMANDO_OK) {
                return 2;
            }

        }
        return 0;

    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Envio de Usuario para o Inner">
    /**
     * Enviar o usuário selecionado do PC para o Inner Bio Envia um template com
     * duas digitais de cada usuário para o Inner Bio cadastrar no seu banco de
     * dados.
     *
     *
     * @throws java.lang.InterruptedException
     */
    public void EnviarUsuarioSelecionado() throws InterruptedException {

        if (uiBio.jTblUsuariosBase.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Selecione um usuário para o envio !");
            return;
        }

        //Mensagem de Status
        uiBio.jTxaManutencao.setText("Iniciando comunicação...");

        try {
            uiBio.jTxaManutencao.setText("Verificando tipo da placa FIM...");

            HashMap<String, Object> InfoInner = bioService.getInfoInner(
                    Integer.parseInt(uiBio.jTxtNumInner.getText()),
                    Integer.parseInt(uiBio.jTxtPorta.getText()),
                    uiBio.jCboTipoConexao.getSelectedIndex()
            );

            HashMap<String, Object> dadosEnvio = new HashMap<>();

            dadosEnvio.put("Cartao", uiBio.jTblUsuariosBase.getValueAt(uiBio.jTblUsuariosBase.getSelectedRow(), 0).toString());
            dadosEnvio.put("Digital1", uiBio.jTblUsuariosBase.getValueAt(uiBio.jTblUsuariosBase.getSelectedRow(), 2).toString());
            dadosEnvio.put("Digital2", uiBio.jTblUsuariosBase.getValueAt(uiBio.jTblUsuariosBase.getSelectedRow(), 3).toString());
            dadosEnvio.put("numInner", uiBio.jTxtNumInner.getText());

            if (Integer.parseInt(InfoInner.get("VersaoAlta").toString()) < 5) {
                dadosEnvio.put("Ligth", InfoInner.get("Ligth"));
                bioService.enviarUsuarioBio(dadosEnvio);
            } else {
                if (uiBio.jRdbDigital2.isSelected()) {
                    dadosEnvio.put("Digital2", uiBio.jTblUsuariosBase.getValueAt(uiBio.jTblUsuariosBase.getSelectedRow(), 3).toString());
                } else {
                    dadosEnvio.put("Digital2", "");
                }
                atualizaContadoresEnvio(bioService.enviarUsuarioBioVariavel(dadosEnvio));
            }

            //Fecha porta de comunicação
            easyInner.FecharPortaComunicacao();
        } catch (InterruptedException ex) {
            Logger.getLogger(JIFEasyInnerBio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void EnviarTodosUsuario() throws InterruptedException {

        //Mensagem de Status
        uiBio.jTxaManutencao.setText("Iniciando comunicação...");

        uiBio.jPgrStatus.setMaximum(uiBio.jTblUsuariosBase.getRowCount());
        uiBio.jPgrStatus.setStringPainted(true);
        uiBio.jTxaManutencao.setText("Verificando tipo da placa FIM...");

        HashMap<String, Object> InfoInner = bioService.getInfoInner(
                Integer.parseInt(uiBio.jTxtNumInner.getText()),
                Integer.parseInt(uiBio.jTxtPorta.getText()),
                uiBio.jCboTipoConexao.getSelectedIndex());
        HashMap<String, Object> dadosEnvio = new HashMap<>();

        for (int i = 0; i < uiBio.jTblUsuariosBase.getRowCount(); i++) {
            dadosEnvio.put("Cartao", uiBio.jTblUsuariosBase.getValueAt(i, 0).toString());
            dadosEnvio.put("Digital1", uiBio.jTblUsuariosBase.getValueAt(i, 2).toString());
            dadosEnvio.put("numInner", uiBio.jTxtNumInner.getText());
            Thread.sleep(100);
            if (Integer.parseInt(InfoInner.get("VersaoAlta").toString()) < 5) {
                dadosEnvio.put("Ligth", InfoInner.get("Ligth"));
                atualizaContadoresEnvio(bioService.enviarUsuarioBio(dadosEnvio));
            } else {
                if (uiBio.jRdbDigital2.isSelected()) {
                    dadosEnvio.put("Digital2", uiBio.jTblUsuariosBase.getValueAt(i, 3).toString());
                } else {
                    dadosEnvio.put("Digital2", "");
                }
                atualizaContadoresEnvio(bioService.enviarUsuarioBioVariavel(dadosEnvio));
            }

            uiBio.jPgrStatus.setValue(i + 1);
        }
        easyInner.FecharPortaComunicacao();
        uiBio.jPgrStatus.setValue(0);

    }
//</editor-fold>

    /**
     * Carrega os dados dos arquivos TXT para o grid
     */
    public void carregaGrid() {

        try {
            DefaultTableModel Templates = (DefaultTableModel) uiBio.jTblUsuariosBase.getModel();
            DefaultTableModel SDigital = (DefaultTableModel) uiBio.jTblUsuarioSemBio.getModel();
            Templates.setNumRows(0);
            SDigital.setNumRows(0);

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();

            ArquivosDAO ArquivosDAO = new ArquivosDAO();

            //templates
            List<String> Templs = ArquivosDAO.LerArquivo(ArquivosDAO.Templates);
            for (String Templ : Templs) {
                String[] tp = Templ.split(";");
                Templates.addRow(new String[]{tp[0], tp[1], tp[2], tp[3], dateFormat.format(date)});
            }

            //usuários sem digital
            List<String> SD = ArquivosDAO.LerArquivo(ArquivosDAO.UsuariosSD);
            for (String SD1 : SD) {
                String[] d = SD1.split(";");
                if (d[1].equals("1")) {
                    SDigital.addRow(new String[]{d[0]});
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * exclui usuário selecionado no Jtable da base .
     */
    public void excluirUsuarioBase() {
        if (uiBio.jTblUsuariosBase.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Selecione um cartão para excluir da base.");
            return;
        }

        try {
            //Usuário encontrado
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null, "Deseja realmente apagar o cartão '"
                    + uiBio.jTblUsuariosBase.getValueAt(uiBio.jTblUsuariosBase.getSelectedRow(), 0)
                    + "'?", "EasyInnerBio", JOptionPane.YES_NO_OPTION)) {
                return;
            }

            String UsuarioExc = uiBio.jTblUsuariosBase.getValueAt(uiBio.jTblUsuariosBase.getSelectedRow(), 0).toString();
            List<String> Registros = arquivosDAO.LerArquivo(ArquivosDAO.Templates);
            if (arquivosDAO.RemoveRegistro(Registros, ArquivosDAO.Templates, UsuarioExc)) {
                carregaGrid();
            } else {
                JOptionPane.showMessageDialog(uiBio, "Não foi possível excluir o usuario !");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Exclui da placa FIM do Inner, o usuario selecionado no JTable.
     *
     * @throws InterruptedException
     */
    public void excluirUsuariosBio() throws InterruptedException {

        uiBio.jTxaManutencao.append("Excluindo usuario ");
        int numInner = Integer.parseInt(uiBio.jTxtNumInner.getText());
        HashMap<String, Object> infoInner = bioService.getInfoInner(
                numInner, Integer.parseInt(uiBio.jTxtPorta.getText()),
                uiBio.jCboTipoConexao.getSelectedIndex());
        Boolean ligth = (boolean) infoInner.get("Ligth");
        int versao = (int) infoInner.get("VersaoAlta");
        int ok = 0;
        int erro = 0;
        int[] selecionados = uiBio.jTUsuariosInner.getSelectedRows();
        for (int i = 0; i < selecionados.length; i++) {
            String usuario = uiBio.jTUsuariosInner.getValueAt(selecionados[i], 0).toString();

            int Retorno = bioService.excluirUsuarioBio(numInner, usuario, ligth, versao);

            ok += Retorno == RET_COMANDO_OK? ok: erro++;
            

            uiBio.jTxaManutencao.setText("");
            uiBio.jTxaManutencao.append("Excluidos :" + ok + "\r\n");
            uiBio.jTxaManutencao.append("Falha :" + erro);
        }

    }

    /**
     * Alualiza contadores do form
     *
     * @param dados
     */
    private void atualizaContadoresEnvio(HashMap<String, Object> dados) {
        uiBio.jTxaManutencao.setText("");
        uiBio.jTxaManutencao.append("ENVIADOS: " + dados.get("Enviado").toString() + "\r\n");
        uiBio.jTxaManutencao.append("JÁ CADASTRADOS: " + dados.get("JaCadastrado").toString() + "\r\n");
        uiBio.jTxaManutencao.append("FALHA ENVIO: " + dados.get("Erro").toString() + "\r\n");

    }

    /**
     * Solicita ao Inner os Usuários cadastrados na placa FIM
     *
     * @throws java.lang.InterruptedException
     */
    public void solicitarUsuariosPlacaFIM() throws InterruptedException, Exception {
        //solicita os dados do Inner
        try {
            HashMap<String, Object> InfoInner = bioService.getInfoInner(
                    Integer.parseInt(uiBio.jTxtNumInner.getText()),
                    Integer.parseInt(uiBio.jTxtPorta.getText()),
                    uiBio.jCboTipoConexao.getSelectedIndex());
            //para versões abaixo da 5.xx utiliza os métodos convencionais
            if (Integer.parseInt(InfoInner.get("VersaoAlta").toString()) < 5) {
                receberUsuario();
            } else {
                //para versões maiores que 5.xx utiliza-se os métodos mais atuais da EasyInner
                receberUsuarioBioVariavel();
            }
        } catch (NumberFormatException | InterruptedException ex) {
            throw new Exception(ex);
        }
    }

    /**
     * Para Inners com versão de firmware a partir de 5xx utiliza-se um comando
     * específico
     */
    private void receberUsuarioBioVariavel() throws InterruptedException {

        try {

            uiBio.jTxaManutencao.setText("Solicitando pacote... \r\n");
            int numInner = Integer.parseInt(uiBio.jTxtNumInner.getText());
            uiBio.jPgrStatus.setMaximum(bioService.getQuantidadeUsuarioPlacaFIM(numInner));
            uiBio.jPgrStatus.setStringPainted(true);

            List<StringBuffer> ListUsuarioFIM = bioService.getListUsuarioBioVariavel(numInner, uiBio.jPgrStatus);

            DefaultTableModel dtm = (DefaultTableModel) uiBio.jTUsuariosInner.getModel();
            dtm.setNumRows(0);

            for (StringBuffer Usuario : ListUsuarioFIM) {
                dtm.addRow(new Object[]{Usuario});
            }

            uiBio.jTxaManutencao.setText("Total de usuários recebidos : " + dtm.getRowCount() + " \r\n");

            uiBio.jPgrStatus.setValue(0);
            easyInner.FecharPortaComunicacao();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(uiBio, ex);
        }

    }

    /**
     * Para manter a compatibilidade, utiliza-se este comando para Inners com
     * versão até 4.xx
     */
    private void receberUsuario() throws InterruptedException {
        try {

            uiBio.jTxaManutencao.setText("Solicitando pacote... \r\n");
            int numInner = Integer.parseInt(uiBio.jTxtNumInner.getText());
            uiBio.jPgrStatus.setMaximum(bioService.getQuantidadeUsuarioPlacaFIM(numInner));
            uiBio.jPgrStatus.setStringPainted(true);

            List<StringBuffer> ListUsuarioFIM = bioService.getListUsuarioBio(numInner, uiBio.jPgrStatus);

            DefaultTableModel dtm = (DefaultTableModel) uiBio.jTUsuariosInner.getModel();
            dtm.setNumRows(0);

            for (StringBuffer Usuario : ListUsuarioFIM) {
                dtm.addRow(new Object[]{Usuario});
            }

            uiBio.jTxaManutencao.setText("Total de usuários recebidos : " + dtm.getRowCount() + " \r\n");
            uiBio.jPgrStatus.setValue(0);
            easyInner.FecharPortaComunicacao();

        } catch (NumberFormatException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Retorna a quantida de usuarios existentes na placa FIM
     *
     * @return
     * @throws java.lang.InterruptedException
     */
    public Integer receberQuantidadeUsuariosBio() throws InterruptedException {

        if (isConectado() == true) {
            return bioService.getQuantidadeUsuarioPlacaFIM(Integer.parseInt(uiBio.jTxtNumInner.getText()));
        }
        return null;
    }

    /**
     * Retorna somente o modelo da placa FIM
     *
     * @return
     * @throws InterruptedException
     */
    public String solicitarModeloBio() throws InterruptedException {

        uiBio.jTxaManutencao.append("Solicitando Modelo bio...");
        HashMap<String, Object> InfoInner = bioService.getInfoInner(
                Integer.parseInt(uiBio.jTxtNumInner.getText()),
                Integer.parseInt(uiBio.jTxtPorta.getText()),
                uiBio.jCboTipoConexao.getSelectedIndex());
        return InfoInner.get("ModeloBioInner").toString();
    }

    /**
     * Retorna somente a Versão da Placa FIM
     *
     * @return
     * @throws InterruptedException
     */
    public String solicitarVersaoBio() throws InterruptedException {

        HashMap<String, Object> InfoInner = bioService.getInfoInner(
                Integer.parseInt(uiBio.jTxtNumInner.getText()),
                Integer.parseInt(uiBio.jTxtPorta.getText()), uiBio.jCboTipoConexao.getSelectedIndex());
        return InfoInner.get("VersaoBio").toString();
    }

    /**
     * Solicita para o Inner uma digital pelo leitor BiométricoS
     *
     * @throws InterruptedException
     */
    public void solicitarDigitalLeitorInner() throws InterruptedException {
        int retorno = -1;
        byte Template[] = new byte[404];
        int tentativas = 0;

        if (isConectado() == true) {

            retorno = easyInner.SolicitarTemplateLeitor(Integer.parseInt(uiBio.jTxtNumInner.getText()));
            Thread.sleep(50l);
            if (retorno == RET_COMANDO_OK) {
                do {
                    retorno = easyInner.ReceberTemplateLeitor(Integer.parseInt(uiBio.jTxtNumInner.getText()), 0, Template);
                    Thread.sleep(100);
                } while (retorno != RET_COMANDO_OK && tentativas++ < 50);
                if (retorno == 0) {
                    StringBuffer msg = EasyInnerUtils.convertArrayByteToHex(Template);
                    for (int i = 0; i < msg.length() / 100; i++) {
                        msg.insert(100 * i, "\n");
                    }
                    JOptionPane.showMessageDialog(uiBio, "Digital recebida : \r\n" + msg);
                }
            }
        } else {
            JOptionPane.showMessageDialog(uiBio, "Erro a solicitar digital !");
        }

    }

    /**
     * Emvia a lista de usuários que não será solicitado a digital, caso esteja
     * habilitado a verificação biométrica
     */
    public void enviarListaSemBio() {
        int Ret = -1;
        try {
            if (isConectado() == true) {
                int i = 0;
                List<String> usuarios = null;

                usuarios = arquivosDAO.LerArquivo(ArquivosDAO.UsuariosSD);
                while (i < usuarios.size()) {
                    easyInner.IncluirUsuarioSemDigitalBio(uiBio.jTblUsuarioSemBio.getModel().getValueAt(i, 0).toString());
                    uiBio.jTxaManutencao.setText(i + " usuários prontos para enviar.");
                    i++;
                }
                Ret = easyInner.EnviarListaUsuariosSemDigitalBio(Integer.parseInt(uiBio.jTxtNumInner.getText()));

                if (Ret == RET_COMANDO_OK) {
                    JOptionPane.showMessageDialog(null, "Lista de usuários sem digital enviada com sucesso!");
                }
            }
            easyInner.FecharPortaComunicacao();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Cadastra na placa FIM do Inner um usuario, passando como parametro o
     * cartão.
     */
    public void cadastrarPeloLeitorInner() {

        try {

            HashMap<String, Object> InfoInner = bioService.getInfoInner(
                    Integer.parseInt(uiBio.jTxtNumInner.getText()),
                    Integer.parseInt(uiBio.jTxtPorta.getText()),
                    uiBio.jCboTipoConexao.getSelectedIndex());
            //Define que o Inner utilizado no momento é um Inner bio light ao invés de
            //um Inner bio 1000/4000.
            boolean placalght = (boolean) InfoInner.get("Ligth");

            if (placalght) {
                easyInner.SetarBioLight(1);
            }

            String Usuario = uiBio.jTxtUsuario.getText();

            //Inserção da primeira digital
            JOptionPane.showMessageDialog(null, "Posicione a primeira digital");
            if (!bioService.setDigitalPlacaFIMInner(Integer.parseInt(uiBio.jTxtNumInner.getText()), Usuario, 1)) {
                JOptionPane.showMessageDialog(uiBio, "Erro ao capturar !");
                return;
            }
            Thread.sleep(20);

            //Inserção da segunda digital
            JOptionPane.showMessageDialog(null, "Posicione a segunda digital");
            if (!bioService.setDigitalPlacaFIMInner(Integer.parseInt(uiBio.jTxtNumInner.getText()), Usuario, 2)) {
                JOptionPane.showMessageDialog(uiBio, "Erro ao capturar !");
                return;
            }

            
            
            //Mensagem Status
            JOptionPane.showMessageDialog(null, "Usuário cadastrado!");
        } catch (InterruptedException ex) {
            Logger.getLogger(JIFEasyInnerBio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Verifica os hamsters Instalados
     */
    @SuppressWarnings({"unchecked", "unchecked"})
    public void carregarHamster() {
        uiBio.jCboDispositivos.removeAllItems();
        List<String> dispositivos = bioService.getListaDispositivosHamster();
        if (dispositivos.isEmpty()) {
            uiBio.jCboDispositivos.addItem("Sem Dispositivos");
        } else {
            uiBio.jCboDispositivos.addItem(dispositivos);
        }
    }

    /**
     * realiza o cadastro das dua digitais na base de dados
     */
    public void cadastrarDigitalHamster() {
        int escolha;
        StringBuilder Digital1 = new StringBuilder();
        StringBuilder Digital2 = new StringBuilder();
        StringBuilder TemplateFinal = new StringBuilder();
        try {
            List<String> ListaTemplatesBaseDados = arquivosDAO.LerArquivo(ArquivosDAO.Templates);

            if (!arquivosDAO.BuscarRegistro(uiBio.jTxtCartao.getText(), ListaTemplatesBaseDados)) {

                escolha = JOptionPane.showConfirmDialog(null, "Posicione a primira digital !", "Cadastro Hamster", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (escolha == JOptionPane.YES_OPTION) {
                    Digital1 = bioService.getDigitalHamster((short) uiBio.jCboDispositivos.getSelectedIndex());

                    escolha = JOptionPane.showConfirmDialog(null, "Posicione a segunda digital !", "Cadastro Hamster", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (escolha == JOptionPane.YES_OPTION) {
                        Digital2 = bioService.getDigitalHamster((short) uiBio.jCboDispositivos.getSelectedIndex());
                    }
                }
                if (!Digital1.toString().isEmpty() && (!Digital2.toString().isEmpty())) {
                    TemplateFinal.append(uiBio.jTxtCartao.getText()).append(";").append(uiBio.jTxtCartao.getText()).append(";").append(Digital1).append(";").append(Digital2);
                    arquivosDAO.AddRegistro(ArquivosDAO.Templates, TemplateFinal.toString());
                    carregaGrid();
                    uiBio.jTxtCartao.setText("");
                } else {
                    JOptionPane.showMessageDialog(uiBio, "Falha ao solicitar templates", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(uiBio, ex);
        }

    }

    /**
     * Solicita ao Inner as digitais dos usuarios e as grava na Base
     *
     * @throws Exception
     */
    public void gravarNaBase() throws Exception {

        List<String> ListaTemplatesRecebido = new ArrayList<>();
        List<String> ListaDeUsuariosSolicitacao = new ArrayList<>();
        List<String> ListaDeUsuarioErrosSolicitacao = new ArrayList<>();
        StringBuilder DadosRecebidos;
        Integer erro = 0;
        uiBio.jPgrStatus.setMaximum(uiBio.jTUsuariosInner.getRowCount());
        for (int i = 0; i < uiBio.jTUsuariosInner.getRowCount(); i++) {
            StringBuffer usr = (StringBuffer) uiBio.jTUsuariosInner.getValueAt(i, 0);
            if (!arquivosDAO.BuscarRegistro(usr.toString(), ListaDeUsuariosSolicitacao)) {
                ListaDeUsuariosSolicitacao.add(usr.toString());
            }
            uiBio.jPgrStatus.setValue(i);
        }

        //Se a lista de usuários estiver vazia finaliza
        if (ListaDeUsuariosSolicitacao.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Não existe usuário novos para gravar na base");
            return;
        }
        int numInner = Integer.parseInt(uiBio.jTxtNumInner.getText());
        //solicita informações do Inner
        HashMap<String, Object> InfoInner = bioService.getInfoInner(
                numInner, Integer.parseInt(uiBio.jTxtPorta.getText()),
                uiBio.jCboTipoConexao.getSelectedIndex());

        uiBio.jPgrStatus.setValue(0);
        uiBio.jPgrStatus.setMaximum(ListaDeUsuariosSolicitacao.size());
        uiBio.jPgrStatus.setStringPainted(true);

        int count = 0;
        //Thread.sleep(168);

        for (String UsuarioSolicitado : ListaDeUsuariosSolicitacao) {
            //Verifica a versão do Inner, caso seja menor que 5.xx solicita os usuarios utilizando 
            //os métodos compátivel com as versões anteriores 
            if ((Integer) InfoInner.get("VersaoAlta") < 5) {
                DadosRecebidos = bioService.getUsuarioBio(numInner, UsuarioSolicitado, (boolean) InfoInner.get("Ligth"));
                if (!(DadosRecebidos.toString().isEmpty())) {
                    ListaTemplatesRecebido.add(DadosRecebidos.toString());
                } else {
                    erro++;
                    ListaDeUsuarioErrosSolicitacao.add(UsuarioSolicitado);
                }
                //caso a versão seja maior que 5.xx utiliza os métodos mais atuais
            } else {
                DadosRecebidos = bioService.getUsuarioBioVariavel(numInner, UsuarioSolicitado);
                if (!(DadosRecebidos.toString().isEmpty())) {
                    ListaTemplatesRecebido.add(DadosRecebidos.toString());
                    count++;
                } else {
                    erro++;
                    ListaDeUsuarioErrosSolicitacao.add(UsuarioSolicitado);
                }
            }
        
            uiBio.jPgrStatus.setValue(count);
            uiBio.jTxaManutencao.setText("");
            uiBio.jTxaManutencao.append(count + " templates carregados. \n");
            uiBio.jTxaManutencao.append(erro + " Erros ao carregar templates.");
        }
        //caso tenha recebido os templates realiza o armazenamento 
        if (!ListaTemplatesRecebido.isEmpty()) {
            arquivosDAO.AddRegistro(ArquivosDAO.Templates, ListaTemplatesRecebido);
        } else {
            JOptionPane.showMessageDialog(uiBio, "Não existe templates para gravar !");
        }

        this.carregaGrid();

        easyInner.FecharPortaComunicacao();
        uiBio.jPgrStatus.setValue(0);
    }

}
