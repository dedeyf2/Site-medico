package menu;

import login.usuario;
import wrapper.Printer;

public class MedicoMenu extends MenuAfterLogin{
    public void menuAfterLogin(usuario user){
        Printer.println("Bom dia, Doutor(a) " + user.getNome());
        Printer.println("[1] Realizar Consulta");
        Printer.println("[2] Alterar Dados da Conta");
        Printer.println(null);
    }
}
