package com.topdata.easyInner.controller;

import com.topdata.EasyInner;
import com.topdata.easyInner.dao.Catraca;
import com.topdata.easyInner.dao.CatracaDao;
import com.topdata.easyInner.dao.Conf_Cliente;
import com.topdata.easyInner.dao.DAO;
import com.topdata.easyInner.entity.Inner;
import com.topdata.easyInner.enumeradores.Enumeradores;
import com.topdata.easyInner.ui.JFRMainCatraca;
import static com.topdata.easyInner.ui.JIFEasyInnerOnLine.TotalInners;
import static com.topdata.easyInner.ui.JIFEasyInnerOnLine.typInnersCadastrados;
import com.topdata.easyInner.utils.Logs;
import com.topdata.easyInner.utils.Mac;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.FutureTask;
import javax.swing.JOptionPane;

public class EasyInnerCatracaController extends DAO {

    private JFRMainCatraca form = null;
    //private DAOx dao = new DAOx();
    private List<Catraca> lista_catraca = new ArrayList();
    private final Conf_Cliente conf_cliente = new Conf_Cliente();

    private final List<Thread> list_thread = new ArrayList();

    public EasyInnerCatracaController(JFRMainCatraca form) {
        this.form = form;
        this.conf_cliente.loadJson();
    }

    public void run() {
        // INICIA A CONFIGURAÇÃO E CONEXÃO
        String mac = Mac.getInstance();

        if (isActive()) {
            // query_execute("DELETE FROM soc_catraca_monitora WHERE id_catraca IN (SELECT id FROM soc_catraca WHERE ds_mac = '" + mac + "')");
            query("SELECT func_catraca_monitora(false, null, '" + mac + "')");
        }

        try {
            //COMEÇA COMUNICAÇÃO COM O BANCO POSTGRES 

            while (!isActive()) {
                Thread.sleep(5000);
            }
            //FINALIZA A COMUNICAÇÃO COM O BANCO

            lista_catraca = new CatracaDao().listaCatraca();

            if (lista_catraca.isEmpty()) {
                JOptionPane.showMessageDialog(null, "LISTA DE CATRACA VAZIA!");
                return;
            }

            TimeZone tz = TimeZone.getTimeZone("America/Sao_Paulo");
            TimeZone.setDefault(tz);

            EasyInner easy_inner_thread = new EasyInner();

            easy_inner_thread.FecharPortaComunicacao();

            easy_inner_thread.DefinirTipoConexao(1);

            // POR SER TCP/IP ESSA CONFIGURAÇÃO É IGNORADA PARA AS DEMAIS PORTAS ex. 3570,3571,3572 ...
            // OU SEJA, É PRECISO ABRIR A PORTA UMA ÚNICA VEZ, QUE ELE ABRIRÁ AS DEMAIS.
            int ret = easy_inner_thread.AbrirPortaComunicacao(3570);

            iniciarMaquinaEstados(easy_inner_thread);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
            Logs logs = new Logs();
            logs.save(logs.getPath(), "EasyInnerCatracaController->run(): " + ex.getMessage());
        }
    }

    private Boolean load_thread_catraca(Catraca catraca, Integer i, EasyInner easy_inner_thread) throws InterruptedException {

        if (isActive()) {
            // query_execute("DELETE FROM soc_catraca_monitora WHERE id_catraca = " + catraca.getId());
            query("SELECT func_catraca_monitora(false, " + catraca.getId() + ", null)");
        }

        typInnersCadastrados[i] = new Inner();
        typInnersCadastrados[i].Numero = (int) catraca.getNumero(); // N° SEMPRE COMEÇA PELO 1, EM SEQUENCIAL, OBRIGATORIAMENTE TEM QUE TER O MESMO NÚMERO QUE FOI CONFIGURADO NA CATRACA, SENÃO O SISTEMA NÃO FUNCIONA
        typInnersCadastrados[i].QtdDigitos = (int) catraca.getQuantidade_digitos(); // QUANTIDADE DE DIGITOS PARA O CARTÃO
        typInnersCadastrados[i].Teclado = (boolean) true; // HABILITA TECLADO
        typInnersCadastrados[i].Lista = (boolean) false; // LISTA DE USUARIOS COM ACESSO OFFLINE
        typInnersCadastrados[i].ListaBio = (boolean) false; // LISTA DE USUARIOS COM BIOMETRIA COM ACESSO OFFLINE
        typInnersCadastrados[i].TipoLeitor = (int) 0; // 0 - CODIGO DE BARRAS, 1 - MAGNÉTICO, 2 - PROXIMIDADE ABATRACK/SMART CARD, 3 - PROXIMIDADE WIEGAND/SMART CARD, 4 - PROXIMIDADE SMART CARD SERIAL
        typInnersCadastrados[i].Identificacao = 1; // 0 - FALSE, 1 - TRUE | IDENTIFICA DIGITAL NO BANCO DE DADOS DA CATRACA
        typInnersCadastrados[i].Verificacao = 0; // 0 - FALSE, 1 - TRUE | SOLICITA CÓDIGO APÓS COLOCAR DIGITAL PARA VERIFICAÇÃO E VICE-VERSA
        typInnersCadastrados[i].DoisLeitores = (boolean) false; // DOIS LEITORES NA MESMA CATRACA
        typInnersCadastrados[i].Catraca = (boolean) true; // FALSE PARA O CASO conf_json.getTipo_giro_catraca() == 0
        typInnersCadastrados[i].Biometrico = (boolean) catraca.getBiometrico();
        typInnersCadastrados[i].CntDoEvents = 0; // PADRÃO DOCUMENTAÇÃO SDK
        typInnersCadastrados[i].CountPingFail = 0; // PADRÃO DOCUMENTAÇÃO SDK
        typInnersCadastrados[i].CountTentativasEnvioComando = 0; // PADRÃO DOCUMENTAÇÃO SDK
        typInnersCadastrados[i].EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        typInnersCadastrados[i].TempoInicialPingOnLine = (int) System.currentTimeMillis();
        typInnersCadastrados[i].EstadoTeclado = Enumeradores.EstadosTeclado.TECLADO_EM_BRANCO;
        typInnersCadastrados[i].ObjectCatraca = catraca;
        typInnersCadastrados[i].PingOnline = false;

        final EasyInnerCatracaControllerThread ei = new EasyInnerCatracaControllerThread(typInnersCadastrados[i], easy_inner_thread);

        if (isActive()) {
            // query_execute("INSERT INTO soc_catraca_monitora (id_catraca, nr_ping, is_ativo) VALUES (" + catraca.getId() + ", 0, false);");
            query("SELECT func_catraca_monitora(true, " + catraca.getId() + ", null)");
        }

        FutureTask theTask = new FutureTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ei.run();
                } catch (RuntimeException e) {
                    //logs.save(logs.getPath(), "EasyInnerCatracaController->iniciarMaquinaEstados()->FutureTask()->run(): " + e.getMessage());

                }
                if (Thread.interrupted()) {
//                    Thread.currentThread().stop();
//                    System.err.println("Opa o projeto parou!!");
                    //logs.save(logs.getPath(), "EasyInnerCatracaController->iniciarMaquinaEstados(): " + "Opa o projeto parou!!");
                }

                //interromperThread(catraca.getNumero());
            }
        }, null);

        try {
            Thread.sleep(2000);
            Thread c = new Thread(theTask, "Catraca 0" + catraca.getNumero());
            c.start();
            list_thread.add(c);
        } catch (InterruptedException e2) {
            //logs.save(logs.getPath(), "EasyInnerCatracaController->iniciarMaquinaEstados()->FutureTask()->run(): " + e2.getMessage());
        }

        catraca.setCliente(conf_cliente.getPostgres_cliente());
        return true;

    }

    public void interromperThread(Integer numero) {
        int count = Thread.activeCount();

        Thread th[] = new Thread[count];

        Thread.enumerate(th);

        for (int i = 0; i < count; i++) {
            if (th[i].getName().contains("Catraca 0" + numero)) {
                th[i].interrupt();
                th[i].stop();
                th[i].isInterrupted();
            }
        }
    }

    /**
     * Realiza a crição do array de equipamentos para serem controlados.
     *
     * @throws InterruptedException
     */
    private void iniciarMaquinaEstados(EasyInner easy_inner_thread) {
        TotalInners = lista_catraca.size();

        //******************************************************
        //MAIS DE UM INNER
        //Define a quantidade de Inners que o sistema terá..
        //final Logs logs = new Logs();
        Boolean parar = false;

        while (!parar) {
            try {
                for (int i = 0; i < lista_catraca.size(); i++) {
                    if (!isActive()) {
                        continue;
                    }
                    // ResultSet rs = query("SELECT is_atualizar FROM soc_catraca_monitora WHERE id_catraca = " + lista_catraca.get(i).getId());
                    ResultSet rs = query("SELECT atualizar FROM vw_catraca_monitora WHERE id_catraca = " + lista_catraca.get(i).getId());
                    if (rs != null) {
                        rs.next();
                        if (rs.getRow() > 0) {
                            if (rs.getBoolean("is_atualizar")) {
                                interromperThread(lista_catraca.get(i).getNumero());
                                //                            if (!list_thread.isEmpty() && list_thread.size() == lista_catraca.size()) {
                                //                                list_thread.get(i).interrupt();
                                //                                list_thread.remove(i);
                                //                            }

                                load_thread_catraca(lista_catraca.get(i), i, easy_inner_thread);
                            }
                        } else {
                            load_thread_catraca(lista_catraca.get(i), i, easy_inner_thread);

                            // DEPOIS ATUALIZAR SINDICAL PARA A ATUALIZAÇÃO SER FEITA PELO BANCO
                            lista_catraca.get(i).setAtualizar(false);
                        }
                    }
                }

                Thread.sleep(10000);
            } catch (InterruptedException | SQLException e) {
                e.getMessage();
            }

        }
    }
//    
//    private void iniciarMaquinaEstados() throws InterruptedException {
//        DAO dao = new DAO();
//        String mac = Mac.getInstance();
//        if (dao.isActive()) {
//            dao.query_execute("DELETE FROM soc_catraca_monitora WHERE id_catraca IN(SELECT id FROM soc_catraca WHERE ds_mac = '" + mac + "')");
//        }
//
//        Conf_t conf_t = new Conf_t();
//        conf_t.loadJson();
//
//        //******************************************************
//        //MAIS DE UM INNER
//        //Define a quantidade de Inners que o sistema terá..
//        TotalInners = catraca.getLista_catraca().size();
//        final Logs logs = new Logs();
//        //Atribui o vetor com os números dos Inners, sempre de 1 a N
//        List<Thread> list_thread = new ArrayList();
//
//        for (int i = 0; i < TotalInners; i++) {
//            Catraca ct = catraca.getLista_catraca().get(i);
//            ct.setCliente(conf_t.getPostgres_cliente());
//
//            typInnersCadastrados[i] = new Inner();
//            typInnersCadastrados[i].Numero = (int) ct.getNumero(); // N° SEMPRE COMEÇA PELO 1, EM SEQUENCIAL, OBRIGATORIAMENTE TEM QUE TER O MESMO NÚMERO QUE FOI CONFIGURADO NA CATRACA, SENÃO O SISTEMA NÃO FUNCIONA
//            typInnersCadastrados[i].QtdDigitos = (int) ct.getQuantidade_digitos(); // QUANTIDADE DE DIGITOS PARA O CARTÃO
//            typInnersCadastrados[i].Teclado = (boolean) true; // HABILITA TECLADO
//            typInnersCadastrados[i].Lista = (boolean) false; // LISTA DE USUARIOS COM ACESSO OFFLINE
//            typInnersCadastrados[i].ListaBio = (boolean) false; // LISTA DE USUARIOS COM BIOMETRIA COM ACESSO OFFLINE
//            typInnersCadastrados[i].TipoLeitor = (int) 0; // 0 - CODIGO DE BARRAS, 1 - MAGNÉTICO, 2 - PROXIMIDADE ABATRACK/SMART CARD, 3 - PROXIMIDADE WIEGAND/SMART CARD, 4 - PROXIMIDADE SMART CARD SERIAL
//            typInnersCadastrados[i].Identificacao = 1; // 0 - FALSE, 1 - TRUE | IDENTIFICA DIGITAL NO BANCO DE DADOS DA CATRACA
//            typInnersCadastrados[i].Verificacao = 0; // 0 - FALSE, 1 - TRUE | SOLICITA CÓDIGO APÓS COLOCAR DIGITAL PARA VERIFICAÇÃO E VICE-VERSA
//            typInnersCadastrados[i].DoisLeitores = (boolean) false; // DOIS LEITORES NA MESMA CATRACA
//            typInnersCadastrados[i].Catraca = (boolean) true; // FALSE PARA O CASO conf_json.getTipo_giro_catraca() == 0
//            typInnersCadastrados[i].Biometrico = (boolean) ct.getBiometrico();
//            typInnersCadastrados[i].CntDoEvents = 0; // PADRÃO DOCUMENTAÇÃO SDK
//            typInnersCadastrados[i].CountPingFail = 0; // PADRÃO DOCUMENTAÇÃO SDK
//            typInnersCadastrados[i].CountTentativasEnvioComando = 0; // PADRÃO DOCUMENTAÇÃO SDK
//            typInnersCadastrados[i].EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
//            typInnersCadastrados[i].TempoInicialPingOnLine = (int) System.currentTimeMillis();
//            typInnersCadastrados[i].EstadoTeclado = Enumeradores.EstadosTeclado.TECLADO_EM_BRANCO;
//            typInnersCadastrados[i].ObjectCatraca = ct;
//            typInnersCadastrados[i].PingOnline = false;
//
//            dao = new DAO();
//            if (dao.isActive()) {
//                dao.query_execute("INSERT INTO soc_catraca_monitora (id_catraca, nr_ping, is_ativo) VALUES (" + ct.getId() + ", 0, false);");
//            }
//
//            final EasyInnerCatracaControllerThread ei = new EasyInnerCatracaControllerThread(typInnersCadastrados[i]);
//
//            list_ei_thread.add(ei);
//
//            FutureTask theTask = new FutureTask(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        ei.run();
//                    } catch (RuntimeException e) {
//                        logs.save(logs.getPath(), "EasyInnerCatracaController->iniciarMaquinaEstados()->FutureTask()->run(): " + e.getMessage());
//                    }
//                    if (Thread.interrupted()) {
//                        System.err.println("Opa o projeto parou!!");
//                        logs.save(logs.getPath(), "EasyInnerCatracaController->iniciarMaquinaEstados(): " + "Opa o projeto parou!!");
//                    }
//                }
//            }, null);
//
//            try {
//                Thread.sleep(2000);
//                Thread c = new Thread(theTask, "Catraca 0" + ct.getNumero());
//                c.start();
//                list_thread.add(c);
//            } catch (InterruptedException e2) {
//                logs.save(logs.getPath(), "EasyInnerCatracaController->iniciarMaquinaEstados()->FutureTask()->run(): " + e2.getMessage());
//            }
//        }
//
////        Boolean thread_stop = false;
////        while (!thread_stop) {
////            for (int i = 0; i < list_thread.size(); i++) {
////                Thread.sleep(5000);
////
////                if (!list_thread.get(i).isAlive()) {
////                    System.err.println("Opa, parou de funcionar ( " + i + " ): " + new DataHoje().hora() + " \n " + list_thread.get(i).getStackTrace());
////                    list_thread.remove(i);
////                    //thread_stop = true;
////
//////                    Thread.sleep(5000);
//////
//////                    list_thread.get(i).start();
//////                    
//////                    Thread.sleep(5000);
////                }
////
////            }
////            
////            if (list_thread.isEmpty()){
////                thread_stop = true;
////                System.err.println("SISTEMA PAROU COMPLETAMENTE");
////            }
////        }
//    }

}
