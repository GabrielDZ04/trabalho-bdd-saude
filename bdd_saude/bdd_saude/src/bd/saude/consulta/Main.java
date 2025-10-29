package bd.saude.consulta;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import bd.saude.consulta.dao.ConsultaDAO;
import bd.saude.consulta.model.Consulta;
import bd.saude.consulta.model.Resultado;

public class Main {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        ConsultaDAO dao = new ConsultaDAO();
        Scanner sc = new Scanner(System.in);
        boolean rodando = true;

        while (rodando) {
            imprimeMenu();

            int opcao;
            try {
                System.out.print("Escolha uma opção: ");
                opcao = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("⚠ Opção inválida.");
                System.out.println("\n(ENTER para continuar)"); sc.nextLine();
                continue;
            }

            try {
                switch (opcao) {
                    case 1 -> inserir(sc, dao);
                    case 2 -> buscar(sc, dao);
                    case 3 -> atualizar(sc, dao);
                    case 4 -> deletar(sc, dao);
                    case 5 -> listarTodas(sc, dao); // Listar todas as consultas
                    case 0 -> {
                        System.out.println("Encerrando... Até logo!");
                        rodando = false;
                    }
                    default -> System.out.println("⚠ Opção inválida.");
                }
            } catch (SQLException e) {
                System.out.println("❌ Erro SQL: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ Erro: " + e.getMessage());
            }

            if (rodando) {
                System.out.println("\n(ENTER para continuar)");
                sc.nextLine();
            }
        }

        sc.close();
    }

    private static void imprimeMenu() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println(  "║      SISTEMA DE CONSULTAS (CRUD)         ║");
        System.out.println(  "╠══════════════════════════════════════════╣");
        System.out.println(  "║ 1) Inserir consulta                      ║");
        System.out.println(  "║ 2) Pesquisar por ID                      ║");
        System.out.println(  "║ 3) Atualizar por ID                      ║");
        System.out.println(  "║ 4) Apagar por ID                         ║");
        System.out.println(  "║ 5) Listar todas as consultas             ║");
        System.out.println(  "╠══════════════════════════════════════════╣");
        System.out.println(  "║ 0) Sair                                  ║");
        System.out.println(  "╚══════════════════════════════════════════╝");
    }

    // ===================== (1) INSERIR =====================
    private static void inserir(Scanner sc, ConsultaDAO dao) throws SQLException {
        System.out.println("\n== INSERIR CONSULTA ==");

        String pacienteCpf;
        while (true) {
            System.out.print("CPF do paciente (11 dígitos): ");
            pacienteCpf = sc.nextLine().trim();
            if (pacienteCpf.matches("\\d{11}")) break;
            System.out.println("⚠ CPF inválido.");
        }

        String medicoCrm;
        while (true) {
            System.out.print("CRM do médico (ex.: CRM-SP-12345): ");
            medicoCrm = sc.nextLine().trim();
            if (!medicoCrm.isBlank()) break;
            System.out.println("⚠ CRM inválido.");
        }

        LocalDateTime dataHora;
        while (true) {
            System.out.print("Data e hora (yyyy-MM-dd HH:mm): ");
            String s = sc.nextLine();
            try { dataHora = LocalDateTime.parse(s.trim(), DTF); break; }
            catch (DateTimeParseException ex) { System.out.println("⚠ Formato inválido. Ex: 2025-10-20 14:30"); }
        }

        System.out.print("Queixa do paciente: ");
        String queixa = sc.nextLine();

        System.out.print("Resultado (livre, ex.: 'Receitado Dipirona' ou 'CONCLUIDA'): ");
        String resultadoTxt = sc.nextLine();

        System.out.print("ID da prescrição (obrigatório, pois a tabela exige NOT NULL): ");
        String sPresc = sc.nextLine();
        Long idPrescricao = sPresc.isBlank() ? null : Long.parseLong(sPresc.trim());

        Consulta c = new Consulta();
        c.setpaciente_cpf(pacienteCpf);
        c.setmedico_crm(medicoCrm);
        c.setdata_hora(dataHora);
        c.setqueixa_paciente(queixa);

        c.setResultadoTexto(resultadoTxt);
        try {
            c.setStatus(Resultado.valueOf(resultadoTxt.trim().toUpperCase()));
        } catch (Exception ignore) {
            c.setStatus(null);
        }

        c.setId_prescricao(idPrescricao);

        Long novoId = dao.inserir(c);
        System.out.println("✅ Inserido com sucesso. ID: " + novoId);
    }

    // ===================== (2) BUSCAR =====================
    private static void buscar(Scanner sc, ConsultaDAO dao) throws SQLException {
        System.out.println("\n== PESQUISAR POR ID ==");
        Long id;
        while (true) {
            System.out.print("ID da consulta: ");
            String s = sc.nextLine();
            try { id = Long.parseLong(s.trim()); break; }
            catch (NumberFormatException ex) { System.out.println("⚠ ID inválido."); }
        }

        Optional<Consulta> opt = dao.buscarPorId(id);
        if (opt.isEmpty()) {
            System.out.println("⚠ Consulta não encontrada.");
        } else {
            imprimirConsulta(opt.get());
        }
    }

    // ===================== (3) ATUALIZAR =====================
    private static void atualizar(Scanner sc, ConsultaDAO dao) throws SQLException {
        System.out.println("\n== ATUALIZAR (por ID) ==");
        Long id;
        while (true) {
            System.out.print("ID da consulta a atualizar: ");
            String s = sc.nextLine();
            try { id = Long.parseLong(s.trim()); break; }
            catch (NumberFormatException ex) { System.out.println("⚠ ID inválido."); }
        }

        Optional<Consulta> opt = dao.buscarPorId(id);
        if (opt.isEmpty()) {
            System.out.println("⚠ Consulta não encontrada.");
            return;
        }

        Consulta atual = opt.get();
        System.out.println("— Valores atuais (ENTER mantém):");
        imprimirConsulta(atual);

        System.out.print("Novo CPF do paciente (11 dígitos): ");
        String s = sc.nextLine();
        if (!s.isBlank()) {
            if (s.trim().matches("\\d{11}")) {
                atual.setpaciente_cpf(s.trim());
            } else {
                System.out.println("⚠ CPF inválido. Mantido.");
            }
        }

        System.out.print("Novo CRM do médico: ");
        s = sc.nextLine();
        if (!s.isBlank()) atual.setmedico_crm(s.trim());

        System.out.print("Nova data/hora (yyyy-MM-dd HH:mm): ");
        s = sc.nextLine();
        if (!s.isBlank()) {
            try { atual.setdata_hora(LocalDateTime.parse(s.trim(), DTF)); }
            catch (DateTimeParseException ex) { System.out.println("⚠ Formato inválido. Mantido."); }
        }

        System.out.print("Nova queixa do paciente: ");
        s = sc.nextLine();
        if (!s.isBlank()) atual.setqueixa_paciente(s);

        System.out.print("Novo resultado (livre) [ENTER mantém]: ");
        s = sc.nextLine();
        if (!s.isBlank()) {
            atual.setResultadoTexto(s);
            try { atual.setStatus(Resultado.valueOf(s.trim().toUpperCase())); }
            catch (IllegalArgumentException ex) { atual.setStatus(null); /* mantém como texto livre */ }
        }

        System.out.print("Novo ID da prescrição (vazio → NULL): ");
        s = sc.nextLine();
        if (s.isBlank()) {
            atual.setId_prescricao(null);
        } else {
            try { atual.setId_prescricao(Long.parseLong(s.trim())); }
            catch (NumberFormatException ex) { System.out.println("⚠ ID de prescrição inválido. Mantido."); }
        }

        boolean ok = dao.atualizarPorId(id, atual);
        System.out.println(ok ? "✅ Atualizado com sucesso." : "⚠ Nada foi atualizado.");
    }

    // ===================== (4) DELETAR =====================
    private static void deletar(Scanner sc, ConsultaDAO dao) throws SQLException {
        System.out.println("\n== APAGAR (por ID) ==");
        Long id;
        while (true) {
            System.out.print("ID da consulta a apagar: ");
            String s = sc.nextLine();
            try { id = Long.parseLong(s.trim()); break; }
            catch (NumberFormatException ex) { System.out.println("⚠ ID inválido."); }
        }

        System.out.print("Confirmar exclusão (S/N)? ");
        String conf = sc.nextLine();
        if (!conf.equalsIgnoreCase("S")) {
            System.out.println("Operação cancelada.");
        } else {
            boolean ok = dao.deletarPorId(id);
            System.out.println(ok ? "🗑 Excluído com sucesso." : "⚠ Consulta não encontrada.");
        }
    }

    // ===================== (5) LISTAR TODAS =====================
    private static void listarTodas(Scanner sc, ConsultaDAO dao) throws SQLException {
        System.out.println("\n== LISTAR TODAS AS CONSULTAS ==\n");
        List<Consulta> consultas = dao.listarTodos(); // atalho sem paginação
        if (consultas == null || consultas.isEmpty()) {
            System.out.println("Nenhuma consulta cadastrada.");
            return;
        }

        int total = consultas.size();
        for (int i = 0; i < total; i++) {
            Consulta c = consultas.get(i);
            imprimirConsulta(c);

            if (i < total - 1) {
                System.out.printf("[%d/%d] ENTER para próxima, 'q' para sair: ", i + 1, total);
                String in = sc.nextLine();
                if (in != null && in.trim().equalsIgnoreCase("q")) break;
            }
        }
    }

    private static void imprimirConsulta(Consulta c) {
        System.out.println("--------------------------------------------");
        System.out.println("ID .............: " + c.getId());
        System.out.println("Paciente (CPF)..: " + c.getpaciente_cpf());
        System.out.println("Médico (CRM) ...: " + c.getmedico_crm());
        System.out.println("Data/Hora ......: " + c.getdata_hora());
        System.out.println("Queixa .........: " + c.getqueixa_paciente());

        String rtxt = c.getResultadoTexto();
        String renum = (c.getStatus() != null ? c.getStatus().name() : "-");
        System.out.println("Resultado ......: " + (rtxt != null && !rtxt.isBlank() ? rtxt : renum));

        System.out.println("ID Prescrição ..: " + c.getId_prescricao());
        System.out.println("--------------------------------------------");
    }
}
