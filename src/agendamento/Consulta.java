package agendamento;

import java.time.LocalDate;

import login.usuario;

public class Consulta {
	private usuario paciente;
    private usuario medico;
    private LocalDate dataConsulta;

    public Consulta(usuario paciente, usuario medico, LocalDate dataConsulta){
        this.paciente = paciente;
        this.medico = medico;
        this.dataConsulta = dataConsulta;
    }
}
