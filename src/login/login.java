package login;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class login {
    private static final String ARQUIVO_JSON = "usuarios.json";

    public static usuario autenticar(String email, String senha) {
        try {
            Map<String, usuario> usuarios = carregarUsuarios();
            if (usuarios.containsKey(email)) {
                usuario user = usuarios.get(email);
                if (user.verificarSenha(senha)) {
                    System.out.println("Login bem-sucedido!");
                    return user;
                }
            }
            System.out.println("Senha ou usuário não encontrado.");
            return null;
        } catch (Exception e) {
            System.out.println("Erro ao tentar autenticar: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static Map<String, usuario> carregarUsuarios() {
        try (FileReader reader = new FileReader(ARQUIVO_JSON)) {
            RuntimeTypeAdapterFactory<usuario> adapter = RuntimeTypeAdapterFactory
                .of(usuario.class, "tipo")
                .registerSubtype(medico.class, "medico")
                .registerSubtype(paciente.class, "paciente");

            Type tipo = new TypeToken<Map<String, usuario>>() {}.getType();
            return new GsonBuilder().registerTypeAdapterFactory(adapter).create().fromJson(reader, tipo);
        } catch (IOException e) {
            System.out.println("Erro ao carregar usuários do arquivo JSON: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}