package menu;

import agendamento.AgendamentoPaciente;
import agendamento.AvaliacaoConsulta;
import agendamento.RealizarConsulta;
import login.paciente;
import login.usuario;
import wrapper.Printer;

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
                    Printer.println("Função de alterar dados ainda não implementada.");
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
}