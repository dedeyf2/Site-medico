package agendamento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import login.medico;
import login.paciente;
import login.registro;
import login.usuario;
import wrapper.Printer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.stream.Collectors;

public class AgendamentoPaciente {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String ARQUIVO_AGENDAMENTOS_JSON = "agendamentos.json";
    private static final String ARQUIVO_TEMP_JSON = "agendamentos_temp.json";
    private AgendamentoMedico agendamentoMedico;
    private Map<String, usuario> usuarios;
     Map<String, List<Agendamento>> agendamentos;
    private Map<String, Queue<AgendamentoEspera>> filasEspera;

    public AgendamentoPaciente() {
        this.agendamentoMedico = new AgendamentoMedico();
        this.usuarios = registro.carregarUsuarios();
        this.agendamentos = carregarAgendamentos();
        this.filasEspera = carregarFilasEspera();
    }

    
    public void realizarAgendamento(paciente paciente) {
        if (!(paciente instanceof paciente)) {
            Printer.println("Apenas pacientes podem realizar agendamentos!");
            return;
        }

        Printer.println("Bem-vindo(a), " + paciente.getNome() + "! Vamos agendar sua consulta.");
        Printer.print("Digite a data desejada (formato: YYYY-MM-DD): ");
        LocalDate data;
        try {
            data = LocalDate.parse(scanner.nextLine());
        } catch (Exception e) {
            Printer.println("Data inválida! Use o formato YYYY-MM-DD.");
            return;
        }

        List<medico> medicosCompativeis = filtrarMedicosCompativeis(paciente, data);
        if (medicosCompativeis.isEmpty()) {
            Printer.println("Nenhum médico disponível para o seu plano na data " + data + ".");
            return;
        }

        listarMedicosCompativeis(medicosCompativeis);
        Printer.print("Digite o número do médico que deseja agendar: ");
        int escolha;
        try {
            escolha = Integer.parseInt(scanner.nextLine());
            if (escolha < 0 || escolha >= medicosCompativeis.size()) {
                Printer.println("Escolha inválida!");
                return;
            }
        } catch (NumberFormatException e) {
            Printer.println("Entrada inválida! Digite um número.");
            return;
        }
        medico medicoEscolhido = medicosCompativeis.get(escolha);

        List<LocalTime> horariosDisponiveis = agendamentoMedico.getHorariosDisponiveis(medicoEscolhido, data);
        if (horariosDisponiveis.isEmpty()) {
            Printer.println("Nenhum horário disponível para " + medicoEscolhido.getNome() + " no dia " + data + ".");
            adicionarFilaEspera(paciente, medicoEscolhido, data);
            return;
        }

        Printer.println("Horários disponíveis para " + medicoEscolhido.getNome() + ":");
        for (int i = 0; i < horariosDisponiveis.size(); i++) {
            Printer.println("[" + i + "] " + horariosDisponiveis.get(i));
        }

        Printer.print("Escolha o número do horário (ou -1 para entrar na fila de espera): ");
        int horarioEscolhido;
        try {
            horarioEscolhido = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            Printer.println("Entrada inválida! Digite um número.");
            return;
        }

        if (horarioEscolhido == -1) {
            adicionarFilaEspera(paciente, medicoEscolhido, data);
            return;
        }

        if (horarioEscolhido < 0 || horarioEscolhido >= horariosDisponiveis.size()) {
            Printer.println("Horário inválido!");
            return;
        }

        LocalTime horario = horariosDisponiveis.get(horarioEscolhido);
        Printer.println("Confirma o agendamento com " + medicoEscolhido.getNome() + " no dia " + data + " às " + horario + "? (S/N)");
        String confirmacao = scanner.nextLine().trim().toUpperCase();
        if (confirmacao.equals("S")) {
            agendamentoMedico.removerHorarioDisponivel(medicoEscolhido, data, horario);
            Printer.println("Salvando agendamento...");
            salvarAgendamento(new Agendamento(paciente, medicoEscolhido, data, horario));
            Printer.println("Agendamento realizado com sucesso!");
        } else {
            Printer.println("Agendamento cancelado.");
        }
    }
    // Método atualizado para filtrar médicos compatíveis
    private List<medico> filtrarMedicosCompativeis(paciente paciente, LocalDate data) {
        List<medico> medicosCompativeis = new ArrayList<>();
        for (usuario usuario : usuarios.values()) {
            if (usuario instanceof medico medico) {
                List<LocalTime> horarios = agendamentoMedico.getHorariosDisponiveis(medico, data);
                if (!horarios.isEmpty() || filasEspera.containsKey(medico.getEmail())) { // Inclui médicos com fila
                    if (paciente.getPlano() == login.PlanoDeSaude.NENHUM) {
                        // Paciente sem plano vê todos os médicos (qualquer plano ou apenas NENHUM)
                        medicosCompativeis.add(medico);
                    } else if (medico.getPlanosAceitos().contains(paciente.getPlano())) {
                        // Paciente com plano vê apenas médicos compatíveis
                        medicosCompativeis.add(medico);
                    }
                }
            }
        }
        return medicosCompativeis;
    }

    // Método para listar médicos (sem alterações, apenas contexto)
    private void listarMedicosCompativeis(List<medico> medicos) {
        AvaliacaoConsulta avaliacaoConsulta = new AvaliacaoConsulta();
        Printer.println("Médicos disponíveis:");
        for (int i = 0; i < medicos.size(); i++) {
            medico m = medicos.get(i);
            String especialidades = m.getEspecialidades().stream()
                                    .map(Enum::name)
                                    .collect(Collectors.joining(", "));
            double mediaEstrelas = avaliacaoConsulta.calcularMediaEstrelas(m);
            String estrelasStr = mediaEstrelas == 0.0 ? "N/A" : String.format("%.1f", mediaEstrelas);
            Printer.println("[" + i + "] " + m.getNome() + " - Especialidade: " + especialidades + " - Estrelas: " + estrelasStr);
        }
    }

    // Métodos restantes (sem alterações, apenas contexto)
    private void adicionarFilaEspera(paciente paciente, medico medico, LocalDate data) {
        Queue<AgendamentoEspera> fila = filasEspera.getOrDefault(medico.getEmail(), new LinkedList<>());
        fila.add(new AgendamentoEspera(paciente, medico, data));
        filasEspera.put(medico.getEmail(), fila);
        salvarFilasEspera();
        Printer.println("Você foi adicionado à fila de espera do Dr(a). " + medico.getNome() + " para o dia " + data + ".");
    }

    public void salvarAgendamento(Agendamento agendamento) {
        List<Agendamento> lista = agendamentos.getOrDefault(agendamento.paciente.getEmail(), new ArrayList<>());
        lista.add(agendamento);
        agendamentos.put(agendamento.paciente.getEmail(), lista);
        salvarAgendamentos();
    }

    private Map<String, List<Agendamento>> carregarAgendamentos() {
        File arquivo = new File(ARQUIVO_AGENDAMENTOS_JSON);
        if (!arquivo.exists() || arquivo.length() == 0) {
            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write("{}");
                Printer.println("Arquivo de agendamentos criado ou reiniciado como vazio.");
            } catch (IOException e) {
                Printer.println("Erro ao criar arquivo de agendamentos: " + e.getMessage());
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

            Type tipoMapa = new TypeToken<Map<String, List<Agendamento>>>() {}.getType();
            Map<String, List<Agendamento>> loaded = gson.fromJson(reader, tipoMapa);

            if (loaded == null) {
                Printer.println("Arquivo de agendamentos corrompido ou inválido. Reiniciando...");
                try (FileWriter writer = new FileWriter(arquivo)) {
                    writer.write("{}");
                } catch (IOException e) {
                    Printer.println("Erro ao reiniciar arquivo de agendamentos: " + e.getMessage());
                }
                return new HashMap<>();
            }
            Printer.println("Agendamentos carregados com sucesso: " + loaded.size() + " entradas.");
            return loaded;
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            Printer.println("Erro ao carregar agendamentos: " + e.getMessage());
            Printer.println("Reiniciando arquivo corrompido...");
            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write("{}");
            } catch (IOException ex) {
                Printer.println("Erro ao reiniciar arquivo de agendamentos: " + ex.getMessage());
            }
            return new HashMap<>();
        }
    }



 // Método atualizado para salvar agendamentos com arquivo temporário
    void salvarAgendamentos() {
        File tempFile = new File(ARQUIVO_TEMP_JSON);
        File finalFile = new File(ARQUIVO_AGENDAMENTOS_JSON);

        try (FileWriter writer = new FileWriter(tempFile)) {
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

            String jsonOutput = gson.toJson(agendamentos);
            Printer.println("Salvando no arquivo temporário: " + jsonOutput);
            writer.write(jsonOutput);
            writer.flush(); // Garante que todos os dados foram escritos
        } catch (IOException e) {
            Printer.println("Erro ao salvar no arquivo temporário: " + e.getMessage());
            return;
        }

        // Tentar mover o arquivo com retries
        boolean sucesso = false;
        int tentativas = 5; // Número de tentativas para mover o arquivo
        for (int i = 0; i < tentativas; i++) {
            try {
                if (finalFile.exists()) {
                    boolean deletado = finalFile.delete(); // Deleta antes de mover
                    if (!deletado) {
                        Printer.println("Erro ao deletar o arquivo antigo de agendamentos. Tentativa " + (i + 1));
                        Thread.sleep(100); // Pequena pausa antes da próxima tentativa
                        continue;
                    }
                }
                Files.move(tempFile.toPath(), finalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Printer.println("Arquivo de agendamentos atualizado com sucesso.");
                sucesso = true;
                break;
            } catch (IOException e) {
                Printer.println("Erro ao substituir arquivo de agendamentos. Tentativa " + (i + 1) + " de " + tentativas);
                try {
                    Thread.sleep(200); // Espera antes de tentar novamente
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        if (!sucesso) {
            Printer.println("Falha ao salvar agendamentos após várias tentativas.");
            throw new RuntimeException("Falha ao salvar os agendamentos");
        }
    }
    private Map<String, Queue<AgendamentoEspera>> carregarFilasEspera() {
        File arquivo = new File("filas_espera.json");
        if (!arquivo.exists() || arquivo.length() == 0) {
            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write("{}");
            } catch (IOException e) {
                Printer.println("Erro ao criar arquivo de filas de espera: " + e.getMessage());
            }
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(arquivo)) {
            Type tipoMapa = new TypeToken<Map<String, LinkedList<AgendamentoEspera>>>() {}.getType();
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
            Map<String, Queue<AgendamentoEspera>> loaded = gson.fromJson(reader, tipoMapa);
            return loaded != null ? loaded : new HashMap<>();
        } catch (IOException e) {
            Printer.println("Erro ao carregar filas de espera: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void salvarFilasEspera() {
        try (FileWriter writer = new FileWriter("filas_espera.json")) {
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
            gson.toJson(filasEspera, writer);
        } catch (IOException e) {
            Printer.println("Erro ao salvar filas de espera: " + e.getMessage());
        }
    }

    // Classes internas (sem alterações)
    static class Agendamento {
        usuario paciente;
        medico medico;
        LocalDate dataConsulta;
        LocalTime horarioConsulta;

        Agendamento(usuario paciente, medico medico, LocalDate dataConsulta, LocalTime horarioConsulta) {
            this.paciente = paciente;
            this.medico = medico;
            this.dataConsulta = dataConsulta;
            this.horarioConsulta = horarioConsulta;
        }
    }

    private static class AgendamentoEspera {
        usuario paciente;
        medico medico;
        LocalDate dataConsulta;

        AgendamentoEspera(usuario paciente, medico medico, LocalDate dataConsulta) {
            this.paciente = paciente;
            this.medico = medico;
            this.dataConsulta = dataConsulta;
        }
    }

    // Outros métodos como cancelarAgendamento e verificarFilaEspera permanecem iguais
    public void cancelarAgendamento(paciente paciente) {
        if (!(paciente instanceof paciente)) {
            Printer.println("Apenas pacientes podem cancelar agendamentos!");
            return;
        }

        List<Agendamento> agendamentosPaciente = agendamentos.getOrDefault(paciente.getEmail(), new ArrayList<>());
        if (agendamentosPaciente.isEmpty()) {
            Printer.println("Você não possui agendamentos para cancelar.");
            return;
        }

        Printer.println("Seus agendamentos:");
        for (int i = 0; i < agendamentosPaciente.size(); i++) {
            Agendamento a = agendamentosPaciente.get(i);
            Printer.println("[" + i + "] " + a.medico.getNome() + " - " + a.dataConsulta + " às " + a.horarioConsulta);
        }

        Printer.print("Digite o número do agendamento que deseja cancelar: ");
        int escolha;
        try {
            escolha = Integer.parseInt(scanner.nextLine());
            if (escolha < 0 || escolha >= agendamentosPaciente.size()) {
                Printer.println("Escolha inválida!");
                return;
            }
        } catch (NumberFormatException e) {
            Printer.println("Entrada inválida! Digite um número.");
            return;
        }

        Agendamento agendamento = agendamentosPaciente.get(escolha);
        Printer.println("Confirma o cancelamento do agendamento com " + agendamento.medico.getNome() +
                        " no dia " + agendamento.dataConsulta + " às " + agendamento.horarioConsulta + "? (S/N)");
        String confirmacao = scanner.nextLine().trim().toUpperCase();
        if (confirmacao.equals("S")) {
            agendamentosPaciente.remove(escolha);
            agendamentoMedico.adicionarHorarioDisponivel(agendamento.medico, agendamento.dataConsulta, agendamento.horarioConsulta);
            salvarAgendamentos();
            verificarFilaEspera(agendamento.medico, agendamento.dataConsulta, agendamento.horarioConsulta);
            Printer.println("Agendamento cancelado com sucesso!");
        } else {
            Printer.println("Cancelamento abortado.");
        }
    }

    private void verificarFilaEspera(medico medico, LocalDate data, LocalTime horario) {
        Queue<AgendamentoEspera> fila = filasEspera.get(medico.getEmail());
        if (fila != null && !fila.isEmpty()) {
            AgendamentoEspera proximo = fila.poll();
            if (proximo.dataConsulta.equals(data)) {
                salvarAgendamento(new Agendamento(proximo.paciente, medico, data, horario));
                agendamentoMedico.removerHorarioDisponivel(medico, data, horario);
                Printer.println("Paciente " + proximo.paciente.getNome() + " da fila de espera foi agendado automaticamente!");
            } else {
                fila.add(proximo); // Reinsere se a data não coincide
            }
            salvarFilasEspera();
        }
    }
}