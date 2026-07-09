package com.bibliotech.unit;

import com.bibliotech.model.Livro;
import com.bibliotech.repository.LivroRepository;
import com.bibliotech.service.LivroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para LivroService.
 *
 * Bugs identificados:
 * - BUG-004: salvar() não valida ISBN duplicado antes de salvar (RN-02)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - LivroService")
public class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private LivroService livroService;

    private Livro livroPadrao;

    @BeforeEach
    void setUp() {
        livroPadrao = new Livro(
            "Clean Code", "Robert Martin",
            "978-0132350884", "Prentice Hall", 2008, 3
        );
    }

    // ==================== TESTES: salvar ====================

    @Test
    @DisplayName("TU-023: Deve salvar livro válido com sucesso")
    void deveSalvarLivroValido() {
        when(livroRepository.save(any(Livro.class))).thenReturn(livroPadrao);

        Livro salvo = livroService.salvar(livroPadrao);

        assertNotNull(salvo, "Livro salvo não deve ser nulo");
        verify(livroRepository, times(1)).save(livroPadrao);
    }

    @Test
    @DisplayName("TU-024: Quantidade disponível deve ser igual à quantidade de exemplares ao salvar novo livro")
    void deveInicializarQuantidadeDisponivelIgualExemplares() {
        Livro novo = new Livro("TDD", "Kent Beck", "978-0321146533", "Addison-Wesley", 2003, 5);
        novo.setQuantidadeDisponivel(null); // simula livro novo sem disponível definido

        when(livroRepository.save(any(Livro.class))).thenAnswer(i -> i.getArguments()[0]);

        Livro salvo = livroService.salvar(novo);

        assertEquals(5, salvo.getQuantidadeDisponivel(),
            "Quantidade disponível deve ser inicializada com o valor de exemplares");
    }

    @Test
    @DisplayName("TU-025: Deve impedir cadastro de livro com ISBN duplicado (RN-02) [BUG-004]")
    void deveImpedirISBNDuplicado() {
        when(livroRepository.findByIsbn("978-0132350884"))
            .thenReturn(Optional.of(livroPadrao));

        Livro duplicado = new Livro("Outro Livro", "Outro Autor",
            "978-0132350884", "Editora", 2020, 2);

        assertThrows(RuntimeException.class,
            () -> livroService.salvar(duplicado),
            "BUG-004: Sistema deveria lançar exceção ao tentar salvar livro com ISBN já existente (RN-02), mas permite o cadastro duplicado");
    }

    // ==================== TESTES: decrementarDisponibilidade ====================

    @Test
    @DisplayName("TU-026: Deve decrementar disponibilidade ao realizar empréstimo")
    void deveDecrementarDisponibilidade() {
        livroPadrao.setQuantidadeDisponivel(3);
        when(livroRepository.save(any(Livro.class))).thenReturn(livroPadrao);

        livroService.decrementarDisponibilidade(livroPadrao);

        assertEquals(2, livroPadrao.getQuantidadeDisponivel(),
            "Disponibilidade deve ser decrementada de 3 para 2");
    }

    @Test
    @DisplayName("TU-027: Não deve decrementar disponibilidade quando livro já está sem exemplares")
    void naoDeveDecrementarQuandoIndisponivel() {
        livroPadrao.setQuantidadeDisponivel(0);

        livroService.decrementarDisponibilidade(livroPadrao);

        assertEquals(0, livroPadrao.getQuantidadeDisponivel(),
            "Não deve decrementar abaixo de zero");
        verify(livroRepository, never()).save(any());
    }

    // ==================== TESTES: incrementarDisponibilidade ====================

    @Test
    @DisplayName("TU-028: Deve incrementar disponibilidade ao registrar devolução")
    void deveIncrementarDisponibilidade() {
        livroPadrao.setQuantidadeDisponivel(2);
        when(livroRepository.save(any(Livro.class))).thenReturn(livroPadrao);

        livroService.incrementarDisponibilidade(livroPadrao);

        assertEquals(3, livroPadrao.getQuantidadeDisponivel(),
            "Disponibilidade deve ser incrementada de 2 para 3");
    }

    // ==================== TESTES: excluir ====================

    @Test
    @DisplayName("TU-029: Deve lançar exceção ao excluir livro não encontrado")
    void deveLancarExcecaoAoExcluirLivroInexistente() {
        when(livroRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> livroService.excluir(999L),
            "Deve lançar exceção ao tentar excluir livro com ID inexistente");
    }

    @Test
    @DisplayName("TU-030: isDisponivel deve retornar true quando há exemplares")
    void isDisponivelDeveRetornarTrueComExemplares() {
        livroPadrao.setQuantidadeDisponivel(2);

        assertTrue(livroPadrao.isDisponivel(),
            "Livro com exemplares disponíveis deve retornar true");
    }

    @Test
    @DisplayName("TU-031: isDisponivel deve retornar false quando não há exemplares")
    void isDisponivelDeveRetornarFalseSemExemplares() {
        livroPadrao.setQuantidadeDisponivel(0);

        assertFalse(livroPadrao.isDisponivel(),
            "Livro sem exemplares deve retornar false em isDisponivel()");
    }
}
