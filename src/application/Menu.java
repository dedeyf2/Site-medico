package application;

import java.util.Scanner;



import login.login;
import login.registro;

public class Menu {
	static Scanner scanner = new Scanner(System.in);
	public static void inicialização()  {
		System.out.println("você gostaria de :");
		System.out.println("1- fazer login");
		System.out.println("2- se registrar");
		int ini1 = scanner.nextInt();
		scanner.nextLine();
		switch(ini1) {
		case 1:
			System.out.println("digite suas informações ");
			System.out.println("digite seu email");
			String email = scanner.nextLine();
			System.out.println("digite sua senha");
			String senha = scanner.nextLine();
			login.autenticar(email, senha);
			break;
		case 2:
			System.out.println("você escolheu se registrar:");
			registro.registrarUsuario();
			break;
		default:
			System.out.println("Opção inválida! Por favor, escolha 1 ou 2.");
			break;
		}
	}
}
