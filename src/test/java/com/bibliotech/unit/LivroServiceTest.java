package com.bibliotech.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
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
import com.bibliotech.repository.LivroRepository;
import com.bibliotech.service.LivroService;

@ExtendWith(MockitoExtension.class)
@DisplayName("LivroService - Testes Unitários")
class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private LivroService livroService;

    private Livro livroValido;

    @BeforeEach
    void setUp() {
        livroValido = new Livro();
        livroValido.setTitulo("Clean Code");
        livroValido.setAutor("Robert C. Martin");
        livroValido.setIsbn("978-0132350884");
        livroValido.setEditora("Prentice Hall");
        livroValido.setAno(2008);
        livroValido.setQuantidadeExemplares(3);
        livroValido.setQuantidadeDisponivel(3);
    }

    @Test
    @DisplayName("TU-008: Deve lançar exceção ao cadastrar livro com ISBN duplicado (RN-02)")
    void deveLancarExcecaoAoCadastrarISBNDuplicado() {
        when(livroRepository.findByIsbn("978-0132350884"))
                .thenReturn(Optional.of(livroValido));

        Livro livroDuplicado = new Livro();
        livroDuplicado.setTitulo("Outro Livro");
        livroDuplicado.setAutor("Outro Autor");
        livroDuplicado.setIsbn("978-0132350884");
        livroDuplicado.setAno(2020);
        livroDuplicado.setQuantidadeExemplares(2);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> livroService.salvar(livroDuplicado),
                "RN-02 violado: Deveria lançar exceção para ISBN duplicado");

        assertTrue(exception.getMessage().contains("ISBN"),
                "Mensagem deve mencionar ISBN");
    }

    @Test
    @DisplayName("TU-009: Deve salvar livro com sucesso (RF-01)")
    void deveSalvarLivroComSucesso() {
        when(livroRepository.save(any(Livro.class))).thenReturn(livroValido);

        Livro salvo = livroService.salvar(livroValido);

        assertNotNull(salvo);
        verify(livroRepository).save(livroValido);
    }

    @Test
    @DisplayName("TU-010: Deve definir quantidade disponível igual à de exemplares (RF-01)")
    void deveDefinirQuantidadeDisponivelAoCadastrar() {
        livroValido.setQuantidadeExemplares(5);
        when(livroRepository.save(any(Livro.class))).thenAnswer(i -> i.getArgument(0));

        Livro salvo = livroService.salvar(livroValido);

        assertEquals(salvo.getQuantidadeExemplares(), salvo.getQuantidadeDisponivel());
    }

    @Test
    @DisplayName("TU-016: Deve impedir exclusão de livro com empréstimos ativos (RN-05)")
    void deveImpedirExclusaoComEmprestimosAtivos() {
        Emprestimo empAtivo = mock(Emprestimo.class);
        when(empAtivo.getAtivo()).thenReturn(true);
        livroValido.getEmprestimos().add(empAtivo);

        when(livroRepository.findById(1L)).thenReturn(Optional.of(livroValido));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> livroService.excluir(1L),
                "RN-05: Deve lançar exceção ao excluir livro com empréstimos ativos");

        assertTrue(exception.getMessage().toLowerCase().contains("empréstimo") ||
                exception.getMessage().toLowerCase().contains("emprestimo"));
        verify(livroRepository, never()).delete(any(Livro.class));
    }

    @Test
    @DisplayName("TU-017: Deve excluir livro com sucesso quando não há empréstimos ativos (RN-05)")
    void deveExcluirLivroSemEmprestimosAtivos() {
        livroValido.setId(1L);
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livroValido));

        assertDoesNotThrow(() -> livroService.excluir(1L));
        verify(livroRepository).delete(livroValido);
    }

    @Test
    @DisplayName("TU-018: Deve buscar livros por título case-insensitive (RF-05)")
    void deveBuscarLivrosPorTituloIgnorandoCase() {
        when(livroRepository.findByTituloContainingIgnoreCase("clean"))
                .thenReturn(List.of(livroValido));

        List<Livro> resultado = livroService.buscarPorTitulo("clean");

        assertFalse(resultado.isEmpty());
        assertEquals("Clean Code", resultado.get(0).getTitulo());
    }

    @Test
    @DisplayName("TU-019: Deve retornar lista vazia ao não encontrar livro (RF-05)")
    void deveRetornarListaVaziaQuandoLivroNaoEncontrado() {
        when(livroRepository.findByTituloContainingIgnoreCase("Inexistente"))
                .thenReturn(new ArrayList<>());

        List<Livro> resultado = livroService.buscarPorTitulo("Inexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("TU-020: Deve decrementar quantidade disponível ao emprestar (RF-10)")
    void deveDecrementarQuantidadeDisponivelAoEmprestar() {
        livroValido.setQuantidadeDisponivel(3);
        when(livroRepository.save(any(Livro.class))).thenAnswer(i -> i.getArgument(0));

        livroService.decrementarDisponibilidade(livroValido);

        assertEquals(2, livroValido.getQuantidadeDisponivel());
    }

    @Test
    @DisplayName("TU-021: Deve incrementar quantidade disponível ao devolver (RF-12)")
    void deveIncrementarQuantidadeDisponivelAoDevolver() {
        livroValido.setQuantidadeDisponivel(1);
        livroValido.setQuantidadeExemplares(3);
        when(livroRepository.save(any(Livro.class))).thenAnswer(i -> i.getArgument(0));

        livroService.incrementarDisponibilidade(livroValido);

        assertEquals(2, livroValido.getQuantidadeDisponivel());
    }
}