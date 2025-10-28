package bd.saude.consulta.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import bd.saude.consulta.config.Database;
import bd.saude.consulta.model.Consulta;
import bd.saude.consulta.model.Resultado;

public class ConsultaDAO {

    private static final String TABELA = "consulta";

    public Long inserir(Consulta c) throws SQLException {
        String valorResultado = c.getResultadoTexto();
        if (valorResultado == null && c.getStatus() != null) {
            valorResultado = c.getStatus().name();
        }

        boolean temId = (c.getId() != null);

        final String sqlComId =
            "INSERT INTO " + TABELA + " (id_consulta, medico_crm, paciente_cpf, data_hora, queixa_paciente, resultado, id_prescricao) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        final String sqlSemId =
            "INSERT INTO " + TABELA + " (medico_crm, paciente_cpf, data_hora, queixa_paciente, resultado, id_prescricao) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(temId ? sqlComId : sqlSemId, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            if (temId) {
                ps.setLong(i++, c.getId());
            }

            ps.setString(i++, c.getmedico_crm());
            ps.setString(i++, c.getpaciente_cpf());
            ps.setTimestamp(i++, c.getdata_hora() != null ? Timestamp.valueOf(c.getdata_hora()) : null);
            ps.setString(i++, c.getqueixa_paciente());
            ps.setString(i++, valorResultado);
            if (c.getId_prescricao() == null) {
                ps.setNull(i++, Types.INTEGER);
            } else {
                ps.setLong(i++, c.getId_prescricao());
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return c.getId();
        }
    }

    public Optional<Consulta> buscarPorId(Long id) throws SQLException {
        final String sql = "SELECT * FROM " + TABELA + " WHERE id_consulta = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        }
    }

    public List<Consulta> listarTodos() throws SQLException {
        return listarTodos(Integer.MAX_VALUE, 0);
    }

    public List<Consulta> listarTodos(int limit, int offset) throws SQLException {
        final String sql =
            "SELECT * FROM " + TABELA + " ORDER BY data_hora DESC LIMIT ? OFFSET ?";
        List<Consulta> out = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        }
        return out;
    }

    public boolean atualizarPorId(Long id, Consulta c) throws SQLException {
        String valorResultado = c.getResultadoTexto();
        if (valorResultado == null && c.getStatus() != null) {
            valorResultado = c.getStatus().name();
        }

        final String sql =
            "UPDATE " + TABELA + " SET " +
            "medico_crm = ?, " +
            "paciente_cpf = ?, " +
            "data_hora = ?, " +
            "queixa_paciente = ?, " +
            "resultado = ?, " +
            "id_prescricao = ? " +
            "WHERE id_consulta = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, c.getmedico_crm());
            ps.setString(i++, c.getpaciente_cpf());
            ps.setTimestamp(i++, c.getdata_hora() != null ? Timestamp.valueOf(c.getdata_hora()) : null);
            ps.setString(i++, c.getqueixa_paciente());
            ps.setString(i++, valorResultado);

            if (c.getId_prescricao() == null) {
                ps.setNull(i++, Types.INTEGER);
            } else {
                ps.setLong(i++, c.getId_prescricao());
            }

            ps.setLong(i++, id);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletarPorId(Long id) throws SQLException {
        final String sql = "DELETE FROM " + TABELA + " WHERE id_consulta = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Consulta mapRow(ResultSet rs) throws SQLException {
        Consulta c = new Consulta();

        long id = rs.getLong("id_consulta");
        c.setId(id);

        c.setmedico_crm(rs.getString("medico_crm"));
        c.setpaciente_cpf(rs.getString("paciente_cpf"));

        Timestamp ts = rs.getTimestamp("data_hora");
        c.setdata_hora(ts != null ? ts.toLocalDateTime() : null);

        c.setqueixa_paciente(rs.getString("queixa_paciente"));

        String raw = rs.getString("resultado");
        c.setResultadoTexto(raw);
        c.setStatus(parseResultadoDB(raw));

        Object o = rs.getObject("id_prescricao");
        c.setId_prescricao(o == null ? null : ((Number) o).longValue());

        return c;
    }

    private Resultado parseResultadoDB(String raw) {
        if (raw == null) return null;
        String k = raw.trim().toUpperCase();
        if (k.equals("AGENDADA"))  return Resultado.AGENDADA;
        if (k.equals("CONCLUIDA")) return Resultado.CONCLUIDA;
        if (k.equals("CANCELADA")) return Resultado.CANCELADA;
        if (k.startsWith("RECEITADO")) return Resultado.CONCLUIDA; // ajuste se quiser
        return null;
    }
}