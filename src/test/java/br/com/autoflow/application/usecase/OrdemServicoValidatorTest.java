package br.com.autoflow.application.usecase;

import br.com.autoflow.adapters.inbound.controller.dto.OrdemServicoRequest;
import br.com.autoflow.application.validator.OrdemServicoValidator;
import br.com.autoflow.domain.enums.StatusOS;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.StatusPagamento;
import br.com.autoflow.domain.enums.TipoOrcamento;
import br.com.autoflow.domain.exception.EntidadeNaoEncontradaException;
import br.com.autoflow.domain.exception.RegraNegocioException;
import br.com.autoflow.domain.model.Funcionario;
import br.com.autoflow.domain.model.Orcamento;
import br.com.autoflow.domain.model.OrdemServico;
import br.com.autoflow.ports.outbound.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoValidatorTest {

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @Mock
    private VeiculoRepositoryPort veiculoRepository;

    @Mock
    private OrcamentoRepositoryPort orcamentoRepository;

    @Mock
    private OrdemServicoRepositoryPort ordemServicoRepository;

    @Mock
    private FuncionarioRepositoryPort funcionarioRepository;

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
    @DisplayName("Deve retornar lista vazia se ids de orçamento forem nulos")
    void deveRetornarListaVaziaSeOrcamentosNulos() {
        List<UUID> orcamentosNulos = null;
        assertTrue(validator.validarECarregarOrcamentosParaOS(orcamentosNulos).isEmpty());
    }

    @Test
    @DisplayName("Deve retornar lista vazia se ids de orçamento forem vazios")
    void deveRetornarListaVaziaSeOrcamentosVazios() {
        List<UUID> orcamentosVazios = Collections.emptyList();
        assertTrue(validator.validarECarregarOrcamentosParaOS(orcamentosVazios).isEmpty());
    }

    @Test
    @DisplayName("Deve lançar exceção se orçamento não estiver aprovado")
    void deveLancarExcecaoOrcamentoNaoAprovado() {
        UUID id = UUID.randomUUID();

        Orcamento orcamento = new Orcamento(
                id,                               // id
                TipoOrcamento.INICIAL,            // tipoOrcamento
                StatusOrcamento.PENDENTE,         // status (Pendente para acionar a regra)
                LocalDateTime.now(),              // dataCriacao
                LocalDateTime.now().plusDays(7),  // dataExpiracao
                null,                             // dataDecisao
                BigDecimal.ZERO,                  // subtotalPecas
                BigDecimal.ZERO,                  // maoObra
                new BigDecimal("150.00"),         // total
                null,                             // ordemServico
                new ArrayList<>(),                // servicos
                new ArrayList<>()                 // itens
        );

        when(orcamentoRepository.findById(id)).thenReturn(Optional.of(orcamento));

        assertThrows(RegraNegocioException.class, () -> validator.validarOrcamentoParaOS(id));
    }

    @Test
    @DisplayName("Deve lançar exceção se orçamento estiver expirado")
    void deveLancarExcecaoOrcamentoExpirado() {
        UUID id = UUID.randomUUID();

        Orcamento orcamento = new Orcamento(
                id,                               // id
                TipoOrcamento.INICIAL,            // tipoOrcamento
                StatusOrcamento.APROVADO,         // status aprovado
                LocalDateTime.now().minusDays(5), // dataCriacao
                LocalDateTime.now().minusDays(1), // dataExpiracao no passado (Expirado)
                null,                             // dataDecisao
                BigDecimal.ZERO,                  // subtotalPecas
                BigDecimal.ZERO,                  // maoObra
                new BigDecimal("150.00"),         // total
                null,                             // ordemServico
                new ArrayList<>(),                // servicos
                new ArrayList<>()                 // itens
        );

        when(orcamentoRepository.findById(id)).thenReturn(Optional.of(orcamento));

        assertThrows(RegraNegocioException.class, () -> validator.validarOrcamentoParaOS(id));
    }
    // --- TESTES DE QUILOMETRAGEM (KM) ---

    @Test
    @DisplayName("Deve lançar exceção se KM de entrada for negativo")
    void deveLancarExcecaoKmNegativo() {
        UUID veiculoId = UUID.randomUUID();
        int kmNegativo = -10;
        assertThrows(RegraNegocioException.class, () -> validator.validarKmEntrada(veiculoId, kmNegativo));
    }

    @Test
    @DisplayName("Deve lançar exceção se KM atual for menor que a última OS")
    void deveLancarExcecaoKmMenorQueAnterior() {
        UUID veiculoId = UUID.randomUUID();
        OrdemServico ultimaOs = mock(OrdemServico.class);
        when(ultimaOs.getNrKmEntrada()).thenReturn(50000);

        when(ordemServicoRepository.findTopByIdVeiculoOrderByDtAberturaOsDesc(veiculoId))
                .thenReturn(Optional.of(ultimaOs));

        int kmAtual = 40000;
        assertThrows(RegraNegocioException.class, () -> validator.validarKmEntrada(veiculoId, kmAtual));
    }

    // --- TESTES DE PAGAMENTO ---

    @Test
    @DisplayName("Deve lançar exceção se o status de pagamento for o mesmo")
    void deveLancarExcecaoPagamentoMesmoStatus() {
        OrdemServico os = mock(OrdemServico.class);
        when(os.getStPagamento()).thenReturn(StatusPagamento.PAGO);

        assertThrows(RegraNegocioException.class, () -> validator.validarAtualizacaoPagamento(os, StatusPagamento.PAGO));
    }

    @Test
    @DisplayName("Deve lançar exceção se OS estiver cancelada ao tentar alterar pagamento")
    void deveLancarExcecaoPagamentoOsCancelada() {
        OrdemServico os = mock(OrdemServico.class);
        when(os.getStPagamento()).thenReturn(StatusPagamento.PENDENTE);
        when(os.getStatusOS()).thenReturn(StatusOS.CANCELADA);

        assertThrows(RegraNegocioException.class, () -> validator.validarAtualizacaoPagamento(os, StatusPagamento.PAGO));
    }

    @Test
    @DisplayName("Deve lançar exceção se OS não estiver finalizada ao tentar alterar pagamento")
    void deveLancarExcecaoPagamentoOsNaoFinalizada() {
        OrdemServico os = mock(OrdemServico.class);
        when(os.getStPagamento()).thenReturn(StatusPagamento.PENDENTE);
        when(os.getStatusOS()).thenReturn(StatusOS.EM_EXECUCAO);

        assertThrows(RegraNegocioException.class, () -> validator.validarAtualizacaoPagamento(os, StatusPagamento.PAGO));
    }

    @Test
    @DisplayName("Deve lançar exceção se pagamento já estiver finalizado (PAGO)")
    void deveLancarExcecaoPagamentoJaFinalizado() {
        OrdemServico os = mock(OrdemServico.class);
        when(os.getStPagamento()).thenReturn(StatusPagamento.PAGO);
        when(os.getStatusOS()).thenReturn(StatusOS.FINALIZADA);

        assertThrows(RegraNegocioException.class, () -> validator.validarAtualizacaoPagamento(os, StatusPagamento.PENDENTE));
    }

    // --- TESTES DE ALOCAÇÃO DE MECÂNICO E DIAGNÓSTICO ---

    @Test
    @DisplayName("Deve permitir passar nulo quando nenhum mecânico for informado")
    void devePermitirMecanicoNulo() {
        assertDoesNotThrow(() -> validator.validarAlteracaoMecanico(null, null));
    }

    @Test
    @DisplayName("Deve lançar exceção se o mecânico já estiver ocupado")
    void deveLancarExcecaoMecanicoOcupado() {
        UUID mecanicoId = UUID.randomUUID();
        Funcionario mecanico = mock(Funcionario.class);
        when(mecanico.isOcupado()).thenReturn(true);
        when(mecanico.getNome()).thenReturn("Carlos");

        when(funcionarioRepository.findById(mecanicoId)).thenReturn(Optional.of(mecanico));

        assertThrows(RegraNegocioException.class, () -> validator.validarAlteracaoMecanico(mecanicoId, null));
    }

    @Test
    @DisplayName("Deve validar diagnóstico preenchido com sucesso")
    void deveValidarDiagnosticoComSucesso() {
        assertDoesNotThrow(() -> validator.validarDiagnosticoPreenchido("Problema no motor"));
    }

    @Test
    @DisplayName("Deve lançar exceção se diagnóstico for nulo")
    void deveLancarExcecaoDiagnosticoNulo() {
        assertThrows(RegraNegocioException.class, () -> validator.validarDiagnosticoPreenchido(null));
    }

    @Test
    @DisplayName("Deve lançar exceção se diagnóstico estiver em branco")
    void deveLancarExcecaoDiagnosticoEmBranco() {
        String diagnosticoEmBranco = "   ";
        assertThrows(RegraNegocioException.class, () -> validator.validarDiagnosticoPreenchido(diagnosticoEmBranco));
    }
}