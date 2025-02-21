package login;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class medico extends usuario {
    private Set<PlanoDeSaude> planosAceitos;
    private Scanner scanner = new Scanner(System.in);

    public medico(String nome, int idade, String email,PlanoDeSaude plano, String senha) {
        super(nome, idade, plano, senha, email);
        this.planosAceitos = new HashSet<>(); // Começa vazio
        this.planosAceitos.add(PlanoDeSaude.NENHUM); // Padrão: Nenhum plano
    }

    @Override
    public void alterarPlano(PlanoDeSaude planoNovo) {
        System.out.println("Digite 'A' para adicionar um plano ou 'R' para remover:");
        String acao = scanner.nextLine().toUpperCase();

        try {
            if (acao.equals("A")) {
                if (planosAceitos.contains(PlanoDeSaude.NENHUM)) {
                    planosAceitos.remove(PlanoDeSaude.NENHUM); // Se tiver "NENHUM", remove antes de adicionar novos
                }
                planosAceitos.add(planoNovo);
                System.out.println("Plano " + planoNovo + " adicionado.");
            } else if (acao.equals("R")) {
                if (planosAceitos.remove(planoNovo)) {
                    System.out.println("Plano " + planoNovo + " removido.");
                    if (planosAceitos.isEmpty()) {
                        planosAceitos.add(PlanoDeSaude.NENHUM); // Se ficou sem planos, adiciona "NENHUM"
                        System.out.println("Nenhum plano cadastrado. Definindo como NENHUM.");
                    }
                } else {
                    System.out.println("Este plano não estava cadastrado.");
                }
            } else {
                System.out.println("Opção inválida.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Plano inválido, tente outro.");
        }
    }

    public Set<PlanoDeSaude> getPlanosAceitos() {
        return planosAceitos;
    }
}