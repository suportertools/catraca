//******************************************************************************
//A Topdata Sistemas de Automação Ltda não se responsabiliza por qualquer
//tipo de dano que este software possa causar, este exemplo deve ser utilizado
//apenas para demonstrar a comunicação off-line com os equipamentos da linha
//inner.
//
//Exemplo Biometria
//Desenvolvido em Java.
//                                           Topdata Sistemas de Automação Ltda.
//******************************************************************************
package com.topdata.easyInner.service;

import com.topdata.easyInner.ui.JIFEasyInnerBio;
import com.topdata.EasyInner;
import com.nitgen.SDK.BSP.NBioBSPJNI;
import com.topdata.easyInner.utils.EasyInnerUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import static com.topdata.easyInner.enumeradores.Enumeradores.*;
import javax.swing.JProgressBar;

/**
 *
 * @author juliano.ezequiel
 */
public class EasyInnerBioService {

    public EasyInnerBioService(EasyInner easyInner) {
        this.easyinner = easyInner;

    }

    private final EasyInner easyinner;
    private Integer iJaCadast = 0;
    private Integer iOk = 0;
    private Integer iFalha = 0;
    private Integer iFalhaEnvio = 0;
    private Integer iTplInvalido = 0;

    private NBioBSPJNI bsp = new NBioBSPJNI();

    /**
     * Envia para o Inner versão firmware minína 5.xx os usuarios com digitais
     *
     * @param objEnvio contém os dados com configurações e informções do usuário
     * que serão enviados para o Inner
     * @return
     */
    public HashMap<String, Object> enviarUsuarioBioVariavel(HashMap<String, Object> objEnvio) throws InterruptedException {

        int Retorno;
        int index1;
        int index2 = 0;
        int tentativas = 0;

        byte[] bDigital1 = new byte[404];
        byte[] bDigital2 = new byte[404];

        String Cartao = (String) objEnvio.get("Cartao");
        String Digital1 = (String) objEnvio.get("Digital1");
        String Digital2 = (String) objEnvio.get("Digital2");
        int numInner = Integer.parseInt(objEnvio.get("numInner").toString());
        HashMap<String, Object> contadores = null;

        //Template
        for (index1 = 0; index1 < 807; index1 += 2) {
            bDigital1[index2] = (byte) Long.parseLong(Digital1.substring(index1, index1 + 2), 16);
            index2++;
        }
        Thread.sleep(50);
        if (Digital2.isEmpty()) {
            Retorno = easyinner.EnviarDigitalUsuario(numInner, Cartao, bDigital1, null);

        } else {
            index2 = 0;
            for (index1 = 0; index1 < 807; index1 += 2) {
                bDigital2[index2] = (byte) Long.parseLong(Digital2.substring(index1, index1 + 2), 16);
                index2++;
            }

            Retorno = easyinner.EnviarDigitalUsuario(numInner, Cartao, bDigital1, bDigital2);

        }

        if (Retorno == RET_COMANDO_OK) {
            do {
                Thread.sleep(40);
                Retorno = easyinner.UsuarioFoiEnviado(numInner, 0);

                contadores = retornosBio(Retorno);

            } while ((Retorno == RET_BIO_PROCESSANDO && tentativas++ < 20));
        }

        return contadores;
    }

    /**
     * Envia para o Inner versão firmware até 4.xx usuarios com digitais
     *
     * @param objEnvio contém os dados com configurações e informções do usuário
     * que serão enviados para o Inner
     * @return
     * @throws java.lang.InterruptedException
     */
    public HashMap<String, Object> enviarUsuarioBio(HashMap<String, Object> objEnvio) throws InterruptedException {

        HashMap<String, Object> contadores = null;

        String Cartao = (String) objEnvio.get("Cartao");
        String Digital1 = (String) objEnvio.get("Digital1");
        String Digital2 = (String) objEnvio.get("Digital2");
        int numInner = Integer.parseInt(objEnvio.get("numInner").toString());
        boolean placaLight = (boolean) objEnvio.get("Ligth");

        byte[] templateFinal;

        if (placaLight) {
            easyinner.SetarBioLight(1);
            templateFinal = template400(Cartao, Digital1, Digital2);
        } else {
            templateFinal = template404(Cartao, Digital1, Digital2);
        }
        //Sleep para aguardar InnerTCPLib.dll realizar processamento do comando anterior.
        Thread.sleep(100);
        int Retorno = easyinner.EnviarUsuarioBio(numInner, templateFinal);

        int tentativas = 0;
        if (Retorno == RET_COMANDO_OK) {
            do {
                Thread.sleep(20);

                Retorno = easyinner.UsuarioFoiEnviado(numInner, 0);

                contadores = retornosBio(Retorno);

            } while ((Retorno == RET_BIO_PROCESSANDO && tentativas++ < 20));

        }
        return contadores;
    }

    /**
     *
     * @param Ret
     * @return
     */
    private HashMap<String, Object> retornosBio(int Ret) {
        HashMap<String, Object> hashMap = new HashMap<>();
        switch (Ret) {
            case RET_COMANDO_OK:
                iOk++;
                break;
            case RET_BIO_USR_JA_CADASTRADO:
                iJaCadast++;
                break;
            case RET_BIO_TEMPLATE_INVALIDO:
            case RET_BIO_PARAMETRO_INVALIDO:
                iTplInvalido++;
                iFalhaEnvio++;
                break;
            case RET_BIO_FALHA_COMUNICACAO:
                iFalha++;
                break;
            case RET_COMANDO_ERRO:
                iFalhaEnvio++;
                break;
        }

        hashMap.put("Enviado", iOk);
        hashMap.put("JaCadastrado", iJaCadast);
        hashMap.put("Erro", iTplInvalido + iFalha + iFalhaEnvio);

        return hashMap;
    }

    /**
     * Monta o buffer de envio para Inner
     *
     * @param Cartao
     * @param Digital1
     * @param Digital2
     * @return
     */
    private byte[] template404(String Cartao, String Digital1, String Digital2) {

        int i = 0;
        int k = 0;
        int j = 0;
        String aux = null;
        byte[] templateFinal = new byte[844];
        byte[] templateTemp = new byte[404];
        //master
        templateFinal[0] = 0;

        //usuario
        k = 0;
        //Template A
        i = 1;

        if (Cartao.length() > 10) {
            Cartao = Cartao.substring(Cartao.length() - 10, Cartao.length());
        }
        Cartao = EasyInnerUtils.completaString(Cartao, 10, "0");

        for (j = 0; j < Cartao.length(); j++) {
            templateFinal[i] = (byte) (Long.parseLong(Cartao.substring(j, j + 1)) + 48);
            i++;
        }

        i = 28;
        k = 0;

        for (j = 0; j < 807; j += 2) {
            templateTemp[k] = (byte) Long.parseLong(Digital1.substring(j, j + 2), 16);
            k++;
        }

        for (j = 0; j <= 403; j++) {
            aux = Integer.toString(templateTemp[j] < 0 ? (templateTemp[j] + 256) : templateTemp[j]);
            templateFinal[i] = (byte) Integer.parseInt(aux);
            i++;
        }

        //Template B
        i = 432;
        k = 0;

        for (j = 0; j <= 403; j++) {
            templateTemp[j] = 0;
        }

        aux = "";
        for (j = 0; j <= 807; j += 2) {
            templateTemp[k] = (byte) Integer.parseInt(Digital2.substring(j, j + 2), 16);
            k++;
        }

        for (j = 0; j <= 403; j++) {
            aux = Integer.toString(templateTemp[j] < 0 ? (templateTemp[j] + 256) : templateTemp[j]);
            templateFinal[i] = (byte) Integer.parseInt(aux);
            i++;
        }

        HashMap<String, Object> dataCad = dataCadastro();
        templateFinal[836] = (byte) Integer.parseInt(dataCad.get("yyyy").toString().substring(0, 2));
        templateFinal[837] = (byte) ((int) dataCad.get("yyyy") % 100);
        templateFinal[838] = (byte) (int) dataCad.get("MM");
        templateFinal[839] = (byte) (int) dataCad.get("dd");
        templateFinal[840] = (byte) (int) dataCad.get("HH");
        templateFinal[841] = (byte) (int) dataCad.get("mm");
        templateFinal[842] = (byte) (int) dataCad.get("ss");
        templateFinal[843] = 0;

        return templateFinal;

    }

    /**
     * Monta o buffer de envio para o Inner com placa Light
     *
     * @param Cartao
     * @param Digital1
     * @param Digital2
     * @return
     */
    private byte[] template400(String Cartao, String Digital1, String Digital2) {
        int i = 0;
        int k = 0;
        int j = 0;

        byte[] templateFinal = new byte[844];
        byte[] templateTemp = new byte[404];

        bsp = new NBioBSPJNI();
        NBioBSPJNI.Export exportEngine = bsp.new Export();
        NBioBSPJNI.FIR_HANDLE hSavedFIR = bsp.new FIR_HANDLE();
        NBioBSPJNI.Export.DATA exportData = exportEngine.new DATA();
        NBioBSPJNI.FIR fullSavedFIR = bsp.new FIR();
        NBioBSPJNI.INPUT_FIR inputFIR = bsp.new INPUT_FIR();

        System.out.println(Cartao);

        System.out.println("new bsp");
        //master
        templateFinal[0] = 0;

        //usuario
        k = 0;
        //Template A
        i = 1;
        //placa light
        if (Cartao.length() > 8) {
            Cartao = Cartao.substring(Cartao.length() - 8, Cartao.length());
        }
        Cartao = EasyInnerUtils.completaString(Cartao, 8, "0");

        for (j = 0; j <= Cartao.length() - 1; j++) {
            templateFinal[i] = (byte) (Long.parseLong(Cartao.substring(j, j + 1)) + 48);
            i++;
        }

        i = 27;
        k = 0;

        for (j = 0; j <= 807; j++) {
            if (j % 2 == 0) {
                templateTemp[k] = (byte) Integer.parseInt(Digital1.substring(j, j + 2), 16);
                k++;
            }
        }

        exportEngine.ImportFIR(templateTemp, 404, 7, 1, hSavedFIR);// ImportFIR(byte[] Template, int nLen, int type, FIR_HANDLE hFIR)
        System.out.println("exportEngine.ImportFIR Template1");
        bsp.GetFIRFromHandle(hSavedFIR, fullSavedFIR);
        System.out.println("bsp.GetFIRFromHandle Template1");
        inputFIR.SetFullFIR(fullSavedFIR);
        System.out.println("inputFIR.SetFullFIR Template1");
        exportEngine.ExportFIR(inputFIR, exportData, NBioBSPJNI.EXPORT_MINCONV_TYPE.FIM10_LV);
        System.out.println("exportEngine.ExportFIR Template1");

        byte[] bDigital1;
        bDigital1 = exportData.FingerData[0].Template[0].Data;
        System.out.println("exportData.FingerData[0].Template[0].Data Template1");

        for (j = 0; j <= 399; j++) {
            //str = Integer.toString(exportData.FingerData[0].Template[0].Data[j] < 0 ? (exportData.FingerData[0].Template[0].Data[j]+ 256) : exportData.FingerData[0].Template[0].Data[j]);
            //Integer aux = (bDigital1[j] < 0 ? (bDigital1[j] + 256) : bDigital1[j]);
            templateFinal[i] = bDigital1[j];
            i++;
        }

        //Template B
        i = 427;
        k = 0;

        for (j = 0; j <= templateTemp.length - 1; j++) {
            templateTemp[j] = 0;
        }

        for (j = 0; j <= 807; j++) {

            if (j % 2 == 0) {
                templateTemp[k] = (byte) Integer.parseInt(Digital2.substring(j, j + 2), 16);
                k++;
            }
        }

        exportEngine.ImportFIR(templateTemp, 404, 7, 1, hSavedFIR);// ImportFIR(byte[] Template, int nLen, int type, FIR_HANDLE hFIR)
        System.out.println("exportEngine.ImportFIR Template2");
        bsp.GetFIRFromHandle(hSavedFIR, fullSavedFIR);
        System.out.println("bsp.GetFIRFromHandle Template2");
        inputFIR.SetFullFIR(fullSavedFIR);
        System.out.println("inputFIR.SetFullFIR Template2");
        exportEngine.ExportFIR(inputFIR, exportData, NBioBSPJNI.EXPORT_MINCONV_TYPE.FIM10_LV);
        System.out.println("exportEngine.ExportFIR Template2");

        byte[] bDigital2;
        bDigital2 = exportData.FingerData[0].Template[0].Data;
        System.out.println("exportData.FingerData[0].Template[0].Data Template2");

        for (j = 0; j <= 399; j++) {
            //Integer aux = bDigital2[j] < 0 ? (bDigital2[j] + 256) : bDigital2[j];
            templateFinal[i] = bDigital2[j];
            i++;
        }
        System.out.println("Fim construção template400");
        bsp.dispose();
        return templateFinal;
    }

    /**
     * Monta um objeto com a data
     *
     * @return
     */
    private HashMap<String, Object> dataCadastro() {
        HashMap<String, Object> hashMapData = new HashMap<>();
        Date Data = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        hashMapData.put("yyyy", Integer.parseInt(formatter.format(Data)));

        formatter = new SimpleDateFormat("MM");
        hashMapData.put("MM", Integer.parseInt(formatter.format(Data)));

        formatter = new SimpleDateFormat("dd");
        hashMapData.put("dd", Integer.parseInt(formatter.format(Data)));

        formatter = new SimpleDateFormat("HH");
        hashMapData.put("HH", Integer.parseInt(formatter.format(Data)));

        formatter = new SimpleDateFormat("mm");
        hashMapData.put("mm", Integer.parseInt(formatter.format(Data)));

        formatter = new SimpleDateFormat("ss");
        hashMapData.put("ss", Integer.parseInt(formatter.format(Data)));

        return hashMapData;
    }

    /**
     * Exclui usuario da placa FIM do Inner.
     *
     * @param numInner
     * @param Usuario
     * @param placalight
     * @param versao
     * @return
     * @throws InterruptedException
     */
    public int excluirUsuarioBio(int numInner, String Usuario, boolean placalight, int versao) throws InterruptedException {

        int Retorno;
        int tentativas = 0;

        if (versao < 5) {
            if (placalight) {
                easyinner.SetarBioLight(1);
            }
        } //else {
        // easyinner.SetarBioVariavel(1);
        //}

        Retorno = easyinner.SolicitarExclusaoUsuario(numInner, EasyInnerUtils.remZeroEsquerda(Usuario));

        Thread.sleep(10);

        if (Retorno == RET_COMANDO_OK) {
            do {
                Retorno = easyinner.UsuarioFoiExcluido(numInner, 0);
                Thread.sleep(40);

            } while (Retorno == RET_BIO_PROCESSANDO && tentativas++ < 10);
        }

        return Retorno;
    }

    /**
     * Realiza a conexão com o Inner e recupera todos os dados do equipamento.
     *
     * @param numInner
     * @param porta
     * @param TipoCon
     * @return HachMap contendo as informações do Inner
     * @throws InterruptedException
     */
    public HashMap<String, Object> getInfoInner(int numInner, int porta, int TipoCon) throws InterruptedException {
        int Versao[] = new int[30];
        long Variacao = 0;
        int Modelo[] = new int[3];
        int Ret = -1;
        HashMap<String, Object> InfoInner = new HashMap<>();
        int tentativas = 0;
        //Solicita a versão do firmware do Inner e dados como o Idioma, se é
        //uma versão especial.

        if (isConectado(numInner, porta, TipoCon) == true) {
            do {
                Ret = easyinner.ReceberVersaoFirmware(numInner, Versao);

                Thread.sleep(40);
            } while (Ret != RET_COMANDO_OK && tentativas++ < 30);

            if (Ret == RET_COMANDO_OK) {
                //Define a linha do Inner
                switch (Versao[0]) {
                    case 1:
                        InfoInner.put("LinhaInner", "Inner Plus");
                        InfoInner.put("InnerAcesso", false);
                        break;

                    case 2:
                        InfoInner.put("LinhaInner", "Inner Disk");
                        InfoInner.put("InnerAcesso", false);
                        break;

                    case 3:
                        InfoInner.put("LinhaInner", "Inner Verid");
                        InfoInner.put("InnerAcesso", false);
                        break;

                    case 6:
                        InfoInner.put("LinhaInner", "Inner Bio");
                        InfoInner.put("InnerAcesso", false);
                        break;

                    case 7:
                        InfoInner.put("LinhaInner", "Inner NET");
                        InfoInner.put("InnerAcesso", false);
                        break;

                    case 14:
                        InfoInner.put("LinhaInner", "Inner Acesso");
                        InfoInner.put("InnerAcesso", true);
                        break;
                }
                InfoInner.put("InnerAcesso", Versao[6]);
                InfoInner.put("VariacaoInner", Variacao);
                InfoInner.put("VersaoAlta", Versao[3]);
                InfoInner.put("VersaoInner", Integer.toString(Versao[3])
                        + '.' + Integer.toString(Versao[4])
                        + '.' + Integer.toString(Versao[5]));
                //Se for biometria
                if ((Versao[0] == 6) || (Versao[0] == 14 && InfoInner.get("InnerAcesso").toString().equals("1"))) {
                    //Solicita o modelo do Inner bio.
                    easyinner.SolicitarModeloBio(numInner);
                    Thread.sleep(100);
                    tentativas = 0;
                    do {
                        //Retorna o resultado do comando SolicitarModeloBio, o modelo
                        //do Inner Bio é retornado por referência no parâmetro da função.
                        Ret = easyinner.ReceberModeloBio(numInner, 0, Modelo);
                        Thread.sleep(100);
                    } while (Ret != RET_COMANDO_OK && tentativas++ < 20);

                    if (Ret == RET_COMANDO_OK) {
                        //Define o modelo do Inner Bio
                        switch (Modelo[0]) {
                            case 1:
                                InfoInner.put("ModeloBioInner", "Light 100  FIM10");
                                InfoInner.put("Ligth", true);
                                break;
                            case 4:
                                InfoInner.put("ModeloBioInner", "1000/4000  FIM01");
                                InfoInner.put("Ligth", false);
                                break;
                            case 51:
                                InfoInner.put("ModeloBioInner", "1000/4000  FIM2030");
                                InfoInner.put("Ligth", false);
                                break;
                            case 52:
                                InfoInner.put("ModeloBioInner", "1000/4000  FIM2040");
                                InfoInner.put("Ligth", false);
                                break;
                            case 48:
                                InfoInner.put("ModeloBioInner", "Light 100  FIM3030");
                                InfoInner.put("Ligth", true);
                                break;
                            case 64:
                                InfoInner.put("ModeloBioInner", "Light 100  FIM3040");
                                InfoInner.put("Ligth", true);
                                break;
                            case 80:
                                InfoInner.put("ModeloBioInner", "1000/4000  FIM5060");
                                InfoInner.put("Ligth", false);
                                break;
                            case 82:
                                InfoInner.put("ModeloBioInner", "1000/4000  FIM5260");
                                InfoInner.put("Ligth", false);
                                break;
                            case 83:
                                InfoInner.put("ModeloBioInner", "Light 100  FIM5360");
                                InfoInner.put("Ligth", true);
                                break;
                            case 255:
                                InfoInner.put("ModeloBioInner", "Modelo do bio: Desconhecido");
                                InfoInner.put("Ligth", false);
                                break;
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Erro ao solicitar Modelo");
                        return null;
                    }
                    //Solicita a versão do Inner bio.
                    Ret = easyinner.SolicitarVersaoBio(numInner);
                    Thread.sleep(40);
                    tentativas = 0;
                    do {
                        //Retorna o resultado do comando SolicitarVersaoBio, a versão
                        //do Inner Bio é retornado por referência nos parâmetros da
                        //função.
                        Ret = easyinner.ReceberVersaoBio(numInner, 0, Versao);
                        Thread.sleep(100);

                    } while (Ret != RET_COMANDO_OK && tentativas++ < 30);
                    if (Ret == RET_COMANDO_OK) {
                        InfoInner.put("VersaoBio", Integer.toString(Versao[0])
                                + "." + Integer.toString(Versao[1]));
                    } else {
                        JOptionPane.showMessageDialog(null, "Erro ao solicitar Versão Bio");
                        return null;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Erro ao solicitar Versão Inner");
                return null;
            }
        }
        return InfoInner;
    }

    /**
     * Realiza a conexao com o Inner.
     *
     * @param numInner
     * @param porta
     * @param TipoCon
     * @return
     * @throws InterruptedException
     */
    public boolean isConectado(int numInner, int porta, int TipoCon) throws InterruptedException {

        long IniConexao;
        long tempo;
        int Retorno;

        easyinner.FecharPortaComunicacao();
        easyinner.DefinirTipoConexao(TipoCon);

        //Abre a porta de Conexão conforme a Porta Indicada..
        Retorno = easyinner.AbrirPortaComunicacao(porta);

        //Tenta Realizar a Conexão
        if (Retorno == RET_COMANDO_OK) {
            int[] DataHora = new int[6];
            IniConexao = System.currentTimeMillis();
            //Realiza loop enquanto o tempo fim for menor que o tempo atual, e o comando retornado diferente de OK.
            do {
                tempo = System.currentTimeMillis() - IniConexao;
                //verifica se a conexão foi bem sucedida.
                Retorno = easyinner.ReceberRelogio(numInner, DataHora);
                Thread.sleep(10l);
            } while (Retorno != RET_COMANDO_OK && tempo < 10000);
        }
        return Retorno == 0;

    }

    /**
     * O usuário será cadastrado no Inner bio com o número do cartão Retorna o
     * resultado
     *
     * @param numInner
     * @param Usr
     * @param numTpl
     * @return
     * @throws InterruptedException
     */
    public boolean setDigitalPlacaFIMInner(int numInner, String Usr, Integer numTpl) throws InterruptedException {
        boolean Retorno = false;
        int Ret = -1;

        //Solicita inserção
        Ret = easyinner.InserirUsuarioLeitorBio(numInner, numTpl, Usr);

        Thread.sleep(1000);

        EasyInnerUtils.setarTimeoutBio();

        do {
            Thread.sleep(20);
            //Retorna resultado inserção
            Ret = easyinner.ResultadoInsercaoUsuarioLeitorBio(numInner, 0);

        } while (EasyInnerUtils.isEsperaRespostaBio(Ret));

        if (numTpl == 0) {
            numTpl++;
        }

        if (Ret != RET_COMANDO_OK) {
            JOptionPane.showMessageDialog(null, "Erro ao inserir digital : Ret " + Ret);
        }

        return (Retorno);
    }

    /**
     * Carrega lista de dispositivos Hamster
     *
     * @return
     */
    public List<String> getListaDispositivosHamster() {

        bsp = new NBioBSPJNI();
        NBioBSPJNI.DEVICE_ENUM_INFO deviceEnumInfo;
        deviceEnumInfo = bsp.new DEVICE_ENUM_INFO();
        bsp.EnumerateDevice(deviceEnumInfo);
        List<String> Dispositivos = new ArrayList<>();
        //Combo Dispositivos
        for (int i = 0; i < deviceEnumInfo.DeviceCount; i++) {
            Dispositivos.add(deviceEnumInfo.DeviceInfo[i].Name);
        }
        bsp.CloseDevice();
        bsp.dispose();
        return Dispositivos;
    }

    /**
     * Realiza a captura da Digital via Hamster
     *
     * @param id
     * @return
     * @throws Exception
     */
    public StringBuilder getDigitalHamster(short id) throws Exception {

        bsp = new NBioBSPJNI();
        NBioBSPJNI.Export exportEngine = bsp.new Export();
        NBioBSPJNI.FIR_HANDLE hSavedFIR = bsp.new FIR_HANDLE();
        NBioBSPJNI.Export.DATA exportData = exportEngine.new DATA();
        NBioBSPJNI.WINDOW_OPTION winOption = bsp.new WINDOW_OPTION();
        NBioBSPJNI.INPUT_FIR inputFIR = bsp.new INPUT_FIR();
        StringBuilder digitalCapturada = new StringBuilder();

        try {

            bsp.OpenDevice();
            bsp.Capture(NBioBSPJNI.FIR_PURPOSE.VERIFY, hSavedFIR, 5000, null, winOption);
            bsp.CloseDevice();
            if (bsp.IsErrorOccured() == false) {
                inputFIR.SetFIRHandle(hSavedFIR);
                exportEngine.ExportFIR(inputFIR, exportData, NBioBSPJNI.EXPORT_MINCONV_TYPE.FIM01_HV);
                digitalCapturada.append(EasyInnerUtils.convertArrayByteToHex(exportData.FingerData[0].Template[0].Data));
            }
        } catch (Exception ex) {
            throw new Exception("Erro ao capturar digital !", ex);
        }
        System.out.println(bsp.GetErrorCode());
        return digitalCapturada;
    }

    /**
     * Solicita para o Inner o Usuário passado como parametro.
     *
     * @param numInner
     * @param cartao
     * @param placaLight
     * @return
     * @throws InterruptedException
     */
    public StringBuilder getUsuarioBio(Integer numInner, String cartao, boolean placaLight) throws InterruptedException {
        StringBuilder TemplatesRecebido = new StringBuilder();
        int tentativas = 0;
        int ret = -1;
        byte Template[] = new byte[843];
        StringBuffer Digital1 = new StringBuffer();
        StringBuffer Digital2 = new StringBuffer();
        int i = 0;
        int y = 0;
        int j = 0;
        byte[] DigitalTemp = new byte[404];

        bsp = new NBioBSPJNI();
        NBioBSPJNI.Export exportEngine = bsp.new Export();
        NBioBSPJNI.FIR_HANDLE hSavedFIR = bsp.new FIR_HANDLE();
        NBioBSPJNI.Export.DATA exportData = exportEngine.new DATA();
        NBioBSPJNI.FIR fullSavedFIR = bsp.new FIR();
        NBioBSPJNI.INPUT_FIR inputFIR = bsp.new INPUT_FIR();

        do {
            Thread.sleep(20);

            if (placaLight) {
                easyinner.SetarBioLight(1);
            }

            //Solicita os dados do usuário cadastrados no leitor
            ret = easyinner.SolicitarUsuarioCadastradoBio(numInner, cartao);

        } while (ret != RET_COMANDO_OK);

        do {
            if (ret == RET_COMANDO_OK) {
                Thread.sleep(20);

                //Recebe os dados do usuário cadastrados no leitor
                ret = easyinner.ReceberUsuarioCadastradoBio(numInner, 0, Template);

                if (ret == RET_COMANDO_OK) {

                    if (placaLight) {

                        j = 27;
                        for (y = 0; y <= 403; y++) {
                            DigitalTemp[y] = Template[j];
                            j++;
                        }

                        exportEngine.ImportFIR(DigitalTemp, 400, 6, 1, hSavedFIR);

                        bsp.GetFIRFromHandle(hSavedFIR, fullSavedFIR);

                        inputFIR.SetFullFIR(fullSavedFIR);

                        exportEngine.ExportFIR(inputFIR, exportData, NBioBSPJNI.EXPORT_MINCONV_TYPE.FIM01_HV);

                        Digital1.append(EasyInnerUtils.convertArrayByteToHex(exportData.FingerData[0].Template[0].Data));

                        for (y = 0; y <= 403; y++) {
                            DigitalTemp[y] = 0;
                        }

                        j = 427;
                        for (y = 0; y <= 403; y++) {
                            DigitalTemp[y] = Template[j];
                            j++;
                        }

                        exportEngine.ImportFIR(DigitalTemp, 400, 6, 1, hSavedFIR);

                        bsp.GetFIRFromHandle(hSavedFIR, fullSavedFIR);

                        inputFIR.SetFullFIR(fullSavedFIR);

                        exportEngine.ExportFIR(inputFIR, exportData, NBioBSPJNI.EXPORT_MINCONV_TYPE.FIM01_HV);

                        Digital2.append(EasyInnerUtils.convertArrayByteToHex(exportData.FingerData[0].Template[0].Data));

                    } else {

                        j = 28;
                        for (y = 0; y <= 403; y++) {
                            DigitalTemp[y] = Template[j];
                            j++;
                        }

                        Digital1 = EasyInnerUtils.convertArrayByteToHex(DigitalTemp);

                        for (y = 0; y <= 403; y++) {
                            DigitalTemp[y] = 0;
                        }

                        j = 432;
                        for (y = 0; y <= 403; y++) {
                            DigitalTemp[y] = Template[j];
                            j++;
                        }

                        Digital2 = EasyInnerUtils.convertArrayByteToHex(DigitalTemp);
                    }
                    TemplatesRecebido.append(cartao).append(";").append(cartao).append(";").append(Digital1).append(";").append(Digital2);
                }
            }
        } while ((ret == RET_BIO_PROCESSANDO) && (tentativas++ < 50));

        bsp.dispose();
        return TemplatesRecebido;
    }

    /**
     * Solicita para o Inner o Usuário passado como parametro.
     *
     * @param numInner
     * @param usuario
     * @return
     * @throws java.lang.InterruptedException
     */
    public StringBuilder getUsuarioBioVariavel(Integer numInner, String usuario) throws InterruptedException {

        int ret = -1;
        StringBuilder TemplatesRecebido = new StringBuilder();
        StringBuilder Digital1 = new StringBuilder();
        StringBuilder Digital2 = new StringBuilder();
        byte Template[] = null;
        int tentativas = 0;

        Thread.sleep(30);
        ret = easyinner.SolicitarDigitalUsuario(numInner, EasyInnerUtils.remZeroEsquerda(usuario));
        if (ret == RET_COMANDO_OK) {
            //System.out.println(ret);
            int[] TamResposta = new int[1];
            do {
                Thread.sleep(40);
                ret = easyinner.ReceberRespostaRequisicaoBio(numInner, TamResposta);
            } while ((ret == RET_BIO_PROCESSANDO) && (tentativas++ < 20));
            if (ret == RET_COMANDO_OK) {
                //Recebe os dados do usuário cadastrados no leitor
                Template = new byte[TamResposta[0]];
                ret = easyinner.ReceberDigitalUsuario(numInner, Template, TamResposta[0]);
            }

            if (ret == RET_COMANDO_OK) {
                byte[] DigitalTemp = new byte[404];

                int y = 0;
                for (int j = 68; j < 472; j++) {
                    DigitalTemp[y] = Template[j];
                    y++;
                }
                Digital1.append(EasyInnerUtils.convertArrayByteToHex(DigitalTemp));
                if (Template.length == 876) {
                    y = 0;
                    for (int j = 472; j < 876; j++) {
                        DigitalTemp[y] = Template[j];
                        y++;
                    }
                    Digital2.append(EasyInnerUtils.convertArrayByteToHex(DigitalTemp));
                } else {
                    Digital2.append(Digital1);
                }
                usuario = EasyInnerUtils.remZeroEsquerda(usuario);
                return TemplatesRecebido.append(usuario).append(";").append(usuario).append(";").append(Digital1).append(";").append(Digital2);
            }
        }
        return TemplatesRecebido;
    }

    /**
     * Solicita a quantidade de usuariona placa FIM
     *
     * @param numInner
     * @return
     */
    public int getQuantidadeUsuarioPlacaFIM(int numInner) {
        int[] Quantidade = new int[1];
        int Ret = 0;

        //if (isConectado() == RET_COMANDO_OK) {
        try {
            //Solicita a quantidade de usuários cadastrados no Inner Bio.
            Ret = easyinner.SolicitarQuantidadeUsuariosBio(numInner);

            Thread.sleep(50);

            if (Ret == RET_COMANDO_OK) {
                EasyInnerUtils.setarTimeoutBio();
                do {
                    //Retorna a quantidade de usuários cadastrados no Inner Bio
                    Ret = easyinner.ReceberQuantidadeUsuariosBio(numInner, 0, Quantidade);

                    if (Ret == RET_COMANDO_OK) {
                        return Quantidade[0];
                    }
                } while (EasyInnerUtils.isEsperaRespostaBio(Ret));
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(JIFEasyInnerBio.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Quantidade[0];
    }

    /**
     * Preenche uma lista com todos os usuários existentes na plava FIM do Inner
     * com versão de firmware superior a 5.xx
     *
     * @param numInner
     * @param jPgrStatus
     * @return
     * @throws java.lang.InterruptedException
     */
    public List<StringBuffer> getListUsuarioBioVariavel(int numInner, JProgressBar jPgrStatus) throws InterruptedException {
        List<StringBuffer> UsuariosPlacFIM = new ArrayList<>();
        int Retorno = -1;
        int nPacote = 0;
        int nUsuario = 0;

        easyinner.InicializarColetaListaUsuariosBio();

        do {
            do {
                //Solicita uma parte(pacote) da lista de usuarios do bio
                Retorno = easyinner.SolicitarListaUsuariosBioVariavel(numInner);

            } while (Retorno == RET_BIO_PROCESSANDO);

            if (Retorno == RET_COMANDO_OK) {
                //Recebe uma parte da lista com os usuarios
                do {
                    Thread.sleep(20);
                    Retorno = easyinner.SolicitarListaUsuariosComDigital(numInner);

                } while (Retorno == RET_BIO_PROCESSANDO);

                if (Retorno == 0) {
                    nPacote++;
                }

                //Verifica se existe um usuário
                while (easyinner.TemProximoUsuario() == 1) {
                    byte[] Usuario = new byte[8];

                    //Pede um usuario da lista
                    Retorno = easyinner.ReceberUsuarioComDigital(Usuario);

                    if (Retorno == RET_COMANDO_OK) {
                        nUsuario++;
                        jPgrStatus.setValue(nUsuario);
                        StringBuffer Usr = new StringBuffer();

                        for (byte b : Usuario) {
                            Usr.append(b <= 9 ? "0" + b : b);
                        }
                        UsuariosPlacFIM.add(Usr);
                    }
                }

            } else {
                JOptionPane.showMessageDialog(null, "Erro ao solicitar usuário cadastrado");
            }
            Thread.sleep(100);
        } while (easyinner.TemProximoPacote() == 1);

        return UsuariosPlacFIM;
    }

    /**
     * Preenche uma lista com todos os usuários existentes na plava FIM do Inner
     * com versão de firmware inferior a 5.xx
     *
     * @param numInner
     * @param jPgrStatus
     * @return
     * @throws java.lang.InterruptedException
     */
    public List<StringBuffer> getListUsuarioBio(int numInner, JProgressBar jPgrStatus) throws InterruptedException {
        List<StringBuffer> UsuariosPlacFIM = new ArrayList<>();
        int Retorno = -1;
        int nUsuario = 0;
        jPgrStatus.setStringPainted(true);

        //método necessário para a easyinner.dll inicar a coleta do usuários
        easyinner.InicializarColetaListaUsuariosBio();

        do {
            do {
                //Solicita uma parte(pacote) da lista de usuarios do bio
                Retorno = easyinner.SolicitarListaUsuariosBio(numInner);

            } while (Retorno == RET_BIO_PROCESSANDO);

            if (Retorno == RET_COMANDO_OK) {
                //Recebe uma parte da lista com os usuarios
                do {
                    Thread.sleep(5);
                    Retorno = easyinner.ReceberPacoteListaUsuariosBio(numInner);

                } while (Retorno == RET_BIO_PROCESSANDO);

                //Verifica se existe um usuário
                while (easyinner.TemProximoUsuario() == 1) {
                    StringBuffer Usuario = new StringBuffer();
                    //Pede um usuario da lista
                    Retorno = easyinner.ReceberUsuarioLista(numInner, Usuario);

                    if (Retorno == RET_COMANDO_OK) {
                        jPgrStatus.setValue(nUsuario++);
                        UsuariosPlacFIM.add(Usuario);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Erro ao solicitar usuário cadastrado");
            }

            Thread.sleep(100);
        } while (easyinner.TemProximoPacote() == 1);

        return UsuariosPlacFIM;
    }

}
