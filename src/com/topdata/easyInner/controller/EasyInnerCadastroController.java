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
import com.topdata.EasyInner;
import com.topdata.easyInner.dao.Catraca;
import com.topdata.easyInner.dao.DAO;
import com.topdata.easyInner.utils.EasyInnerUtils;
import java.awt.HeadlessException;
import java.util.HashMap;
import javax.swing.JOptionPane;
import static com.topdata.easyInner.enumeradores.Enumeradores.*;
import com.topdata.easyInner.ui.JFRCadastro;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author juliano.ezequiel
 */
public class EasyInnerCadastroController {

    private final JFRCadastro uiCadastro;
    private final EasyInner easyInner;
    private final EasyInnerBioService bioService;
    private List<String> dispositivos = new ArrayList();
    
    private Boolean digital_capturada_1 = false;
    private Boolean digital_capturada_2 = false;
    
    private Catraca catraca = new Catraca();

    public EasyInnerCadastroController(JFRCadastro innerCadastroBio) {
        this.uiCadastro = innerCadastroBio;
        easyInner = new EasyInner();
        bioService = new EasyInnerBioService(easyInner);
    }

    /**
     * Verifica os hamsters Instalados
     */
    @SuppressWarnings({"unchecked", "unchecked"})
    public void carregarHamster() {
        //uiBio.jCboDispositivos.removeAllItems();
        dispositivos = bioService.getListaDispositivosHamster();
        if (dispositivos.isEmpty()) {
            JOptionPane.showMessageDialog(null, "NENHUM DISPOSITIVO ENCONTRADO"); //uiBio.jCboDispositivos.addItem("Sem Dispositivos");
        } else {
            //JOptionPane.showMessageDialog(null, "DISPOSITIVOS ENCONTRADOS");
        }
        
        digital_capturada_1 = false;
        digital_capturada_2 = false;
    }

    /**
     * realiza o cadastro das dua digitais na base de dados
     */
    public void cadastrarDigitalHamster() {
        StringBuilder digital1 = new StringBuilder();
        StringBuilder digital2 = new StringBuilder();
        
        try {
            if (!digital_capturada_1) {
                JOptionPane.showMessageDialog(null, "Coloque a PRIMEIRA Digital");
                digital1 = bioService.getDigitalHamster((short) 0);
                digital_capturada_1 = true;
            }

            if (!digital_capturada_2) {
                JOptionPane.showMessageDialog(null, "Coloque a SEGUNDA Digital");
                digital2 = bioService.getDigitalHamster((short) 0);
                digital_capturada_2 = true;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex);
            //limpar();
            cadastrarDigitalHamster();
            return;
        }

        if (!digital_capturada_1 || !digital_capturada_2) {
            JOptionPane.showMessageDialog(null, "ERRO AO CAPTURAR DIGITAIS!");
            limpar();
            return;
        }

        Integer codigo = Integer.valueOf(uiCadastro.textCodigo.getText());

        new DAO().query(
                "INSERT INTO pes_biometria (dt_lancamento, is_ativo, ds_biometria, ds_biometria2, id_pessoa, is_enviado) \n "
                + "VALUES (CURRENT_DATE, true, '" + digital1.toString() + "','" + digital2.toString() + "', " + codigo + ", false);"
        );

        JOptionPane.showMessageDialog(null, "DIGITAL CADASTRADA COM SUCESSO!");
        limpar();
    }

    /**
     * CONECTAR Rotina responsável por efetuar a conexão com o Inner
     *
     * @return
     */
    public boolean isConectado() {

        boolean isConectado = false;

        try {
            easyInner.FecharPortaComunicacao();
            easyInner.DefinirTipoConexao(2);
            isConectado = bioService.isConectado(
                    1, // NUMERO DA CATRACA
                    3570,
                    2
            );

            //Caso o retorno seja OK.. volta a função chamadora..
            if (isConectado) {
                //JOptionPane.showMessageDialog(null, "CONECTOU");
            } else {
                JOptionPane.showMessageDialog(null, "NÃO CONECTOU");
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
        } else {
            //Se erro de Configuração
            if (ret == 1) {
                JOptionPane.showMessageDialog(null, "Erro ao configurar o Inner!");
            } else {
                //Se erro de Configuração da Biometria
                JOptionPane.showMessageDialog(null, "Erro ao configurar o Inner bio!");
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
        easyInner.DefinirPadraoCartao(1); // 0 - TOP DATA, 1 - LIVRE
        easyInner.DefinirQuantidadeDigitosCartao(catraca.getQuantidade_digitos());
        easyInner.ConfigurarInnerOffLine();
        easyInner.HabilitarTeclado(1, 0);
        easyInner.DefinirQuantidadeDigitosCartao(catraca.getQuantidade_digitos());
        easyInner.ConfigurarTipoLeitor(0); // CÓDIGO DE BARRAS
        easyInner.ConfigurarLeitor1(3);
        easyInner.ConfigurarAcionamento1(1, 5);

        //easyInner.ConfigurarBioVariavel(uiBio.jChkHabilitaMaior10Digitos.isSelected() ? 1 : 0);
//
        HashMap<String, Object> infoInner = bioService.getInfoInner(
                1,
                3570,
                2
        );

        //Configura o tipo de registro que será associado a uma marcação, quando for
        //inserido o dedo no Inner bio sem que o usuário tenha definido se é uma entrada,
        //saída, função...
        if (catraca.getBiometrico()) {
            easyInner.DefinirFuncaoDefaultSensorBiometria(10);
        } else //Desativa
        {
            easyInner.DefinirFuncaoDefaultSensorBiometria(0);
        }

        Retorno = easyInner.EnviarConfiguracoes(1);

        return Retorno;
    }

    /**
     * MONTAR CONFIGURAÇÃO BIO Envia as configurações da Inner Bio
     *
     * @return
     */
    private int ConfigurarInnerBio() throws InterruptedException {

        int Ret = -1;

        //Verifica se estão checados validação e Identificação..
        int Identificacao = 1;
        int Verificacao = 0;

        if (Identificacao == 1 || Verificacao == 1) {
            //Envia comando de Configuração..
            Ret = easyInner.ConfigurarBio(
                    1, // NÚMERO CATRACA
                    Identificacao,
                    Verificacao
            );

            //Testa retorno do comando..
            if (Ret == RET_COMANDO_OK) {
                EasyInnerUtils.setarTimeoutBio();

                //Espera resposta do comando..
                do {
                    Ret = easyInner.ResultadoConfiguracaoBio(1, 0);
                    Thread.sleep(1);
                } while (EasyInnerUtils.isEsperaRespostaBio(Ret));
            }

            if (Ret != RET_COMANDO_OK) {
                return 2;
            }

        }
        return 0;

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

        String digital1 = "", digital2 = "";

        if (isConectado() == true) {
            JOptionPane.showMessageDialog(null, "Coloque a PRIMEIRA Digital");

            retorno = easyInner.SolicitarTemplateLeitor(1);
            Thread.sleep(50l);
            if (retorno == RET_COMANDO_OK) {
                do {
                    retorno = easyInner.ReceberTemplateLeitor(1, 0, Template);
                    Thread.sleep(100);
                } while (retorno != RET_COMANDO_OK && tentativas++ < 50);
                if (retorno == 0) {
                    StringBuffer msg = EasyInnerUtils.convertArrayByteToHex(Template);
//                    for (int i = 0; i < msg.length() / 100; i++) {
//                        msg.insert(100 * i, "\n");
//                    }
//                    JOptionPane.showMessageDialog(null, "Digital recebida : \r\n" + msg);
                    digital1 = msg.toString();
                }
            }

            JOptionPane.showMessageDialog(null, "Coloque a SEGUNDA Digital");
            retorno = easyInner.SolicitarTemplateLeitor(1);
            Thread.sleep(50l);
            if (retorno == RET_COMANDO_OK) {
                do {
                    retorno = easyInner.ReceberTemplateLeitor(1, 0, Template);
                    Thread.sleep(100);
                } while (retorno != RET_COMANDO_OK && tentativas++ < 50);
                if (retorno == 0) {
                    StringBuffer msg = EasyInnerUtils.convertArrayByteToHex(Template);
//                    for (int i = 0; i < msg.length() / 100; i++) {
//                        msg.insert(100 * i, "\n");
//                    }
//                    JOptionPane.showMessageDialog(null, "Digital recebida : \r\n" + msg);
                    digital2 = msg.toString();
                }
            }

            if (digital1.isEmpty() || digital2.isEmpty()) {
                JOptionPane.showMessageDialog(null, "ERRO AO CAPTURAR DIGITAIS!");
                limpar();
                return;
            }

            Integer codigo = Integer.valueOf(uiCadastro.textCodigo.getText());

            new DAO().query(
                    "INSERT INTO pes_biometria (dt_lancamento, is_ativo, ds_biometria, ds_biometria2, id_pessoa, is_enviado) \n "
                    + "VALUES (CURRENT_DATE, true, '" + digital1 + "','" + digital2 + "', " + codigo + ", false);"
            );

            JOptionPane.showMessageDialog(null, "DIGITAL CADASTRADA COM SUCESSO!");

        } else {
            JOptionPane.showMessageDialog(null, "Erro a solicitar digital !");
        }
        limpar();
    }

    public void limpar() {
        uiCadastro.jbSolicitarDigitais.setText("SOLICITAR DIGITAIS");
        uiCadastro.jbSolicitarDigitais.setEnabled(true);
        uiCadastro.textCodigo.setText("");
        uiCadastro.textCodigo.setEnabled(true);
    }
}
