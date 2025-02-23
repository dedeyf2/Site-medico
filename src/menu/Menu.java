package menu;

import java.util.Scanner;



import login.login;
import login.registro;
import wrapper.Printer;

public class Menu implements InterfaceMenu{
	public static void inicialização()  {
		Printer.println("você gostaria de :");
		Printer.println("1- fazer login");
		Printer.println("2- se registrar");
		int ini1 = scanner.nextInt();
		scanner.nextLine();
		switch(ini1) {
		case 1:
			Printer.println("digite suas informações ");
			Printer.println("digite seu email");
			String email = scanner.nextLine();
			Printer.println("digite sua senha");
			String senha = scanner.nextLine();
			login.autenticar(email, senha);
			break;
		case 2:
			Printer.println("você escolheu se registrar:");
			registro.registrarUsuario();
			break;
		default:
			Printer.println("Opção inválida! Por favor, escolha 1 ou 2.");
			break;
		}
	}
}
