package login;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import wrapper.Printer;

public class medico extends usuario {
    private Set<PlanoDeSaude> planosAceitos;
    private Set<Especialidade> especialidades;
    private transient Scanner scanner = new Scanner(System.in);

    public medico(String nome, int idade, String email, PlanoDeSaude plano, Especialidade especialidadePadrao, String senha) {
        super(nome, idade, plano, email, senha);
        this.tipo = "medico";
        this.planosAceitos = new HashSet<>();
        this.planosAceitos.add(PlanoDeSaude.NENHUM);
        this.especialidades = new HashSet<>();
        this.especialidades.add(especialidadePadrao); // Padrão é CLINICO_GERAL
    }

    @Override
    public void alterarPlano(PlanoDeSaude planoNovo) {
        Printer.println("Digite 'A' para adicionar um plano ou 'R' para remover um plano:");
        String acao = scanner.nextLine().toUpperCase();
        try {
            if (acao.equals("A")) {
                if (planosAceitos.contains(PlanoDeSaude.NENHUM)) {
                    planosAceitos.remove(PlanoDeSaude.NENHUM);
                }
                Printer.println("Escolha um plano de saúde para adicionar (HAPVIDA, AMIL, PORTO_SAUDE):");
                String entrada = scanner.nextLine().toUpperCase();
                planoNovo = PlanoDeSaude.valueOf(entrada);
                planosAceitos.add(planoNovo);
                Printer.println("Plano " + planoNovo + " adicionado.");
            } else if (acao.equals("R")) {
                Printer.println("Escolha um plano de saúde para remover:");
                String entrada = scanner.nextLine().toUpperCase();
                planoNovo = PlanoDeSaude.valueOf(entrada);
                if (planosAceitos.remove(planoNovo)) {
                    Printer.println("Plano " + planoNovo + " removido.");
                    if (planosAceitos.isEmpty()) {
                        planosAceitos.add(PlanoDeSaude.NENHUM);
                        Printer.println("Nenhum plano cadastrado. Definindo como NENHUM.");
                    }
                } else {
                    Printer.println("Este plano não estava cadastrado.");
                }
            } else {
                Printer.println("Opção inválida.");
            }
            registro.atualizarUsuario(this);
        } catch (IllegalArgumentException e) {
            Printer.println("Plano inválido, tente outro.");
        }
    }

    public Set<PlanoDeSaude> getPlanosAceitos() {
        return planosAceitos;
    }

    public Set<Especialidade> getEspecialidades() {
        return especialidades;
    }
}