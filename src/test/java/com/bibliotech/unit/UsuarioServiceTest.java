package com.bibliotech.unit;

import com.bibliotech.model.Usuario;
import com.bibliotech.repository.UsuarioRepository;
import com.bibliotech.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para UsuarioService.
 *
 * Bugs identificados:
 * - BUG-003: autenticar() usa == em vez de .equals() para comparar Strings,
 *            fazendo com que credenciais válidas nunca autentiquem (RF-15)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - UsuarioService")
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioPadrao;

    @BeforeEach
    void setUp() {
        usuarioPadrao = new Usuario(
            "Admin",
            "admin@bibliotech.com",
            "123.456.789-09",
            "admin123",
            Usuario.TipoUsuario.FUNCIONARIO
        );
    }

    // ==================== TESTES: validarCPF ====================

    @Test
    @DisplayName("TU-013: CPF no formato correto deve ser válido (RN-07)")
    void deveValidarCPFFormatoCorreto() {
        assertTrue(usuarioService.validarCPF("123.456.789-00"),
            "CPF no formato ###.###.###-## deve ser válido");
    }

    @Test
    @DisplayName("TU-014: CPF sem pontuação deve ser inválido (RN-07)")
    void deveInvalidarCPFSemPontuacao() {
        assertFalse(usuarioService.validarCPF("12345678900"),
            "CPF sem pontuação não deve ser aceito");
    }

    @Test
    @DisplayName("TU-015: CPF nulo deve ser inválido (RN-07)")
    void deveInvalidarCPFNulo() {
        assertFalse(usuarioService.validarCPF(null),
            "CPF nulo deve retornar false");
    }

    @Test
    @DisplayName("TU-016: CPF vazio deve ser inválido (RN-07)")
    void deveInvalidarCPFVazio() {
        assertFalse(usuarioService.validarCPF(""),
            "CPF vazio deve retornar false");
    }

    @Test
    @DisplayName("TU-017: CPF com formato parcialmente errado deve ser inválido (RN-07)")
    void deveInvalidarCPFFormatoParcialmenteErrado() {
        assertFalse(usuarioService.validarCPF("123.456.789-0"),
            "CPF com formato parcialmente errado não deve ser aceito");
    }

    // ==================== TESTES: autenticar ====================

    @Test
    @DisplayName("TU-018: Autenticação com credenciais válidas deve retornar o usuário (RF-15) [BUG-003]")
    void deveAutenticarComCredenciaisValidas() {
        when(usuarioRepository.findByEmail("admin@bibliotech.com"))
            .thenReturn(Optional.of(usuarioPadrao));

        Optional<Usuario> resultado = usuarioService.autenticar(
            "admin@bibliotech.com", "admin123");

        assertTrue(resultado.isPresent(),
            "BUG-003: Autenticação com credenciais válidas deve retornar o usuário. " +
            "O bug usa == em vez de .equals() para comparar a senha, o que nunca é verdadeiro para objetos String distintos.");
    }

    @Test
    @DisplayName("TU-019: Autenticação com senha errada deve retornar vazio")
    void deveRetornarVazioComSenhaErrada() {
        when(usuarioRepository.findByEmail("admin@bibliotech.com"))
            .thenReturn(Optional.of(usuarioPadrao));

        Optional<Usuario> resultado = usuarioService.autenticar(
            "admin@bibliotech.com", "senhaErrada");

        assertFalse(resultado.isPresent(),
            "Autenticação com senha incorreta deve falhar");
    }

    @Test
    @DisplayName("TU-020: Autenticação com email inexistente deve retornar vazio")
    void deveRetornarVazioComEmailInexistente() {
        when(usuarioRepository.findByEmail("naoexiste@teste.com"))
            .thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.autenticar(
            "naoexiste@teste.com", "qualquersenha");

        assertFalse(resultado.isPresent(),
            "Autenticação com email inexistente deve retornar Optional vazio");
    }

    // ==================== TESTES: salvar ====================

    @Test
    @DisplayName("TU-021: Deve lançar exceção ao salvar usuário com CPF inválido")
    void deveLancarExcecaoComCPFInvalido() {
        Usuario u = new Usuario("Teste", "teste@email.com", "cpfinvalido",
            "senha", Usuario.TipoUsuario.ALUNO);

        assertThrows(RuntimeException.class,
            () -> usuarioService.salvar(u),
            "Deve lançar exceção ao tentar salvar usuário com CPF inválido");
    }

    @Test
    @DisplayName("TU-022: Deve lançar exceção ao cadastrar email duplicado")
    void deveLancarExcecaoEmailDuplicado() {
        when(usuarioRepository.findByEmail("admin@bibliotech.com"))
            .thenReturn(Optional.of(usuarioPadrao));

        Usuario novoUsuario = new Usuario("Outro", "admin@bibliotech.com",
            "987.654.321-00", "senha456", Usuario.TipoUsuario.ALUNO);

        assertThrows(RuntimeException.class,
            () -> usuarioService.salvar(novoUsuario),
            "Deve lançar exceção ao tentar cadastrar email já existente");
    }
}
