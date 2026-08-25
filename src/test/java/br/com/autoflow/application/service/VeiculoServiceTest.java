package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.VeiculoRequest;
import br.com.autoflow.application.dto.VeiculoResponse;
import br.com.autoflow.domain.model.Cliente;
import br.com.autoflow.domain.model.Veiculo;
import br.com.autoflow.domain.repository.ClienteRepository;
import br.com.autoflow.domain.repository.VeiculoRepository;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.infrastructure.mapper.VeiculoMapper;
import jakarta.validation.constraints.*;
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
    private VeiculoRepository veiculoRepository;

    @Mock
    private VeiculoMapper veiculoMapper;

    @Mock
    private VeiculoValidator veiculoValidator;

    @InjectMocks
    private VeiculoService veiculoService;

    @Nested
    @DisplayName("Criar Veículo")
    class CriarVeiculoTests {

        @Test
        @DisplayName("Deve criar um veículo com sucesso")
        void deveCriarVeiculoComSucesso() {
            // Arrange (Given)
            UUID clienteId = UUID.randomUUID();
            VeiculoRequest request = criarVeiculoRequest(clienteId);
            Cliente cliente = new Cliente();
            Veiculo veiculoSemId = new Veiculo();
            Veiculo veiculoSalvo = new Veiculo();
            VeiculoResponse responseEsperada = criarVeiculoResponse();

            doNothing().when(veiculoValidator).validarParaCriar(request);
            when(veiculoValidator.buscarCliente(clienteId)).thenReturn(cliente);
            when(veiculoMapper.toEntity(request, cliente)).thenReturn(veiculoSemId);
            when(veiculoRepository.save(veiculoSemId)).thenReturn(veiculoSalvo);
            when(veiculoMapper.toResponse(veiculoSalvo)).thenReturn(responseEsperada);

            // Act (When)
            VeiculoResponse resultado = veiculoService.criar(request);

            // Assert (Then)
            assertThat(resultado).isNotNull().isEqualTo(responseEsperada);
            verify(veiculoValidator).validarParaCriar(request);
            verify(veiculoValidator).buscarCliente(clienteId);
            verify(veiculoMapper).toEntity(request, cliente);
            verify(veiculoRepository).save(veiculoSemId);
            verify(veiculoMapper).toResponse(veiculoSalvo);
        }

        @Test
        @DisplayName("Deve lançar exceção quando a validação de criação falhar")
        void deveLancarExcecaoQuandoValidacaoFalhar() {
            // Arrange
            VeiculoRequest request = criarVeiculoRequest(UUID.randomUUID());
            doThrow(new IllegalArgumentException("Placa já cadastrada"))
                    .when(veiculoValidator).validarParaCriar(request);

            // Act & Assert
            assertThatThrownBy(() -> veiculoService.criar(request))
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
            Veiculo v1 = new Veiculo();
            Veiculo v2 = new Veiculo();
            VeiculoResponse r1 = criarVeiculoResponse();
            VeiculoResponse r2 = criarVeiculoResponse();

            when(veiculoRepository.findAll()).thenReturn(List.of(v1, v2));
            when(veiculoMapper.toResponse(v1)).thenReturn(r1);
            when(veiculoMapper.toResponse(v2)).thenReturn(r2);

            // Act
            List<VeiculoResponse> resultado = veiculoService.listar();

            // Assert
            assertThat(resultado).hasSize(2).containsExactly(r1, r2);
            verify(veiculoRepository).findAll();
            verify(veiculoMapper, times(2)).toResponse(any());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver veículos cadastrados")
        void deveRetornarListaVazia() {
            // Arrange
            when(veiculoRepository.findAll()).thenReturn(List.of());

            // Act
            List<VeiculoResponse> resultado = veiculoService.listar();

            // Assert
            assertThat(resultado).isEmpty();
            verify(veiculoRepository).findAll();
            verify(veiculoMapper, never()).toResponse(any());
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
            Veiculo veiculo = new Veiculo();
            VeiculoResponse responseEsperada = criarVeiculoResponse();

            when(veiculoValidator.buscarVeiculo(id)).thenReturn(veiculo);
            when(veiculoMapper.toResponse(veiculo)).thenReturn(responseEsperada);

            // Act
            VeiculoResponse resultado = veiculoService.buscarPorId(id);

            // Assert
            assertThat(resultado).isNotNull().isEqualTo(responseEsperada);
            verify(veiculoValidator).buscarVeiculo(id);
            verify(veiculoMapper).toResponse(veiculo);
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

            verify(veiculoMapper, never()).toResponse(any());
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
                VeiculoRequest request = criarVeiculoRequest(clienteId);

                Veiculo veiculoExistente = new Veiculo(); // Mudado de mock para instância real
                Cliente novoCliente = new Cliente();
                VeiculoResponse responseEsperada = criarVeiculoResponse();

                when(veiculoValidator.buscarVeiculo(id)).thenReturn(veiculoExistente);
                doNothing().when(veiculoValidator).validarParaAtualizar(id, request);
                when(veiculoValidator.buscarCliente(clienteId)).thenReturn(novoCliente);

                // Não é necessário mockar o void do mapper, mas podemos verificar a interação abaixo

                when(veiculoRepository.save(veiculoExistente)).thenReturn(veiculoExistente);
                when(veiculoMapper.toResponse(veiculoExistente)).thenReturn(responseEsperada);

                // Act
                VeiculoResponse resultado = veiculoService.atualizar(id, request);

                // Assert
                assertThat(resultado).isNotNull().isEqualTo(responseEsperada);
                verify(veiculoValidator).buscarVeiculo(id);
                verify(veiculoValidator).validarParaAtualizar(id, request);
                verify(veiculoValidator).buscarCliente(clienteId);

                // Verifica se o mapper foi chamado corretamente para atualizar a entidade
                verify(veiculoMapper).updateEntity(request, veiculoExistente, novoCliente);

                verify(veiculoRepository).save(veiculoExistente);
                verify(veiculoMapper).toResponse(veiculoExistente);
            }
        }

    @Nested
    @DisplayName("Deletar Veículo")
    class DeletarVeiculoTests {

        @Test
        @DisplayName("Deve deletar veículo com sucesso")
        void deveDeletarVeiculoComSucesso() {
            // Arrange
            UUID id = UUID.randomUUID();
            Veiculo veiculo = new Veiculo();

            when(veiculoValidator.buscarVeiculo(id)).thenReturn(veiculo);

            // Act
            veiculoService.deletar(id);

            // Assert
            verify(veiculoValidator).buscarVeiculo(id);
            verify(veiculoRepository).delete(veiculo);
        }
    }

    // --- Helpers auxiliares para construção dos DTOs dos testes ---

    private VeiculoRequest criarVeiculoRequest(UUID clienteId) {
        return new VeiculoRequest(
                "ABC1D23",
                "Toyota",
                "Corolla",
                55000,
                Short.valueOf("2022"),
                "Prata",
                clienteId
        );
    }


    private VeiculoResponse criarVeiculoResponse() {
        return new VeiculoResponse(
                UUID.randomUUID(),
                "ABC1D23",
                "Toyota",
                "Corolla",
                10000,
                Short.valueOf("2022"),
                "Prata",
                UUID.randomUUID()
        );
    }
}