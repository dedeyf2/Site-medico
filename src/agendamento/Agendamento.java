package agendamento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Agendamento {
    private String paciente;
    private String medico;
    
    public Agendamento(String paciente, String medico) {
        this.paciente = paciente;
        this.medico = medico;
    }
    
    // Getters e Setters
    public String getPaciente() {
        return paciente;
    }
    
    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }
    
    public String getMedico() {
        return medico;
    }
    
    public void setMedico(String medico) {
        this.medico = medico;
    }
    
    public static void main(String[] args) {
        List<Agendamento> agendamentos = new ArrayList<>();
        agendamentos.add(new Agendamento("Paciente 1", "Dr. Silva"));
        agendamentos.add(new Agendamento("Paciente 2", "Dra. Souza"));
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(agendamentos);
        
        try (FileWriter writer = new FileWriter("agendamentos.json")) {
            writer.write(json);
            System.out.println("Agendamentos gravados com sucesso em agendamentos.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}