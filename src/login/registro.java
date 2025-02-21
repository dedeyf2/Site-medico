package login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class registro {
    private static final String ARQUIVO_JSON = "usuarios.json"; // Nome do arquivo

    public static void registrarUsuario(usuario novoUsuario) throws Exception {
        Map<String, usuario> usuarios = carregarUsuarios();

        // Verifica se o e-mail já existe
        if (usuarios.containsKey(novoUsuario.getEmail())) {
            throw new Exception("Email já usado.");
        }

        // Adiciona o novo usuário
        usuarios.put(novoUsuario.getEmail(), novoUsuario);

        // Salva os usuários atualizados no arquivo JSON
        salvarUsuarios(usuarios);
    }

    private static Map<String, usuario> carregarUsuarios() {
        try (FileReader reader = new FileReader(ARQUIVO_JSON)) {
            Gson gson = new Gson();
            Type tipo = new TypeToken<Map<String, usuario>>() {}.getType();
            return gson.fromJson(reader, tipo);
        } catch (IOException e) {
            return new HashMap<>(); // Se não existir, retorna uma lista vazia
        }
    }

    private static void salvarUsuarios(Map<String, usuario> usuarios) {
        try (FileWriter writer = new FileWriter(ARQUIVO_JSON)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(usuarios, writer);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar usuários.", e);
        }
    }
}