package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.OrdemServicoRequest;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.domain.repository.*;
import br.com.autoflow.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoValidatorTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private OrdemServicoValidator validator;

    // --- TESTES DE VALIDAÇÃO DE CRIAÇÃO (Fluxo Principal) ---

    @Test
    @DisplayName("Deve validar criação de OS com sucesso quando todas as regras forem atendidas")
    void deveValidarCriacaoComSucesso() {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();

        OrdemServicoRequest request = mock(OrdemServicoRequest.class);
        when(request.idCliente()).thenReturn(clienteId);
        when(request.idVeiculo()).thenReturn(veiculoId);
        when(request.idFuncionario()).thenReturn(funcionarioId);
        when(request.nrKmEntrada()).thenReturn(10000);
        when(request.stTermoAceito()).thenReturn(true);
        when(request.dtAceiteTermo()).thenReturn(LocalDateTime.now().minusHours(1));
        when(request.idsOrcamento()).thenReturn(List.of(orcamentoId));

        when(clienteRepository.existsById(clienteId)).thenReturn(true);
        when(veiculoRepository.existsById(veiculoId)).thenReturn(true);
        when(funcionarioRepository.existsById(funcionarioId)).thenReturn(true);
        when(veiculoRepository.existsByIdAndClienteId(veiculoId, clienteId)).thenReturn(true);
        when(ordemServicoRepository.findTopByIdVeiculoOrderByDtAberturaOsDesc(veiculoId)).thenReturn(Optional.empty());

        Orcamento orcamento = mock(Orcamento.class);
        when(orcamento.getStatus()).thenReturn(StatusOrcamento.APROVADO);
        when(orcamento.getDataExpiracao()).thenReturn(LocalDateTime.now().plusDays(1));
        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
        when(orcamentoRepository.existsByIdAndOrdemServicoIsNotNull(orcamentoId)).thenReturn(false);

        when(ordemServicoRepository.existsByIdVeiculoAndStatusOSNotIn(eq(veiculoId), any())).thenReturn(false);

        assertDoesNotThrow(() -> validator.validarCriacao(request, true, 5L));
    }

    @Test
    @DisplayName("Deve lançar exceção se já existir OS aberta para o veículo")
    void deveLancarExcecaoSeExistirOsAberta() {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();

        OrdemServicoRequest request = mock(OrdemServicoRequest.class);
        when(request.idCliente()).thenReturn(clienteId);
        when(request.idVeiculo()).thenReturn(veiculoId);
        when(request.idFuncionario()).thenReturn(null);
        when(request.nrKmEntrada()).thenReturn(5000);
        when(request.stTermoAceito()).thenReturn(true);
        when(request.dtAceiteTermo()).thenReturn(LocalDateTime.now());
        when(request.idsOrcamento()).thenReturn(null);

        when(clienteRepository.existsById(clienteId)).thenReturn(true);
        when(veiculoRepository.existsById(veiculoId)).thenReturn(true);
        when(veiculoRepository.existsByIdAndClienteId(veiculoId, clienteId)).thenReturn(true);
        when(ordemServicoRepository.findTopByIdVeiculoOrderByDtAberturaOsDesc(veiculoId)).thenReturn(Optional.empty());
        when(ordemServicoRepository.existsByIdVeiculoAndStatusOSNotIn(eq(veiculoId), any())).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> validator.validarCriacao(request, true, 2L));
    }

    // --- TESTES DE CLIENTE, FUNCIONÁRIO E VEÍCULO ---

    @Test
    @DisplayName("Deve lançar exceção se cliente não existir")
    void deveLancarExcecaoClienteInexistente() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.existsById(id)).thenReturn(false);
        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.validarCliente(id));
    }

    @Test
    @DisplayName("Deve passar silenciosamente se ID do funcionário for nulo")
    void devePassarSeFuncionarioNulo() {
        assertDoesNotThrow(() -> validator.validarFuncionarioID(null));
        verifyNoInteractions(funcionarioRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção se funcionário não existir")
    void deveLancarExcecaoFuncionarioInexistente() {
        UUID id = UUID.randomUUID();
        when(funcionarioRepository.existsById(id)).thenReturn(false);
        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.validarFuncionarioID(id));
    }

    @Test
    @DisplayName("Deve lançar exceção se veículo não existir")
    void deveLancarExcecaoVeiculoInexistente() {
        UUID id = UUID.randomUUID();
        when(veiculoRepository.existsById(id)).thenReturn(false);
        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.validarVeiculoPorID(id));
    }

    @Test
    @DisplayName("Deve lançar exceção se veículo não pertencer ao cliente")
    void deveLancarExcecaoVeiculoNaoPertenceAoCliente() {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        when(veiculoRepository.existsByIdAndClienteId(veiculoId, clienteId)).thenReturn(false);

        assertThrows(RegraNegocioException.class, () -> validator.validarPropriedadeVeiculo(clienteId, veiculoId));
    }

    // --- TESTES DE TERMO DE ACEITE ---

    @Test
    @DisplayName("Deve lançar exceção se termo de aceite não estiver assinado")
    void deveLancarExcecaoTermoNaoAssinado() {
        assertThrows(RegraNegocioException.class, () -> validator.validarTermoDeAceite(false));
    }

    @Test
    @DisplayName("Deve validar data de aceite do termo com sucesso")
    void deveValidarDataAceiteTermoComSucesso() {
        LocalDateTime dataValida = LocalDateTime.now().minusMinutes(10);
        assertDoesNotThrow(() -> validator.validarDataAceiteTermo(true, dataValida));
    }

    @Test
    @DisplayName("Deve lançar exceção se data do aceite for nula com termo assinado")
    void deveLancarExcecaoDataAceiteNula() {
        assertThrows(RegraNegocioException.class, () -> validator.validarDataAceiteTermo(true, null));
    }

    @Test
    @DisplayName("Deve lançar exceção se data do aceite estiver no futuro")
    void deveLancarExcecaoDataAceiteNoFuturo() {
        LocalDateTime dataFutura = LocalDateTime.now().plusDays(1);
        assertThrows(RegraNegocioException.class, () -> validator.validarDataAceiteTermo(true, dataFutura));
    }

    // --- TESTES DE ORÇAMENTO ---

    @Test
    @DisplayName("Deve retornar lista vazia se ids de orçamento forem nulos ou vazios")
    void deveRetornarListaVaziaSeOrcamentosNulos() {
        assertTrue(validator.validarECarregarOrcamentosParaOS(null).isEmpty());
        assertTrue(validator.validarECarregarOrcamentosParaOS(Collections.emptyList()).isEmpty());
    }

    @Test
    @DisplayName("Deve lançar exceção se orçamento não estiver aprovado")
    void deveLancarExcecaoOrcamentoNaoAprovado() {
        UUID id = UUID.randomUUID();
        Orcamento orcamento = new Orcamento();
        orcamento.setStatus(StatusOrcamento.PENDENTE);

        when(orcamentoRepository.findById(id)).thenReturn(Optional.of(orcamento));

        assertThrows(RegraNegocioException.class, () -> validator.validarOrcamentoParaOS(id));
    }

    @Test
    @DisplayName("Deve lançar exceção se orçamento estiver expirado")
    void deveLancarExcecaoOrcamentoExpirado() {
        UUID id = UUID.randomUUID();
        Orcamento orcamento = new Orcamento();
        orcamento.setStatus(StatusOrcamento.APROVADO);
        orcamento.setDataExpiracao(LocalDateTime.now().minusDays(1));

        when(orcamentoRepository.findById(id)).thenReturn(Optional.of(orcamento));

        assertThrows(RegraNegocioException.class, () -> validator.validarOrcamentoParaOS(id));
    }

    @Test
    @DisplayName("Deve lançar exceção se orçamento já estiver vinculado a outra OS")
    void deveLancarExcecaoOrcamentoJaVinculado() {
        UUID id = UUID.randomUUID();
        Orcamento orcamento = new Orcamento();
        orcamento.setStatus(StatusOrcamento.APROVADO);
        orcamento.setDataExpiracao(LocalDateTime.now().plusDays(1));

        when(orcamentoRepository.findById(id)).thenReturn(Optional.of(orcamento));
        when(orcamentoRepository.existsByIdAndOrdemServicoIsNotNull(id)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> validator.validarOrcamentoParaOS(id));
    }

    // --- TESTES DE QUILOMETRAGEM (KM) ---

    @Test
    @DisplayName("Deve lançar exceção se KM de entrada for negativo")
    void deveLancarExcecaoKmNegativo() {
        assertThrows(RegraNegocioException.class, () -> validator.validarKmEntrada(UUID.randomUUID(), -10));
    }

    @Test
    @DisplayName("Deve lançar exceção se KM atual for menor que a última OS")
    void deveLancarExcecaoKmMenorQueAnterior() {
        UUID veiculoId = UUID.randomUUID();
        OrdemServico ultimaOs = new OrdemServico();
        ultimaOs.setNrKmEntrada(50000);

        when(ordemServicoRepository.findTopByIdVeiculoOrderByDtAberturaOsDesc(veiculoId))
                .thenReturn(Optional.of(ultimaOs));

        assertThrows(RegraNegocioException.class, () -> validator.validarKmEntrada(veiculoId, 40000));
    }

    // --- TESTES DE PAGAMENTO ---

    @Test
    @DisplayName("Deve lançar exceção se o status de pagamento for o mesmo")
    void deveLancarExcecaoPagamentoMesmoStatus() {
        OrdemServico os = new OrdemServico();
        os.setStPagamento(StatusPagamento.PAGO);

        assertThrows(RegraNegocioException.class, () -> validator.validarAtualizacaoPagamento(os, StatusPagamento.PAGO));
    }

    @Test
    @DisplayName("Deve lançar exceção se OS estiver cancelada ao tentar alterar pagamento")
    void deveLancarExcecaoPagamentoOsCancelada() {
        OrdemServico os = new OrdemServico();
        os.setStPagamento(StatusPagamento.PENDENTE);
        os.setStatusOS(StatusOS.CANCELADA);

        assertThrows(RegraNegocioException.class, () -> validator.validarAtualizacaoPagamento(os, StatusPagamento.PAGO));
    }

    @Test
    @DisplayName("Deve lançar exceção se OS não estiver finalizada ao tentar alterar pagamento")
    void deveLancarExcecaoPagamentoOsNaoFinalizada() {
        OrdemServico os = new OrdemServico();
        os.setStPagamento(StatusPagamento.PENDENTE);
        os.setStatusOS(StatusOS.EM_EXECUCAO);

        assertThrows(RegraNegocioException.class, () -> validator.validarAtualizacaoPagamento(os, StatusPagamento.PAGO));
    }

    @Test
    @DisplayName("Deve lançar exceção se pagamento já estiver finalizado (PAGO)")
    void deveLancarExcecaoPagamentoJaFinalizado() {
        OrdemServico os = new OrdemServico();
        os.setStPagamento(StatusPagamento.PAGO);
        os.setStatusOS(StatusOS.FINALIZADA);

        assertThrows(RegraNegocioException.class, () -> validator.validarAtualizacaoPagamento(os, StatusPagamento.PENDENTE));
    }

    // --- TESTES DE ALOCAÇÃO DE MECÂNICO E DIAGNÓSTICO ---

    @Test
    @DisplayName("Deve lançar exceção se nenhum mecânico for informado para o diagnóstico")
    void deveLancarExcecaoMecanicoNaoInformado() {
        assertThrows(RegraNegocioException.class, () -> validator.validarAlocacaoMecanico(null, null));
    }

    @Test
    @DisplayName("Deve lançar exceção se o mecânico já estiver ocupado")
    void deveLancarExcecaoMecanicoOcupado() {
        UUID mecanicoId = UUID.randomUUID();
        Funcionario mecanico = mock(Funcionario.class);
        when(mecanico.isOcupado()).thenReturn(true);
        when(mecanico.getNome()).thenReturn("Carlos");

        when(funcionarioRepository.findById(mecanicoId)).thenReturn(Optional.of(mecanico));

        assertThrows(RegraNegocioException.class, () -> validator.validarAlocacaoMecanico(mecanicoId, null));
    }

    @Test
    @DisplayName("Deve validar diagnóstico preenchido com sucesso")
    void deveValidarDiagnosticoComSucesso() {
        assertDoesNotThrow(() -> validator.validarDiagnosticoPreenchido("Problema no motor"));
    }

    @Test
    @DisplayName("Deve lançar exceção se diagnóstico estiver em branco ou nulo")
    void deveLancarExcecaoDiagnosticoVazio() {
        assertThrows(RegraNegocioException.class, () -> validator.validarDiagnosticoPreenchido(null));
        assertThrows(RegraNegocioException.class, () -> validator.validarDiagnosticoPreenchido("   "));
    }
}