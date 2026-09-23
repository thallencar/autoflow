package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.VeiculoRequest;
import br.com.autoflow.application.validator.VeiculoValidator;
import br.com.autoflow.domain.exception.DadosJaCadastradosException;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Veiculo;
import br.com.autoflow.ports.outbound.ClienteRepositoryPort;
import br.com.autoflow.ports.outbound.VeiculoRepositoryPort;
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
    private VeiculoRepositoryPort veiculoRepository;

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @InjectMocks
    private VeiculoValidator veiculoValidator;

    @Nested
    @DisplayName("Testes de Criação (validarParaCriar)")
    class ValidarParaCriarTests {

        @Test
        @DisplayName("Deve validar criação com sucesso quando dados forem válidos")
        void deveValidarParaCriarComSucesso() {
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "abc-1d23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

            assertDoesNotThrow(() -> veiculoValidator.validarParaCriar(request));
            verify(veiculoRepository, times(1)).existsByPlaca("ABC1D23");
            verify(clienteRepository, times(1)).existsById(clienteId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando placa já estiver cadastrada na criação")
        void deveLancarExcecaoQuandoPlacaJaExisteNaCriacao() {
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(true);

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
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
            when(clienteRepository.existsById(clienteId)).thenReturn(false);

            assertThrows(
                    EntidadeNaoEncontradaException.class,
                    () -> veiculoValidator.validarParaCriar(request)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando ano de fabricação for maior que o permitido")
        void deveLancarExcecaoQuandoAnoFabricacaoInvalido() {
            UUID clienteId = UUID.randomUUID();
            int anoInvalido = Year.now().getValue() + 2;
            VeiculoRequest request = new VeiculoRequest(
                    "ABC1D23", "Toyota", "Corolla", 12000, (short) anoInvalido, "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

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
            UUID veiculoId = UUID.randomUUID();
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "ABC-1D23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Preto", clienteId
            );

            // Construtor completo do Cliente (id, nome, documento, email, dataNascimento, telefone, genero, endereco)
            Cliente cliente = new Cliente(clienteId, "João da Silva", "12345678901", "joao@email.com", null, "11988887777", null, null);
            Veiculo veiculoExistente = new Veiculo(veiculoId, "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Prata", cliente.getId());

            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculoExistente));
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

            assertDoesNotThrow(() -> veiculoValidator.validarParaAtualizar(veiculoId, request));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar se a placa pertencer a OUTRO veículo")
        void deveLancarExcecaoQuandoPlacaPertenceAOutroVeiculo() {
            UUID veiculoId = UUID.randomUUID();
            UUID outroVeiculoId = UUID.randomUUID();
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Preto", clienteId
            );

            Cliente cliente = new Cliente(clienteId, "Maria Souza", "98765432101", "maria@email.com", null, "11977778888", null, null);
            Veiculo outroVeiculo = new Veiculo(outroVeiculoId, "ABC1D23", "Honda", "Civic", 15000, Short.valueOf("2022"), "Branco", cliente.getId());

            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(outroVeiculo));

            assertThrows(
                    DadosJaCadastradosException.class,
                    () -> veiculoValidator.validarParaAtualizar(veiculoId, request)
            );
        }

        @Test
        @DisplayName("Deve validar atualização com sucesso quando a placa informada não for encontrada em nenhum veículo")
        void deveValidarAtualizacaoComPlacaInexistenteComSucesso() {
            UUID veiculoId = UUID.randomUUID();
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = new VeiculoRequest(
                    "XYZ9876", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Preto", clienteId
            );

            when(veiculoRepository.findByPlaca("XYZ9876")).thenReturn(Optional.empty());
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

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
            UUID clienteId = UUID.randomUUID();
            Cliente cliente = new Cliente(clienteId, "Carlos", "11122233344", "carlos@email.com", null, "11966665555", null, null);
            Veiculo veiculo = new Veiculo(id, "ABC1D23", "Fiat", "Uno", 50000, Short.valueOf("2015"), "Vermelho", cliente.getId());

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
            Cliente cliente = new Cliente(clienteId, "Ana", "55566677788", "ana@email.com", null, "11955554444", null, null);

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