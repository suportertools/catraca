/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.topdata.easyInner.dao;

import com.topdata.easyInner.utils.Mac;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Claudemir Rtools
 */
public class CatracaDao extends DAO {

    public final List<Catraca> listaCatraca() {

        String macString = Mac.getInstance();

        try {
            List<Catraca> list = new ArrayList();

//            ResultSet rs = query(
//                    " SELECT c.* \n "
//                    + " FROM soc_catraca c \n "
//                    + "WHERE c.is_ativo = TRUE \n "
//                    + "  AND c.ds_mac = '" + macString + "' \n "
//                    + "ORDER BY c.nr_numero"
//            );
            ResultSet rs = query(
                    " SELECT c.* \n "
                    + " FROM vw_catraca c \n "
                    + "WHERE c.is_ativo = TRUE \n "
                    + "  AND c.ds_mac = '" + macString + "' \n "
                    + "ORDER BY c.nr_numero"
            );

            while (rs.next()) {
                list.add(
                        new Catraca(
                                rs.getInt("id"),
                                rs.getInt("nr_numero"),
                                rs.getInt("nr_porta"),
                                rs.getInt("nr_quantidade_digitos"),
                                rs.getBoolean("is_bloquear_sem_foto"),
                                rs.getInt("nr_tipo_giro_catraca"),
                                rs.getString("ds_lado_giro_catraca"),
                                rs.getInt("id_departamento"),
                                rs.getString("ds_servidor_foto"),
                                rs.getBoolean("is_servidor_beep"),
                                rs.getBoolean("is_biometrico"),
                                rs.getBoolean("is_leitor_biometrico_externo"),
                                rs.getBoolean("is_grava_frequencia_catraca"),
                                rs.getBoolean("is_verifica_biometria"),
                                rs.getBoolean("is_verifica_liberacao"),
                                "",
                                rs.getString("ds_ip"),
                                rs.getString("ds_mac"),
                                rs.getInt("nr_servidor"),
                                true,
                                rs.getInt("nr_socket_porta")
                        )
                );
            }
            return list;
        } catch (SQLException e) {
            e.getMessage();
        }
        return new ArrayList();
    }

    public final Catraca pesquisaCatraca(Integer id_catraca) {

        String macString = Mac.getInstance();

        try {

//            ResultSet rs = query(
//                    " SELECT c.* \n "
//                    + " FROM soc_catraca c \n "
//                    + "WHERE c.is_ativo = TRUE \n "
//                    + "  AND c.ds_mac = '" + macString + "' \n "
//                    + "  AND id = " + id_catraca
//            );
            ResultSet rs = query(
                    " SELECT c.* \n "
                    + " FROM vw_catraca c \n "
                    + "WHERE c.is_ativo = TRUE \n "
                    + "  AND c.ds_mac = '" + macString + "' \n "
                    + "  AND id = " + id_catraca
            );

            rs.next();

            return new Catraca(
                    rs.getInt("id"),
                    rs.getInt("nr_numero"),
                    rs.getInt("nr_porta"),
                    rs.getInt("nr_quantidade_digitos"),
                    rs.getBoolean("is_bloquear_sem_foto"),
                    rs.getInt("nr_tipo_giro_catraca"),
                    rs.getString("ds_lado_giro_catraca"),
                    rs.getInt("id_departamento"),
                    rs.getString("ds_servidor_foto"),
                    rs.getBoolean("is_servidor_beep"),
                    rs.getBoolean("is_biometrico"),
                    rs.getBoolean("is_leitor_biometrico_externo"),
                    rs.getBoolean("is_grava_frequencia_catraca"),
                    rs.getBoolean("is_verifica_biometria"),
                    rs.getBoolean("is_verifica_liberacao"),
                    "",
                    rs.getString("ds_ip"),
                    rs.getString("ds_mac"),
                    rs.getInt("nr_servidor"),
                    true,
                    rs.getInt("nr_socket_porta")
            );
        } catch (SQLException e) {
            e.getMessage();
        }
        return null;
    }
}
