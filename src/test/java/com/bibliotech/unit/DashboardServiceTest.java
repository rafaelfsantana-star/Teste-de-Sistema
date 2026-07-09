package com.bibliotech.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.repository.EmprestimoRepository;
import com.bibliotech.repository.LivroRepository;
import com.bibliotech.repository.UsuarioRepository;
import com.bibliotech.service.DashboardService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do DashboardService")
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
    @DisplayName("Deve retornar estatísticas corretas para o dashboard")
    void deveRetornarEstatisticasCorretas() {
        // Arrange
        when(livroRepository.count()).thenReturn(50L);
        when(usuarioRepository.findByAtivoTrue()).thenReturn(Arrays.asList(new Usuario()));
        when(emprestimoRepository.count()).thenReturn(5L);
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0))
                .thenReturn(Arrays.asList(new Livro()));
        when(emprestimoRepository.findEmprestimosAtrasados()).thenReturn(Arrays.asList());

        // Act
        Map<String, Object> stats = dashboardService.obterEstatisticas();

        // Assert
        assertEquals(50L, stats.get("totalLivros"));
        assertEquals(1L, stats.get("totalUsuarios")); // um usuário na lista mockada
        assertEquals(5L, stats.get("emprestimosAtivos"));
        assertEquals(1L, stats.get("livrosDisponiveis"));
        assertEquals(0L, stats.get("emprestimosAtrasados"));
    }
}