package login;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class usuario {
    protected String nome;
    protected int idade;
    protected PlanoDeSaude plano;
    protected String email;
    protected String senhaHash;
    public transient String tipo;

    public usuario(String nome, int idade, PlanoDeSaude plano, String email, String senha) {
        this.nome = nome;
        this.idade = idade;
        this.plano = plano;
        this.email = email;
        this.senhaHash = hashSenha(senha);
    }

    // Getters e outros métodos permanecem iguais
    public String getNome() { return nome; }
    public int getIdade() { return idade; }
    public PlanoDeSaude getPlano() { return plano; }
    public String getEmail() { return email; }

    public void alterarNome(String novoNome) {
        if (novoNome != null && !novoNome.trim().isEmpty()) {
            this.nome = novoNome;
            salvarAlteracoes();
        }
    }

    public void alterarEmail(String novoEmail) {
        if (novoEmail != null && novoEmail.contains("@")) {
            this.email = novoEmail;
            salvarAlteracoes();
        }
    }

    public void alterarIdade(int novaIdade) {
        if (novaIdade > 0) {
            this.idade = novaIdade;
            salvarAlteracoes();
        }
    }

    public void alterarSenha(String senhaAntiga, String novaSenha) {
        if (verificarSenha(senhaAntiga)) {
            this.senhaHash = hashSenha(novaSenha);
            salvarAlteracoes();
        } else {
            throw new IllegalArgumentException("Senha antiga incorreta!");
        }
    }

    public abstract void alterarPlano(PlanoDeSaude planoNovo);

    public boolean verificarSenha(String senha) {
        return senhaHash.equals(hashSenha(senha));
    }

    private String hashSenha(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(senha.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao calcular hash da senha", e);
        }
    }

    private void salvarAlteracoes() {
        registro.atualizarUsuario(this);
    }
}