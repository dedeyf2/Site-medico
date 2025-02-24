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

public class AvaliacaoConsulta {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String ARQUIVO_AVALIACOES_JSON = "avaliacoes.json";
    private RealizarConsulta realizarConsulta;
    private Map<String, List<Avaliacao>> avaliacoes; // Avaliações por consulta (chave composta)

    public AvaliacaoConsulta() {
        this.realizarConsulta = new RealizarConsulta();
        this.avaliacoes = carregarAvaliacoes();
    }

    // Método para avaliar uma consulta
    public void avaliarConsulta(paciente paciente) {
        if (!(paciente instanceof paciente)) {
            Printer.println("Apenas pacientes podem avaliar consultas!");
            return;
        }

        List<RealizarConsulta.ConsultaRealizada> consultasRealizadas = realizarConsulta.historicoConsultas
                .getOrDefault(paciente.getEmail(), new ArrayList<>());

        if (consultasRealizadas.isEmpty()) {
            Printer.println("Você não possui consultas realizadas para avaliar.");
            return;
        }

        // Mostrar até as últimas 5 consultas
        Printer.println("Suas últimas consultas realizadas (máximo 5):");
        int limite = Math.min(consultasRealizadas.size(), 5);
        for (int i = 0; i < limite; i++) {
            RealizarConsulta.ConsultaRealizada consulta = consultasRealizadas.get(consultasRealizadas.size() - 1 - i);
            Printer.println("[" + i + "] " + consulta.medico.getNome() + " - " + consulta.dataConsulta + " às " + consulta.horarioConsulta);
        }

        Printer.print("Escolha o número da consulta que deseja avaliar: ");
        int escolha;
        try {
            escolha = Integer.parseInt(scanner.nextLine());
            if (escolha < 0 || escolha >= limite) {
                Printer.println("Escolha inválida!");
                return;
            }
        } catch (NumberFormatException e) {
            Printer.println("Entrada inválida! Digite um número.");
            return;
        }

        RealizarConsulta.ConsultaRealizada consultaEscolhida = consultasRealizadas.get(consultasRealizadas.size() - 1 - escolha);
        String chaveConsulta = gerarChaveConsulta(consultaEscolhida);

        // Verificar se já foi avaliada
        if (avaliacoes.containsKey(chaveConsulta)) {
            Printer.println("Esta consulta já foi avaliada!");
            return;
        }

        // Avaliação com estrelas
        Printer.print("Digite a nota (0 a 5 estrelas): ");
        int estrelas;
        try {
            estrelas = Integer.parseInt(scanner.nextLine());
            if (estrelas < 0 || estrelas > 5) {
                Printer.println("Nota inválida! Use um valor entre 0 e 5.");
                return;
            }
        } catch (NumberFormatException e) {
            Printer.println("Entrada inválida! Digite um número.");
            return;
        }

        // Avaliação textual (até 5 linhas)
        Printer.println("Digite sua avaliação (máximo 5 linhas, pressione Enter após cada linha, deixe em branco para encerrar):");
        List<String> textoAvaliacao = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String linha = scanner.nextLine().trim();
            if (linha.isEmpty()) {
                break;
            }
            textoAvaliacao.add(linha);
        }

        Avaliacao avaliacao = new Avaliacao(paciente, consultaEscolhida.medico, consultaEscolhida.dataConsulta,
                consultaEscolhida.horarioConsulta, estrelas, textoAvaliacao);
        List<Avaliacao> listaAvaliacoes = new ArrayList<>();
        listaAvaliacoes.add(avaliacao);
        avaliacoes.put(chaveConsulta, listaAvaliacoes);
        salvarAvaliacoes();
        Printer.println("Avaliação registrada com sucesso!");
    }

    // Calcular média de estrelas de um médico
    public double calcularMediaEstrelas(medico medico) {
        List<Avaliacao> avaliacoesMedico = new ArrayList<>();
        for (List<Avaliacao> lista : avaliacoes.values()) {
            for (Avaliacao a : lista) {
                if (a.medico.getEmail().equals(medico.getEmail())) {
                    avaliacoesMedico.add(a);
                }
            }
        }

        if (avaliacoesMedico.isEmpty()) {
            return 0.0; // Sem avaliações
        }

        double soma = avaliacoesMedico.stream().mapToInt(a -> a.estrelas).sum();
        return soma / avaliacoesMedico.size();
    }

    // Gerar chave única para uma consulta
    private String gerarChaveConsulta(RealizarConsulta.ConsultaRealizada consulta) {
        return consulta.paciente.getEmail() + "|" + consulta.medico.getEmail() + "|" +
               consulta.dataConsulta.toString() + "|" + consulta.horarioConsulta.toString();
    }

    // Carregar avaliações
    private Map<String, List<Avaliacao>> carregarAvaliacoes() {
        File arquivo = new File(ARQUIVO_AVALIACOES_JSON);
        if (!arquivo.exists() || arquivo.length() == 0) {
            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write("{}");
            } catch (IOException e) {
                Printer.println("Erro ao criar arquivo de avaliações: " + e.getMessage());
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

            Type tipoMapa = new TypeToken<Map<String, List<Avaliacao>>>() {}.getType();
            Map<String, List<Avaliacao>> loaded = gson.fromJson(reader, tipoMapa);
            return loaded != null ? loaded : new HashMap<>();
        } catch (IOException e) {
            Printer.println("Erro ao carregar avaliações: " + e.getMessage());
            return new HashMap<>();
        }
    }


    // Salvar avaliações
    private void salvarAvaliacoes() {
        // Garantir que o campo 'tipo' esteja preenchido antes de salvar
        for (List<Avaliacao> lista : avaliacoes.values()) {
            for (Avaliacao a : lista) {
                if (a.paciente instanceof paciente && (a.paciente.tipo == null || a.paciente.tipo.isEmpty())) {
                    a.paciente.tipo = "paciente";
                }
                if (a.medico instanceof medico && (a.medico.tipo == null || a.medico.tipo.isEmpty())) {
                    a.medico.tipo = "medico";
                }
            }
        }

        try (FileWriter writer = new FileWriter(ARQUIVO_AVALIACOES_JSON)) {
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

            gson.toJson(avaliacoes, writer);
        } catch (IOException e) {
            Printer.println("Erro ao salvar avaliações: " + e.getMessage());
        }
    }
    // Classe interna para representar uma avaliação
    private static class Avaliacao {
        usuario paciente;
        medico medico;
        LocalDate dataConsulta;
        LocalTime horarioConsulta;
        int estrelas;
        List<String> texto;

        Avaliacao(usuario paciente, medico medico, LocalDate dataConsulta, LocalTime horarioConsulta, int estrelas, List<String> texto) {
            this.paciente = paciente;
            this.medico = medico;
            this.dataConsulta = dataConsulta;
            this.horarioConsulta = horarioConsulta;
            this.estrelas = estrelas;
            this.texto = texto;
        }
    }
}