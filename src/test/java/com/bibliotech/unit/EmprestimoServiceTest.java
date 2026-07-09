package com.bibliotech.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.repository.EmprestimoRepository;
import com.bibliotech.service.EmprestimoService;
import com.bibliotech.service.LivroService;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmprestimoService - Testes Unitários")
class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private LivroService livroService;

    @InjectMocks
    private EmprestimoService emprestimoService;

    private Usuario usuarioAtivo;
    private Livro livroDisponivel;

    @BeforeEach
    void setUp() {
        usuarioAtivo = new Usuario();
        usuarioAtivo.setId(1L);
        usuarioAtivo.setNome("João Silva");
        usuarioAtivo.setEmail("joao@email.com");
        usuarioAtivo.setCpf("123.456.789-00");
        usuarioAtivo.setSenha("senha123");
        usuarioAtivo.setTipo(Usuario.TipoUsuario.ALUNO);
        usuarioAtivo.setAtivo(true);

        livroDisponivel = new Livro();
        livroDisponivel.setId(1L);
        livroDisponivel.setTitulo("Clean Code");
        livroDisponivel.setAutor("Robert Martin");
        livroDisponivel.setIsbn("978-0132350884");
        livroDisponivel.setAno(2008);
        livroDisponivel.setQuantidadeExemplares(3);
        livroDisponivel.setQuantidadeDisponivel(2); // começa com 2 disponíveis
    }

    // =========================================================
    // RN-03: Testes de Cálculo de Multa
    // =========================================================

    @Test
    @DisplayName("TU-001: Deve calcular multa de R$ 10,00 para 5 dias de atraso (RN-03)")
    void deveCalcularMultaParaCincoDiasDeAtraso() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().minusDays(5));
        emprestimo.setDataDevolucaoReal(LocalDate.now());

        double multa = emprestimoService.calcularMulta(emprestimo);

        // RN-03: R$ 2,00 por dia × 5 dias = R$ 10,00
        // BUG ESPERADO: sistema usa R$ 3,00 em vez de R$ 2,00 -> retorna 15.0
        assertEquals(10.0, multa, 0.01,
                "BUG-002: Multa deveria ser R$ 2,00 × 5 dias = R$ 10,00");
    }

    @Test
    @DisplayName("TU-002: Deve retornar multa zero para devolução no prazo (RN-03)")
    void deveRetornarMultaZeroParaDevolucaoNoPrazo() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().plusDays(2));
        emprestimo.setDataDevolucaoReal(LocalDate.now());

        double multa = emprestimoService.calcularMulta(emprestimo);
        assertEquals(0.0, multa, 0.01);
    }

    @Test
    @DisplayName("TU-003: Deve calcular multa de R$ 2,00 para 1 dia de atraso (RN-03)")
    void deveCalcularMultaParaUmDiaDeAtraso() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().minusDays(1));
        emprestimo.setDataDevolucaoReal(LocalDate.now());

        double multa = emprestimoService.calcularMulta(emprestimo);
        assertEquals(2.0, multa, 0.01,
                "RN-03: Multa para 1 dia de atraso deve ser R$ 2,00");
    }

    @Test
    @DisplayName("TU-004: Deve calcular multa de R$ 20,00 para 10 dias de atraso (RN-03)")
    void deveCalcularMultaParaDezDiasDeAtraso() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().minusDays(10));
        emprestimo.setDataDevolucaoReal(LocalDate.now());

        double multa = emprestimoService.calcularMulta(emprestimo);
        assertEquals(20.0, multa, 0.01);
    }

    // =========================================================
    // RN-01: Testes de Prazo de Empréstimo (14 dias)
    // =========================================================

    @Test
    @DisplayName("TU-005: Deve calcular data de devolução como 14 dias corridos (RN-01)")
    void deveCalcularDataDevolucaoCom14Dias() {
        LocalDate dataEmprestimo = LocalDate.now();
        LocalDate dataEsperada = dataEmprestimo.plusDays(14);

        // BUG ESPERADO: sistema retorna +7 dias -> dataEsperada será diferente
        LocalDate dataCalculada = emprestimoService.calcularDataDevolucao(dataEmprestimo);
        assertEquals(dataEsperada, dataCalculada,
                "BUG-003: Prazo de devolução deve ser 14 dias");
    }

    // =========================================================
    // RN-04: Testes de Disponibilidade para Empréstimo
    // =========================================================

    @Test
    @DisplayName("TU-006: Deve impedir empréstimo quando livro não tem exemplares disponíveis (RN-04)")
    void deveImpedirEmprestimoComLivroIndisponivel() {
        // Torna o livro indisponível
        livroDisponivel.setQuantidadeDisponivel(0);

        // O serviço real NÃO valida, então nenhuma exceção será lançada -> teste falha
        assertThrows(RuntimeException.class,
                () -> emprestimoService.realizarEmprestimo(usuarioAtivo, livroDisponivel),
                "BUG-004: Deveria lançar exceção ao tentar emprestar livro sem exemplares");
    }

    @Test
    @DisplayName("TU-007: Deve realizar empréstimo com sucesso quando livro tem exemplares disponíveis (RN-04)")
    void deveRealizarEmprestimoComSucesso() {
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(i -> i.getArgument(0));

        Emprestimo emprestimo = emprestimoService.realizarEmprestimo(usuarioAtivo, livroDisponivel);

        assertNotNull(emprestimo);
        assertTrue(emprestimo.getAtivo());
        assertNotNull(emprestimo.getDataEmprestimo());
        assertNotNull(emprestimo.getDataDevolucaoPrevista());
        verify(livroService).decrementarDisponibilidade(livroDisponivel);
    }

    // =========================================================
    // RN-12: Testes de Devolução Única
    // =========================================================

    @Test
    @DisplayName("TU-008: Deve impedir devolução duplicada de um empréstimo (RN-12)")
    void deveImpedirDevolucaoDuplicada() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(1L);
        emprestimo.setDataDevolucaoReal(LocalDate.now().minusDays(1)); // já devolvido
        emprestimo.setAtivo(false);

        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));

        assertThrows(RuntimeException.class,
                () -> emprestimoService.registrarDevolucao(1L),
                "RN-12: Deve lançar exceção ao tentar devolver empréstimo já devolvido");
    }

    @Test
    @DisplayName("TU-009: Deve registrar devolução com sucesso e incrementar disponibilidade (RF-12)")
    void deveRegistrarDevolucaoComSucesso() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(1L);
        emprestimo.setLivro(livroDisponivel);
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().plusDays(3));
        emprestimo.setAtivo(true);

        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(i -> i.getArgument(0));

        Emprestimo devolvido = emprestimoService.registrarDevolucao(1L);

        assertNotNull(devolvido.getDataDevolucaoReal());
        assertFalse(devolvido.getAtivo());
        assertEquals(0.0, devolvido.getMulta(), 0.01);
        verify(livroService).incrementarDisponibilidade(livroDisponivel);
    }
}