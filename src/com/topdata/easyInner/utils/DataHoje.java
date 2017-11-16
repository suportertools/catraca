package com.topdata.easyInner.utils;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class DataHoje {
  
    public static String data(){
        Date dateTime = new Date();
        //DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  data e hora
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(dateTime);
    }
    
    public static String hora() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        return sdf.format(gc.getTime());
    }

}