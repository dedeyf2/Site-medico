package agendamento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import login.medico;
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

public class AgendamentoMedico {
    private static final String ARQUIVO_JSON = "horarios_medicos.json";
    private static final String ARQUIVO_PADRAO_JSON = "horarios_padrao_medicos.json";
    private static final int MAX_HORARIOS_POR_DIA = 4;
    private static final Scanner scanner = new Scanner(System.in);

    private Map<String, Map<LocalDate, List<LocalTime>>> horariosDisponiveis;
    private Map<String, List<LocalTime>> horariosPadrao; // Horários padrão por médico

    public AgendamentoMedico() {
        this.horariosDisponiveis = carregarHorarios();
        this.horariosPadrao = carregarHorariosPadrao();
    }

    // Método para adicionar horário disponível 
    public void adicionarHorarioDisponivel(usuario medico, LocalDate data, LocalTime horario) {
        if (!(medico instanceof medico)) {
            Printer.println("Apenas médicos podem alterar seus próprios horários!");
            return;
        }

        String emailMedico = medico.getEmail();
        Map<LocalDate, List<LocalTime>> horariosDoMedico = horariosDisponiveis.getOrDefault(emailMedico, new HashMap<>());
        List<LocalTime> horariosDoDia = horariosDoMedico.getOrDefault(data, new ArrayList<>());

        if (horariosDoDia.size() >= MAX_HORARIOS_POR_DIA) {
            Printer.println("Limite máximo de " + MAX_HORARIOS_POR_DIA + " horários por dia atingido!");
            return;
        }

        if (horariosDoDia.contains(horario)) {
            Printer.println("Este horário já está registrado para o dia " + data + "!");
            return;
        }

        horariosDoDia.add(horario);
        horariosDoMedico.put(data, horariosDoDia);
        horariosDisponiveis.put(emailMedico, horariosDoMedico);
        salvarHorarios();
        Printer.println("Horário " + horario + " adicionado com sucesso para o dia " + data + "!");
    }

    // Método para remover horário disponível 
    public void removerHorarioDisponivel(usuario medico, LocalDate data, LocalTime horario) {
        if (!(medico instanceof medico)) {
            Printer.println("Apenas médicos podem alterar seus próprios horários!");
            return;
        }

        String emailMedico = medico.getEmail();
        Map<LocalDate, List<LocalTime>> horariosDoMedico = horariosDisponiveis.get(emailMedico);

        if (horariosDoMedico == null || !horariosDoMedico.containsKey(data)) {
            Printer.println("Nenhum horário disponível encontrado para o dia " + data + "!");
            return;
        }

        List<LocalTime> horariosDoDia = horariosDoMedico.get(data);
        if (horariosDoDia.remove(horario)) {
            if (horariosDoDia.isEmpty()) {
                horariosDoMedico.remove(data);
            }
            horariosDisponiveis.put(emailMedico, horariosDoMedico);
            salvarHorarios();
            Printer.println("Horário " + horario + " removido com sucesso do dia " + data + "!");
        } else {
            Printer.println("Horário " + horario + " não encontrado para o dia " + data + "!");
        }
    }

    // Novo método para definir horários padrão
    public void definirHorariosPadrao(usuario medico) {
        if (!(medico instanceof medico)) {
            Printer.println("Apenas médicos podem definir horários padrão!");
            return;
        }

        String emailMedico = medico.getEmail();
        List<LocalTime> horariosPadraoMedico = new ArrayList<>();
        
        Printer.println("Defina até " + MAX_HORARIOS_POR_DIA + " horários padrão (formato HH:MM). Digite 'fim' para encerrar.");
        while (horariosPadraoMedico.size() < MAX_HORARIOS_POR_DIA) {
            Printer.print("Horário " + (horariosPadraoMedico.size() + 1) + ": ");
            String entrada = scanner.nextLine().trim();
            if (entrada.equalsIgnoreCase("fim")) {
                break;
            }
            try {
                LocalTime horario = LocalTime.parse(entrada);
                if (horariosPadraoMedico.contains(horario)) {
                    Printer.println("Horário já adicionado! Tente outro.");
                } else {
                    horariosPadraoMedico.add(horario);
                    Printer.println("Horário " + horario + " adicionado ao padrão.");
                }
            } catch (Exception e) {
                Printer.println("Horário inválido! Use o formato HH:MM (ex.: 09:00).");
            }
        }

        if (horariosPadraoMedico.isEmpty()) {
            Printer.println("Nenhum horário padrão definido.");
            return;
        }

        horariosPadrao.put(emailMedico, horariosPadraoMedico);
        salvarHorariosPadrao();
        Printer.println("Horários padrão salvos com sucesso!");
    }

    // Aplicar horários padrão a um dia específico
    public void aplicarHorariosPadrao(usuario medico, LocalDate data) {
        if (!(medico instanceof medico)) {
            Printer.println("Apenas médicos podem aplicar horários padrão!");
            return;
        }

        String emailMedico = medico.getEmail();
        List<LocalTime> padrao = horariosPadrao.get(emailMedico);
        if (padrao == null || padrao.isEmpty()) {
            Printer.println("Nenhum horário padrão definido para este médico!");
            return;
        }

        Map<LocalDate, List<LocalTime>> horariosDoMedico = horariosDisponiveis.getOrDefault(emailMedico, new HashMap<>());
        List<LocalTime> horariosDoDia = horariosDoMedico.getOrDefault(data, new ArrayList<>());

        for (LocalTime horario : padrao) {
            if (horariosDoDia.size() >= MAX_HORARIOS_POR_DIA) {
                Printer.println("Limite de " + MAX_HORARIOS_POR_DIA + " horários atingido para o dia " + data + "!");
                break;
            }
            if (!horariosDoDia.contains(horario)) {
                horariosDoDia.add(horario);
            }
        }

        horariosDoMedico.put(data, horariosDoDia);
        horariosDisponiveis.put(emailMedico, horariosDoMedico);
        salvarHorarios();
        Printer.println("Horários padrão aplicados ao dia " + data + " com sucesso!");
    }

    // Método para listar horários disponíveis 
    public List<LocalTime> getHorariosDisponiveis(usuario medico, LocalDate data) {
        String emailMedico = medico.getEmail();
        Map<LocalDate, List<LocalTime>> horariosDoMedico = horariosDisponiveis.getOrDefault(emailMedico, new HashMap<>());
        return horariosDoMedico.getOrDefault(data, new ArrayList<>());
    }

    // Carregar horários disponíveis 
    private Map<String, Map<LocalDate, List<LocalTime>>> carregarHorarios() {
        File arquivo = new File(ARQUIVO_JSON);
        if (!arquivo.exists() || arquivo.length() == 0) {
            try {
                arquivo.createNewFile();
                try (FileWriter writer = new FileWriter(arquivo)) {
                    writer.write("{}");
                }
            } catch (IOException e) {
                Printer.println("Erro ao criar arquivo de horários: " + e.getMessage());
            }
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(arquivo)) {
            Type tipoMapa = new TypeToken<Map<String, Map<LocalDate, List<LocalTime>>>>() {}.getType();
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .create();
            Map<String, Map<LocalDate, List<LocalTime>>> horarios = gson.fromJson(reader, tipoMapa);
            return horarios != null ? horarios : new HashMap<>();
        } catch (IOException e) {
            Printer.println("Erro ao carregar horários: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // Salvar horários disponíveis (já existente)
    private void salvarHorarios() {
        File arquivo = new File(ARQUIVO_JSON);
        try (FileWriter writer = new FileWriter(arquivo)) {
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .setPrettyPrinting()
                .create();
            gson.toJson(horariosDisponiveis, writer);
        } catch (IOException e) {
            Printer.println("Erro ao salvar horários: " + e.getMessage());
            throw new RuntimeException("Falha ao salvar os horários", e);
        }
    }

    // Carregar horários padrão
    private Map<String, List<LocalTime>> carregarHorariosPadrao() {
        File arquivo = new File(ARQUIVO_PADRAO_JSON);
        if (!arquivo.exists() || arquivo.length() == 0) {
            try {
                arquivo.createNewFile();
                try (FileWriter writer = new FileWriter(arquivo)) {
                    writer.write("{}");
                }
            } catch (IOException e) {
                Printer.println("Erro ao criar arquivo de horários padrão: " + e.getMessage());
            }
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(arquivo)) {
            Type tipoMapa = new TypeToken<Map<String, List<LocalTime>>>() {}.getType();
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .create();
            Map<String, List<LocalTime>> horarios = gson.fromJson(reader, tipoMapa);
            return horarios != null ? horarios : new HashMap<>();
        } catch (IOException e) {
            Printer.println("Erro ao carregar horários padrão: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // Salvar horários padrão
    private void salvarHorariosPadrao() {
        File arquivo = new File(ARQUIVO_PADRAO_JSON);
        try (FileWriter writer = new FileWriter(arquivo)) {
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .setPrettyPrinting()
                .create();
            gson.toJson(horariosPadrao, writer);
        } catch (IOException e) {
            Printer.println("Erro ao salvar horários padrão: " + e.getMessage());
            throw new RuntimeException("Falha ao salvar os horários padrão", e);
        }
    }
}