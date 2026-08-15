package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.VeiculoRequest;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Veiculo;
import br.com.autoflow.domain.repository.ClienteRepository;
import br.com.autoflow.domain.repository.VeiculoRepository;
import br.com.autoflow.exception.DadosJaCadastradosException;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoValidatorTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private VeiculoValidator veiculoValidator;

    @Nested
    @DisplayName("Testes de Criação (validarParaCriar)")
    class ValidarParaCriarTests {

        @Test
        @DisplayName("Deve validar criação com sucesso quando dados forem válidos")
        void deveValidarParaCriarComSucesso() {
            // Arrange
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "abc-1d23", "Toyota", "Corolla", Short.valueOf("2023"), "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> veiculoValidator.validarParaCriar(request));
            verify(veiculoRepository, times(1)).existsByPlaca("ABC1D23");
            verify(clienteRepository, times(1)).existsById(clienteId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando placa já estiver cadastrada na criação")
        void deveLancarExcecaoQuandoPlacaJaExisteNaCriacao() {
            // Arrange
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "ABC1D23", "Toyota", "Corolla", Short.valueOf("2023"), "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(true);

            // Act & Assert
            DadosJaCadastradosException ex = assertThrows(
                    DadosJaCadastradosException.class,
                    () -> veiculoValidator.validarParaCriar(request)
            );
            assertTrue(ex.getMessage().contains("Placa já cadastrada: ABC1D23"));
            verify(clienteRepository, never()).existsById(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existir na criação")
        void deveLancarExcecaoQuandoClienteNaoExisteNaCriacao() {
            // Arrange
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "ABC1D23", "Toyota", "Corolla", Short.valueOf("2023"), "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
            when(clienteRepository.existsById(clienteId)).thenReturn(false);

            // Act & Assert
            assertThrows(
                    EntidadeNaoEncontradaException.class,
                    () -> veiculoValidator.validarParaCriar(request)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando ano de fabricação for maior que o permitido")
        void deveLancarExcecaoQuandoAnoFabricacaoInvalido() {
            // Arrange
            UUID clienteId = UUID.randomUUID();
            int anoInvalido = Year.now().getValue() + 2;
            VeiculoRequest request = new VeiculoRequest(
                    "ABC1D23", "Toyota", "Corolla", (short) anoInvalido, "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

            // Act & Assert
            RegraNegocioException ex = assertThrows(
                    RegraNegocioException.class,
                    () -> veiculoValidator.validarParaCriar(request)
            );
            assertTrue(ex.getMessage().contains("O ano de fabricação não pode ser maior que"));
        }
    }

    @Nested
    @DisplayName("Testes de Atualização (validarParaAtualizar)")
    class ValidarParaAtualizarTests {

        @Test
        @DisplayName("Deve validar atualização quando a placa pertence ao próprio veículo")
        void deveValidarAtualizacaoMesmoVeiculoComSucesso() {
            // Arrange
            UUID veiculoId = UUID.randomUUID();
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "ABC-1D23", "Toyota", "Corolla", Short.valueOf("2023"), "Preto", clienteId
            );

            Veiculo veiculoExistente = new Veiculo();
            veiculoExistente.setId(veiculoId);

            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculoExistente));
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> veiculoValidator.validarParaAtualizar(veiculoId, request));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar se a placa pertencer a OUTRO veículo")
        void deveLancarExcecaoQuandoPlacaPertenceAOutroVeiculo() {
            // Arrange
            UUID veiculoId = UUID.randomUUID();
            UUID outroVeiculoId = UUID.randomUUID();
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "ABC1D23", "Toyota", "Corolla", Short.valueOf("2023"), "Preto", clienteId
            );

            Veiculo outroVeiculo = new Veiculo();
            outroVeiculo.setId(outroVeiculoId);

            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(outroVeiculo));

            // Act & Assert
            assertThrows(
                    DadosJaCadastradosException.class,
                    () -> veiculoValidator.validarParaAtualizar(veiculoId, request)
            );
        }

        @Test
        @DisplayName("Deve validar atualização com sucesso quando a placa informada não for encontrada em nenhum veículo")
        void deveValidarAtualizacaoComPlacaInexistenteComSucesso() {
            // Arrange
            UUID veiculoId = UUID.randomUUID();
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "XYZ9876", "Toyota", "Corolla", Short.valueOf("2023"), "Preto", clienteId
            );

            when(veiculoRepository.findByPlaca("XYZ9876")).thenReturn(Optional.empty());
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> veiculoValidator.validarParaAtualizar(veiculoId, request));
        }
    }

    @Nested
    @DisplayName("Testes dos Métodos Auxiliares e de Busca")
    class AuxiliaresAndBuscaTests {

        @Test
        @DisplayName("Deve formatar placa removendo caracteres especiais e deixando em caixa alta")
        void deveFormatarPlacaCorretamente() {
            assertEquals("ABC1D23", veiculoValidator.formatarPlaca("abc-1d23"));
            assertEquals("ABC1234", veiculoValidator.formatarPlaca("abc.1234"));
            assertNull(veiculoValidator.formatarPlaca(null));
        }

        @Test
        @DisplayName("Deve retornar veículo ao buscar por ID existente")
        void deveBuscarVeiculoPorIdComSucesso() {
            UUID id = UUID.randomUUID();
            Veiculo veiculo = new Veiculo();
            veiculo.setId(id);

            when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculo));

            Veiculo resultado = veiculoValidator.buscarVeiculo(id);

            assertNotNull(resultado);
            assertEquals(id, resultado.getId());
        }

        @Test
        @DisplayName("Deve lançar exceção ao buscar veículo por ID inexistente")
        void deveLancarExcecaoAoBuscarVeiculoInexistente() {
            UUID id = UUID.randomUUID();
            when(veiculoRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(EntidadeNaoEncontradaException.class, () -> veiculoValidator.buscarVeiculo(id));
        }

        @Test
        @DisplayName("Deve retornar cliente ao buscar por ID existente")
        void deveBuscarClientePorIdComSucesso() {
            UUID clienteId = UUID.randomUUID();
            Cliente cliente = new Cliente();
            cliente.setId(clienteId);

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

            Cliente resultado = veiculoValidator.buscarCliente(clienteId);

            assertNotNull(resultado);
            assertEquals(clienteId, resultado.getId());
        }

        @Test
        @DisplayName("Deve lançar exceção ao buscar cliente por ID inexistente")
        void deveLancarExcecaoAoBuscarClienteInexistente() {
            UUID clienteId = UUID.randomUUID();
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

            assertThrows(EntidadeNaoEncontradaException.class, () -> veiculoValidator.buscarCliente(clienteId));
        }
    }
}