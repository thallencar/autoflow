package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.VeiculoValidator;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.model.Veiculo;
import br.com.autoflow.ports.outbound.OrdemServicoRepositoryPort;
import br.com.autoflow.ports.outbound.VeiculoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepositoryPort veiculoRepository;

    @Mock
    private OrdemServicoRepositoryPort ordemServicoRepository;

    @Mock
    private VeiculoValidator veiculoValidator;

    @InjectMocks
    private VeiculoUseCaseImpl veiculoService;

    @Nested
    @DisplayName("Criar Veículo")
    class CriarVeiculoTests {

        @Test
        @DisplayName("Deve criar um veículo com sucesso")
        void deveCriarVeiculoComSucesso() {
            // Arrange (Given)
            UUID clienteId = UUID.randomUUID();
            Veiculo veiculoParam = criarVeiculoExemplo(clienteId);
            Veiculo veiculoSalvo = criarVeiculoExemplo(clienteId);
            veiculoSalvo.setId(UUID.randomUUID());

            doNothing().when(veiculoValidator).validarParaCriar(veiculoParam);
            when(veiculoValidator.formatarPlaca(veiculoParam.getPlaca())).thenReturn("ABC1A23");
            when(veiculoRepository.save(veiculoParam)).thenReturn(veiculoSalvo);

            // Act (When)
            Veiculo resultado = veiculoService.criar(veiculoParam);

            // Assert (Then)
            assertThat(resultado).isNotNull().isEqualTo(veiculoSalvo);
            verify(veiculoValidator).validarParaCriar(veiculoParam);
            verify(veiculoValidator).formatarPlaca(veiculoParam.getPlaca());
            verify(veiculoRepository).save(veiculoParam);
        }

        @Test
        @DisplayName("Deve lançar exceção quando a validação de criação falhar")
        void deveLancarExcecaoQuandoValidacaoFalhar() {
            // Arrange
            Veiculo veiculo = criarVeiculoExemplo(UUID.randomUUID());
            doThrow(new IllegalArgumentException("Placa já cadastrada"))
                    .when(veiculoValidator).validarParaCriar(veiculo);

            // Act & Assert
            assertThatThrownBy(() -> veiculoService.criar(veiculo))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Placa já cadastrada");

            verify(veiculoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Listar Veículos")
    class ListarVeiculosTests {

        @Test
        @DisplayName("Deve listar todos os veículos com sucesso")
        void deveListarTodosOsVeiculos() {
            // Arrange
            Veiculo v1 = criarVeiculoExemplo(UUID.randomUUID());
            Veiculo v2 = criarVeiculoExemplo(UUID.randomUUID());

            when(veiculoRepository.findAll()).thenReturn(List.of(v1, v2));

            // Act
            List<Veiculo> resultado = veiculoService.listar();

            // Assert
            assertThat(resultado).hasSize(2).containsExactly(v1, v2);
            verify(veiculoRepository).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver veículos cadastrados")
        void deveRetornarListaVazia() {
            // Arrange
            when(veiculoRepository.findAll()).thenReturn(List.of());

            // Act
            List<Veiculo> resultado = veiculoService.listar();

            // Assert
            assertThat(resultado).isEmpty();
            verify(veiculoRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Buscar Veículo por ID")
    class BuscarPorIdTests {

        @Test
        @DisplayName("Deve buscar veículo por ID com sucesso")
        void deveBuscarPorIdComSucesso() {
            // Arrange
            UUID id = UUID.randomUUID();
            Veiculo veiculo = criarVeiculoExemplo(UUID.randomUUID());

            when(veiculoValidator.buscarVeiculo(id)).thenReturn(veiculo);

            // Act
            Veiculo resultado = veiculoService.buscarPorId(id);

            // Assert
            assertThat(resultado).isNotNull().isEqualTo(veiculo);
            verify(veiculoValidator).buscarVeiculo(id);
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não for encontrado")
        void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(veiculoValidator.buscarVeiculo(id))
                    .thenThrow(new EntidadeNaoEncontradaException("Veículo", id));

            // Act & Assert
            assertThatThrownBy(() -> veiculoService.buscarPorId(id))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Atualizar Veículo")
    class AtualizarVeiculoTests {

        @Test
        @DisplayName("Deve atualizar veículo com sucesso")
        void deveAtualizarVeiculoComSucesso() {
            // Arrange
            UUID id = UUID.randomUUID();
            UUID clienteId = UUID.randomUUID();
            Veiculo veiculoParam = criarVeiculoExemplo(clienteId);
            Veiculo veiculoExistente = criarVeiculoExemplo(clienteId);
            veiculoExistente.setId(id);

            when(veiculoValidator.buscarVeiculo(id)).thenReturn(veiculoExistente);
            when(veiculoValidator.formatarPlaca(veiculoParam.getPlaca())).thenReturn("ABC1A23");
            doNothing().when(veiculoValidator).validarParaAtualizar(id, veiculoParam);
            when(veiculoRepository.save(veiculoExistente)).thenReturn(veiculoExistente);

            // Act
            Veiculo resultado = veiculoService.atualizar(id, veiculoParam);

            // Assert
            assertThat(resultado).isNotNull();
            verify(veiculoValidator).buscarVeiculo(id);
            verify(veiculoValidator).validarParaAtualizar(id, veiculoParam);
            verify(veiculoRepository).save(veiculoExistente);
        }
    }

    @Nested
    @DisplayName("Deletar Veículo")
    class DeletarVeiculoTests {

        @Test
        @DisplayName("Deve deletar veículo com sucesso quando não houver OS vinculada")
        void deveDeletarVeiculoComSucesso() {
            UUID id = UUID.randomUUID();
            Veiculo veiculo = criarVeiculoExemplo(UUID.randomUUID());

            when(veiculoValidator.validarParaDeletar(id)).thenReturn(veiculo);

            veiculoService.deletar(id);

            verify(veiculoValidator).validarParaDeletar(id);
            verify(veiculoRepository).delete(veiculo);
        }
    }

    // Método auxiliar para instanciar o Domínio Veiculo
    private Veiculo criarVeiculoExemplo(UUID clienteId) {
        return new Veiculo(
                UUID.randomUUID(),
                "ABC1A23",
                "Fiat",
                "Argo",
                12000,
                (short) 2022,
                "Prata",
                clienteId
        );
    }
}