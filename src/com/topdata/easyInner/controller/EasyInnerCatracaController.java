/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.topdata.easyInner.controller;

import com.topdata.easyInner.dao.Catraca;
import com.topdata.easyInner.dao.Conf_t;
import com.topdata.easyInner.dao.DAO;
import com.topdata.easyInner.entity.Inner;
import com.topdata.easyInner.enumeradores.Enumeradores;
import com.topdata.easyInner.ui.JFRMainCatraca;
import static com.topdata.easyInner.ui.JIFEasyInnerOnLine.TotalInners;
import static com.topdata.easyInner.ui.JIFEasyInnerOnLine.typInnersCadastrados;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.FutureTask;

public class EasyInnerCatracaController {

    private JFRMainCatraca form = null;
    //private DAOx dao = new DAOx();
    private final Catraca catraca = new Catraca();

    public EasyInnerCatracaController(JFRMainCatraca form) {
        this.form = form;
    }

    public void run() {
        // INICIA A CONFIGURAÇÃO E CONEXÃO
        try {
            if (catraca.getLista_catraca().isEmpty()) {
                return;
            }

            TimeZone tz = TimeZone.getTimeZone("America/Sao_Paulo");
            TimeZone.setDefault(tz);

            //COMEÇA COMUNICAÇÃO COM O BANCO POSTGRES 
            DAO dao = new DAO();

            while (!dao.getConectado()) {
                dao = new DAO();
                if (!dao.getConectado()) {
                    Thread.sleep(3000);
                }
            }

            //FINALIZA A COMUNICAÇÃO COM O BANCO
            iniciarMaquinaEstados();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Realiza a crição do array de equipamentos para serem controlados.
     *
     * @throws InterruptedException
     */
    private void iniciarMaquinaEstados() throws InterruptedException {
        DAO dao = new DAO();
        if (dao.getConectado()) {
            dao.query("DELETE FROM soc_catraca_monitora");
        }
        
        Conf_t conf_t = new Conf_t();
        conf_t.loadJson();

        //******************************************************
        //MAIS DE UM INNER
        //Define a quantidade de Inners que o sistema terá..
        TotalInners = catraca.getLista_catraca().size();

        //Atribui o vetor com os números dos Inners, sempre de 1 a N
        List<Thread> list_thread = new ArrayList();
        for (int i = 0; i < TotalInners; i++) {
            Catraca ct = catraca.getLista_catraca().get(i);
            ct.setCliente(conf_t.getPostgres_cliente());

            typInnersCadastrados[i] = new Inner();
            typInnersCadastrados[i].Numero = (int) ct.getNumero(); // N° SEMPRE COMEÇA PELO 1, EM SEQUENCIAL, OBRIGATORIAMENTE TEM QUE TER O MESMO NÚMERO QUE FOI CONFIGURADO NA CATRACA, SENÃO O SISTEMA NÃO FUNCIONA
            typInnersCadastrados[i].QtdDigitos = (int) ct.getQuantidade_digitos(); // QUANTIDADE DE DIGITOS PARA O CARTÃO
            typInnersCadastrados[i].Teclado = (boolean) true; // HABILITA TECLADO
            typInnersCadastrados[i].Lista = (boolean) false; // LISTA DE USUARIOS COM ACESSO OFFLINE
            typInnersCadastrados[i].ListaBio = (boolean) false; // LISTA DE USUARIOS COM BIOMETRIA COM ACESSO OFFLINE
            typInnersCadastrados[i].TipoLeitor = (int) 0; // 0 - CODIGO DE BARRAS, 1 - MAGNÉTICO, 2 - PROXIMIDADE ABATRACK/SMART CARD, 3 - PROXIMIDADE WIEGAND/SMART CARD, 4 - PROXIMIDADE SMART CARD SERIAL
            typInnersCadastrados[i].Identificacao = 1; // 0 - FALSE, 1 - TRUE | IDENTIFICA DIGITAL NO BANCO DE DADOS DA CATRACA
            typInnersCadastrados[i].Verificacao = 0; // 0 - FALSE, 1 - TRUE | SOLICITA CÓDIGO APÓS COLOCAR DIGITAL PARA VERIFICAÇÃO E VICE-VERSA
            typInnersCadastrados[i].DoisLeitores = (boolean) false; // DOIS LEITORES NA MESMA CATRACA
            typInnersCadastrados[i].Catraca = (boolean) true; // FALSE PARA O CASO conf_json.getTipo_giro_catraca() == 0
            typInnersCadastrados[i].Biometrico = (boolean) ct.getBiometrico();
            typInnersCadastrados[i].CntDoEvents = 0; // PADRÃO DOCUMENTAÇÃO SDK
            typInnersCadastrados[i].CountPingFail = 0; // PADRÃO DOCUMENTAÇÃO SDK
            typInnersCadastrados[i].CountTentativasEnvioComando = 0; // PADRÃO DOCUMENTAÇÃO SDK
            typInnersCadastrados[i].EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
            typInnersCadastrados[i].TempoInicialPingOnLine = (int) System.currentTimeMillis();
            typInnersCadastrados[i].EstadoTeclado = Enumeradores.EstadosTeclado.TECLADO_EM_BRANCO;
            typInnersCadastrados[i].ObjectCatraca = ct;
            
            dao = new DAO();
            if (dao.getConectado()) {
                dao.query("INSERT INTO soc_catraca_monitora (id_catraca, nr_ping, is_ativo) VALUES (" + ct.getId() + ", 0, false);");
            }

            EasyInnerCatracaControllerThread ei = new EasyInnerCatracaControllerThread(typInnersCadastrados[i]);

            FutureTask theTask = new FutureTask(() -> {
                try {
                    ei.run();
                } catch (RuntimeException e) {
                    e.getMessage();
                }
                if (Thread.interrupted()) {
                    System.err.println("Opa o projeto parou!!");
                }
            }, null);

            Thread.sleep(2000);
            
            Thread c = new Thread(theTask);
            c.start();
            list_thread.add(c);
        }
            
        Boolean thread_stop = false;
        while (!thread_stop){
            for (int i = 0; i < list_thread.size(); i++){
                Thread.sleep(5000);
                if (!list_thread.get(i).isAlive()){
                    list_thread.get(i).getStackTrace();
                    System.err.println("Opa, parou de funcionar!");
                    thread_stop = true;
                }
            }
        }
    }

}
