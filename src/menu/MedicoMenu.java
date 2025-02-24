package menu;

import agendamento.AgendamentoMedico;
import agendamento.RealizarConsulta;
import login.medico;
import login.usuario;
import wrapper.Printer;

import java.time.LocalDate;

public class MedicoMenu extends MenuAfterLogin {
    @Override
    public void menuAfterLogin(usuario user) {
        if (!(user instanceof medico medico)) {
            Printer.println("Erro: Usuário não é um médico!");
            return;
        }

        RealizarConsulta realizarConsulta = new RealizarConsulta();
        AgendamentoMedico agendamentoMedico = new AgendamentoMedico();

        while (true) {
            Printer.println("Bom dia, Doutor(a) " + user.getNome());
            Printer.println("[1] Realizar Consulta");
            Printer.println("[2] Definir Horários Padrão");
            Printer.println("[3] Aplicar Horários Padrão a um Dia");
            Printer.println("[4] Alterar Dados da Conta");
            Printer.println("[5] Sair");

            int opcao;
            try {
                opcao = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                Printer.println("Opção inválida! Digite um número.");
                continue;
            }

            switch (opcao) {
                case 1:
                    realizarConsulta.realizarConsulta(medico);
                    break;
                case 2:
                    agendamentoMedico.definirHorariosPadrao(medico);
                    break;
                case 3:
                    Printer.print("Digite a data para aplicar os horários padrão (formato YYYY-MM-DD): ");
                    try {
                        LocalDate data = LocalDate.parse(scanner.nextLine());
                        agendamentoMedico.aplicarHorariosPadrao(medico, data);
                    } catch (Exception e) {
                        Printer.println("Data inválida! Use o formato YYYY-MM-DD.");
                    }
                    break;
                case 4:
                    Printer.println("Função de alterar dados ainda não implementada.");
                    break;
                case 5:
                    Printer.println("Saindo...");
                    return;
                default:
                    Printer.println("Opção inválida!");
                    break;
            }
        }
    }
}