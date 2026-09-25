package br.com.autoflow.application.usecase;

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
            Veiculo veiculo = new Veiculo(
                    null, "abc-1d23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

            assertDoesNotThrow(() -> veiculoValidator.validarParaCriar(veiculo));
            verify(veiculoRepository, times(1)).existsByPlaca("ABC1D23");
            verify(clienteRepository, times(1)).existsById(clienteId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando placa já estiver cadastrada na criação")
        void deveLancarExcecaoQuandoPlacaJaExisteNaCriacao() {
            UUID clienteId = UUID.randomUUID();
            Veiculo veiculo = new Veiculo(
                    null, "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(true);

            DadosJaCadastradosException ex = assertThrows(
                    DadosJaCadastradosException.class,
                    () -> veiculoValidator.validarParaCriar(veiculo)
            );
            assertTrue(ex.getMessage().contains("Placa já cadastrada: ABC1D23"));
            verify(clienteRepository, never()).existsById(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existir na criação")
        void deveLancarExcecaoQuandoClienteNaoExisteNaCriacao() {
            UUID clienteId = UUID.randomUUID();
            Veiculo veiculo = new Veiculo(
                    null, "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
            when(clienteRepository.existsById(clienteId)).thenReturn(false);

            assertThrows(
                    EntidadeNaoEncontradaException.class,
                    () -> veiculoValidator.validarParaCriar(veiculo)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando ano de fabricação for maior que o permitido")
        void deveLancarExcecaoQuandoAnoFabricacaoInvalido() {
            UUID clienteId = UUID.randomUUID();
            int anoInvalido = Year.now().getValue() + 2;
            Veiculo veiculo = new Veiculo(
                    null, "ABC1D23", "Toyota", "Corolla", 12000, (short) anoInvalido, "Prata", clienteId
            );

            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

            RegraNegocioException ex = assertThrows(
                    RegraNegocioException.class,
                    () -> veiculoValidator.validarParaCriar(veiculo)
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
            Veiculo veiculoParam = new Veiculo(
                    veiculoId, "ABC-1D23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Preto", clienteId
            );

            Veiculo veiculoExistente = new Veiculo(veiculoId, "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Prata", clienteId);

            when(veiculoRepository.findByPlaca("ABC-1D23")).thenReturn(Optional.of(veiculoExistente));
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

            assertDoesNotThrow(() -> veiculoValidator.validarParaAtualizar(veiculoId, veiculoParam));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar se a placa pertencer a OUTRO veículo")
        void deveLancarExcecaoQuandoPlacaPertenceAOutroVeiculo() {
            UUID veiculoId = UUID.randomUUID();
            UUID outroVeiculoId = UUID.randomUUID();
            UUID clienteId = UUID.randomUUID();
            Veiculo veiculoParam = new Veiculo(
                    veiculoId, "ABC1D23", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Preto", clienteId
            );

            Veiculo outroVeiculo = new Veiculo(outroVeiculoId, "ABC1D23", "Honda", "Civic", 15000, Short.valueOf("2022"), "Branco", clienteId);

            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(outroVeiculo));

            assertThrows(
                    DadosJaCadastradosException.class,
                    () -> veiculoValidator.validarParaAtualizar(veiculoId, veiculoParam)
            );
        }

        @Test
        @DisplayName("Deve validar atualização com sucesso quando a placa informada não for encontrada em nenhum veículo")
        void deveValidarAtualizacaoComPlacaInexistenteComSucesso() {
            UUID veiculoId = UUID.randomUUID();
            UUID clienteId = UUID.randomUUID();
            Veiculo veiculoParam = new Veiculo(
                    veiculoId, "XYZ9876", "Toyota", "Corolla", 12000, Short.valueOf("2023"), "Preto", clienteId
            );

            when(veiculoRepository.findByPlaca("XYZ9876")).thenReturn(Optional.empty());
            when(clienteRepository.existsById(clienteId)).thenReturn(true);

            assertDoesNotThrow(() -> veiculoValidator.validarParaAtualizar(veiculoId, veiculoParam));
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
            Veiculo veiculo = new Veiculo(id, "ABC1D23", "Fiat", "Uno", 50000, Short.valueOf("2015"), "Vermelho", clienteId);

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
    }
}