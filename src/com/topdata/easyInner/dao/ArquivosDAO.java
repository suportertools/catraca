//******************************************************************************
//A Topdata Sistemas de Automação Ltda não se responsabiliza por qualquer
//tipo de dano que este software possa causar, este exemplo deve ser utilizado
//apenas para demonstrar a comunicação com os equipamentos da linha
//inner e não deve ser alterado, por este motivo ele não deve ser incluso em
//suas aplicações comerciais.
//
//Desenvolvido em Java.
//                                           Topdata Sistemas de Automação Ltda.
//******************************************************************************.
package com.topdata.easyInner.dao;

import com.topdata.easyInner.entity.Inner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author juliano.ezequiel
 */
public class ArquivosDAO {

    public static final String UsuariosSD = "UsuariosSD";
    public static final String Horarios = "Horarios";
    public static final String Templates = "Templates";
    public static final String Sirenes = "Sirenes";

    public enum Dados {

        IdBio(0), Cartao(1), Template1(2), Template2(3);

        public int valorTpl;

        Dados(int valor) {
            valorTpl = valor;
        }
    }

    public enum Sirene {

        Hora,
        Minuto,
        Seg,
        Ter,
        Qua,
        Qui,
        Sex,
        Sab,
        Dom
    }

    public String CaminhoArquivo() throws Exception {
        String cam = "";
        cam = "c:\\BD_SDK_Exemplos\\Dados\\";
        File dir = new File(cam);
        if (!dir.exists())
        {
            cam = "c:\\BD_SDK_Exemplos\\Dados\\";
        }
        dir = new File(cam);
        if (!dir.exists())
        {
            if (!CriarArquivos(cam))
            {
                cam = "Não foi possivel criar os arquivos!";
            }
        }
        return cam;
    }

    public Boolean CriarArquivos(String caminho) {
        File d = new File(caminho);
        d.mkdirs();
        FileWriter arq;
        try {
            //Horarios
            String nvCam = caminho + "Horarios.txt";
            arq = new FileWriter(nvCam);
            arq.write("//Horário;DiaDaSemana;Faixa;Hora;Minuto\r\n"
                    + "1;1;1;8;0\r\n" + "1;1;2;12;0\r\n" + "1;1;3;13;0\r\n" + "1;1;4;18;0\r\n"
                    + "1;2;1;8;0\r\n" + "1;2;2;12;0\r\n" + "1;2;3;13;0\r\n" + "1;2;4;18;0\r\n"
                    + "1;3;1;8;0\r\n" + "1;3;2;12;0\r\n" + "1;3;3;13;0\r\n" + "1;3;4;18;0\r\n"
                    + "1;4;1;8;0\r\n" + "1;4;2;12;0\r\n" + "1;4;3;13;0\r\n" + "1;4;4;18;0\r\n"
                    + "1;5;1;8;0\r\n" + "1;5;2;12;0\r\n" + "1;5;3;13;0\r\n" + "1;5;4;18;0\r\n");
            arq.close();

            //Sirene
            nvCam = caminho + "Sirenes.txt";
            arq = new FileWriter(nvCam);
            arq.write("//Hora;Minuto;Segunda;Terca;Quarta;Quinta;Sexta;Sabado,DomingoFeriado\r\n"
                    + "07;59;1;1;1;1;1;1;0\r\n" + "11;31;1;1;1;1;1;1;0\r\n"
                    + "14;30;1;1;1;1;1;0;0\r\n" + "18;10;1;1;1;1;1;0;0");
            arq.close();

            //Templates
            nvCam = caminho + "Templates.txt";
            arq = new FileWriter(nvCam);
            arq.write("//Cartão;IdBio;Template1;Template2;DataHoraCadastro\r\n"
                    + "14954;14954;"
                    + "00000003BF47B77D581880A5B7FF495BE7F9321A5CCC65C210B0DF8B3082EAB"
                    + "072B144AE61168EBC14F569DAB66C3ADFF918C337F2155FC3EF4BEDCFDB7FD5"
                    + "8C457D850B41F6D9E13F4F3120D6722A86C17C2C920E94FB2D495AF160CDB7B"
                    + "111D279162101C68B894F0FE9D1059D8624C2234AA649B2B13E6B1F0C71DD60"
                    + "0FE38E6AB21192B2572865C1111D0FF9CC15FA7CA277C9E750156F54A9A26A3"
                    + "B3F0234D50C7F3E3B1537A4606BD5FFBBB9DDA937FDD140179365545189C7E6"
                    + "B68140E1540089B594C1ADDA6FD6AFF1F3E2A01FB2F8E42767068B97E17F158"
                    + "1F5FD97779A2866DC017309C2F444198E3B9B23D5FA7A928D71E19899247ECF"
                    + "0CCD225F7EF60349F10802A7B3F25FC9D7A35BB9626546AC35ACF43F6999441"
                    + "77110364CE8E74901071D40E61CFDCC9F741F48106E838C9E5D3BFE58FCF1EA"
                    + "11B7698956FFE0400D702E3DA69AC4FAD7D131EA2AA58ABDD673E39B2745584"
                    + "126EA6C03A2CA359C504E25C6479E7C4D0D52F507BCD7F35CA352045003698B"
                    + "4AAE1F79A94CAB6C9DF0707FF3BAACF4E34B080BCE89ACEAEC1D;"
                    + "0000000319C77D14D2B4032521B72C8F10B154EF43790E7595D3B40E50B4A40"
                    + "7D81D86A59ADA20936EF830E6A5DFDC9C1F78604A8B7FFE292E7EDEF028E468"
                    + "2540842D6E1457A0F1E6A70ECA6CBCEA0BB637C59493846EDCEF02633820E64"
                    + "D5E473A5F382D1CB9E4E865C6771979B11C06AB6FE6BCA652743C18555F77E0"
                    + "27F1BEAFCA6ED7A3458B418DEED47F9140969AA104DBE38DFA3DA854571B3FC"
                    + "B9DC88A1929FEF6F8B366D821AC961BA9D9413CA80C344A6AA170DF11E7486B"
                    + "760E439371FB0A4A1AB745DB048D06BF8A9E7F6EB67BC282599E95F50D3E2C5"
                    + "E174F6BF67D0D2E811040BA8FF61A18D9C7C3351AB911801838878CCF86383B"
                    + "2B2403EB96A2166BA0AE114E4186495ED97B1000E654CF34C8F1E9FD2ED79CD"
                    + "65E87FE6A5FB4ACD4B95BDD1528A2112CAF99FEB3A3E9F79C12FFF2BF5D379B"
                    + "4C0BAC33E48008AAAC4E838A05B2FD11F8738F7BB391FE9DE29000F035CB622"
                    + "CB3AC4DE1255E739E14FDFCCCF6EC73DEE3E048900A864D45091DEA62C939B6"
                    + "3826CF8300FD64F94E66068800E1F6CF09EBB2C206592921EA15\r\n"
                    + "14956;14956;"
                    + "00000003276E6EBA6DFCAEE2ABF292B6A7B901E5BC77E013AA23A012331F533"
                    + "335AC76FF2103CFEC8B3DDC5AC00AFDF7D0418F93493BACF840F272E3DD1BDE"
                    + "00734A83DCD2EC071FC254FD99B2B6F7C5CE57287045A79891FC16F282081BC"
                    + "7679C26EDC5A93755DF73119063DE2719EDB985438E95B92068EDF3A7E9B639"
                    + "4E004D1482ECAB5BC129D0F9DF62A2140CB8519F669A08F9EF34F0FD45D1E6A"
                    + "03C54059681FF5BAAEE57F14F10046B3FB4BB2050568D244FA3E5512AD9C870"
                    + "E2CB849C577137D9A5777B40D45009B133D61A31154409B1A3362277F30FD0B"
                    + "3FEAFEE6AF329270958B92ED1AE83EF6BBA5692B710CB8907CF8ECE8C308AF3"
                    + "229CFC5C66E90729A03167BE0F1691BEF21390E4B14D1D466E4BC72F84B48AB"
                    + "A987D391387A04628C6EF75D22D01546AAF39E55BB9AED2DE6674FD0D2B0CFC"
                    + "14637247C230BC09FD3A92A56C0F7594197BE3F825668E4BF4505B1128EE7CF"
                    + "240FC3659F7CF862453CFF9B4C2E15FC7976F801C3023631918B7EDF0AC4D1B"
                    + "D4A05586782D926EB4BAB33CDB7AE00D4BA821EC1979F462B8D5;"
                    + "00000003570960F91F5FFEF0A0D89B43D054198A86E60ACA57E478B73DFDB7B"
                    + "F4B37179ECD2F6BCE19886B941BE0CC1E78D7E4288A58727EC8772B19F99076"
                    + "FE17069E7544C294C54A70BFF2D78D55D6E0CD72C667DB645844E87BE95F334"
                    + "60E41FAF4C9061BE942655069BE4D6E679307741EA54E9DBA559AFCE359D048"
                    + "904AD6AD2BE4626F8E68D91D86E299382ECFD04280ECBD075489D32716C3DA9"
                    + "64F148CDBE536AB108ECA01B9DAB955DC420D2ACF36234848F12FE3D0808938"
                    + "D5896096DEEBF4C39EF5D4956A90EF7884E4618AC57F190FD55138AE1897ED7"
                    + "8211C2A0DA99709B0D9761D69C9E25C71DF6D82EC02F5D003FA3FC8143F5F8C"
                    + "35470459D5DD2111CC2EEC0ED5465234B99F1F6195328598A1983E20C1EC5F4"
                    + "0C84B69C11499CB8D75EFA1E68B4015CEC7A5687E3180ABA99E55EF23468474"
                    + "5B4D91C6D022CEE752FA3265EEB48F851399291E42C9616ED80C8699BB04AA2"
                    + "CB3F2E48997F993B340EC07CBE52D03966794BDDA6A34C3AA465D8274C8BE47"
                    + "CD2FDD2C8BD17443AA9ACA1C9513F2D33DAF0118E59CF0F6442D");
            arq.close();

            //UsuariosSD
            nvCam = caminho + "UsuariosSD.txt";
            arq = new FileWriter(nvCam);
            arq.write("//Cartão;SemVerificaçãoDigital\r\n"
                    + "1;1\r\n" + "187;1\r\n" + "2;1\r\n"
                    + "123;1\r\n" + "123456;1");

            arq.close();
            return true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public List<String> LerArquivo(String nomeArquivo) throws Exception {

        if (Inner.CaminhoDados == null || Inner.CaminhoDados.equals("")) {
            Inner.CaminhoDados = CaminhoArquivo();
        }

        List<String> lista = new ArrayList<>();
        try (InputStream is = new FileInputStream(Inner.CaminhoDados + nomeArquivo + ".txt")) {
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = bf.readLine()) != null) {

                if (!line.startsWith("//")) {
                    lista.add(line);
                }

            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return lista;
    }

    public void InserirRegistro(String nomeArquivo, String dado) throws Exception {
        List<String> dados = this.LerArquivo(nomeArquivo);

    }

    public void AddRegistro(String nomeArquivo, List<String> registro) throws Exception {

        List<String> Dados = this.LerArquivo(nomeArquivo);
        Dados.add(0, "//Cartão;IdBio;Template1;Template2;DataHoraCadastro");
        for (String registro1 : registro) {
            Dados.add(registro1);
        }

        try (FileWriter arq = new FileWriter(Inner.CaminhoDados + nomeArquivo + ".txt")) {
            PrintWriter gravarArq = new PrintWriter(arq);
            //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            for (String Dado : Dados) {
                gravarArq.println(Dado);
            }
            arq.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void AddRegistro(String nomeArquivo, String registro) throws Exception {

        List<String> Dados = this.LerArquivo(nomeArquivo);
        Dados.add(0, "//Cartão;IdBio;Template1;Template2;DataHoraCadastro");
        Dados.add(registro);

        try (FileWriter arq = new FileWriter(Inner.CaminhoDados + nomeArquivo + ".txt")) {
            PrintWriter gravarArq = new PrintWriter(arq);
            //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            for (String Dado : Dados) {
                gravarArq.println(Dado);
            }
            arq.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public Boolean BuscarRegistro(String Registro, List<String> lstRegistros) throws IOException {
        boolean ret = false;
        try {
            for (Object obj : lstRegistros) {
                String[] teste = obj.toString().split(";");
                if (RemZeroEsquerda(teste[0]).equals(RemZeroEsquerda(Registro))) {
                    return true;
                } else {
                    ret = false;
                }
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return ret;
    }

    public Boolean RemoveRegistro(List<String> Dados, String nomeArquivo, String RegistroParaRemover) {
        boolean DadoRemovido = false;

        try (FileWriter arq = new FileWriter(Inner.CaminhoDados + nomeArquivo + ".txt")) {
            PrintWriter gravarArq = new PrintWriter(arq);
            for (String Dado : Dados) {
                String[] teste = Dado.split(";");
                if (!RemZeroEsquerda(teste[0]).equals(RemZeroEsquerda(RegistroParaRemover))) {
                    gravarArq.println(Dado);
                } else {
                    DadoRemovido = true;
                }
            }
            arq.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return DadoRemovido;
    }

    public String RemZeroEsquerda(String valor) {
        boolean blnNum = false;
        String str = "";
        for (int i = 0; i < valor.length(); i++) {
            if (!"0".equals(valor.substring(i, i + 1))) {
                blnNum = true;
            }
            if (blnNum) {
                str += valor.substring(i, i + 1);
            }
        }
        return str;
    }
}
