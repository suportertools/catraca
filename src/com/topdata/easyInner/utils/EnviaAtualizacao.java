package com.topdata.easyInner.utils;

import com.topdata.easyInner.dao.Conf_Cliente;
import com.topdata.easyInner.dao.DAO;
import com.topdata.easyInner.entity.Inner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class EnviaAtualizacao {

    public static RetornoJson webservice(Integer id_pessoa, Inner inner) {
        return webservice(id_pessoa, null, inner);
    }

    public static RetornoJson webservice(String cartao, Inner inner) {
        return webservice(null, cartao, inner);
    }

    public static RetornoJson webservice(Integer id_pessoa, String cartao, Inner inner) {
        if (id_pessoa != null) {
            return retorna_pessoa_funcao(id_pessoa, inner);
        } else {
            if (cartao.length() < 10) {
                try {
                    Integer.valueOf(cartao);

                    return retorna_pessoa_funcao(Integer.valueOf(cartao), inner);
                } catch (Exception e) {
                    e.getMessage();
                    return retorna_pessoa_funcao(-11, inner);
                }
            }
            if (cartao.length() > 14) {
                return retorna_pessoa_funcao(-11, inner);
            }
            return retorna_catraca_funcao(cartao, inner);
        }
    }

    private static RetornoJson retorna_pessoa_funcao(Integer nr_pessoa, Inner inner) {
        RetornoJson json = new RetornoJson();
        try {
            if (isCaiu_conexao()) {
                return null;
            }

            if (nr_pessoa >= 0) {
                if (nr_pessoa == 0) {
                    //  LIBERA A CATRACA PARA VISITANTE
                    json = new RetornoJson(
                            nr_pessoa,
                            "VISITANTE",
                            "",
                            "",
                            null,
                            "Catraca Liberada",
                            null,
                            true
                    );
                } else {
                    // FUNÇÃO PARA VERIFICAÇÃO DA PESSOA (SE FOR LIBERADA OU NÃO)
                    ResultSet rs_funcao = new DAO().query("SELECT func_catraca(" + nr_pessoa + ", " + inner.ObjectCatraca.getDepartamento() + ", 2, null) AS retorno");
                    try {
                        rs_funcao.next();
                    } catch (Exception e) {
                        json = new RetornoJson(
                                nr_pessoa,
                                "PESSOA NÃO ENCONTRADA",
                                "",
                                "",
                                -1,
                                "Erro ao pesquisar pessoa na função",
                                null,
                                false
                        );
                        return json;
                    }

//                    ResultSet rs_pessoa = new DAO().query(
//                            "SELECT p.ds_nome AS nome, \n "
//                            + "     f.ds_foto AS foto \n "
//                            + "FROM pes_pessoa p \n "
//                            + "INNER JOIN pes_fisica f ON f.id_pessoa = p.id \n "
//                            + "WHERE p.id = " + nr_pessoa);
                    ResultSet rs_pessoa = new DAO().query(
                            "  SELECT P.nome,             \n "
                            + "       P.foto              \n "
                            + "  FROM vw_catraca_pessoa P \n "
                            + " WHERE P.codigo = " + nr_pessoa);
                    try {
                        rs_pessoa.next();
                        //  VERIFICA SE O CÓDIGO ENVIADO É VÁLIDO
                        rs_pessoa.getString("nome");
                    } catch (Exception e) {
                        json = new RetornoJson(
                                nr_pessoa,
                                "",
                                "",
                                "",
                                -1,
                                "Código da Pessoa não Encontrado",
                                null,
                                false
                        );
                        return json;
                    }

                    // SE O RETORNO DA FUNÇÃO FOR LIBERADA
                    if (rs_funcao.getInt("retorno") > 0) {
                        json = new RetornoJson(
                                nr_pessoa,
                                rs_pessoa.getString("nome"),
                                rs_pessoa.getString("foto"),
                                "",
                                null,
                                "Catraca Liberada",
                                null,
                                true
                        );
                    } else {
                        // SE O RETORNO DA FUNÇÃO NÃO FOR LIBERADA
//                        ResultSet rs_erro = new DAO().query(
//                                "  SELECT ce.id AS id, \n "
//                                + "       ce.ds_descricao AS descricao_erro \n "
//                                + "  FROM soc_catraca_erro ce \n "
//                                + " WHERE ce.nr_codigo = " + rs_funcao.getInt("retorno")
//                        );
                        ResultSet rs_erro = new DAO().query(
                                "  SELECT ce.codigo, \n "
                                + "       ce.descricao_erro \n "
                                + "  FROM vw_catraca_erro ce \n "
                                + " WHERE ce.codigo = " + rs_funcao.getInt("retorno")
                        );

                        rs_erro.next();

                        json = new RetornoJson(
                                nr_pessoa,
                                rs_pessoa.getString("nome"),
                                rs_pessoa.getString("foto"),
                                "",
                                rs_erro.getInt("codigo"),
                                rs_erro.getString("descricao_erro"),
                                null,
                                false
                        );
                        return json;
                    }
                }
            }

            if (nr_pessoa < 0) {
//                ResultSet rs_erro = new DAO().query(
//                        "  SELECT ce.id AS id, \n "
//                        + "       ce.ds_descricao AS descricao_erro \n "
//                        + "  FROM soc_catraca_erro ce \n "
//                        + " WHERE ce.nr_codigo = " + nr_pessoa
//                );
                ResultSet rs_erro = new DAO().query(
                        "  SELECT ce.codigo, \n "
                        + "       ce.descricao_erro \n "
                        + "  FROM vw_catraca_erro ce \n "
                        + " WHERE ce.codigo = " + nr_pessoa
                );

                rs_erro.next();

                json = new RetornoJson(
                        nr_pessoa,
                        "",
                        "",
                        "",
                        rs_erro.getInt("codigo"),
                        rs_erro.getString("descricao_erro"),
                        null,
                        false
                );
            }

        } catch (Exception e) {
            e.getMessage();
        }
        return json;
    }

    private static RetornoJson retorna_catraca_funcao(String cartao, Inner inner) {
        // 8 É O NÚMERO MÍNIMO DE CARACTÉRES PARA O CÓDIGO
        // 8 ESTA DEFINIDO 8 TAMBÉM NO SINDICAL WEB
        // INDICE FINAL SEMPRE - 1 ex 8 - 1 = 7
        RetornoJson json = new RetornoJson();
        try {
            if (isCaiu_conexao()) {
                return null;
            }

//            ResultSet rs_cartao = new DAO().query(
//                    "SELECT nr_cartao_posicao_via AS via, \n "
//                    + "     nr_cartao_posicao_codigo AS codigo \n "
//                    + " FROM conf_social"
//            );
            ResultSet rs_cartao = new DAO().query(
                    "SELECT via, \n "
                    + "     codigo \n "
                    + " FROM vw_conf_social"
            );
            rs_cartao.next();

            // 1 OU 2 PARA VIA = 99
            int _via = rs_cartao.getInt("via"), _codigo = rs_cartao.getInt("codigo");

            String via_string = cartao.substring(_via, _via + 2);
            Integer numero_via = Integer.valueOf(via_string);

            String codigo_string = cartao.substring(_codigo, _codigo + 8);
            Integer nr_cartao = Integer.parseInt(codigo_string);

            ResultSet rs_funcao;
            if (numero_via != 99) {
                rs_funcao = new DAO().query("SELECT func_catraca(" + nr_cartao + "," + inner.ObjectCatraca.getDepartamento() + ", 1, " + numero_via + ") AS retorno");
            } else {
                rs_funcao = new DAO().query("SELECT func_catraca(" + nr_cartao + "," + inner.ObjectCatraca.getDepartamento() + ", 3, null) AS retorno");
            }
            rs_funcao.next();

            Integer nr_retorno = rs_funcao.getInt("retorno");

            if (nr_retorno >= 0) {
                if (nr_retorno == 0) {
                    //  LIBERA A CATRACA PARA VISITANTE
                    json = new RetornoJson(
                            nr_retorno,
                            "VISITANTE",
                            "",
                            "",
                            null,
                            "Catraca Liberada",
                            numero_via,
                            true
                    );
                } else {
                    ResultSet rs_pessoa = null;

                    if (numero_via != 99) {
//                        rs_pessoa = new DAO().query(
//                                "SELECT s.ds_nome AS nome, \n "
//                                + "     s.ds_foto_perfil AS foto \n "
//                                + "FROM sis_pessoa s \n "
//                                + "WHERE s.id = " + nr_retorno);
                        rs_pessoa = new DAO().query(
                                "SELECT s.nome, \n "
                                + "     s.foto \n "
                                + "FROM vw_catraca_sis_pessoa s \n "
                                + "WHERE s.codigo = " + nr_retorno
                        );

                    } else {
//                        rs_pessoa = new DAO().query(
//                                "SELECT p.ds_nome AS nome, \n "
//                                + "     f.ds_foto AS foto \n "
//                                + "FROM pes_pessoa p \n "
//                                + "INNER JOIN pes_fisica f ON f.id_pessoa = p.id \n "
//                                + "WHERE p.id = " + nr_retorno);
                        rs_pessoa = new DAO().query(
                                "SELECT P.nome, \n "
                                + "     P.foto \n "
                                + "FROM vw_catraca_pessoa P \n "
                                + "WHERE P.codigo = " + nr_retorno);
                    }

                    rs_pessoa.next();

                    try {
                        //  VERIFICA SE O CÓDIGO ENVIADO É VÁLIDO
                        rs_pessoa.getString("nome");
                    } catch (Exception e) {
                        json = new RetornoJson(
                                nr_retorno,
                                "",
                                "",
                                "",
                                -1,
                                "Código da Pessoa não Encontrado",
                                numero_via,
                                false
                        );
                        return json;
                    }

                    json = new RetornoJson(
                            nr_retorno,
                            rs_pessoa.getString("nome"),
                            rs_pessoa.getString("foto"),
                            "",
                            null,
                            "Catraca Liberada",
                            numero_via,
                            true
                    );
                    return json;
                }
            }

            if (nr_retorno < 0) {
                // "SELECT id_pessoa FROM soc_carteirinha WHERE nr_cartao = " + nr_cartao
                ResultSet rs_carteirinha = new DAO().query(
                        "SELECT id_pessoa FROM vw_carteirinha WHERE cartao = " + nr_cartao
                );
                rs_carteirinha.next();

                Integer nr_pessoa = rs_carteirinha.getInt("id_pessoa");
//
//                ResultSet rs_pessoa = new DAO().query(
//                        "SELECT p.ds_nome AS nome, \n "
//                        + "     f.ds_foto AS foto \n "
//                        + "FROM pes_pessoa p \n "
//                        + "INNER JOIN pes_fisica f ON f.id_pessoa = p.id \n "
//                        + "WHERE p.id = " + nr_pessoa);

                ResultSet rs_pessoa = new DAO().query(
                        "SELECT p.nome, \n "
                        + "     p.foto \n "
                        + "FROM vw_catraca_pessoa p \n "
                        + "WHERE p.codigo = " + nr_pessoa);
                rs_pessoa.next();

                ResultSet rs_erro = new DAO().query(
                        "  SELECT ce.id AS id, \n "
                        + "       ce.ds_descricao AS descricao_erro \n "
//                        + "  FROM soc_catraca_erro ce \n "
                        + "  FROM vw_catraca_erro ce \n "                                
                        + " WHERE ce.nr_codigo = " + nr_retorno
                );

                rs_erro.next();

                json = new RetornoJson(
                        nr_pessoa,
                        rs_pessoa.getString("nome"),
                        rs_pessoa.getString("foto"),
                        "",
                        rs_erro.getInt("id"),
                        rs_erro.getString("descricao_erro"),
                        numero_via,
                        false
                );
            }
        } catch (SQLException | NumberFormatException e) {
            e.getMessage();
        }
        return json;
    }
//
//    public static void ping(Integer catraca_id) {
//        try {
//            Random random = new Random();
//            Integer random_id = random.nextInt(10000000);
//            if (!isCaiu_conexao()) {
//                new DAO().query_execute("UPDATE soc_catraca_monitora SET nr_ping = " + random_id + ", is_ativo = true WHERE id_catraca = " + catraca_id);
//            }
//        } catch (Exception e) {
//            e.getMessage();
//        }
//    }

    public static void status(Integer catraca_id, Boolean ativo, String status) {
        if (!isCaiu_conexao()) {
            Random random = new Random();
            Integer random_id = random.nextInt(10000000);
            // new DAO().query_execute("UPDATE soc_catraca_monitora SET nr_ping = " + random_id + ", is_ativo = " + ativo + ", ds_status = '" + status + "' WHERE id_catraca = " + catraca_id);
            new DAO().query("select func_catraca_monitora_status(" + random_id + ", " + ativo + ", '" + status + "', " + catraca_id + ")");
        }
    }

    public static void atualiza_tela(Inner inner) {
        atualiza_tela(inner, null, true);
    }

    public static void atualiza_tela(Inner inner, RetornoJson json) {
        atualiza_tela(inner, json, false);
    }

    private static void atualiza_tela(Inner inner, RetornoJson json, Boolean limpar) {
        if (limpar) {
            if (!isCaiu_conexao()) {
//                new DAO().query_execute(
//                        "UPDATE soc_catraca_monitora \n "
//                        + " SET nr_pessoa = null, \n"
//                        + "     ds_mensagem = null, \n"
//                        + "     ds_nome = null, \n"
//                        + "     ds_foto = null, \n"
//                        + "     ds_observacao = null \n"
//                        //+ "     ds_status = 'Ativa' \n"
//                        + "WHERE id_catraca = " + inner.ObjectCatraca.getId()
//                );
                new DAO().query(
                        "SELECT func_catraca_monitora_tela("
                        + "null, "
                        + "null, "
                        + "null, "
                        + "null, "
                        + "null, "
                        + "false, "
                        + "" + inner.ObjectCatraca.getId() + ""
                        + ")"
                );
            }
        } else {
            if (!isCaiu_conexao()) {
//                new DAO().query_execute(
//                        "UPDATE soc_catraca_monitora \n "
//                        + " SET nr_pessoa = " + json.getNr_pessoa() + ", \n"
//                        + "     ds_nome = '" + json.getNome() + "', \n"
//                        + "     ds_foto = '" + json.getFoto() + "', \n"
//                        + "     ds_observacao = '" + json.getObservacao() + "', \n"
//                        + "     ds_mensagem = '" + json.getMensagem() + "', \n"
//                        + "     is_liberado = " + json.getLiberado() + " \n"
//                        + "WHERE id_catraca = " + inner.ObjectCatraca.getId()
//                );
                new DAO().query(
                        "SELECT func_catraca_monitora_tela("
                        + "" + json.getNr_pessoa() + ", "
                        + "'" + json.getNome() + "', "
                        + "'" + json.getFoto() + "', "
                        + "'" + json.getObservacao() + "', "
                        + "'" + json.getMensagem() + "', "
                        + "" + json.getLiberado() + ", "
                        + "" + inner.ObjectCatraca.getId() + ")"
                );
            }
        }

        enviarAtualizacaoTelaCatraca(inner);
    }

    public static void inativar_todas_catracas() {
        try {
//            ResultSet rs = new DAO().query("SELECT nr_numero FROM soc_catraca WHERE is_ativo = true ORDER BY nr_numero");
//
//            List<Integer> list = new ArrayList();
//            while (rs.next()) {
//                list.add(rs.getInt("nr_numero"));
//            }
//
//            new DAO().query("DELETE FROM soc_catraca_monitora");
//
//            list.stream().forEach((n) -> {
//                enviarAtualizacaoTelaCatraca(n);
//            });
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public static void enviarAtualizacaoTelaCatraca(Inner inner) {
        Conf_Cliente conf_cliente = new Conf_Cliente();
        conf_cliente.loadJson();
        try {
            URL url = new URL("http://" + conf_cliente.getServidor_monitor() + "/monitorCatraca/envia_atualizacao.xhtml?cliente=" + inner.ObjectCatraca.getCliente() + "&catraca=" + inner.ObjectCatraca.getNumero() + "&servidor=" + inner.ObjectCatraca.getServidor());
            Charset charset = Charset.forName("UTF8");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
            con.setRequestMethod("GET");
            con.connect();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset))) {
                in.close();
            }
            con.disconnect();
        } catch (IOException e) {
            e.getMessage();
        }
    }

    public static boolean isCaiu_conexao() {
        // return !new DAO().getConectado();
        return !new DAO().isActive();
    }

}
