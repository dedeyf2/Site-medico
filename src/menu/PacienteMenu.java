package menu;

import agendamento.AgendamentoPaciente;
import agendamento.AvaliacaoConsulta;
import agendamento.RealizarConsulta;
import login.paciente;
import login.registro;
import login.usuario;
import wrapper.Printer;

import java.util.Map;

public class PacienteMenu extends MenuAfterLogin {
    @Override
    public void menuAfterLogin(usuario user) {
        if (!(user instanceof paciente paciente)) {
            Printer.println("Erro: Usuário não é um paciente!");
            return;
        }

        AgendamentoPaciente agendamentoPaciente = new AgendamentoPaciente();
        RealizarConsulta realizarConsulta = new RealizarConsulta();
        AvaliacaoConsulta avaliacaoConsulta = new AvaliacaoConsulta();

        while (true) {
            Printer.println("Bem-vindo(a), " + user.getNome() + "!");
            Printer.println("[1] Agendar Consulta");
            Printer.println("[2] Cancelar Consulta");
            Printer.println("[3] Visualizar Contas Pendentes");
            Printer.println("[4] Avaliar Consulta");
            Printer.println("[5] Alterar Dados da Conta");
            Printer.println("[6] Sair");

            int opcao;
            try {
                opcao = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                Printer.println("Opção inválida! Digite um número.");
                continue;
            }

            switch (opcao) {
                case 1:
                    agendamentoPaciente.realizarAgendamento(paciente);
                    break;
                case 2:
                    agendamentoPaciente.cancelarAgendamento(paciente);
                    break;
                case 3:
                    realizarConsulta.visualizarContas(paciente);
                    break;
                case 4:
                    avaliacaoConsulta.avaliarConsulta(paciente);
                    break;
                case 5:
                    alterarDadosPaciente(paciente);
                    break;
                case 6:
                    Printer.println("Saindo...");
                    return;
                default:
                    Printer.println("Opção inválida!");
                    break;
            }
        }
    }

    private void alterarDadosPaciente(paciente paciente) {
        Printer.println("O que você deseja alterar?");
        Printer.println("[1] Email");
        Printer.println("[2] Senha");
        Printer.println("[3] Plano de Saúde");
        Printer.println("[4] Voltar");

        int escolha;
        try {
            escolha = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            Printer.println("Opção inválida! Digite um número.");
            return;
        }

        switch (escolha) {
            case 1:
                Printer.print("Digite o novo email: ");
                String novoEmail = scanner.nextLine();
                Map<String, usuario> usuarios = registro.carregarUsuarios();
                if (!paciente.getEmail().equals(novoEmail) && usuarios.containsKey(novoEmail)) {
                    Printer.println("Email já está em uso!");
                } else {
                    String emailAntigo = paciente.getEmail();
                    paciente.alterarEmail(novoEmail);
                    usuarios.remove(emailAntigo);
                    usuarios.put(novoEmail, paciente);
                    registro.salvarUsuarios(usuarios);
                    Printer.println("Email alterado com sucesso!");
                }
                break;
            case 2:
                Printer.print("Digite a senha antiga: ");
                String senhaAntiga = scanner.nextLine();
                Printer.print("Digite a nova senha: ");
                String novaSenha = scanner.nextLine();
                try {
                    paciente.alterarSenha(senhaAntiga, novaSenha);
                    Printer.println("Senha alterada com sucesso!");
                } catch (IllegalArgumentException e) {
                    Printer.println(e.getMessage());
                }
                break;
            case 3:
                paciente.alterarPlano(null, scanner); // Passa o scanner existente
                break;
            case 4:
                Printer.println("Voltando ao menu...");
                break;
            default:
                Printer.println("Opção inválida!");
                break;
        }
    }
}