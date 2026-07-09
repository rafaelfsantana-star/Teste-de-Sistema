package com.bibliotech.unit;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.repository.EmprestimoRepository;
import com.bibliotech.service.EmprestimoService;
import com.bibliotech.service.LivroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para EmprestimoService.
 *
 * Bugs identificados:
 * - BUG-001: calcularDataDevolucao retorna 7 dias em vez de 14 (RN-01)
 * - BUG-002: calcularMulta usa R$ 3,00/dia em vez de R$ 2,00/dia (RN-03)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - EmprestimoService")
public class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private LivroService livroService;

    @InjectMocks
    private EmprestimoService emprestimoService;

    private Emprestimo emprestimo;
    private Livro livro;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        livro = new Livro("Clean Code", "Robert Martin", "978-0132350884", "Prentice Hall", 2008, 3);
        usuario = new Usuario("João Silva", "joao@teste.com", "123.456.789-00", "senha123", Usuario.TipoUsuario.ALUNO);
        emprestimo = new Emprestimo();
    }

    // ==================== TESTES: calcularDataDevolucao ====================

    @Test
    @DisplayName("TU-001: Prazo de devolução deve ser de 14 dias corridos (RN-01) [BUG-001]")
    void deveTerPrazoDevolucaoDe14Dias() {
        LocalDate dataEmprestimo = LocalDate.now();
        LocalDate esperada = dataEmprestimo.plusDays(14);

        LocalDate obtida = emprestimoService.calcularDataDevolucao(dataEmprestimo);

        assertEquals(esperada, obtida,
            "BUG-001: Prazo deveria ser 14 dias, mas o sistema retorna 7 dias (RN-01)");
    }

    @Test
    @DisplayName("TU-002: Data de devolução não deve ser igual à data de empréstimo")
    void dataDevolucaoNaoDeveSerIgualDataEmprestimo() {
        LocalDate dataEmprestimo = LocalDate.now();
        LocalDate dataDevolucao = emprestimoService.calcularDataDevolucao(dataEmprestimo);

        assertNotEquals(dataEmprestimo, dataDevolucao,
            "A data de devolução não pode ser igual à data de empréstimo");
    }

    @Test
    @DisplayName("TU-003: Data de devolução deve ser posterior à data de empréstimo")
    void dataDevolucaoDeveSerPosteriorAoEmprestimo() {
        LocalDate dataEmprestimo = LocalDate.of(2025, 1, 10);
        LocalDate dataDevolucao = emprestimoService.calcularDataDevolucao(dataEmprestimo);

        assertTrue(dataDevolucao.isAfter(dataEmprestimo),
            "Data de devolução deve ser sempre posterior à data de empréstimo");
    }

    // ==================== TESTES: calcularMulta ====================

    @Test
    @DisplayName("TU-004: Multa deve ser R$ 2,00 por dia de atraso (RN-03) [BUG-002]")
    void deveCalcularMultaCorretamentePor5Dias() {
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().minusDays(5));
        emprestimo.setDataDevolucaoReal(LocalDate.now());

        double multa = emprestimoService.calcularMulta(emprestimo);

        assertEquals(10.0, multa, 0.01,
            "BUG-002: Multa deveria ser R$ 2,00 x 5 dias = R$ 10,00, mas o sistema calcula R$ 3,00/dia");
    }

    @Test
    @DisplayName("TU-005: Multa para 1 dia de atraso deve ser R$ 2,00 (RN-03) [BUG-002]")
    void deveCalcularMultaParaUmDiaDeAtraso() {
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().minusDays(1));
        emprestimo.setDataDevolucaoReal(LocalDate.now());

        double multa = emprestimoService.calcularMulta(emprestimo);

        assertEquals(2.0, multa, 0.01,
            "BUG-002: Multa para 1 dia de atraso deveria ser R$ 2,00");
    }

    @Test
    @DisplayName("TU-006: Multa deve ser R$ 0,00 quando devolvido no prazo")
    void deveTerMultaZeroQuandoDevolvidoNoPrazo() {
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().plusDays(3));
        emprestimo.setDataDevolucaoReal(LocalDate.now());

        double multa = emprestimoService.calcularMulta(emprestimo);

        assertEquals(0.0, multa, 0.01,
            "Devolução no prazo não deve gerar multa");
    }

    @Test
    @DisplayName("TU-007: Multa deve ser R$ 0,00 quando devolvido na data exata")
    void deveTerMultaZeroQuandoDevolvidoNaDataExata() {
        emprestimo.setDataDevolucaoPrevista(LocalDate.now());
        emprestimo.setDataDevolucaoReal(LocalDate.now());

        double multa = emprestimoService.calcularMulta(emprestimo);

        assertEquals(0.0, multa, 0.01,
            "Devolução na data exata não deve gerar multa");
    }

    @Test
    @DisplayName("TU-008: Empréstimo ativo com atraso deve calcular multa com valor correto (RN-03) [BUG-002]")
    void deveCalcularMultaParaEmprestimoAtivoComAtraso() {
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().minusDays(3));
        emprestimo.setDataDevolucaoReal(null); // ainda ativo

        double multa = emprestimoService.calcularMulta(emprestimo);

        assertEquals(6.0, multa, 0.01,
            "BUG-002: Multa para empréstimo ativo com 3 dias de atraso deveria ser R$ 6,00 (R$ 2,00 x 3)");
    }

    @Test
    @DisplayName("TU-009: Empréstimo ativo sem atraso não deve gerar multa")
    void deveRetornarMultaZeroParaEmprestimoAtivoSemAtraso() {
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().plusDays(5));
        emprestimo.setDataDevolucaoReal(null);

        double multa = emprestimoService.calcularMulta(emprestimo);

        assertEquals(0.0, multa, 0.01,
            "Empréstimo ativo sem atraso não deve ter multa");
    }

    // ==================== TESTES: registrarDevolucao ====================

    @Test
    @DisplayName("TU-010: Deve lançar exceção ao tentar devolver empréstimo já devolvido")
    void deveLancarExcecaoAoDevoverEmprestimoJaDevolvido() {
        emprestimo.setDataDevolucaoReal(LocalDate.now().minusDays(1));

        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));

        assertThrows(RuntimeException.class,
            () -> emprestimoService.registrarDevolucao(1L),
            "Deve lançar exceção ao tentar devolver empréstimo que já foi devolvido");
    }

    @Test
    @DisplayName("TU-011: Deve lançar exceção quando empréstimo não é encontrado")
    void deveLancarExcecaoQuandoEmprestimoNaoEncontrado() {
        when(emprestimoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> emprestimoService.registrarDevolucao(999L),
            "Deve lançar exceção quando empréstimo não existe");
    }

    @Test
    @DisplayName("TU-012: Devolução deve registrar data real, inativar e calcular multa")
    void deveDevolverCorretamente() {
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().plusDays(2));
        emprestimo.setDataDevolucaoReal(null);
        emprestimo.setAtivo(true);
        emprestimo.setLivro(livro);

        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimo);

        Emprestimo resultado = emprestimoService.registrarDevolucao(1L);

        assertNotNull(resultado.getDataDevolucaoReal(),
            "Data de devolução real deve ser preenchida");
        assertFalse(resultado.getAtivo(),
            "Empréstimo deve ser marcado como inativo após devolução");
        verify(livroService, times(1)).incrementarDisponibilidade(livro);
    }
}
