package agendamento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import login.medico;
import login.paciente;
import login.usuario;
import wrapper.Printer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class RealizarConsulta {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String ARQUIVO_HISTORICO_JSON = "historico_consultas.json";
    private static final double VALOR_CONSULTA = 150.0;
    private AgendamentoPaciente agendamentoPaciente;
     Map<String, List<ConsultaRealizada>> historicoConsultas; // Histórico por paciente
    private Map<String, List<Double>> contasPendentes; // Contas por paciente

    public RealizarConsulta() {
        this.agendamentoPaciente = new AgendamentoPaciente();
        this.historicoConsultas = carregarHistoricoConsultas();
        this.contasPendentes = carregarContasPendentes();
    }

    // Método para realizar consultas
    public void realizarConsulta(medico medico) {
        if (!(medico instanceof medico)) {
            Printer.println("Apenas médicos podem realizar consultas!");
            return;
        }

        LocalDate hoje = LocalDate.now();
        Printer.println("Consultas marcadas para hoje (" + hoje + "):");
        List<AgendamentoPaciente.Agendamento> consultasHoje = listarConsultasDoDia(medico, hoje);

        if (consultasHoje.isEmpty()) {
            Printer.println("Nenhuma consulta agendada para hoje.");
            return;
        }

        Printer.print("Digite o horário atual (formato: HH:MM): ");
        LocalTime horarioAtual;
        try {
            horarioAtual = LocalTime.parse(scanner.nextLine());
        } catch (Exception e) {
            Printer.println("Horário inválido! Use o formato HH:MM.");
            return;
        }

        AgendamentoPaciente.Agendamento consultaAtual = consultasHoje.stream()
                .filter(a -> a.horarioConsulta.equals(horarioAtual))
                .findFirst()
                .orElse(null);

        if (consultaAtual != null) {
            realizarConsultaAtual(medico, consultaAtual);
        } else {
            anteciparConsulta(medico, consultasHoje, horarioAtual);
        }
    }

    // Listar consultas do dia
    private List<AgendamentoPaciente.Agendamento> listarConsultasDoDia(medico medico, LocalDate data) {
        List<AgendamentoPaciente.Agendamento> consultasHoje = new ArrayList<>();
        for (List<AgendamentoPaciente.Agendamento> agendamentos : agendamentoPaciente.agendamentos.values()) {
            for (AgendamentoPaciente.Agendamento a : agendamentos) {
                if (a.medico.getEmail().equals(medico.getEmail()) && a.dataConsulta.equals(data)) {
                    Printer.println("- " + a.paciente.getNome() + " às " + a.horarioConsulta);
                    consultasHoje.add(a);
                }
            }
        }
        return consultasHoje;
    }

    // Realizar consulta no horário atual
    private void realizarConsultaAtual(medico medico, AgendamentoPaciente.Agendamento consulta) {
        Printer.println("Iniciando consulta com " + consulta.paciente.getNome() + " às " + consulta.horarioConsulta);
        ConsultaRealizada consultaRealizada = processarConsulta(medico, consulta.paciente, consulta.dataConsulta, consulta.horarioConsulta);
        finalizarConsulta(consultaRealizada, consulta);
    }

    // Anticipar consulta
    private void anteciparConsulta(medico medico, List<AgendamentoPaciente.Agendamento> consultasHoje, LocalTime horarioAtual) {
        Printer.println("Nenhuma consulta marcada para este horário.");
        Printer.println("Deseja antecipar uma consulta? (S/N)");
        if (!scanner.nextLine().trim().toUpperCase().equals("S")) {
            return;
        }

        AgendamentoPaciente.Agendamento maisProxima = consultasHoje.stream()
                .filter(a -> a.horarioConsulta.isAfter(horarioAtual))
                .min((a1, a2) -> a1.horarioConsulta.compareTo(a2.horarioConsulta))
                .orElse(null);

        if (maisProxima == null) {
            Printer.println("Não há consultas futuras para antecipar.");
            return;
        }

        Printer.println("Consulta mais próxima: " + maisProxima.paciente.getNome() + " às " + maisProxima.horarioConsulta);
        Printer.println("Deseja realizar agora? (S/N)");
        if (scanner.nextLine().trim().toUpperCase().equals("S")) {
            ConsultaRealizada consultaRealizada = processarConsulta(medico, maisProxima.paciente, maisProxima.dataConsulta, horarioAtual);
            finalizarConsulta(consultaRealizada, maisProxima);
        }
    }

    // Processar consulta (sintomas, tratamento, etc.)
    private ConsultaRealizada processarConsulta(medico medico, usuario paciente, LocalDate data, LocalTime horario) {
        Printer.print("Digite os sintomas relatados pelo paciente: ");
        String sintomas = scanner.nextLine();

        Printer.print("Digite o tratamento prescrito: ");
        String tratamento = scanner.nextLine();

        Printer.print("Digite os medicamentos prescritos: ");
        String medicamentos = scanner.nextLine();

        Printer.print("Digite os exames solicitados: ");
        String exames = scanner.nextLine();

        return new ConsultaRealizada(paciente, medico, data, horario, sintomas, tratamento, medicamentos, exames, VALOR_CONSULTA);
    }

    // Finalizar consulta e atualizar JSONs
    private void finalizarConsulta(ConsultaRealizada consultaRealizada, AgendamentoPaciente.Agendamento agendamento) {
        Printer.println("Deseja finalizar a consulta? (S/N)");
        if (scanner.nextLine().trim().toUpperCase().equals("S")) {
            // Remover agendamento
            List<AgendamentoPaciente.Agendamento> lista = agendamentoPaciente.agendamentos.get(agendamento.paciente.getEmail());
            lista.remove(agendamento);
            if (lista.isEmpty()) {
                agendamentoPaciente.agendamentos.remove(agendamento.paciente.getEmail());
            } else {
                agendamentoPaciente.agendamentos.put(agendamento.paciente.getEmail(), lista);
            }
            agendamentoPaciente.salvarAgendamentos();

            // Salvar no histórico
            List<ConsultaRealizada> historico = historicoConsultas.getOrDefault(consultaRealizada.paciente.getEmail(), new ArrayList<>());
            historico.add(consultaRealizada);
            historicoConsultas.put(consultaRealizada.paciente.getEmail(), historico);
            salvarHistoricoConsultas();

            // Gerar conta se não tiver plano
            if (((paciente) consultaRealizada.paciente).getPlano() == login.PlanoDeSaude.NENHUM) {
                List<Double> contas = contasPendentes.getOrDefault(consultaRealizada.paciente.getEmail(), new ArrayList<>());
                contas.add(VALOR_CONSULTA);
                contasPendentes.put(consultaRealizada.paciente.getEmail(), contas);
                salvarContasPendentes();
                Printer.println("Consulta finalizada. Conta de R$" + VALOR_CONSULTA + " gerada para " + consultaRealizada.paciente.getNome() + ".");
            } else {
                Printer.println("Consulta finalizada. Paciente possui plano de saúde, sem custos adicionais.");
            }
        } else {
            Printer.println("Consulta não finalizada.");
        }
    }

    // Visualizar contas pendentes (para pacientes)
    public void visualizarContas(paciente paciente) {
        if (!(paciente instanceof paciente)) {
            Printer.println("Apenas pacientes podem visualizar contas!");
            return;
        }

        List<Double> contas = contasPendentes.getOrDefault(paciente.getEmail(), new ArrayList<>());
        if (contas.isEmpty()) {
            Printer.println("Você não possui contas pendentes.");
        } else {
            Printer.println("Contas pendentes de " + paciente.getNome() + ":");
            double total = 0;
            for (int i = 0; i < contas.size(); i++) {
                Printer.println("[" + i + "] R$" + contas.get(i));
                total += contas.get(i);
            }
            Printer.println("Total devido: R$" + total);

            // Perguntar se deseja pagar
            Printer.println("Deseja pagar a conta? (S/N)");
            String resposta = scanner.nextLine().trim().toUpperCase();

            if (resposta.equals("S")) {
                // Remover todas as contas pendentes do paciente
                contasPendentes.remove(paciente.getEmail());
                salvarContasPendentes(); // Atualizar o JSON
                Printer.println("Pagamento realizado com sucesso! Todas as contas foram quitadas.");
            } else {
                Printer.println("Voltando ao menu...");
            }
        }
    }

    // Carregar histórico de consultas

    private Map<String, List<ConsultaRealizada>> carregarHistoricoConsultas() {
        File arquivo = new File(ARQUIVO_HISTORICO_JSON);
        if (!arquivo.exists() || arquivo.length() == 0) {
            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write("{}");
            } catch (IOException e) {
                Printer.println("Erro ao criar arquivo de histórico: " + e.getMessage());
            }
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(arquivo)) {
            RuntimeTypeAdapterFactory<usuario> adapter = RuntimeTypeAdapterFactory
                .of(usuario.class, "tipo")
                .registerSubtype(paciente.class, "paciente")
                .registerSubtype(medico.class, "medico");

            Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(adapter)
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .create();

            Type tipoMapa = new TypeToken<Map<String, List<ConsultaRealizada>>>() {}.getType();
            Map<String, List<ConsultaRealizada>> loaded = gson.fromJson(reader, tipoMapa);
            return loaded != null ? loaded : new HashMap<>();
        } catch (IOException e) {
            Printer.println("Erro ao carregar histórico: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // Salvar histórico de consultas
    private void salvarHistoricoConsultas() {
        // Garantir que o campo 'tipo' esteja preenchido antes de salvar
        for (List<ConsultaRealizada> lista : historicoConsultas.values()) {
            for (ConsultaRealizada c : lista) {
                if (c.paciente instanceof paciente && (c.paciente.tipo == null || c.paciente.tipo.isEmpty())) {
                    c.paciente.tipo = "paciente";
                }
                if (c.medico instanceof medico && (c.medico.tipo == null || c.medico.tipo.isEmpty())) {
                    c.medico.tipo = "medico";
                }
            }
        }

        try (FileWriter writer = new FileWriter(ARQUIVO_HISTORICO_JSON)) {
            RuntimeTypeAdapterFactory<usuario> adapter = RuntimeTypeAdapterFactory
                .of(usuario.class, "tipo")
                .registerSubtype(paciente.class, "paciente")
                .registerSubtype(medico.class, "medico");

            Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(adapter)
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .setPrettyPrinting()
                .create();

            gson.toJson(historicoConsultas, writer);
        } catch (IOException e) {
            Printer.println("Erro ao salvar histórico: " + e.getMessage());
        }
    }

    // Carregar contas pendentes
    private Map<String, List<Double>> carregarContasPendentes() {
        File arquivo = new File("contas_pendentes.json");
        if (!arquivo.exists() || arquivo.length() == 0) {
            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write("{}");
            } catch (IOException e) {
                Printer.println("Erro ao criar arquivo de contas: " + e.getMessage());
            }
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(arquivo)) {
            Type tipoMapa = new TypeToken<Map<String, List<Double>>>() {}.getType();
            Gson gson = new GsonBuilder().create();
            Map<String, List<Double>> loaded = gson.fromJson(reader, tipoMapa);
            return loaded != null ? loaded : new HashMap<>();
        } catch (IOException e) {
            Printer.println("Erro ao carregar contas: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // Salvar contas pendentes
    private void salvarContasPendentes() {
        try (FileWriter writer = new FileWriter("contas_pendentes.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(contasPendentes, writer);
        } catch (IOException e) {
            Printer.println("Erro ao salvar contas: " + e.getMessage());
        }
    }

    // Classe interna para representar uma consulta realizada
    static class ConsultaRealizada {
        usuario paciente;
        medico medico;
        LocalDate dataConsulta;
        LocalTime horarioConsulta;
        String sintomas;
        String tratamento;
        String medicamentos;
        String exames;
        double valor;

        ConsultaRealizada(usuario paciente, medico medico, LocalDate dataConsulta, LocalTime horarioConsulta,
                          String sintomas, String tratamento, String medicamentos, String exames, double valor) {
            this.paciente = paciente;
            this.medico = medico;
            this.dataConsulta = dataConsulta;
            this.horarioConsulta = horarioConsulta;
            this.sintomas = sintomas;
            this.tratamento = tratamento;
            this.medicamentos = medicamentos;
            this.exames = exames;
            this.valor = valor;
        }
    }
}