package com.topdata.easyInner.utils;

import javax.swing.JOptionPane;

public class Debugs {

    public static Boolean ON = false;

    public static void breakPoint(String message) {
        if (ON) {
            Object[] options = {"Seguir", "Cancelar Depuração", "Fechar Aplicativo"};
            int response = JOptionPane.showOptionDialog(null, message, "DEBUG", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            if (response == 1) {
                System.exit(1);
            } else if (response == 2) {
                ON = false;
            }
        }
    }
}
