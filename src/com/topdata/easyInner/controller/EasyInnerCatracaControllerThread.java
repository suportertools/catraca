package com.topdata.easyInner.controller;

import com.topdata.EasyInner;
import com.topdata.easyInner.dao.DAO;
import com.topdata.easyInner.entity.Inner;
import com.topdata.easyInner.enumeradores.Enumeradores;
import com.topdata.easyInner.utils.EasyInnerUtils;
import com.topdata.easyInner.utils.EnviaAtualizacao;
import com.topdata.easyInner.utils.RetornoJson;
import com.topdata.easyInner.utils.SoundUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.LineUnavailableException;

public class EasyInnerCatracaControllerThread {

    private Inner inner;
    private EasyInner easy_inner_thread;
    private RetornoJson json;

    private final Integer TEMPORIZADOR = 3000;

    private boolean LiberaEntrada = false;
    private boolean LiberaSaida = false;
    private boolean LiberaEntradaInvertida = false;
    private boolean LiberaSaidaInvertida = false;

    private Boolean Parar = false;

    public EasyInnerCatracaControllerThread(Inner inner) {
        this.inner = inner;
    }

    /**
     * FUNCIONAMENTO DA MÁQUINA DE ESTADOS MÉTODO RESPONSÁVEL EM EXECUTAR OS
     * PROCEDIMENTOS DO MODO ONLINE A Máquina de Estados nada mais é do que uma
     * rotina que fica em loop testando uma variável que chamamos de Estado.
     * Dependendo do estado atual, executamos alguns procedimentos e em seguida
     * alteramos o estado que será verificado pela máquina de estados novamente
     * no próximo passo do loop.
     *
     */
    public void run() {
        try {
            // System.loadLibrary("EasyInner");
            easy_inner_thread = new EasyInner();
            //Fecha qualquer conexão que estivesse aberta..
            easy_inner_thread.FecharPortaComunicacao();
            //Define o tipo de conexão conforme o selecionado no combo (serial, TCP porta Variavel, TCP Porta Fixa..etc)
            easy_inner_thread.DefinirTipoConexao(1);

            //Abre a porta de Comunicação com os Inners..
            int Ret = easy_inner_thread.AbrirPortaComunicacao(inner.ObjectCatraca.getPorta()); // PORTA PADRÃO

            if (Ret != Enumeradores.RET_COMANDO_OK) {
                return;
            }

            //Enquanto Parar = false prosseguir a maquina...
            while (!Parar) {
                //Verifica o Estado do Inner Atual..
                switch (inner.EstadoAtual) {
                    case ESTADO_CONECTAR:
                        PASSO_ESTADO_CONECTAR();
                        break;

                    case ESTADO_ENVIAR_CFG_OFFLINE:
                        PASSO_ESTADO_ENVIAR_CFG_OFFLINE();
                        break;

                    case ESTADO_COLETAR_BILHETES:
                        PASSO_ESTADO_COLETAR_BILHETES();
                        break;

                    case ESTADO_ENVIAR_CFG_ONLINE:
                        PASSO_ESTADO_ENVIAR_CFG_ONLINE();
                        break;

                    case ESTADO_ENVIAR_DATA_HORA:
                        PASSO_ESTADO_ENVIAR_DATA_HORA();
                        break;

                    case ESTADO_ENVIAR_MSG_PADRAO:
                        PASSO_ENVIAR_MENSAGEM_PADRAO();
                        break;

                    case ESTADO_CONFIGURAR_ENTRADAS_ONLINE:
                        PASSO_ESTADO_CONFIGURAR_ENTRADAS_ONLINE();
                        break;

                    case ESTADO_POLLING:
                        PASSO_ESTADO_POLLING();
                        break;

                    case ESTADO_LIBERAR_CATRACA:
                        PASSO_LIBERA_GIRO_CATRACA();
                        break;

                    case ESTADO_MONITORA_GIRO_CATRACA:
                        PASSO_MONITORA_GIRO_CATRACA();
                        break;

                    case PING_ONLINE:
                        PASSO_ESTADO_ENVIA_PING_ONLINE();
                        break;

                    case ESTADO_RECONECTAR:
                        PASSO_ESTADO_RECONECTAR();
                        break;

                    case AGUARDA_TEMPO_MENSAGEM:
                        PASSO_AGUARDA_TEMPO_MENSAGEM();
                        break;

                    case ESTADO_DEFINICAO_TECLADO:
                        PASSO_ESTADO_DEFINICAO_TECLADO();
                        break;

                    case ESTADO_AGUARDA_DEFINICAO_TECLADO:
                        PASSO_ESTADO_AGUARDA_DEFINICAO_TECLADO();
                        break;

                    case ESTADO_ENVIA_MSG_URNA:
                        PASSO_ESTADO_ENVIA_MSG_URNA();
                        break;

                    case ESTADO_MONITORA_URNA:
                        PASSO_ESTADO_MONITORA_URNA();
                        break;

                    case ACIONAR_RELE:
                        ACIONAR_RELE();
                        break;

                    case ESTADO_ENVIAR_CONFIGMUD_ONLINE_OFFLINE:
                        PASSO_ESTADO_ENVIAR_CONFIGMUD_ONLINE_OFFLINE();
                        break;

                    case ESTADO_ENVIAR_MENSAGEM:
                        PASSO_ESTADO_ENVIAR_MSG_OFFLINE();
                        break;

                    case ESTADO_ENVIAR_HORARIOS:
                        PASSO_ESTADO_ENVIAR_HORARIOS();
                        break;

                    case ESTADO_ENVIAR_MENSAGEM_ACESSO_NEGADO:
                        PASSO_ENVIAR_MENSAGEM_ACESSO_NEGADO();
                        break;

                    case ESTADO_ENVIAR_USUARIOS_LISTAS:
                        PASSO_ESTADO_ENVIAR_USUARIOS_LISTAS();
                        break;
                }

                if (inner.CntDoEvents++ > 10) {
                    inner.CntDoEvents = 0;
                }
            }

            //Fecha a porta de Comunicação quando sai da maquina de estados..
            easy_inner_thread.FecharPortaComunicacao();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Metodo responsável por realizar um comando simples com o equipamento para
     * detectar se esta conectado.
     *
     * @param Inner
     * @return
     */
    private Integer testarConexaoInner() {
        int[] DataHora = new int[6];
        Integer ret = easy_inner_thread.ReceberRelogio(inner.Numero, DataHora);
        return ret;
    }

    /**
     * CONECTAR Inicia a conexão com o Inner Próximo passo:
     * ESTADO_ENVIAR_CFG_OFFLINE
     */
    private void PASSO_ESTADO_CONECTAR() {
        try {
            long tempo;

            int Ret = Enumeradores.Limpar;
            //Inicia tempo ping online
            inner.TempoInicialPingOnLine = (int) System.currentTimeMillis();

            long IniConexao = System.currentTimeMillis();
            //Realiza loop enquanto o tempo fim for menor que o tempo atual, e o comando retornado diferente de OK.
            do {
                EnviaAtualizacao.status(inner.ObjectCatraca.getId(), false, "Conectando");
                EnviaAtualizacao.enviarAtualizacaoTelaCatraca(inner);

                tempo = System.currentTimeMillis() - IniConexao;
                //Tenta abrir a conexão 
                Thread.sleep(101);
                Ret = testarConexaoInner();
            } while (Ret != Enumeradores.RET_COMANDO_OK && tempo < TEMPORIZADOR);

            if (Ret == Enumeradores.RET_COMANDO_OK) {
                //caso consiga o Inner vai para o Passo de Configuração OFFLINE, posteriormente para coleta de Bilhetes.
                inner.CountTentativasEnvioComando = 0;
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_CFG_OFFLINE;

                //atualizaMonitoraCatraca(true, inner.Numero, easy_inner_thread, "conectar");
            } else {
                //caso ele não consiga, tentará enviar três vezes, se não conseguir volta para o passo Reconectar
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inner.CountTentativasEnvioComando++;

                //atualizaMonitoraCatraca(false, inner.Numero, easy_inner_thread, "conectar " + .CountTentativasEnvioComando);
                //enviarAtualizacaoTelaCatraca(inner.Numero);
                return;
            }

            Calendar calendar = new GregorianCalendar();
            Date date = new Date();
            calendar.setTime(date);

            //--- CARREGAR LISTA DE BIOMETRIAS PARA MEMÓRIA DA CATRACA
            if (inner.Biometrico) {
                loadBiometria();
            }

            //AcionarBipCurto(atual);
            if (inner.ObjectCatraca.getServidor_beep()) {
                SoundUtils.tone(2500, 400);
            }

            /* EXEMPLOS DE BEEPS
             SoundUtils.tone(100, 1000);
             SoundUtils.tone(5000, 100);
             SoundUtils.tone(400, 500);
             SoundUtils.tone(400, 500, 0.2);
             //Toolkit.getDefaultToolkit().beep();// 1ª opção
             */
            //form.get(0).getLblStatus().setText("Catraca Ativa");
            //form.get(0).getLblNumero().setText("0" + typInnersCadastrados[numero_inner].Numero);
            //logs.save("status_catraca", "CATRACA ONLINE");
            //enviarAtualizacaoTelaCatraca(inner.Numero);
        } catch (InterruptedException | LineUnavailableException ex) {
            System.out.println("Exception 01: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
        }
    }

    /**
     * Passo responsável pelo envio das configurações off-line do Inner
     */
    private void PASSO_ESTADO_ENVIAR_CFG_OFFLINE() {
        try {
            //Mensagem Status
            //form.get(0).getLblStatus().setText("Configurações OFF-LINE");

            //Preenche os campos de configuração do Inner
            MontaConfiguracaoInner(Enumeradores.MODO_OFF_LINE);

            //Envia o comando de configuração
            int Ret = easy_inner_thread.EnviarConfiguracoes(inner.Numero);

            //Testa o retorno do envio das configurações Off Line
            if (Ret == Enumeradores.RET_COMANDO_OK) {
                DefineVersao();
                inner.CountTentativasEnvioComando = 0;
                //verifica se o enviar lista esta selecionado
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_HORARIOS;
                inner.TempoColeta = (int) System.currentTimeMillis() + TEMPORIZADOR;
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "enviar cfg offline");
            } else {
                //caso ele não consiga, tentará enviar três vezes, se não conseguir volta para o passo Reconectar
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inner.CountTentativasEnvioComando++;
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "enviar cfg offline");
            }
        } catch (InterruptedException ex) {
            System.out.println("Exception 02: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Passo resposável pela coleta dos bilhetes registrados em modo off-line
     */
    private void PASSO_ESTADO_COLETAR_BILHETES() {
        try {
            if (inner.InnerNetAcesso) {
                ColetarBilhetesInnerAcesso();
            } else {
                ColetarBilhetesInnerNet();
            }
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_CFG_ONLINE;
        } catch (InterruptedException e) {
            System.out.println("Exception 03: " + e.getMessage());
        }
        //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "coletar bilhetes");
    }

    /**
     * Configura modo On-line Próximo passo: ESTADO_ENVIAR_DATA_HORA
     */
    private void PASSO_ESTADO_ENVIAR_CFG_ONLINE() {
        try {
            //Monta configuração modo Online
            MontaConfiguracaoInner(Enumeradores.MODO_ON_LINE);

            //Envia as configurações ao Inner Atual.
            int Ret = easy_inner_thread.EnviarConfiguracoes(inner.Numero);

            if (Ret == Enumeradores.RET_COMANDO_OK) {
                //caso consiga enviar as configurações, passa para o passo Enviar Data Hora
                inner.CountTentativasEnvioComando = 0;
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_DATA_HORA;

                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "enviar cfg online");
            } else {
                //caso ele não consiga, tentará enviar três vezes, se não conseguir volta para o passo Reconectar
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inner.CountTentativasEnvioComando++;
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "enviar cfg online");
            }
        } catch (Exception ex) {
            System.out.println("Exception 04: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Envia ao Inner data e hora atual Próximo passo: ESTADO_ENVIAR_MSG_PADRAO
     */
    private void PASSO_ESTADO_ENVIAR_DATA_HORA() {
        try {
            //Exibe estado do Inner no Rodapé da Janela
            //form.get(0).getLblStatus().setText("Enviando data e hora");

            Date Data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yy");
            int Ano = Integer.parseInt(formatter.format(Data));
            formatter = new SimpleDateFormat("MM");
            int Mes = Integer.parseInt(formatter.format(Data));
            formatter = new SimpleDateFormat("dd");
            int Dia = Integer.parseInt(formatter.format(Data));
            formatter = new SimpleDateFormat("HH");
            int Hora = Integer.parseInt(formatter.format(Data));
            formatter = new SimpleDateFormat("mm");
            int Minuto = Integer.parseInt(formatter.format(Data));
            formatter = new SimpleDateFormat("ss");
            int Segundo = Integer.parseInt(formatter.format(Data));
            //Envia Comando de Relógio ao Inner Atual..
//          RelogioInner relogioInner = new RelogioInner();
            int Ret = easy_inner_thread.EnviarRelogio(inner.Numero, Dia, Mes, Ano, Hora, Minuto, Segundo);
            //Testa o Retorno do comando de Envio de Relógio..
            if (Ret == Enumeradores.RET_COMANDO_OK) {
                //Vai para o passo de Envio de Msg Padrão..
                inner.CountTentativasEnvioComando = 0;
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MSG_PADRAO;
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "enviar data e hora");
            } else {
                //caso ele não consiga, tentará enviar três vezes, se não conseguir volta para o passo Reconectar
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inner.CountTentativasEnvioComando++;
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "enviar data e hora");
            }
        } catch (NumberFormatException ex) {
            System.out.println("Exception 05: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Envia mensagem padrão modo Online Próximo passo:
     * ESTADO_CONFIGURAR_ENTRADAS_ONLINE
     */
    private void PASSO_ENVIAR_MENSAGEM_PADRAO() {
        try {
            //Exibe estado do Inner no Rodapé da Janela
            //form.get(0).getLblStatus().setText("Enviando Mensagem Padrão");

            //Envia comando definindo a mensagem Padrão Online para o Inner.
            int Ret = easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, "R'TOOLS SISTEMAS");

            //Testa o retorno da mensagem enviada..
            if (Ret == Enumeradores.RET_COMANDO_OK) {
                //Muda o passo para configuração de entradas Online.
                inner.CountTentativasEnvioComando = 0;
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONFIGURAR_ENTRADAS_ONLINE;
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "enviar mensagem padrao");
            } else {
                //caso ele não consiga, tentará enviar três vezes, se não conseguir volta para o passo Reconectar
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                //Adiciona 1 ao contador de tentativas
                inner.CountTentativasEnvioComando++;
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "enviar mensagem padrao");
            }
        } catch (Exception ex) {
            System.out.println("Exception 06: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Preparação configuração online para entrar em modo Polling Próximo passo:
     * ESTADO_POLLING
     */
    private void PASSO_ESTADO_CONFIGURAR_ENTRADAS_ONLINE() {
        try {
            //Exibe estado do Inner no Rodapé da Janela
            //form.get(0).getLblStatus().setText("Configurando Entradas Online");

            //Converte Binário para Decimal
            int ValorDecimal = ConfiguraEntradasMudancaOnLine(); //Ver no manual Anexo III

            int Ret = easy_inner_thread.EnviarFormasEntradasOnLine(inner.Numero, (byte) inner.QtdDigitos, 1, (byte) ValorDecimal, 15, 17);
            //Testa o retorno do comando..
            if (Ret == Enumeradores.RET_COMANDO_OK) {
                //Vai para o Estado De Polling.
                inner.TempoInicialPingOnLine = (int) System.currentTimeMillis();
                inner.CountTentativasEnvioComando = 0;
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_POLLING;

                EnviaAtualizacao.status(inner.ObjectCatraca.getId(), true, "Ativa");
                EnviaAtualizacao.enviarAtualizacaoTelaCatraca(inner);

                if (inner.Catraca) {
//                    jBtnEntrada.setText("Entrada");
//                    jBtnSaida.setText("Saída");
//                    jBtnEntrada.setEnabled(true);
//                    jBtnSaida.setEnabled(true);
                } else {
//                    jBtnEntrada.setText("Porta 1");
//                    jBtnSaida.setText("Porta 2");
//                    jBtnEntrada.setEnabled(true);
//                    jBtnSaida.setEnabled(true);
                }
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "configurar entradas online");
            } else {
                //caso ele não consiga, tentará enviar três vezes, se não conseguir volta para o passo Reconectar
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                //Adiciona 1 ao contador de tentativas
                inner.CountTentativasEnvioComando++;
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "configurar entradas online");
            }
        } catch (Exception ex) {
            System.out.println("Exception 07: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * É onde funciona todo o processo do modo online Passagem de cartão,
     * catraca, urna, mensagens...
     *
     */
    private void PASSO_ESTADO_POLLING() {
        try {
            // ATUALIZA AUTOMATICAMENTE AS BIOMETRIAS SE TIVER ALGUMA NOVA
            if (inner.Biometrico) {
                DAO dao = new DAO();
                if (!dao.getConectado()) {
                    return;
                }

                ResultSet rs = dao.query("SELECT * FROM pes_biometria WHERE is_ativo = true AND is_enviado = false AND ds_biometria <> '' AND ds_biometria2 <> ''");
                if (rs.next()) {
                    //form.get(0).getLblStatus().setText("Atualizando Biometrias");
                    loadBiometria();
                }
            }

            json = RetornaPessoaLiberada();
            if (json != null) {
                if (!json.getLiberado()) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MENSAGEM_ACESSO_NEGADO;
                } else {
                    LiberaCatraca(json);
                }
                return;
            }

            StringBuffer Cartao = new StringBuffer();

            //Thread.sleep(10l);
            int[] DadosOnLine = new int[8];

            //Envia o Comando de Coleta de Bilhetes..
            int Ret = easy_inner_thread.ReceberDadosOnLine(inner.Numero, DadosOnLine, Cartao);

            //Atribui Temporizador
            inner.Temporizador = (int) System.currentTimeMillis();

            if (Ret == Enumeradores.RET_COMANDO_OK) {
                if (DadosOnLine[0] == Enumeradores.FIM_TEMPO_ACIONAMENTO
                        || DadosOnLine[0] == Enumeradores.GIRO_DA_CATRACA_TOPDATA
                        || DadosOnLine[0] == Enumeradores.TECLA_FUNCAO
                        || DadosOnLine[0] == Enumeradores.TECLA_ANULA
                        || ((Cartao.length() == 0) && !(inner.EstadoTeclado == Enumeradores.EstadosTeclado.AGUARDANDO_TECLADO))) {
                    inner.CountTentativasEnvioComando = 0;
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MSG_PADRAO;
                    return;
                }

                String NumCartao = Cartao.toString();
                switch (DadosOnLine[0]) {
                    case 1:
                        // TECLADO CATRACA
                        json = RetornaPessoaCatraca(Integer.valueOf(NumCartao), null);
                        break;
                    case 2:
                        // CARTÃO DA CATRACA
                        json = RetornaPessoaCatraca(null, NumCartao);
                        break;
                    case 12:
                    case 18:
                        // BIOMETRIA DA CATRACA
                        if (DadosOnLine[0] == 12) {
                            // RETORNO JÁ É O CÓDIGO DA PESSOA
                            json = RetornaPessoaCatraca(Integer.valueOf(NumCartao), null);
                        } else {
                            // CONVERTE A BIOMETRIA CAPTURADA PARA ENCONTRAR O CÓDIGO
                        }
                        break;
                    default:
                        break;
                }

                if (json != null) {
                    if (!json.getLiberado()) {
                        inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MENSAGEM_ACESSO_NEGADO;
                    } else {
                        LiberaCatraca(json);
                    }
                }
            } else {
                long temp = System.currentTimeMillis() - inner.TempoInicialPingOnLine;
                //Se passar 3 segundos sem receber nada, passa para o estado enviar ping on line, para manter o equipamento em on line.
                if ((int) temp > TEMPORIZADOR) {
                    inner.EstadoSolicitacaoPingOnLine = inner.EstadoAtual;
                    inner.CountTentativasEnvioComando = 0;
                    inner.TempoInicialPingOnLine = (int) System.currentTimeMillis();
                    inner.EstadoAtual = Enumeradores.EstadosInner.PING_ONLINE;
                    EnviaAtualizacao.ping(inner.Numero);
                }
            }
        } catch (SQLException | NumberFormatException ex) {
            System.out.println("Exception 08: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Libera a catraca de acordo com o lado informado Próximo Passo:
     * ESTADO_MONITORA_GIRO_CATRACA
     */
    private void PASSO_LIBERA_GIRO_CATRACA() {
        try {
            //Exibe estado do Inner no Rodapé da Janela
            //form.get(0).getLblStatus().setText("Liberando Giro");

            int Ret;
            //Envia comando de liberar a catraca para Entrada.
            if (LiberaEntrada) {
                easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, "ENTRADA LIBERADA");
                LiberaEntrada = false;
                Ret = easy_inner_thread.LiberarCatracaEntrada(inner.Numero);
            } else if (LiberaEntradaInvertida) {
                easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, "ENTRADA LIBERADA");
                LiberaEntradaInvertida = false;
                Ret = easy_inner_thread.LiberarCatracaEntradaInvertida(inner.Numero);
            } else if (LiberaSaida) {
                //Envia comando de liberar a catraca para Saída.
                easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, "SAIDA LIBERADA");
                LiberaSaida = false;
                Ret = easy_inner_thread.LiberarCatracaSaida(inner.Numero);
            } else if (LiberaSaidaInvertida) {
                easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, "SAIDA LIBERADA");
                LiberaSaidaInvertida = false;
                Ret = easy_inner_thread.LiberarCatracaSaidaInvertida(inner.Numero);
            } else {
                easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, "CATRACA LIBERADA");
                Ret = easy_inner_thread.LiberarCatracaDoisSentidos(inner.Numero);
            }

//Testa Retorno do comando..
            if (Ret == Enumeradores.RET_COMANDO_OK) {
                easy_inner_thread.AcionarBipCurto(inner.Numero);
                try {
                    if (inner.ObjectCatraca.getServidor_beep()) {
                        SoundUtils.tone(2500, 400);
                    }
                } catch (LineUnavailableException ex) {
                    System.out.println("Exception 09: " + ex.getMessage());
                }

                //liberarCatraca(nome_pessoa, NumCartao);
                inner.CountPingFail = 0;
                inner.CountTentativasEnvioComando = 0;
                inner.TempoInicialPingOnLine = (int) System.currentTimeMillis();
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_MONITORA_GIRO_CATRACA;

                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "libera giro catraca");
            } else {
                //Se o retorno for diferente de 0 tenta liberar a catraca 3 vezes, caso não consiga enviar o comando volta para o passo reconectar.
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.CountTentativasEnvioComando = 0;
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inner.CountTentativasEnvioComando++;
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "libera giro catraca");
            }
        } catch (Exception ex) {
            System.out.println("Exception 10: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Verifica se a catraca foi girada ou não e caso sim para qual lado.
     * Próximo Passo: ESTADO_ENVIAR_MSG_PADRAO
     */
    private void PASSO_MONITORA_GIRO_CATRACA() {
        try {
            int[] Bilhete = new int[8];
            StringBuffer Cartao = new StringBuffer();

//            // ENQUANTO ESPERA O GIRO LIMPAR VARIÁVEL COM O NUMERO DO CARTÃO
//            NumCartao = "";
            //Exibe estado do giro
            //System.out.println("Monitorando Giro de Catraca!");
            //Exibe estado do Inner no Rodapé da Janela
            //form.get(0).getLblStatus().setText("Esperando Giro da Catraca");
            //EnviaAtualizacao.status(inner.ObjectCatraca.getId(), true, "Aguardando Giro");
            //EnviaAtualizacao.enviarAtualizacaoTelaCatraca(inner.Numero);
            //Monitora o giro da catraca..
            int Ret = easy_inner_thread.ReceberDadosOnLine(inner.Numero, Bilhete, Cartao);

            //Testa o retorno do comando..
            if (Ret == Enumeradores.RET_COMANDO_OK) {
                //Testa se girou o não a catraca..
                if (Bilhete[0] == Enumeradores.FIM_TEMPO_ACIONAMENTO) {

                    //System.out.println("Não girou a catraca!");
                    //String msg_string = "NÃO GIROU CATRACA { " + nome_pessoa + " }";
                    //logs.save("giro_catraca", msg_string);
                    //logs.save("catraca", msg_string);
                    EnviaAtualizacao.atualiza_tela(inner);
                } else if (Bilhete[0] == (int) Enumeradores.GIRO_DA_CATRACA_TOPDATA) {
                    String es;
                    if (inner.ObjectCatraca.getLado_giro_catraca().equals("esquerda")) {
                        if (Integer.parseInt(String.valueOf(Bilhete[1])) == 0) {
                            //System.out.println("Girou a catraca para saída.");
                            //String msg_string = "GIROU CATRACA PARA SAÍDA { " + nome_pessoa + " }";
                            //logs.save("giro_catraca", msg_string);
                            //logs.save("catraca", msg_string);
                            es = "S";
                        } else {
                            //System.out.println("Girou a catraca para entrada.");
                            ///String msg_string = "GIROU CATRACA PARA ENTRADA { " + nome_pessoa + " }";
                            //logs.save("giro_catraca", msg_string);
                            //logs.save("catraca", msg_string);
                            es = "E";
                        }
                    } else if (Integer.parseInt(String.valueOf(Bilhete[1])) == 0) {
                        //System.out.println("Girou a catraca para entrada.");
                        //String msg_string = "GIROU CATRACA PARA ENTRADA { " + nome_pessoa + " }";
                        //logs.save("giro_catraca", msg_string);
                        //logs.save("catraca", msg_string);
                        es = "E";
                    } else {
                        //System.out.println("Girou a catraca para saída.");
                        //String msg_string = "GIROU CATRACA PARA SAÍDA { " + nome_pessoa + " }";
                        //logs.save("giro_catraca", msg_string);
                        //logs.save("catraca", msg_string);
                        es = "S";
                    }
                    //if ((tipo_entrada == 3 || tipo_entrada == 1) && numero_via == 99) {
                    if (json.getVia() != null) {
                        switch (json.getVia()) {
                            case 99:
                                if (inner.ObjectCatraca.getGrava_frequencia_catraca()) {
                                    new DAO().query("INSERT INTO soc_catraca_frequencia (dt_acesso, ds_hora_acesso, id_departamento, id_sis_pessoa, ds_es) VALUES (CURRENT_DATE, to_char(LOCALTIME(0), 'HH24:MI'), " + inner.ObjectCatraca.getDepartamento() + ", " + json.getNr_pessoa() + ", '" + es + "');");
                                }
                                new DAO().query("UPDATE conv_movimento SET is_ativo = false, dt_entrada = CURRENT_DATE WHERE id = " + json.getNr_pessoa() + " AND is_ativo = true;");
                                break;
                            default:
                                if (inner.ObjectCatraca.getGrava_frequencia_catraca()) {
                                    new DAO().query("INSERT INTO soc_catraca_frequencia (dt_acesso, ds_hora_acesso, id_departamento, id_pessoa, ds_es) VALUES (CURRENT_DATE, to_char(LOCALTIME(0), 'HH24:MI'), " + inner.ObjectCatraca.getDepartamento() + ", " + json.getNr_pessoa() + ", '" + es + "');");
                                }
                                break;
                        }
                    } else {
                        if (inner.ObjectCatraca.getGrava_frequencia_catraca()) {
                            new DAO().query("INSERT INTO soc_catraca_frequencia (dt_acesso, ds_hora_acesso, id_departamento, id_pessoa, ds_es) VALUES (CURRENT_DATE, to_char(LOCALTIME(0), 'HH24:MI'), " + inner.ObjectCatraca.getDepartamento() + ", " + json.getNr_pessoa() + ", '" + es + "');");
                        }
                    }

                    EnviaAtualizacao.atualiza_tela(inner);

                    Thread.sleep(100);
                }

                //Vai para o estado de Envio de Msg Padrão..
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MSG_PADRAO;
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "monitora giro catraca");
            } else {
                //Caso o tempo que estiver monitorando o giro chegue a 3 segundos,
                //deverá enviar o ping on line para manter o equipamento em modo on line
                long tempo = (System.currentTimeMillis() - inner.TempoInicialPingOnLine);
                //Se passar 3 segundos sem receber nada, passa para o estado enviar ping on line, para manter o equipamento em on line.
                if (tempo >= 5000) {
                    inner.EstadoSolicitacaoPingOnLine = inner.EstadoAtual;
                    inner.CountTentativasEnvioComando = 0;
                    inner.TempoInicialPingOnLine = System.currentTimeMillis();
                    inner.EstadoAtual = Enumeradores.EstadosInner.PING_ONLINE;
                }
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "monitora giro catraca");
            }
        } catch (NumberFormatException | InterruptedException ex) {
            System.out.println("Exception 11: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Testa comunicação com o Inner e mantém o Inner em OnLine quando a mudança
     * automática está configurada. Especialmente indicada para a verificação da
     * conexão em comunicação TCP/IP. Próximo Passo: RETORNA MÉTODO QUE O
     * ACIONOU
     */
    private void PASSO_ESTADO_ENVIA_PING_ONLINE() {
        try {
            //Exibe estado do Inner no Rodapé da Janela
            //form.get(0).getLblStatus().setText("Catraca " + typInnersCadastrados[numero_inner].Numero + " PING ONLINE...");

            //Envia o comando de PING ON LINE, se o retorno for OK volta para o estado onde chamou o método
            int retorno = easy_inner_thread.PingOnLine(inner.Numero);
            if (retorno == easy_inner_thread.RET_COMANDO_OK) {
                inner.EstadoAtual = inner.EstadoSolicitacaoPingOnLine;
            } else {
                //caso ele não consiga, tentará enviar três vezes, se não conseguir volta para o passo Reconectar
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inner.CountTentativasEnvioComando++;
            }
            inner.TempoInicialPingOnLine = System.currentTimeMillis();
        } catch (Exception ex) {
            System.out.println("Exception 12: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
        }
    }

    /**
     * Se a conexão cair tenta conectar novamente Próximo Passo:
     * ESTADO_ENVIAR_CFG_OFFLINE
     */
    private void PASSO_ESTADO_RECONECTAR() {
        try {
            long IniConexao = 0;

            long tempo = System.currentTimeMillis() - inner.TempoInicialPingOnLine;
            if (tempo < TEMPORIZADOR) {
                return;
            }
            inner.TempoInicialPingOnLine = (int) System.currentTimeMillis();

            //bloquearDesconexao("CATRACA DESCONECTADA");
            //form.get(0).getLblMensagem().setText("");
            //form.get(0).getLblStatus().setText("Reconectando ...");
            //logs.save("status_catraca", "SEM CONEXÃO COM A CATRACA");
            int Ret = Enumeradores.Limpar;

            IniConexao = System.currentTimeMillis();
            //Realiza loop enquanto o tempo fim for menor que o tempo atual, e o comando retornado diferente de OK.
            do {
                EnviaAtualizacao.status(inner.ObjectCatraca.getId(), false, "Reconectando");
                EnviaAtualizacao.enviarAtualizacaoTelaCatraca(inner);

                tempo = System.currentTimeMillis() - IniConexao;
                Thread.sleep(101);
                Ret = testarConexaoInner();
            } while (Ret != Enumeradores.RET_COMANDO_OK && tempo < TEMPORIZADOR);

            //Testa o comando de envio de relógio para o Inner
            if (Ret == Enumeradores.RET_COMANDO_OK) {
                //Zera as variáveis de controle da maquina de estados.
                inner.CountTentativasEnvioComando = 0;
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_CFG_OFFLINE;
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "reconectar");
                //logs.save("status_catraca", "CATRACA ONLINE");
            } else {
                //caso ele não consiga, tentará enviar três vezes, se não conseguir volta para o passo Reconectar
                if (inner.CountTentativasEnvioComando >= 10) {
                    //inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
                }
                inner.CountTentativasEnvioComando++;
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "reconectar");
                //enviarAtualizacaoTelaCatraca(numero_inner);
                inner.CountRepeatPingOnline = 0;

//                run();
//
//                easy_inner_thread = new EasyInner();
//                //Fecha qualquer conexão que estivesse aberta..
//                easy_inner_thread.FecharPortaComunicacao();
//                //Define o tipo de conexão conforme o selecionado no combo (serial, TCP porta Variavel, TCP Porta Fixa..etc)
//                easy_inner_thread.DefinirTipoConexao(2);
//
//                //Abre a porta de Comunicação com os Inners..
//                Ret = easy_inner_thread.AbrirPortaComunicacao(inner.ObjectCatraca.getPorta()); // PORTA PADRÃO
//
//                if (Ret != Enumeradores.RET_COMANDO_OK) {
//                    return;
//                }
//                
//                easy_inner_thread.PingOnLine(inner.Numero); // PORTA PADRÃO
            }

//        if (Ret != Enumeradores.RET_COMANDO_OK) {
//            return;
//        }
        } catch (InterruptedException ex) {
            //System.out.println("Passo Reconectar :  " + ex);
            System.out.println("Exception 13: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Passo agurada 2 segundos o tempo para apresentação da mensagem no visor
     * do Inner
     */
    private void PASSO_AGUARDA_TEMPO_MENSAGEM() {
        try {
            //Após passar os 2 segundos volta para o passo enviar mensagem padrão
            long tempo = System.currentTimeMillis() - inner.TempoInicialMensagem;
            if (tempo > 2000) {
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MSG_PADRAO;
            }
            //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "aguarda tempo mensagem");
        } catch (Exception ex) {
            System.out.println("Exception 14: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Mostra mensagem para que seja informado se é entrada ou saída Este estado
     * configura a mensagem padrão que será exibida no dispositivo em seu
     * funcionamento Online utilizando o método EnviarMensagemPadraoOnline. O
     * passo posterior a este estado é o passo de configuração de entradas
     * online, ou em caso de erro pode retornar para o estado de conexão após
     * alcançar o número máximo de tentativas. Próximo passo: ESTADO_POLLING
     */
    private void PASSO_ESTADO_DEFINICAO_TECLADO() {
        //Envia mensagem Padrão Online..
        easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, "ENTRADA OU SAIDA?");
        int Ret = easy_inner_thread.EnviarFormasEntradasOnLine(inner.Numero,
                0, //Quantidade de Digitos do Teclado.. (Não aceita digitação numérica)
                0, //0 – não ecoa
                Enumeradores.ACEITA_TECLADO,
                10, // Tempo de entrada do Teclado (10s).
                32 //Posição do Cursor (32 fica fora..)
        );

        //Se Retorno OK, vai para proximo estado..
        if (Ret == Enumeradores.RET_COMANDO_OK) {
            inner.CountTentativasEnvioComando = 0;
            inner.EstadoTeclado = Enumeradores.EstadosTeclado.AGUARDANDO_TECLADO;
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_AGUARDA_DEFINICAO_TECLADO;
            //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "definicao teclado");
        } else //Caso o retorno não for OK, tenta novamente até 3x..
        {
            if (inner.CountTentativasEnvioComando > 3) {
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
            } //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "definicao teclado");
        }
    }

    /**
     * PASSO_ESTADO_AGUARDAR_DEFINICAO_TECLADO Aguarda a resposta do teclado
     * (Entrada, Saida, anula ou confirma) Proximo estado: ESTADO_POLLING
     */
    private void PASSO_ESTADO_AGUARDA_DEFINICAO_TECLADO() {
        try {
            int[] Bilhete = new int[8];

            //form.get(0).getLblStatus().setText("Aguardando definição do teclado");
            StringBuffer Cartao = new StringBuffer();

            //Envia o Comando de Coleta de Bilhetes..
            int Ret = easy_inner_thread.ReceberDadosOnLine(inner.Numero, Bilhete, Cartao);

            //Atribui Temporizador
            inner.Temporizador = (int) System.currentTimeMillis();

            if (Ret == Enumeradores.RET_COMANDO_OK) {
                if (inner.EstadoTeclado == Enumeradores.EstadosTeclado.AGUARDANDO_TECLADO) {
                    //****************************************************
                    //Entrada, saída liberada, confirma, anula ou função tratar mensagem
                    //66 - "Entrada" via teclado
                    //67 - "Saída" via teclado
                    //35 - "Confirma" via teclado
                    //42 - "Anula" via teclado
                    //65 - "Função" via teclado
                    if (Integer.parseInt(String.valueOf(Bilhete[1])) == Enumeradores.TECLA_ENTRADA) //entrada
                    {
                        easy_inner_thread.AcionarBipCurto(inner.Numero);
                        try {
                            if (inner.ObjectCatraca.getServidor_beep()) {
                                SoundUtils.tone(2500, 400);
                            }
                        } catch (Exception ex) {
                            System.out.println("Exception 15: " + ex.getMessage());
                        }
                        verificaLadoCatraca("Entrada");
                        inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                    } else if (Integer.parseInt(String.valueOf(Bilhete[1])) == Enumeradores.TECLA_SAIDA) //saida
                    {
                        easy_inner_thread.AcionarBipCurto(inner.Numero);
                        try {
                            if (inner.ObjectCatraca.getServidor_beep()) {
                                SoundUtils.tone(2500, 400);
                            }
                        } catch (Exception ex) {
                            System.out.println("Exception 16: " + ex.getMessage());
                        }
                        verificaLadoCatraca("Saida");
                        inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                    } else if (Integer.parseInt(String.valueOf(Bilhete[1])) == Enumeradores.TECLA_CONFIRMA) //confirma
                    {
                        easy_inner_thread.AcionarBipCurto(inner.Numero);
                        try {
                            if (inner.ObjectCatraca.getServidor_beep()) {
                                SoundUtils.tone(2500, 400);
                            }
                        } catch (Exception ex) {
                            System.out.println("Exception 17: " + ex.getMessage());
                        }
                        easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, "LIBERADO DOIS   SENTIDOS.");
                        inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                    } else if (Integer.parseInt(String.valueOf(Bilhete[1])) == Enumeradores.TECLA_ANULA) //anula
                    {
                        easy_inner_thread.LigarBackLite(inner.Numero);
                        inner.TempoInicialMensagem = (int) System.currentTimeMillis();
                        inner.CountTentativasEnvioComando = 0;
                        inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MSG_PADRAO;
                    } else if (Integer.parseInt(String.valueOf(Bilhete[1])) == Enumeradores.TECLA_FUNCAO) //função
                    {
                        inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_DEFINICAO_TECLADO;
                    }
                    inner.EstadoTeclado = Enumeradores.EstadosTeclado.TECLADO_EM_BRANCO;
                }
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "aguarda definicao teclado");
            } else {
                long temp = System.currentTimeMillis() - inner.TempoInicialPingOnLine;
                //Se passar 3 segundos sem receber nada, passa para o estado enviar ping on line, para manter o equipamento em on line.
                if ((int) temp > TEMPORIZADOR) {
                    inner.EstadoSolicitacaoPingOnLine = inner.EstadoAtual;
                    inner.CountTentativasEnvioComando = 0;
                    inner.TempoInicialPingOnLine = (int) System.currentTimeMillis();
                    inner.EstadoAtual = Enumeradores.EstadosInner.PING_ONLINE;
                }
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "aguarda definicao teclado");
            }
        } catch (Exception ex) {
            System.out.println("Exception 18: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Envia mensagem padrão estado Urna Próximo passo: ESTADO_MONITORA_URNA
     */
    private void PASSO_ESTADO_ENVIA_MSG_URNA() {
        try {
            //Testa o Retorno do comando de Envio de Mensagem Padrão On Line
            if (easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, " DEPOSITE O       CARTAO") == Enumeradores.RET_COMANDO_OK) {
                easy_inner_thread.AcionarRele2(inner.Numero);
                inner.CountTentativasEnvioComando = 0;
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_MONITORA_URNA;
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "envia msg urna");
            } else {
                //caso ele não consiga, tentará enviar três vezes, se não conseguir volta para o passo Reconectar
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inner.CountTentativasEnvioComando++;
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "envia msg urna");
            }
        } catch (Exception ex) {
            System.out.println("Exception 19: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Monitora o depósito do cartão na Urna Próximo passo:
     * ESTADO_LIBERAR_CATRACA
     */
    private void PASSO_ESTADO_MONITORA_URNA() {
        try {
            //Exibe estado do giro
            //System.out.println("Monitorando Giro de Catraca!");

            //Exibe estado do Inner no Rodapé da Janela
            //form.get(0).getLblStatus().setText("Monitorando Giro da Catraca");
            //Declaração de Variáveis..
            int[] Bilhete = new int[8];

            StringBuffer Cartao_ = new StringBuffer();
            String NumCartao_ = new String();

            //Monitora o giro da catraca..
            int Ret = easy_inner_thread.ReceberDadosOnLine(inner.Numero, Bilhete, Cartao_);

            //Testa o retorno do comando..
            if (Ret == Enumeradores.RET_COMANDO_OK) {
                //Testa se girou o não a catraca..
                if (Bilhete[0] == Enumeradores.URNA) {
                    //System.out.println("URNA RECOLHEU CARTÃO");
                    //Vai para o estado de Envio de Msg Padrão..
                    LiberaSaida = true;
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                } else if (Bilhete[0] == Enumeradores.FIM_TEMPO_ACIONAMENTO) {
                    //System.out.println("NÃO DEPOSITOU CARTÃO");
                    //easyInner.AcionarBipLongo(typInnersCadastrados[numero_inner].Numero);
                    easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, "ACESSO          NEGADO");
                    //Vai para o estado de Envio de Msg Padrão..
                    inner.TempoInicialMensagem = (int) System.currentTimeMillis();
                    inner.EstadoAtual = Enumeradores.EstadosInner.AGUARDA_TEMPO_MENSAGEM;
                }
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "monitora urna");
            } else {
                //Caso o tempo que estiver monitorando o giro chegue a 3 segundos,
                long tempo = (System.currentTimeMillis() - inner.TempoInicialPingOnLine);
                //deverá enviar o ping on line para manter o equipamento em modo on line
                if (tempo > 5000) {
                    inner.EstadoSolicitacaoPingOnLine = inner.EstadoAtual;
                    inner.CountTentativasEnvioComando = 0;
                    inner.TempoInicialPingOnLine = (int) System.currentTimeMillis();
                    inner.EstadoAtual = Enumeradores.EstadosInner.PING_ONLINE;
                }
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "monitora urna");
            }
        } catch (Exception ex) {
            System.out.println("Exception 20: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Aciona o rele do Inner 1
     */
    private void ACIONAR_RELE() {
        easy_inner_thread.AcionarBipCurto(1);
        try {
            if (inner.ObjectCatraca.getServidor_beep()) {
                SoundUtils.tone(2500, 400);
            }
        } catch (Exception ex) {
            System.out.println("Exception 21: " + ex.getMessage());
        }
        if (Inner.rele) {
            easy_inner_thread.AcionarRele1(1);
        } else {
            easy_inner_thread.AcionarRele2(1);
        }
        inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_POLLING;
        //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "aciona rele");
    }

    /**
     * Configura a mudança automática Habilita/Desabilita a mudança automática
     * do modo OffLine do Inner para OnLine e vice-versa. Habilita a mudança
     * Offline
     */
    private void PASSO_ESTADO_ENVIAR_CONFIGMUD_ONLINE_OFFLINE() {

        try {
            String TipoComunicacao = "2"; //boTipoConexao.getSelectedItem().toString();

            int Posicao = TipoComunicacao.indexOf("TCP");
            if (Posicao != -1) {
                //Habilita a mudança Offline
                easy_inner_thread.HabilitarMudancaOnLineOffLine(2, 20);
            } else {
                //Habilita a mudança Offline
                easy_inner_thread.HabilitarMudancaOnLineOffLine(1, 20);
            }

            //Configura o teclado para quando o Inner voltar para OnLine após uma queda
            //para OffLine.
            easy_inner_thread.DefinirConfiguracaoTecladoOnLine(inner.QtdDigitos, 1, 5, 17);

            //Define Mudanças OnLine
            //Função que configura BIT a BIT, Ver no manual Anexo III
            easy_inner_thread.DefinirEntradasMudancaOnLine(ConfiguraEntradasMudancaOnLine());

            if (inner.Biometrico) {
                // Configura entradas mudança OffLine com Biometria
                Integer r = easy_inner_thread.DefinirEntradasMudancaOffLineComBiometria((inner.Teclado ? Enumeradores.Opcao_SIM : Enumeradores.Opcao_NAO), 3, (byte) (inner.DoisLeitores ? 3 : 0), inner.Verificacao, inner.Identificacao);
                //System.out.println(r);
            } else {
                // Configura entradas mudança OffLine
                easy_inner_thread.DefinirEntradasMudancaOffLine((inner.Teclado ? Enumeradores.Opcao_SIM : Enumeradores.Opcao_NAO), (byte) (inner.DoisLeitores ? 1 : 3), (byte) (inner.DoisLeitores ? 2 : 0), 0);
            }

            //Define mensagem de Alteração Online -> Offline.
            easy_inner_thread.DefinirMensagemPadraoMudancaOffLine(1, " Modo OffLine");

            //Define mensagem de Alteração OffLine -> OnLine.
            easy_inner_thread.DefinirMensagemPadraoMudancaOnLine(1, "Modo Online");

            //Envia Configurações.
            int Ret = easy_inner_thread.EnviarConfiguracoesMudancaAutomaticaOnLineOffLine(inner.Numero);

            if (Ret == Enumeradores.RET_COMANDO_OK) {
                inner.CountTentativasEnvioComando = 0;
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_COLETAR_BILHETES;
                inner.TempoColeta = (int) System.currentTimeMillis() + TEMPORIZADOR;
                inner.TentativasColeta = 0;
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "enviar configmud online offline");
            } else {
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inner.CountTentativasEnvioComando++;
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "enviar configmud online offline");
            }
        } catch (Exception ex) {
            System.out.println("Exception 22: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Passo responsável pelo envio das mensagens off-line para o Inner
     */
    private void PASSO_ESTADO_ENVIAR_MSG_OFFLINE() {
        try {
            //Mensagem Entrada e Saida Offline Liberado!
            easy_inner_thread.DefinirMensagemEntradaOffLine(1, "ENTRADA LIBERADA");
            easy_inner_thread.DefinirMensagemSaidaOffLine(1, "SAIDA LIBERADA");
            easy_inner_thread.DefinirMensagemPadraoOffLine(1, "CATRACA OFFLINE");

            int Ret = easy_inner_thread.EnviarMensagensOffLine(inner.Numero);

            if (Ret == Enumeradores.RET_COMANDO_OK) {
                inner.CountTentativasEnvioComando = 0;
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_CONFIGMUD_ONLINE_OFFLINE;
                inner.TempoColeta = (int) System.currentTimeMillis() + TEMPORIZADOR;
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "enviar msg offline");
            } else {
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inner.CountTentativasEnvioComando++;
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "enviar msg offline");
            }
        } catch (Exception ex) {
            System.out.println("Exception 23: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Passo responsável pela criação e envio da lista de horários de acesso.
     *
     */
    private void PASSO_ESTADO_ENVIAR_HORARIOS() {
        try {
            if (true == false) {
                MontarHorarios();

                int Ret = easy_inner_thread.EnviarHorariosAcesso(inner.Numero);

                if (Ret == Enumeradores.RET_COMANDO_OK) {
                    inner.CountTentativasEnvioComando = 0;
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_USUARIOS_LISTAS;
                    inner.TempoColeta = (int) System.currentTimeMillis() + TEMPORIZADOR;
                    //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "enviar horarios");
                } else {
                    if (inner.CountTentativasEnvioComando >= 3) {
                        inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                    }
                    inner.CountTentativasEnvioComando++;
                    //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "enviar horarios");
                }
            } else {
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_USUARIOS_LISTAS;
            }
        } catch (Exception ex) {
            //System.out.println(ex.getMessage());
            System.out.println("Exception 24: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * MENSAGEM_PADRAO Envia mensagem acesso negado Próximo passo:
     * AGUARDA_TEMPO_MENSAGEM
     */
    private void PASSO_ENVIAR_MENSAGEM_ACESSO_NEGADO() {
        try {
            //Testa o Retorno do comando de Envio de Mensagem Padrão On Line
            //if (easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, mensagem_bloqueia_dados) == Enumeradores.RET_COMANDO_OK) {

            EnviaAtualizacao.atualiza_tela(inner, json);
            if (easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, json.getMensagem()) == Enumeradores.RET_COMANDO_OK) {
//                EnviaAtualizacao.atualiza_tela(inner, olPessoa);

                inner.TempoInicialMensagem = System.currentTimeMillis();
                easy_inner_thread.AcionarBipLongo(inner.Numero);

                try {
                    if (inner.ObjectCatraca.getServidor_beep()) {
                        SoundUtils.tone(2500, 400);
                    }
                } catch (Exception ex) {
                    System.out.println("Exception 25: " + ex.getMessage());
                }

                Thread.sleep(3000);

                //bloqueiaDados(typInnersCadastrados[numero_inner].Numero, easy_inner_thread, mensagem_bloqueia_dados);
                //bloqueiaDados(mensagem_bloqueia_dados);
                if (inner.InnerNetAcesso) {
                    easy_inner_thread.LigarLedVermelho(inner.Numero);
                }
                //Muda o passo para configuração de entradas Online.
                inner.CountTentativasEnvioComando = 0;
                inner.EstadoAtual = Enumeradores.EstadosInner.AGUARDA_TEMPO_MENSAGEM;
            } else {
                //caso ele não consiga, tentará enviar três vezes, se não conseguir volta para o passo Reconectar
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                //Adiciona 1 ao contador de tentativas
                inner.CountTentativasEnvioComando++;
            }

            EnviaAtualizacao.atualiza_tela(inner);

        } catch (Exception ex) {
            System.out.println("Exception 26: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Passo responsável pelo envio da lista usuário com acesso off-line
     */
    private void PASSO_ESTADO_ENVIAR_USUARIOS_LISTAS() {
        try {
            if (inner.Lista) {
                //Define qual tipo de lista(controle) de acesso o Inner vai utilizar.
                //Utilizar lista branca (cartões fora da lista tem o acesso negado).
                easy_inner_thread.DefinirTipoListaAcesso(1);
            } else {
                //Não utilizar a lista de acesso.
                easy_inner_thread.DefinirTipoListaAcesso(0);
            }

            if (inner.ListaBio) {
                //Chama rotina que monta o buffer de cartoes que nao irao precisar da digital
//                MontarBufferListaSemDigital();
                //Envia o buffer com a lista de usuarios sem digital
//                Ret = easyInner.EnviarListaUsuariosSemDigitalBio(Integer.parseInt(jSpnNumInner.getValue().toString()));
            }
            int Ret = 0;
            if (Ret == Enumeradores.RET_COMANDO_OK) {
                inner.CountTentativasEnvioComando = 0;
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MENSAGEM;
                inner.TempoColeta = (int) System.currentTimeMillis() + TEMPORIZADOR;
                //atualizaMonitoraCatraca(true, numero_inner, easy_inner_thread, "enviar usuario listas");
            } else {
                if (inner.CountTentativasEnvioComando >= 3) {
                    inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inner.CountTentativasEnvioComando++;
                //atualizaMonitoraCatraca(false, numero_inner, easy_inner_thread, "enviar usuario listas");
            }
        } catch (Exception ex) {
            //System.out.println(ex.getMessage());
            System.out.println("Exception 27: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * De acordo com o que foi informado (Esquerda ou Direita)F
     *
     * @param lado
     */
    private void verificaLadoCatraca(String lado) {
        if (lado.equals("Entrada")) {
            //entrada
            if (inner.ObjectCatraca.getLado_giro_catraca().equals("direita")) {
                LiberaEntrada = true;
                LiberaEntradaInvertida = false;
            } else {
                LiberaEntradaInvertida = true;
                LiberaEntrada = false;
            }
        }

        if (lado.equals("Saida")) {
            //saída
            if (inner.ObjectCatraca.getLado_giro_catraca().equals("direita")) {
                LiberaSaida = true;
                LiberaSaidaInvertida = false;
            } else {
                LiberaSaidaInvertida = true;
                LiberaSaida = false;
            }
        }
    }

    private void LiberaCatraca(RetornoJson json) {
        Integer tipo_leitor = 0; // 0 - CÓDIGO DE BARRAS
        if ((((inner.ObjectCatraca.getTipo_giro_catraca() == Enumeradores.Acionamento_Catraca_Urna)
                || (inner.ObjectCatraca.getTipo_giro_catraca() == Enumeradores.Acionamento_Catraca_Entrada_E_Saida)
                || (inner.ObjectCatraca.getTipo_giro_catraca() == Enumeradores.Acionamento_Catraca_Liberada_2_Sentidos)
                || (inner.ObjectCatraca.getTipo_giro_catraca() == Enumeradores.Acionamento_Catraca_Sentido_Giro))
                && ((tipo_leitor == 2) || (tipo_leitor == 3) || (tipo_leitor == 4)))) {
            if (inner.EstadoTeclado == Enumeradores.EstadosTeclado.TECLADO_EM_BRANCO) {
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_DEFINICAO_TECLADO;
            }

            //Se estamos trabalhando com Urna e 1 leitor
            if ((inner.Catraca) && (inner.ObjectCatraca.getTipo_giro_catraca() == Enumeradores.Acionamento_Catraca_Urna)) {
                inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIA_MSG_URNA;
            }
        } else if (inner.Catraca) {
            if (null != inner.ObjectCatraca.getTipo_giro_catraca()) {
                switch (inner.ObjectCatraca.getTipo_giro_catraca()) {
                    case Enumeradores.Acionamento_Catraca_Entrada:
                        verificaLadoCatraca("Entrada");
                        inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                        break;
                    case Enumeradores.Acionamento_Catraca_Saida:
                        verificaLadoCatraca("Saida");
                        inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                        break;
                    default:
                        inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                        break;
                }

                EnviaAtualizacao.atualiza_tela(inner, json);
            }
        } else {
            //Aciona Bip Curto..
            easy_inner_thread.AcionarBipCurto(inner.Numero);
            try {
                if (inner.ObjectCatraca.getServidor_beep()) {
                    SoundUtils.tone(2500, 400);
                }
            } catch (Exception ex) {
                System.out.println("Exception 28: " + ex.getMessage());
            }

            //Desliga Led Verde
            easy_inner_thread.LigarBackLite(inner.Numero);
            inner.TempoInicialMensagem = (int) System.currentTimeMillis();
            inner.CountTentativasEnvioComando = 0;
            easy_inner_thread.EnviarFormasEntradasOnLine(0, 0, 0, 0, 0, 0);
            easy_inner_thread.EnviarMensagemPadraoOnLine(inner.Numero, 0, "CATRACA LIBERADA");
            easy_inner_thread.AcionarRele1(inner.Numero);
            inner.EstadoAtual = Enumeradores.EstadosInner.AGUARDA_TEMPO_MENSAGEM;
        }
    }

    private boolean LiberaAcessoPelaCatraca() {
//        Integer numero_via = 0;
//
//        if (!olPessoa.numero_cartao.isEmpty()) {
//            try {
//                ResultSet rs;
//                String via_string = "00";
//                String codigo_string;
//
//                // 8 É O NÚMERO MÍNIMO DE CARACTÉRES PARA O CÓDIGO
//                // 8 ESTA DEFINIDO 8 TAMBÉM NO SINDICAL WEB
//                // INDICE FINAL SEMPRE - 1 ex 8 - 1 = 7
//                rs = new DAO().query("SELECT nr_cartao_posicao_via via, nr_cartao_posicao_codigo codigo FROM conf_social");
//
//                rs.next();
//
//                // 1 OU 2 PARA VIA = 99
//                int _via = rs.getInt("via"),
//                        _codigo = rs.getInt("codigo");
//
//                Integer cartao;
//
//                switch (olPessoa.tipo_entrada) {
//                    case "liberacao_pela_catraca":
//                        // CATRACA ---
//                        via_string = olPessoa.numero_cartao.substring(_via, _via + 2);
//                        codigo_string = olPessoa.numero_cartao.substring(_codigo, _codigo + 8);
//
//                        cartao = Integer.parseInt(codigo_string);
//                        olPessoa.numero_cartao = codigo_string;
//                        numero_via = Integer.valueOf(via_string);
//
//                        if (numero_via != 99) {
//                            rs = queryTempoLimite("SELECT func_catraca(" + cartao + "," + inner.ObjectCatraca.getDepartamento() + ", 1, " + numero_via + ")");
//                        } else {
//                            rs = queryTempoLimite("SELECT func_catraca(" + cartao + "," + inner.ObjectCatraca.getDepartamento() + ", 3, null)");
//                        }
//                        break;
//                    case "liberacao_pelo_teclado":
//                        // TECLADO CATRACA ---
//                        cartao = Integer.parseInt(olPessoa.numero_cartao);
//                        rs = queryTempoLimite("SELECT func_catraca(" + cartao + "," + inner.ObjectCatraca.getDepartamento() + ", 2, null)");
//                        break;
//                    case "liberacao_pela_leitora":
//                        // LEITORA ---
////                        via_string = NumCartao.substring(_via, _via + 2);
////                        codigo_string = NumCartao.substring(_codigo, _codigo + 8);
////
////                        cartao = Integer.parseInt(codigo_string);
////                        NumCartao = codigo_string;
////
////                        numero_via = Integer.valueOf(via_string);
////                        if (numero_via != 99) {
////                            rs = queryTempoLimite("SELECT func_catraca(" + cartao + "," + inner.ObjectCatraca.getDepartamento() + ", 1, " + numero_via + ")");
////                        } else {
////                            rs = queryTempoLimite("SELECT func_catraca(" + cartao + "," + inner.ObjectCatraca.getDepartamento() + ", 3, null)");
////                        }
//                        break;
//                }
//
//                rs.next();
//
//                Integer result = rs.getInt("func_catraca");
//
//                if (result > 0) {
//                    // ENTRADA NORMAL --
//                    if (Integer.valueOf(via_string) != 99) {
//                        rs = new DAO().query(
//                                "  SELECT p.id AS id, \n "
//                                + "       f.ds_foto AS foto, \n "
//                                + "       p.ds_nome AS nome \n "
//                                + "  FROM pes_fisica f \n "
//                                + " INNER JOIN pes_pessoa p ON p.id = f.id_pessoa \n "
//                                + " WHERE p.id = " + result
//                        );
//
//                        rs.next();
//
//                        olPessoa.convite_cartao = "cartao";
//
//                        if (inner.ObjectCatraca.getBloquear_sem_foto()) {
//                            //bloqueiaDados("PESSOA SEM FOTO CADASTRADA");
//                            //mensagem_bloqueia_dados = "PESSOA SEM FOTO CADASTRADA";
//
//                            //save_logs(tipo_entrada, result, "PESSOA SEM FOTO CADASTRADA");
//                            return false;
//                        }
//                    } else {
//                        // ENTRADA POR CONVITE --
//                        rs = new DAO().query(
//                                "  SELECT id, \n "
//                                + "       ds_nome nome, \n "
//                                + "       ds_foto_perfil foto, \n "
//                                + "       ds_documento documento \n "
//                                + "  FROM sis_pessoa \n "
//                                + " WHERE id = " + result
//                        );
//
//                        rs.next();
//
//                        olPessoa.convite_cartao = "convite";
//
//                        //save_logs(tipo_entrada, result, "ACESSO LIBERADO SEM FOTO (CONVITE)");
//                        return true;
//
//                    }
//                } else if (result == 0) {
//                    // VISITANTE
//                    //save_logs(tipo_entrada, null, "ACESSO LIBERADO VISITANTE");
//                    mensagemDados("ACESSO LIBERADO VISITANTE");
//
//                    return true;
//                } else {
//                    //String mensagem_erro = logErro(result);
//                    //easyInner.EnviarMensagemPadraoOnLine(typInnersCadastrados[numero_inner].Numero, 0, mensagem_erro);
//                    //bloqueiaDados(mensagem_erro);
//                    //mensagem_bloqueia_dados = mensagem_erro;
//
//                    //if (!NumCartao.isEmpty()) {
//                    //save_logs(tipo_entrada, Integer.valueOf(NumCartao), mensagem_erro);
//                    //} else {
//                    //save_logs(tipo_entrada, id_pessoa, mensagem_erro);
//                    //}
//                }
//            } catch (NumberFormatException | SQLException e) {
//                e.getMessage();
//            }
//        }
        return false;
    }

    /**
     * MONTAR HORARIOS Insere no buffer da dll um horário de acesso. O Inner
     * possui uma tabela de 100 horários de acesso, para cada horário é possível
     * definir 4 faixas de acesso para cada dia da semana. Tabela de horários
     * numero 1
     *
     * @throws Exception
     */
    private void MontarHorarios() throws Exception {

        //Insere no buffer da DLL horario de acesso    
        //Arq = new ArquivosDAO();
        List<String> Horarios = new ArrayList();//Arq.LerArquivo(ArquivosDAO.Horarios);
        byte Horario = 0, Dia = 0, faixa = 0, Hora = 0, Minuto = 0;

        for (String s : Horarios) {
            if (s.length() > 3) {
                String[] splitS = s.split(";");
                Horario = Byte.parseByte(splitS[0]);
                Dia = Byte.parseByte(splitS[1]);
                faixa = Byte.parseByte(splitS[2]);
                Hora = Byte.parseByte(splitS[3]);
                Minuto = Byte.parseByte(splitS[4]);

                easy_inner_thread.InserirHorarioAcesso(Horario, Dia, faixa, Hora, Minuto);
            }
        }
    }

    public RetornoJson RetornaPessoaCatraca(Integer pessoa_id, String numero_cartao) {
        RetornoJson json_webservice;
        if (pessoa_id != null) {
            json_webservice = EnviaAtualizacao.webservice(pessoa_id, inner);
        } else {
            json_webservice = EnviaAtualizacao.webservice(numero_cartao, inner);
        }
        return json_webservice;
    }

    public RetornoJson RetornaPessoaLiberada() {
        try {
            DAO dao = new DAO();
            if (!dao.getConectado()) {
                return null;
            }
            if (inner.ObjectCatraca.getVerificacao_de_liberacao()) {
                ResultSet rs_test = dao.query(
                        "SELECT COUNT(*) AS qnt \n "
                        + " FROM soc_catraca_liberada cl \n"
                        + "INNER JOIN soc_catraca c ON c.id = cl.id_catraca\n"
                        + "WHERE c.nr_numero = " + inner.Numero
                );

                rs_test.next();

                if (rs_test.getInt("qnt") > 1) {
                    dao = new DAO();
                    if (!dao.getConectado()) {
                        return null;
                    }
                    dao.query_execute("DELETE FROM soc_catraca_liberada WHERE id_catraca = " + inner.ObjectCatraca.getId());
                }

                rs_test.close();

                dao = new DAO();
                if (!dao.getConectado()) {
                    return null;
                }
                ResultSet rs = dao.query(
                        "SELECT cl.nr_pessoa AS pessoa, \n "
                        + "      cl.ds_cartao AS cartao \n "
                        + " FROM soc_catraca_liberada cl \n"
                        + "INNER JOIN soc_catraca c ON c.id = cl.id_catraca\n"
                        + "WHERE c.nr_numero = " + inner.Numero
                );

                rs.next();

                RetornoJson json_webservice = null;
                if (rs.getRow() > 0) {
                    if (rs.getObject("pessoa") != null) {
                        json_webservice = EnviaAtualizacao.webservice(rs.getInt("pessoa"), inner);
                    } else {
                        json_webservice = EnviaAtualizacao.webservice(rs.getString("cartao"), inner);
                    }
                    dao = new DAO();
                    if (!dao.getConectado()) {
                        return null;
                    }
                    dao.query("DELETE FROM soc_catraca_liberada WHERE id_catraca = " + inner.ObjectCatraca.getId());

                    rs.close();
                    return json_webservice;
                }
            }

            if (inner.ObjectCatraca.getVerificacao_de_biometria()) {
                dao = new DAO();
                if (!dao.getConectado()) {
                    return null;
                }
                ResultSet rs = dao.query(
                        "SELECT id_pessoa AS pessoa FROM pes_biometria_catraca WHERE ds_ip = '" + inner.ObjectCatraca.getIP() + "'"
                );

                rs.next();

                RetornoJson json_webservice = null;

                if (rs.getRow() > 0) {
                    if (rs.getObject("pessoa") != null) {
                        json_webservice = EnviaAtualizacao.webservice(rs.getInt("pessoa"), inner);
                    }

                    dao = new DAO();
                    if (!dao.getConectado()) {
                        return null;
                    }
                    dao.query("DELETE FROM pes_biometria_catraca WHERE ds_ip = '" + inner.ObjectCatraca.getIP() + "'");

                    rs.close();

                    return json_webservice;
                }
//                String my_ip = "";
//                DAO dao = new DAO();
//                if (!dao.getConectado()){
//                    return;
//                }
//                ResultSet rs = new DAOx().query("SELECT id_pessoa FROM pes_biometria_catraca WHERE ds_ip = '" + my_ip + "'");
//
//                rs.next();
//
//                Object id = rs.getObject("id_pessoa");
//
//                if (id == null) {
//                    //return new ObjectPessoaLibera(-1, null, null, "Digital não contrada", null, null);
//                }
//
//                if (rs.getInt("id_pessoa") == 1) {
//                    new DAOx().query("DELETE FROM pes_biometria_catraca WHERE ds_ip = '" + my_ip + "'");
//                    //return new ObjectPessoaLibera(-1, null, null, "Pessoa igual a 1, comparecer a secretaria", null, null);
//                }
//
//                // BIOMETRIA
//                new DAOx().query("DELETE FROM pes_biometria_catraca WHERE ds_ip = '" + my_ip + "'");
//                //return new ObjectPessoaLibera(rs.getInt("id_pessoa"), "liberacao_pela_biometria", null, "Catraca Liberada", null, null);
            }
        } catch (SQLException e) {
            System.out.println("Exception 29: " + e.getMessage());
            return null;
        }
        return null;
    }

    public void bloqueiaDados(Integer numero_inner, EasyInner easy_inner_thread, String smensagem) throws InterruptedException {

        //atualizaAcessoCatraca(numero_inner, easy_inner_thread, smensagem);
        Thread.sleep(101);
        //enviarAtualizacaoTelaCatraca(numero_inner);

        //form.get(0).getLblMensagem().setText(smensagem);
        //form.get(0).getLblImage().setBorder(BorderFactory.createLineBorder(Color.RED, 10));
        //form.get(0).getColorPanel().setBackground(Color.RED);
        Thread.sleep(3500);

        //atualizaAcessoCatraca(numero_inner, easy_inner_thread, "");
        Thread.sleep(101);
        //enviarAtualizacaoTelaCatraca(numero_inner);
    }

    public void mensagemDados(String smensagem) {
        //form.get(0).getLblMensagem().setText(smensagem);
        //form.get(0).getLblImage().setBorder(BorderFactory.createLineBorder(Color.RED, 10));
        //form.get(0).getColorPanel().setBackground(Color.RED);
    }

    public ResultSet queryTempoLimite(final String query_string) {
//        // EXECUTA A QUERY NORMAL ----------------------
//        ResultSet rs = null;
//        try {
//            rs = new DAOx().query(query_string);
//        } catch (Exception e) {
//            e.getMessage();
//        }
//        return rs;

        // EXECUTA A QUERY COM UM TEMPO LIMITE ----------------------
//        FutureTask<ResultSet> theTask = null;
//        try {
//            // EXECUTA UM MÉTODO SEM RETORNO Runnable()
////            // CRIA UMA NOVA TAREFA
////            theTask = new FutureTask(new Runnable() {
////                @Override
////                public void run() {
////                    try {
////                        // MÉTODO AQUI
////                    } catch (Exception e) {
////                        e.getMessage();
////                    }
////                    if (Thread.interrupted()) {
////                        return;
////                    }
////                }
////            }, rs);
//            // EXECUTA UM MÉTODO RETORNANDO UM OBJETO Callable()
//            // CRIA UMA NOVA TAREFA
//            theTask = new FutureTask(new Callable() {
//                @Override
//                public Object call() throws Exception {
//                    ResultSet rs = null;
//                    try {
//                        rs = dao.query(query_string);
//                    } catch (Exception e) {
//                        e.getMessage();
//                    }
//                    return rs;
//                }
//
//            });
//
//            // INICIA A TAREFA EM UMA NOVA THREAD
//            new Thread(theTask).start();
//
//            // BLOQUEAR A TAREFA CASO ULTRAPASSE 5 SEGUNDOS
//            return theTask.get(3L, TimeUnit.SECONDS);
//            //return theTask.get();
//        } catch (TimeoutException e) {
//            // CAI AQUI QUANDO ULTRAPASSAR OS 5 SEGUNDOS
//            //System.out.println("Processo ultrapassou os 5 segundos");
//
//            // PARAR O PROCESSO CASO DESEJADO
//            boolean cancel = theTask.cancel(true);
//            // OU SAIR DA APLICAÇÃO
//            //System.exit(1);
//        } catch (InterruptedException | ExecutionException ee) {
//            ee.getMessage();
//        }
        return null;
    }

    public String logErro(int codigo_erro) {
        try {
            DAO dao = new DAO();
            if (!dao.getConectado()) {
                return "CÓDIGO DE ERRO NÃO ENCONTRADO";
            }
            ResultSet rs = dao.query(
                    "  SELECT ce.ds_descricao AS descricao_erro \n "
                    + "  FROM soc_catraca_erro ce \n "
                    + " WHERE ce.nr_codigo = " + codigo_erro
            );

            rs.next();

            return rs.getString("descricao_erro");

        } catch (Exception e) {
            System.out.println("Exception 30: " + e.getMessage());
        }
        return "CÓDIGO DE ERRO NÃO ENCONTRADO";
    }

    public void liberarCatraca(String nome, String cartao) {
        // NOVO FORM
//        form.get(0).getLblCodigo().setText(cartao);
//        form.get(0).getLblNome().setText(nome);
//
//        Image image = getScaledImage(img.getImage(), 930, 550);
//        img.setImage(image);
//        form.get(0).getLblImage().setIcon(img);
//        form.get(0).getLblImage().setIcon(img);
//
//        form.get(0).getLblImage().setBorder(BorderFactory.createLineBorder(Color.GREEN, 10));
//        form.get(0).getColorPanel().setBackground(Color.GREEN);
    }

    public void atualizaMonitoraCatraca(Boolean status, String descricao_status) {
        Random random = new Random();
        Integer random_id = random.nextInt(10000000);
        DAO dao = new DAO();
        if (dao.getConectado()) {
            dao.query("UPDATE soc_catraca_monitora SET nr_ping = " + random_id + ", is_ativo = " + status + ", ds_status = '" + descricao_status + "' WHERE id_catraca = " + inner.ObjectCatraca.getId());
        }
    }

    public void atualizaAcessoCatraca(String mensagem) {
        DAO dao = new DAO();
        if (dao.getConectado()) {
            dao.query("UPDATE soc_catraca_monitora SET ds_mensagem = '" + mensagem + "' WHERE id_catraca = " + inner.ObjectCatraca.getId());
        }
    }

    public boolean loadBiometria() {
        try {
            // INSERE OS USUÁRIOS NA MEMÓRIA DA CATRACA QUE ESTA ATIVO E NÃO ENVIADO
            DAO dao = new DAO();
            if (!dao.getConectado()) {
                return false;
            }

            ResultSet rs = dao.query("SELECT * FROM pes_biometria WHERE is_ativo = true AND is_enviado = false AND ds_biometria <> '' AND ds_biometria2 <> ''");

            while (rs.next()) {
                Integer id_usuario = rs.getInt("id_pessoa");
                String digital1 = rs.getString("ds_biometria");
                String digital2 = rs.getString("ds_biometria2");

                byte[] b_template;
                try {
                    b_template = template404(Integer.toString(id_usuario), digital1, digital2);
                } catch (Exception e) {
                    System.out.println("Exception 31: " + e.getMessage());
                    return false;
                }

                easy_inner_thread.EnviarUsuarioBio(inner.Numero, b_template);
                /*
                 128 – Processando o último comando.  
                 129 – Falha de comunicação com a placa do Inner bio.  
                 130 – Inner bio não está no modo master.  
                 131 – Usuário já cadastrado no banco de dados do Inner bio.  
                 132 – Usuário não cadastrado no banco de dados do Inner bio.  
                 133 – Base de dados dos usuários está cheia.  
                 134 – Erro no segundo dedo do usuário.  
                 135 – Solicitação para o Inner bio inválida.  
                 136 – Template inválido.  
                 137 – Parametros inválidos. 
                 */

                int xx2 = easy_inner_thread.UsuarioFoiEnviado(inner.Numero, 0);
                //System.out.println(xx2);
                if (xx2 == 0 || xx2 == 131) {
                    dao = new DAO();
                    if (!dao.getConectado()) {
                        return false;
                    }
                    dao.query("UPDATE pes_biometria SET is_enviado = true WHERE id_pessoa = " + id_usuario);
                }
            }

            // EXCLUI USUARIOS DA CATRACA QUE ESTÃO INATIVOS E ENVIADOS
            dao = new DAO();
            if (!dao.getConectado()) {
                return false;
            }

            rs = dao.query("SELECT id_pessoa FROM pes_biometria WHERE is_ativo = false AND is_enviado = true");

            Integer r = 0;
            while (rs.next()) {
                Integer id_usuario = rs.getInt("id_pessoa");
                r = easy_inner_thread.SolicitarExclusaoUsuario(inner.Numero, EasyInnerUtils.remZeroEsquerda(String.valueOf(id_usuario)));

                Thread.sleep(10);

                Integer tentativas = 0;
                if (r == Enumeradores.RET_COMANDO_OK) {
                    do {
                        r = easy_inner_thread.UsuarioFoiExcluido(inner.Numero, 0);
                        Thread.sleep(40);

                    } while (r == Enumeradores.RET_BIO_PROCESSANDO && tentativas++ < 10);
                }
            }

            // EXCLUI USUARIOS DA CATRACA QUE ESTÃO NA MEMÓRIA E NÃO NO BANCO DE DADOS
            //método necessário para a easyinner.dll inicar a coleta do usuários
            easy_inner_thread.InicializarColetaListaUsuariosBio();

            do {
                do {
                    //Solicita uma parte(pacote) da lista de usuarios do bio
                    r = easy_inner_thread.SolicitarListaUsuariosBio(inner.Numero);
                } while (r == Enumeradores.RET_BIO_PROCESSANDO);

                if (r == Enumeradores.RET_COMANDO_OK) {
                    //Recebe uma parte da lista com os usuarios
                    do {
                        Thread.sleep(5);
                        r = easy_inner_thread.ReceberPacoteListaUsuariosBio(inner.Numero);
                    } while (r == Enumeradores.RET_BIO_PROCESSANDO);

                    //Verifica se existe um usuário
                    while (easy_inner_thread.TemProximoUsuario() == 1) {
                        StringBuffer Usuario = new StringBuffer();
                        //Pede um usuario da lista
                        r = easy_inner_thread.ReceberUsuarioLista(inner.Numero, Usuario);

                        if (r == Enumeradores.RET_COMANDO_OK) {
                            dao = new DAO();
                            if (!dao.getConectado()) {
                                return false;
                            }

                            rs = dao.query("SELECT * FROM pes_biometria WHERE id_pessoa = " + Integer.valueOf(Usuario.toString()) + " AND is_ativo = true");
                            r = 0;
                            if (!rs.next()) {
                                r = easy_inner_thread.SolicitarExclusaoUsuario(inner.Numero, EasyInnerUtils.remZeroEsquerda(Usuario.toString()));

                                Thread.sleep(10);

                                Integer tentativas = 0;
                                if (r == Enumeradores.RET_COMANDO_OK) {
                                    do {
                                        r = easy_inner_thread.UsuarioFoiExcluido(inner.Numero, 0);
                                        Thread.sleep(40);

                                    } while (r == Enumeradores.RET_BIO_PROCESSANDO && tentativas++ < 10);
                                }

                            }
                        }
                    }
                } else {
                    //JOptionPane.showMessageDialog(null, "Erro ao solicitar usuário cadastrado");
                }

                Thread.sleep(100);
            } while (easy_inner_thread.TemProximoPacote() == 1);
        } catch (SQLException | InterruptedException | NumberFormatException e) {
            System.out.println("Exception 32: " + e.getMessage());
        }

        return true;
    }

    private byte[] template404(String Cartao, String Digital1, String Digital2) {
        //Template A
        int i = 1;
        //usuario
        int k = 0;
        int j = 0;
        String aux = null;
        byte[] templateFinal = new byte[844];
        byte[] templateTemp = new byte[404];
        //master
        templateFinal[0] = 0;

        Cartao = "00000000000".substring(0, 10 - Cartao.length()) + Cartao;

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
     * Monta as configurações necessária para o funcionamento do Inner. Esta
     * função é utilizada on-line ou off-line. modo = 0 off line/modo = 1 on
     * line
     *
     * @param modo
     */
    private void MontaConfiguracaoInner(int modo) {
        try {
            // ANTES de realizar a configuração precisa definir o Padrão do cartão
            // easyInner.DefinirPadraoCartao(Enumeradores.PADRAO_TOPDATA);
            easy_inner_thread.DefinirPadraoCartao(Enumeradores.PADRAO_LIVRE);

            //Define Modo de comunicação
            if (modo == Enumeradores.MODO_OFF_LINE) {
                //Configurações para Modo Offline.
                //Prepara o Inner para trabalhar no modo Off-Line, porém essa função
                //ainda não envia essa informação para o equipamento.
                easy_inner_thread.ConfigurarInnerOffLine();
            } else {
                //Configurações para Modo Online.
                //Prepara o Inner para trabalhar no modo On-Line, porém essa função
                //ainda não envia essa informação para o equipamento.
                easy_inner_thread.ConfigurarInnerOnLine();
            }

            //Verificar
            //Acionamentos 1 e 2
            //Configura como irá funcionar o acionamento(rele) 1 e 2 do Inner, e por
            //quanto tempo ele será acionado.
            switch (inner.ObjectCatraca.getTipo_giro_catraca()) {
                //Coletor
                case Enumeradores.Acionamento_Coletor:
                    easy_inner_thread.ConfigurarAcionamento1(Enumeradores.ACIONA_REGISTRO_ENTRADA_OU_SAIDA, 3);
                    easy_inner_thread.ConfigurarAcionamento2(Enumeradores.ACIONA_REGISTRO_ENTRADA_OU_SAIDA, 2);
                    break;

                //Catraca
                case Enumeradores.Acionamento_Catraca_Entrada_E_Saida:
                    easy_inner_thread.ConfigurarAcionamento1(Enumeradores.ACIONA_REGISTRO_ENTRADA_OU_SAIDA, 5);
                    easy_inner_thread.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Entrada:
                    easy_inner_thread.ConfigurarAcionamento1(Enumeradores.ACIONA_REGISTRO_ENTRADA, 5);
                    easy_inner_thread.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Saida:
                    easy_inner_thread.ConfigurarAcionamento1(Enumeradores.ACIONA_REGISTRO_SAIDA, 5);
                    easy_inner_thread.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Urna:
                    easy_inner_thread.ConfigurarAcionamento1(Enumeradores.ACIONA_REGISTRO_ENTRADA_OU_SAIDA, 5);
                    easy_inner_thread.ConfigurarAcionamento2(Enumeradores.ACIONA_REGISTRO_SAIDA, 5);
                    break;

                case Enumeradores.Acionamento_Catraca_Saida_Liberada:
                    //Se Esquerda Selecionado - Inverte código
                    if (inner.ObjectCatraca.getLado_giro_catraca().equals("esquerda")) {
                        easy_inner_thread.ConfigurarAcionamento1(Enumeradores.CATRACA_ENTRADA_LIBERADA, 5);
                    } else {
                        easy_inner_thread.ConfigurarAcionamento1(Enumeradores.CATRACA_SAIDA_LIBERADA, 5);
                    }
                    easy_inner_thread.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Entrada_Liberada:
                    //Se Esquerda Selecionado - Inverte código
                    if (inner.ObjectCatraca.getLado_giro_catraca().equals("esquerda")) {
                        easy_inner_thread.ConfigurarAcionamento1(Enumeradores.CATRACA_SAIDA_LIBERADA, 5);
                    } else {
                        easy_inner_thread.ConfigurarAcionamento1(Enumeradores.CATRACA_ENTRADA_LIBERADA, 5);
                    }
                    easy_inner_thread.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Liberada_2_Sentidos:
                    easy_inner_thread.ConfigurarAcionamento1(Enumeradores.CATRACA_LIBERADA_DOIS_SENTIDOS, 5);
                    easy_inner_thread.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Sentido_Giro:
                    easy_inner_thread.ConfigurarAcionamento1(Enumeradores.CATRACA_LIBERADA_DOIS_SENTIDOS_MARCACAO_REGISTRO, 5);
                    easy_inner_thread.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;
            }

            //Configurar tipo do leitor
            switch (inner.TipoLeitor) {
                case Enumeradores.CODIGO_DE_BARRAS:
                    easy_inner_thread.ConfigurarTipoLeitor(Enumeradores.CODIGO_DE_BARRAS);
                    break;
                case Enumeradores.MAGNETICO:
                    easy_inner_thread.ConfigurarTipoLeitor(Enumeradores.MAGNETICO);
                    break;
                case Enumeradores.PROXIMIDADE_ABATRACK2:
                    easy_inner_thread.ConfigurarTipoLeitor(Enumeradores.PROXIMIDADE_ABATRACK2);
                    break;
                case Enumeradores.WIEGAND:
                    easy_inner_thread.ConfigurarTipoLeitor(Enumeradores.WIEGAND);
                    break;
                case Enumeradores.PROXIMIDADE_SMART_CARD:
                    easy_inner_thread.ConfigurarTipoLeitor(Enumeradores.PROXIMIDADE_SMART_CARD);
                    break;
            }

            easy_inner_thread.DefinirQuantidadeDigitosCartao(inner.QtdDigitos);

            //Habilitar teclado
            easy_inner_thread.HabilitarTeclado((inner.Teclado ? Enumeradores.Opcao_SIM : Enumeradores.Opcao_NAO), 0);

            //Define os valores para configurar os leitores de acordo com o tipo de inner
            DefineValoresParaConfigurarLeitores();
            easy_inner_thread.ConfigurarLeitor1(inner.ValorLeitor1);
            easy_inner_thread.ConfigurarLeitor2(inner.ValorLeitor2);

            //Box = Configura equipamentos com dois leitores
            if (inner.DoisLeitores) {
                // exibe mensagens do segundo leitor
                easy_inner_thread.ConfigurarWiegandDoisLeitores(1, Enumeradores.Opcao_SIM);
            }

            // Registra acesso negado
            easy_inner_thread.RegistrarAcessoNegado(1);

            //Catraca
            //Define qual será o tipo do registro realizado pelo Inner ao aproximar um
            //cartão do tipo proximidade no leitor do Inner, sem que o usuário tenha
            //pressionado a tecla entrada, saída ou função.
            if ((inner.ObjectCatraca.getTipo_giro_catraca() == Enumeradores.Acionamento_Catraca_Entrada_E_Saida) || (inner.ObjectCatraca.getTipo_giro_catraca() == Enumeradores.Acionamento_Catraca_Liberada_2_Sentidos) || (inner.ObjectCatraca.getTipo_giro_catraca() == Enumeradores.Acionamento_Catraca_Sentido_Giro)) {
                easy_inner_thread.DefinirFuncaoDefaultLeitoresProximidade(12); // 12 – Libera a catraca nos dois sentidos e registra o bilhete conforme o sentido giro.
            } else if ((inner.ObjectCatraca.getTipo_giro_catraca() == Enumeradores.Acionamento_Catraca_Entrada) || (inner.ObjectCatraca.getTipo_giro_catraca() == Enumeradores.Acionamento_Catraca_Saida_Liberada)) {
                if (inner.ObjectCatraca.getLado_giro_catraca().equals("direita")) {
                    easy_inner_thread.DefinirFuncaoDefaultLeitoresProximidade(10);  // 10 – Registrar sempre como entrada.
                } else {
                    easy_inner_thread.DefinirFuncaoDefaultLeitoresProximidade(11);  // 11 – Registrar sempre como saída.
                }
            } else if (inner.ObjectCatraca.getLado_giro_catraca().equals("direita")) {
                easy_inner_thread.DefinirFuncaoDefaultLeitoresProximidade(11);  // 11 – Registrar sempre como saída.
            } else {
                easy_inner_thread.DefinirFuncaoDefaultLeitoresProximidade(10);  // 10 – Registrar sempre como entrada.
            }

            //Configura o tipo de registro que será associado a uma marcação
            if (inner.Biometrico) {
                easy_inner_thread.DefinirFuncaoDefaultSensorBiometria(10);
            } else {
                easy_inner_thread.DefinirFuncaoDefaultSensorBiometria(0);
            }

            //Configura para receber o horario dos dados qdo Online.
            if (inner.QtdDigitos <= 14) {
                easy_inner_thread.ReceberDataHoraDadosOnLine(Enumeradores.Opcao_SIM);
            }

            // easyInner.InserirQuantidadeDigitoVariavel(8);
            // easyInner.InserirQuantidadeDigitoVariavel(10);
            // easyInner.InserirQuantidadeDigitoVariavel(14);
        } catch (Exception ex) {
            System.out.println("Exception 33: " + ex.getMessage());
            //JOptionPane.showMessageDialog(null, ex);
        }
    }

    /**
     * CONFIGURAÇÃO LEITORES De acordo com o lado da catraca, coletor ou se é
     * dois leitores
     */
    private void DefineValoresParaConfigurarLeitores() {

        //Configuração Catraca Esquerda ou Direita
        //define os valores para configurar os leitores de acordo com o tipo de inner
        if (inner.DoisLeitores) {
            if (inner.ObjectCatraca.getLado_giro_catraca().equals("direita")) {
                //Direita Selecionado
                inner.ValorLeitor1 = Enumeradores.SOMENTE_ENTRADA;
                inner.ValorLeitor2 = Enumeradores.SOMENTE_SAIDA;
            } else {
                //Esquerda Selecionado
                inner.ValorLeitor1 = Enumeradores.SOMENTE_SAIDA;
                inner.ValorLeitor2 = Enumeradores.SOMENTE_ENTRADA;
            }
        } else {
            if (inner.ObjectCatraca.getLado_giro_catraca().equals("direita")) {
                //Direita Selecionado
                inner.ValorLeitor1 = Enumeradores.ENTRADA_E_SAIDA;
            } else {
                //Esquerda Selecionado
                inner.ValorLeitor1 = Enumeradores.ENTRADA_E_SAIDA_INVERTIDAS;
            }

            inner.ValorLeitor2 = Enumeradores.DESATIVADO;
        }
    }

    /**
     * Esta rotina é responsável por identificar a versão do inner e modelo.
     *
     * @throws InterruptedException
     */
    private void DefineVersao() throws InterruptedException {
        int Versao[] = new int[30];
        long Variacao = 0;
        int Modelo[] = new int[3];

        //Solicita a versão do firmware do Inner e dados como o Idioma, se é
        //uma versão especial.
        int Ret = easy_inner_thread.ReceberVersaoFirmware(inner.Numero, Versao);

        inner.InnerAcessoBio = Versao[6];
        //Se selecionado Biometria, valida se o equipamento é compatível
        if (inner.ObjectCatraca.getBiometrico()) {
            if ((((Versao[0] != 6) && (Versao[0] != 14)) || ((Versao[0] == 14) && (inner.InnerAcessoBio == 0))) && inner.ObjectCatraca.getBiometrico()) {
                //JOptionPane.showMessageDialog(null, "Equipamento " + String.valueOf(typInnersCadastrados[numero_inner].Numero) + " não compatível com Biometria.", "Atenção", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        if (Ret == Enumeradores.RET_COMANDO_OK) {
            //Define a linha do Inner
            switch (Versao[0]) {
                case 1:
                    inner.LinhaInner = "Inner Plus";
                    break;

                case 2:
                    inner.LinhaInner = "Inner Disk";
                    break;

                case 3:
                    inner.LinhaInner = "Inner Verid";
                    break;

                case 6:
                    inner.LinhaInner = "Inner Bio";
                    break;

                case 7:
                    inner.LinhaInner = "Inner NET";
                    break;

                case 14:
                    inner.LinhaInner = "Inner Acesso";
                    inner.InnerNetAcesso = true;
                    break;
            }

            inner.VariacaoInner = Variacao;
            inner.VersaoInner = Integer.toString(Versao[3]) + '.' + Integer.toString(Versao[4]) + '.' + Integer.toString(Versao[5]);

            //Se for biometria
            if ((Versao[0] == 6) || (Versao[0] == 14 && inner.InnerAcessoBio == 1)) {

                //Solicita o modelo do Inner bio.
                Ret = easy_inner_thread.SolicitarModeloBio(inner.Numero);

                do {
                    //Retorna o resultado do comando SolicitarModeloBio, o modelo
                    //do Inner Bio é retornado por referência no parâmetro da função.
                    Ret = easy_inner_thread.ReceberModeloBio(inner.Numero, 0, Modelo);
                    Thread.sleep(100);
                } while (Ret == 128);

                //Define o modelo do Inner Bio
                switch (Modelo[0]) {
                    case 1:
                        inner.ModeloBioInner = "Modelo do bio: Light 100 usuários FIM10";
                        break;
                    case 4:
                        inner.ModeloBioInner = "Modelo do bio: 1000/4000 usuários FIM01";
                        break;
                    case 51:
                        inner.ModeloBioInner = "Modelo do bio: 1000/4000 usuários FIM2030";
                        break;
                    case 52:
                        inner.ModeloBioInner = "Modelo do bio: 1000/4000 usuários FIM2040";
                        break;
                    case 48:
                        inner.ModeloBioInner = "Modelo do bio: Light 100 usuários FIM3030";
                        break;
                    case 64:
                        inner.ModeloBioInner = "Modelo do bio: Light 100 usuários FIM3040";
                        break;
                    case 80:
                        inner.ModeloBioInner = "Modelo do bio: 1000/4000 usuários FIM5060";
                        break;
                    case 82:
                        inner.ModeloBioInner = "Modelo do bio: 1000/4000 usuários FIM5260";
                        break;
                    case 83:
                        inner.ModeloBioInner = "Modelo do bio: Light 100 usuários FIM5360";
                        break;
                    case 255:
                        inner.ModeloBioInner = "Modelo do bio: Desconhecido";
                        break;
                }

                //Solicita a versão do Inner bio.
                easy_inner_thread.SolicitarVersaoBio(inner.Numero);

                do {
                    //Retorna o resultado do comando SolicitarVersaoBio, a versão
                    //do Inner Bio é retornado por referência nos parâmetros da
                    //função.
                    Thread.sleep(100);
                    Ret = easy_inner_thread.ReceberVersaoBio(inner.Numero, 0, Versao);
                } while (Ret == 128);
                inner.VersaoBio = Integer.toString(Versao[0]) + "." + Integer.toString(Versao[1]);

                // TEXT COM A VERSÃO DA CATRACA
                //jTxaVersao.append(typInnersCadastrados[numero_inner].LinhaInner + " - Versão: " + typInnersCadastrados[numero_inner].VersaoInner + " ");
                //jTxaVersao.append(typInnersCadastrados[numero_inner].ModeloBioInner + " -> " + typInnersCadastrados[numero_inner].VersaoBio + "\r\n");
            }
        }
    }

    /**
     * Realiza a coleta dos bilhetes off line do equipamentos da linha Inner
     * Acesso
     *
     * @throws InterruptedException
     */
    private void ColetarBilhetesInnerAcesso() throws InterruptedException {
        int[] Bilhete = new int[8];
        StringBuffer Cartao;

        int receber[] = new int[2];

        //Verifica conexao
        easy_inner_thread.ReceberQuantidadeBilhetes(inner.Numero, receber);
        int QtdeBilhetes = receber[0];
        Integer Ret;
        do {
            if (QtdeBilhetes > 0) {
                do {
                    Thread.sleep(100l);

                    Cartao = new StringBuffer();
                    //Coleta um bilhete Off-Line que está armazenado na memória do Inner
                    Ret = easy_inner_thread.ColetarBilhete(inner.Numero, Bilhete, Cartao);
                    if (Ret == Enumeradores.RET_COMANDO_OK) {
                        QtdeBilhetes--;
                    }

                } while (QtdeBilhetes > 0);

                easy_inner_thread.ReceberQuantidadeBilhetes(inner.Numero, receber);
                QtdeBilhetes = receber[0];
            }
        } while (QtdeBilhetes > 0);
    }

    /**
     * Realiza a coleta dos bilhetes off line do equipamentos da linha Inner Net
     */
    private void ColetarBilhetesInnerNet() {
        try {
            int[] Bilhete = new int[8];
            StringBuffer Cartao;
            Long tempo;

            //Exibe no rodapé da janela o estado da maquina.
            //form.get(0).getLblStatus().setText("Coletando Bilhetes");
            tempo = System.currentTimeMillis() + 200;
            do {
                Cartao = new StringBuffer();
                //Envia o Comando de Coleta de Bilhetes..
                int Ret = easy_inner_thread.ColetarBilhete(inner.Numero, Bilhete, Cartao);

                //Zera a contagem de Ping Online.
                inner.CntDoEvents = 0;
                inner.CountPingFail = 0;
                inner.CountRepeatPingOnline = 0;

                //Caso exista bilhete a coletar..
                if (Ret == Enumeradores.RET_COMANDO_OK) {
                    //Recebe hora atual para inicio do PingOnline
                    inner.TempoInicialPingOnLine = (int) System.currentTimeMillis();
                }
            } while (System.currentTimeMillis() < tempo);
            inner.TentativasColeta += 1;
        } catch (Exception ex) {
            System.out.println("Exception 34: " + ex.getMessage());
            inner.EstadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Define Mudanças OnLine Função que configura BIT a BIT, Ver no manual
     * Anexo III
     *
     * @param InnerAtual
     * @return
     */
    private int ConfiguraEntradasMudancaOnLine() {
        //Habilita Teclado
        String Configuracao = (inner.Teclado ? "1" : "0");

        if (!inner.Biometrico) {
            //Código de Barras e Proximidade

            //Dois leitores
            if (inner.DoisLeitores) {
                Configuracao = "010"
                        + //Leitor 2 só saida
                        "001"
                        + //Leitor 1 só entrada
                        Configuracao;
            } else { //Apenas um leitores
                Configuracao = "000"
                        + //Leitor 2 Desativado
                        "011"
                        + //Leitor 1 configurado para Entrada e Saída
                        Configuracao;
            }

            Configuracao = "1"
                    + // Habilitado
                    Configuracao;

            /*
             --------------------------------------------------------------------------------------------------
             |       7        |     6      |   5    |   4    |   3    |    2    |      1     |        0       |
             --------------------------------------------------------------------------------------------------
             | Seta/Reseta    |  Bit 2     |  Bit 1 |  Bit 0 | Bit 2  |  Bit 1  |   Bit 0    |  Teclado       |
             |   config.      | Leitor 2   |        |        |        |         |            |                |
             |   bit-a-bit    |            |        |        |        |         |            |                |
             --------------------------------------------------------------------------------------------------
             | 1 – Habilita   | 000 – Desativa leitor        |  000 - Desativa leitor        | 1 – Habilita   |
             | 0 – Desabilita | 001 - Leitor só entrada      |  001 - Leitor só entrada      | 0 – Desabilita |
             |                | 010 - Leitor só saída        |  010 - Leitor só saída        |                |
             |                | 011 - Leitor Entrada e saída |  011 - Leitor Entrada e saída |                |
             |                | 100 - Leitor Entrada e Saída |  100 - Leitor Entrada e       |                |
             |                |   Invertido                  |   Saída Invertido             |                |
             --------------------------------------------------------------------------------------------------
             */
        } else { //Com Biometria

            Configuracao = "0"
                    + //Bit Fixo
                    "1"
                    + //Habilitado
                    inner.Identificacao
                    + //Identificação
                    inner.Verificacao
                    + //Verificação
                    "0"
                    + //Bit fixo
                    (inner.DoisLeitores ? "11" : "10")
                    + // 11 -> habilita leitor 1 e 2, 10 -> habilita apenas leitor 1
                    Configuracao;

            /*
             ------------------------------------------------------------------------------------------------------------------------
             |    7     |       6       |       5       |       4       |      3       |       2      |      1       |      0       |
             ------------------------------------------------------------------------------------------------------------------------
             | Bit fixo | Seta/Reseta   | Identificação |  Verificação  |   Bit fixo   |   Leitor 1   | Leitor 2     |  Teclado     |
             |   '0'    |    config.    |      Bio      |      Bio      |    Config    |              |              |              |
             |          | bit-a-bit bio |               |               |      L2      |              |              |              |
             |          |               |               |               |     '0'      |              |              |              |
             ------------------------------------------------------------------------------------------------------------------------
             |    0     |  1-Habilita   | 1-Habilita    | 1-Habilita    | 1-Habilita   | 1-Habilita   | 1-Habilita   | 1-Habilita   |
             |          |  0-Desabilita | 0-Desabilita  | 0-Desabilita  | 0-Desabilita | 0-Desabilita | 0-Desabilita | 0-Desabilita |
             ------------------------------------------------------------------------------------------------------------------------
             */
        }

        //Converte Binário para Decimal
        return BinarioParaDecimal(Configuracao);
    }

    /**
     * Realiza a conversão Binário para Decimal
     *
     * @param valorBinario
     * @return
     */
    private static int BinarioParaDecimal(String valorBinario) {
        int length_bin = 0, aux = 0, retorno = 0, i;
        length_bin = valorBinario.length();

        for (i = 0; i < length_bin; i++) {
            aux = Integer.parseInt(valorBinario.substring(i, i + 1));
            retorno += aux * (int) Math.pow(2, (length_bin - i)) / 2;
        }
        return (retorno);
    }

    public Inner getInner() {
        return inner;
    }

    public void setInner(Inner inner) {
        this.inner = inner;
    }

    public class UsuariosBio {

        private Integer id;
        private byte[] template;

        public UsuariosBio(Integer id, byte[] template) {
            this.id = id;
            this.template = template;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public byte[] getTemplate() {
            return template;
        }

        public void setTemplate(byte[] template) {
            this.template = template;
        }
    }

    public class Pessoa {

        private Integer id;
        private String nome;
        private String documento;
        private String foto;

        public Pessoa(Integer id_pessoa) {
            id = -1;
            nome = "";
            documento = "";
            foto = "";

            try {
                DAO dao = new DAO();
                if (!dao.getConectado()) {
                    return;
                }

                ResultSet rs = dao.query(
                        "  SELECT p.id AS id, \n "
                        + "       p.ds_nome AS nome, \n "
                        + "       p.ds_documento AS documento, \n "
                        + "       f.ds_foto AS foto \n "
                        + "  FROM pes_fisica f \n "
                        + " INNER JOIN pes_pessoa p ON p.id = f.id_pessoa \n "
                        + " WHERE p.id = " + id_pessoa
                );

                rs.next();

                id = rs.getInt("id");
                nome = rs.getString("nome");
                documento = rs.getString("documento");
                foto = rs.getString("foto");

            } catch (SQLException e) {
                System.out.println("Exception 35: " + e.getMessage());
            }
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getDocumento() {
            return documento;
        }

        public void setDocumento(String documento) {
            this.documento = documento;
        }

        public String getFoto() {
            return foto;
        }

        public void setFoto(String foto) {
            this.foto = foto;
        }
    }
}
