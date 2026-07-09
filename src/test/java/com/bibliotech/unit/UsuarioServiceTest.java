package com.bibliotech.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bibliotech.model.Usuario;
import com.bibliotech.repository.UsuarioRepository;
import com.bibliotech.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioService")
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioJoao;

    @BeforeEach
    void setUp() {
        usuarioJoao = new Usuario();
        usuarioJoao.setNome("João Silva");
        usuarioJoao.setEmail("joao@email.com");
        usuarioJoao.setCpf("123.456.789-00");
        usuarioJoao.setSenha("senha123");
        usuarioJoao.setTipo(Usuario.TipoUsuario.ALUNO);
        usuarioJoao.setAtivo(true);
    }

    @Test
    @DisplayName("TU-010: Deve validar CPF no formato correto")
    void deveValidarCPFCorreto() {
        assertTrue(usuarioService.validarCPF("123.456.789-00"));
        assertTrue(usuarioService.validarCPF("987.654.321-99"));
    }

    @Test
    @DisplayName("TU-011: Deve rejeitar CPF com formato inválido")
    void deveRejeitarCPFInvalido() {
        assertFalse(usuarioService.validarCPF("123.456.789"));
        assertFalse(usuarioService.validarCPF("123.456.789-0"));
        assertFalse(usuarioService.validarCPF("abc.def.ghi-jk"));
        assertFalse(usuarioService.validarCPF(""));
        assertFalse(usuarioService.validarCPF(null));
    }

    @Test
    @DisplayName("TU-012: Deve lançar exceção ao salvar usuário com email já cadastrado")
    void deveLancarExcecaoEmailDuplicado() {
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuarioJoao));

        RuntimeException excecao = assertThrows(RuntimeException.class,
                () -> usuarioService.salvar(usuarioJoao),
                "Deveria lançar exceção ao salvar email duplicado");

        assertEquals("Email já cadastrado", excecao.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("TU-013: Deve lançar exceção ao salvar usuário com CPF já cadastrado")
    void deveLancarExcecaoCPFDuplicado() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.findByCpf("123.456.789-00")).thenReturn(Optional.of(usuarioJoao));

        RuntimeException excecao = assertThrows(RuntimeException.class,
                () -> usuarioService.salvar(usuarioJoao),
                "Deveria lançar exceção ao salvar CPF duplicado");

        assertEquals("CPF já cadastrado", excecao.getMessage());
    }

    @Test
    @DisplayName("TU-014: Deve autenticar usuário com credenciais corretas (BUG esperado: comparação de senha com ==)")
    void deveAutenticarUsuarioAtivo() {
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuarioJoao));

        // Cria uma senha idêntica, mas como objeto separado (evita o pool de strings)
        String senhaFornecida = new String("senha123");
        Optional<Usuario> resultado = usuarioService.autenticar("joao@email.com", senhaFornecida);

        // O serviço usa '==', portanto a autenticação falhará mesmo com a senha correta
        assertTrue(resultado.isPresent(), "BUG-006: Deveria autenticar usuário com senha correta");
        assertEquals("João Silva", resultado.get().getNome());
    }

    @Test
    @DisplayName("TU-017: Deve lançar exceção ao excluir usuário com empréstimos ativos (BUG esperado: não verifica)")
    void deveImpedirExclusaoComEmprestimosAtivos() {
        // O método excluir atualmente NÃO verifica empréstimos ativos, portanto deve falhar.
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioJoao));

        // Como o serviço não implementa a regra RN-06, a exceção NÃO será lançada.
        RuntimeException excecao = assertThrows(RuntimeException.class,
                () -> usuarioService.excluir(1L),
                "Deveria lançar exceção ao excluir usuário com empréstimos ativos");

        assertEquals("Não é possível excluir usuário com empréstimos ativos", excecao.getMessage());
    }
}