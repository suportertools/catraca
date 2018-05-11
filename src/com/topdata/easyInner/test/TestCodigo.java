package com.topdata.easyInner.test;

import com.topdata.easyInner.utils.Debugs;
import com.topdata.easyInner.utils.EnviaAtualizacao;

public class TestCodigo {

    public static void main(String[] args) {
        Debugs.ON = true;
        if (args.length > 0) {
            if (args[0] != null && !args[0].isEmpty()) {
                EnviaAtualizacao.webservice(Integer.parseInt(args[0]), "", 12, null);
            }
        } else {
            EnviaAtualizacao.webservice(2138, "", 12, null);
        }
    }

}
