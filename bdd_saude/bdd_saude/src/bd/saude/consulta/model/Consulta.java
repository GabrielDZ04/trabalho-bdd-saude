package bd.saude.consulta.model;

import java.time.LocalDateTime;

public class Consulta {

    private Long id;
    private String paciente_cpf;
    private String medico_crm;
    private LocalDateTime data_hora;
    private String queixa_paciente;

    private String resultadoTexto;

    private Resultado status;

    private Long id_prescricao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getpaciente_cpf() { return paciente_cpf; }
    public void setpaciente_cpf(String paciente_cpf) { this.paciente_cpf = paciente_cpf; }

    public String getmedico_crm() { return medico_crm; }
    public void setmedico_crm(String medico_crm) { this.medico_crm = medico_crm; }

    public LocalDateTime getdata_hora() { return data_hora; }
    public void setdata_hora(LocalDateTime data_hora) { this.data_hora = data_hora; }

    public String getqueixa_paciente() { return queixa_paciente; }
    public void setqueixa_paciente(String queixa_paciente) { this.queixa_paciente = queixa_paciente; }

    // Texto livre do banco
    public String getResultadoTexto() { return resultadoTexto; }
    public void setResultadoTexto(String resultadoTexto) { this.resultadoTexto = resultadoTexto; }

    // Enum opcional (derivado)
    public Resultado getStatus() { return status; }
    public void setStatus(Resultado status) { this.status = status; }

    public Long getId_prescricao() { return id_prescricao; }
    public void setId_prescricao(Long id_prescricao) { this.id_prescricao = id_prescricao; }
}