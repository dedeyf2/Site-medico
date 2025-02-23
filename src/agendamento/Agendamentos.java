package agendamento;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Agendamentos {
    public static Agendamentos AgendamentosInstance;
    ArrayList<Agendamento> agendamentos;

    private Agendamentos(){
        agendamentos = new ArrayList<>();
    }

    public Agendamentos getAgendamentos(){
        if (AgendamentosInstance == null) {
            AgendamentosInstance = new Agendamentos();
        }
        return AgendamentosInstance;
    }
    public static void main(String[] args) {
        Type listType = new TypeToken<List<Agendamento>>() {}.getType();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // Lê e desserializa o conteúdo do arquivo JSON
        try (FileReader reader = new FileReader("agendamentos.json")) {
            List<Agendamento> agendamentosLidos = gson.fromJson(reader, listType);
            System.out.println("Agendamentos lidos do JSON:");
            agendamentosLidos.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAgendamento(Agendamento novaConsulta){
        agendamentos.add(novaConsulta);
    }
}
