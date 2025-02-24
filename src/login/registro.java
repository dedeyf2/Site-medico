package login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import wrapper.Printer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class registro {
    private static final String ARQUIVO_JSON = "usuarios.json";
    private static final transient Scanner scanner = new Scanner(System.in);

    public static void registrarUsuario() {
        try {
            File arquivo = new File(ARQUIVO_JSON);
            if (!arquivo.exists()) {
                Printer.println("Criando arquivo JSON...");
                try (FileWriter writer = new FileWriter(ARQUIVO_JSON)) {
                    writer.write("{}");
                    Printer.println("Arquivo JSON criado com sucesso!");
                } catch (IOException e) {
                    Printer.println("Erro ao criar o arquivo JSON: " + e.getMessage());
                }
            }

            Map<String, usuario> usuarios = carregarUsuarios();
            Printer.println("Você é Médico (M) ou Paciente (P)? ");
            String tipo = scanner.nextLine().trim().toUpperCase();

            Printer.print("Nome: ");
            String nome = scanner.nextLine();

            Printer.print("Idade: ");
            int idade = Integer.parseInt(scanner.nextLine());

            Printer.print("Email: ");
            String email = scanner.nextLine();

            if (usuarios.containsKey(email)) {
                Printer.println("Email já usado.");
                return;
            }

            Printer.print("Senha: ");
            String senha = scanner.nextLine();

            usuario novoUsuario;
            if (tipo.equals("P")) {
                Printer.println("Escolha um plano de saúde: HAPVIDA, AMIL, PORTO_SAUDE ou NENHUM");
                PlanoDeSaude plano = escolherPlano();
                novoUsuario = new paciente(nome, idade, plano, email, senha);
            } else if (tipo.equals("M")) {
                Set<PlanoDeSaude> planosAceitos = new HashSet<>();
                while (true) {
                    Printer.println("Adicione um plano de saúde (ou digite 'fim' para encerrar): HAPVIDA, AMIL, PORTO_SAUDE, NENHUM");
                    String entrada = scanner.nextLine().toUpperCase();
                    if (entrada.equals("FIM")) break;
                    try {
                        planosAceitos.add(PlanoDeSaude.valueOf(entrada));
                    } catch (IllegalArgumentException e) {
                        Printer.println("Plano inválido, tente novamente.");
                    }
                }
                Set<Especialidade> especialidades = new HashSet<>();
                while (true) {
                    Printer.println("Adicione uma especialidade (ou digite 'fim' para encerrar): CLINICO_GERAL, NEURO, PEDIATRA, CARDIOLOGISTA");
                    String entrada = scanner.nextLine().toUpperCase();
                    if (entrada.equals("FIM")) break;
                    try {
                        especialidades.add(Especialidade.valueOf(entrada));
                    } catch (IllegalArgumentException e) {
                        Printer.println("Especialidade inválida, tente novamente.");
                    }
                }
                novoUsuario = new medico(nome, idade, email, PlanoDeSaude.NENHUM, Especialidade.CLINICO_GERAL, senha);
                ((medico) novoUsuario).getPlanosAceitos().addAll(planosAceitos);
                if (!especialidades.isEmpty()) {
                    ((medico) novoUsuario).getEspecialidades().clear();
                    ((medico) novoUsuario).getEspecialidades().addAll(especialidades);
                }
            } else {
                Printer.println("Opção inválida!");
                return;
            }
            usuarios.put(email, novoUsuario);
            salvarUsuarios(usuarios);
            Printer.println("Usuário registrado com sucesso!");
        } catch (NumberFormatException e) {
            Printer.println("Erro: Idade deve ser um número válido.");
            e.printStackTrace();
        } catch (Exception e) {
            Printer.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static PlanoDeSaude escolherPlano() {
        while (true) {
            try {
                String entrada = scanner.nextLine().toUpperCase();
                return PlanoDeSaude.valueOf(entrada);
            } catch (IllegalArgumentException e) {
                Printer.println("Plano inválido, tente novamente.");
            }
        }
    }

    public static Map<String, usuario> carregarUsuarios() {
        File arquivo = new File(ARQUIVO_JSON);
        if (!arquivo.exists() || arquivo.length() == 0) {
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(arquivo)) {
            RuntimeTypeAdapterFactory<usuario> adapter = RuntimeTypeAdapterFactory
                .of(usuario.class, "tipo")
                .registerSubtype(paciente.class, "paciente")
                .registerSubtype(medico.class, "medico");

            Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(adapter)
                .create();

            Type tipoMapa = new TypeToken<Map<String, usuario>>() {}.getType();
            Map<String, usuario> usuarios = gson.fromJson(reader, tipoMapa);
            return usuarios != null ? usuarios : new HashMap<>();
        } catch (IOException e) {
            Printer.println("Erro ao carregar usuários: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static void atualizarUsuario(usuario usuarioAtualizado) {
        Map<String, usuario> usuarios = carregarUsuarios();
        if (usuarios.containsKey(usuarioAtualizado.getEmail())) {
            usuarios.put(usuarioAtualizado.getEmail(), usuarioAtualizado);
            salvarUsuarios(usuarios);
            Printer.println("Dados atualizados com sucesso!");
        } else {
            Printer.println("Usuário não encontrado.");
        }
    }

    public static void salvarUsuarios(Map<String, usuario> usuarios) {
        File arquivo = new File(ARQUIVO_JSON);

        try (FileWriter writer = new FileWriter(arquivo)) {
            RuntimeTypeAdapterFactory<usuario> adapter = RuntimeTypeAdapterFactory
                .of(usuario.class, "tipo")
                .registerSubtype(paciente.class, "paciente")
                .registerSubtype(medico.class, "medico");

            Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(adapter)
                .serializeSpecialFloatingPointValues() // Adicionado para garantir robustez
                .setPrettyPrinting()
                .create();

            // Serializar o mapa com o tipo correto
            Type tipoMapa = new TypeToken<Map<String, usuario>>() {}.getType();
            String jsonOutput = gson.toJson(usuarios, tipoMapa);
            Printer.println("Salvando usuários..." );
            writer.write(jsonOutput);
            writer.flush();
            Printer.println("Usuários salvos com sucesso em " + ARQUIVO_JSON);
        } catch (IOException e) {
            Printer.println("Erro ao salvar usuários: " + e.getMessage());
            throw new RuntimeException("Falha ao salvar os usuários", e);
        }
    }
}