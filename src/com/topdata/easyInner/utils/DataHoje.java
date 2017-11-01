package com.topdata.easyInner.utils;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DataHoje {
  
    public static String data(){
        Date dateTime = new Date();
        //DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  data e hora
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(dateTime);
    }

}







