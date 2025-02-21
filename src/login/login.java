package login;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class login {
    private static final String ARQUIVO_JSON = "usuarios.json"; // Caminho do arquivo JSON

    public static usuario autenticar(String email, String senha) throws Exception {
        Map<String, usuario> usuarios = carregarUsuarios();

        if (usuarios.containsKey(email)) {
            usuario user = usuarios.get(email);
            if (user.verificarSenha(senha)) {
                System.out.println("Login bem-sucedido!");
                return user;
            }
        }

        throw new Exception("Senha ou usuário não encontrado.");
    }

    private static Map<String, usuario> carregarUsuarios() {
        try (FileReader reader = new FileReader(ARQUIVO_JSON)) {
            Gson gson = new Gson();
            Type tipo = new TypeToken<Map<String, usuario>>() {}.getType();
            return gson.fromJson(reader, tipo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar usuários do arquivo JSON.", e);
        }
    }
}