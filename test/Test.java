
public class Test {
    
     public static void main(String[] args){
        
    
            // 1 OU 2 PARA VIA = 99
            int _via = 8, _codigo = 0;

            String via_string = "10000013017885".substring(_via, _via + 2);
            Integer numero_via = Integer.valueOf(via_string);

            String codigo_string = "10000013017885".substring(_codigo, _codigo + 8);
            Integer nr_cartao = Integer.parseInt(codigo_string);
            System.out.println(nr_cartao);
}
    }
