package com.bibliotech.unit;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.repository.EmprestimoRepository;
import com.bibliotech.repository.LivroRepository;
import com.bibliotech.repository.UsuarioRepository;
import com.bibliotech.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para DashboardService.
 *
 * Bugs identificados:
 * - BUG-005: obterEstatisticas() usa emprestimoRepository.count() para "emprestimosAtivos",
 *            mas count() retorna TODOS os empréstimos (incluindo devolvidos), não só os ativos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - DashboardService")
public class DashboardServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("TU-032: Estatísticas devem conter todas as chaves esperadas")
    void estatisticasDevemConterTodasAsChaves() {
        when(livroRepository.count()).thenReturn(10L);
        when(usuarioRepository.findByAtivoTrue()).thenReturn(List.of());
        when(emprestimoRepository.count()).thenReturn(5L);
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0)).thenReturn(List.of());
        when(emprestimoRepository.findEmprestimosAtrasados()).thenReturn(List.of());

        Map<String, Object> stats = dashboardService.obterEstatisticas();

        assertTrue(stats.containsKey("totalLivros"), "Deve conter totalLivros");
        assertTrue(stats.containsKey("totalUsuarios"), "Deve conter totalUsuarios");
        assertTrue(stats.containsKey("emprestimosAtivos"), "Deve conter emprestimosAtivos");
        assertTrue(stats.containsKey("livrosDisponiveis"), "Deve conter livrosDisponiveis");
        assertTrue(stats.containsKey("emprestimosAtrasados"), "Deve conter emprestimosAtrasados");
    }

    @Test
    @DisplayName("TU-033: Total de livros deve refletir o valor do repositório")
    void totalLivrosDeveEstarCorreto() {
        when(livroRepository.count()).thenReturn(42L);
        when(usuarioRepository.findByAtivoTrue()).thenReturn(List.of());
        when(emprestimoRepository.count()).thenReturn(0L);
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0)).thenReturn(List.of());
        when(emprestimoRepository.findEmprestimosAtrasados()).thenReturn(List.of());

        Map<String, Object> stats = dashboardService.obterEstatisticas();

        assertEquals(42L, stats.get("totalLivros"),
            "Total de livros deve ser 42");
    }

    @Test
    @DisplayName("TU-034: Empréstimos ativos devem contar apenas os não devolvidos (RN) [BUG-005]")
    void emprestimosAtivosDeveContarApenasOsAtivos() {
        // Simula: 3 empréstimos totais, mas só 1 ativo
        Emprestimo ativo = new Emprestimo();
        ativo.setAtivo(true);
        ativo.setDataDevolucaoReal(null);

        Emprestimo devolvido1 = new Emprestimo();
        devolvido1.setAtivo(false);
        devolvido1.setDataDevolucaoReal(LocalDate.now().minusDays(5));

        Emprestimo devolvido2 = new Emprestimo();
        devolvido2.setAtivo(false);
        devolvido2.setDataDevolucaoReal(LocalDate.now().minusDays(2));

        // count() retorna todos (3) — é o bug
        when(emprestimoRepository.count()).thenReturn(3L);
        when(livroRepository.count()).thenReturn(5L);
        when(usuarioRepository.findByAtivoTrue()).thenReturn(List.of());
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0)).thenReturn(List.of());
        when(emprestimoRepository.findEmprestimosAtrasados()).thenReturn(List.of());

        Map<String, Object> stats = dashboardService.obterEstatisticas();

        long emprestimosAtivos = (long) stats.get("emprestimosAtivos");

        assertEquals(1L, emprestimosAtivos,
            "BUG-005: O dashboard exibe " + emprestimosAtivos + " como 'empréstimos ativos', " +
            "mas o correto seria 1. O sistema usa count() que retorna TODOS os empréstimos, " +
            "incluindo os já devolvidos.");
    }
}
