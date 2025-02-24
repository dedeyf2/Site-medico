package login;

import java.util.Scanner;
import wrapper.Printer;

public class paciente extends usuario {

    public paciente(String nome, int idade, PlanoDeSaude plano, String email, String senha) {
        super(nome, idade, plano, email, senha);
    }

    @Override
    public void alterarPlano(PlanoDeSaude planoNovo) {
        throw new UnsupportedOperationException("Use o método com Scanner como parâmetro.");
    }

    // Novo método que aceita Scanner como parâmetro
    public void alterarPlano(PlanoDeSaude planoNovo, Scanner scanner) {
        Printer.println("Escolha um novo plano de saúde (HAPVIDA, AMIL, PORTO_SAUDE, NENHUM):");
        try {
            String entrada = scanner.nextLine().toUpperCase();
            planoNovo = PlanoDeSaude.valueOf(entrada);
            if (planoNovo != PlanoDeSaude.NENHUM) {
                this.plano = planoNovo;
            } else {
                this.plano = PlanoDeSaude.NENHUM;
            }
            registro.atualizarUsuario(this);
            Printer.println("Plano alterado com sucesso!");
        } catch (IllegalArgumentException e) {
            Printer.println("Plano inválido, tente outro.");
        }
    }
}