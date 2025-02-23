package application;

import login.medico;
import login.paciente;
import login.usuario;
import menu.MedicoMenu;
import menu.Menu;
import menu.MenuAfterLogin;
import menu.PacienteMenu;

public class Main {

	public static void main(String[] args) {
		Menu.inicialização();
		MenuAfterLogin menu = chooseMenu();
		menu.menuAfterLogin(login.login.getCurrentUser());
	}

	public static MenuAfterLogin chooseMenu(){
		usuario user = login.login.getCurrentUser();
		switch (user) {
			case medico i:
				return new MedicoMenu();
			case paciente i:
				return new PacienteMenu();
			default:
				return null;
		}
		
	}

}
