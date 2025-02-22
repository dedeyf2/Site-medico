package login;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class paciente extends usuario {
    private transient Scanner scanner = new Scanner(System.in);

    public paciente(String nome, int idade, PlanoDeSaude plano, String email, String senha) {
        super(nome, idade, plano, email, senha);
        this.tipo ="paciente";
    }

    @Override
    public void alterarPlano(PlanoDeSaude planoNovo) {
        System.out.println("Escolha um novo plano de saúde (HAPVIDA, AMIL, PORTO_SAUDE, NENHUM):");
        try {
            String entrada = scanner.nextLine().toUpperCase();
            planoNovo = PlanoDeSaude.valueOf(entrada);
            if (planoNovo != PlanoDeSaude.NENHUM) {
                this.plano = planoNovo;
            } else {
                this.plano = PlanoDeSaude.NENHUM;
            }
            registro.atualizarUsuario(this);
            System.out.println("Plano alterado com sucesso!");
        } catch (IllegalArgumentException e) {
            System.out.println("Plano inválido, tente outro.");
        }
    }
}