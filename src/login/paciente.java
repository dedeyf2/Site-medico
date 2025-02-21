package login;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;



public class paciente extends usuario{
	Scanner scanner = new Scanner(System.in);
	    public paciente(String nome, int idade, PlanoDeSaude plano, String email, String senha) {
		super(nome, idade, plano, email, senha);
		
	}
	  
	    
		@Override
		public void alterarPlano(PlanoDeSaude planoNovo) {
			try {
	    		String entrada = scanner.nextLine().toUpperCase();
	    		 planoNovo = PlanoDeSaude.valueOf(entrada);
	            this.plano = planoNovo;
	    	}catch (IllegalArgumentException e){
	            	System.out.println("Plano Invalido,tente outro.");
	            }
			
		}


}
