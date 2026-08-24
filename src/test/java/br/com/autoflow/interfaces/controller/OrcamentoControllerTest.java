package br.com.autoflow.interfaces.controller;

import br.com.autoflow.application.dto.AtualizarStatusOrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoRequest;
import br.com.autoflow.application.dto.OrcamentoResponse;
import br.com.autoflow.application.dto.OrcamentoServicoRequest;
import br.com.autoflow.application.service.OrcamentoService;
import br.com.autoflow.domain.enums.StatusOrcamento;
import br.com.autoflow.domain.enums.TipoOrcamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoControllerTest {

    @Mock
    private OrcamentoService orcamentoService;

    @InjectMocks
    private OrcamentoController controller;

    @Test
    void deveCriar() {
        OrcamentoRequest request = new OrcamentoRequest(UUID.randomUUID(), TipoOrcamento.INICIAL, LocalDateTime.now(), List.of(), List.of());
        OrcamentoResponse response = new OrcamentoResponse(UUID.randomUUID(), request.idOs(), "INICIAL", "PENDENTE", LocalDateTime.now(), request.dataExpiracao(), null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of());
        when(orcamentoService.criar(request)).thenReturn(response);

        OrcamentoResponse result = controller.criar(request);

        assertEquals(response, result);
        verify(orcamentoService).criar(request);
    }

    @Test
    void deveListarTodos() {
        OrcamentoResponse response = new OrcamentoResponse(UUID.randomUUID(), UUID.randomUUID(), "PREVENTIVA", "PENDENTE", LocalDateTime.now(), LocalDateTime.now(), null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of());
        when(orcamentoService.listarTodos()).thenReturn(List.of(response));

        List<OrcamentoResponse> result = controller.listarTodos();

        assertEquals(List.of(response), result);
        verify(orcamentoService).listarTodos();
    }

    @Test
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        OrcamentoResponse response = new OrcamentoResponse(id, UUID.randomUUID(), "PREVENTIVA", "PENDENTE", LocalDateTime.now(), LocalDateTime.now(), null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of());
        when(orcamentoService.buscarPorId(id)).thenReturn(response);

        OrcamentoResponse result = controller.buscarPorId(id);

        assertEquals(response, result);
        verify(orcamentoService).buscarPorId(id);
    }

    @Test
    void deveDeletar() {
        UUID id = UUID.randomUUID();

        controller.delete(id);

        verify(orcamentoService).delete(id);
    }

    @Test
    void deveAtualizarStatus() {
        UUID id = UUID.randomUUID();
        AtualizarStatusOrcamentoRequest request = new AtualizarStatusOrcamentoRequest(StatusOrcamento.APROVADO);
        OrcamentoResponse response = new OrcamentoResponse(id, UUID.randomUUID(), "PREVENTIVA", "APROVADO", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of());
        when(orcamentoService.atualizarStatus(id, request)).thenReturn(response);

        OrcamentoResponse result = controller.atualizarStatus(id, request);

        assertEquals(response, result);
        verify(orcamentoService).atualizarStatus(id, request);
    }

    @Test
    void deveListarPorOrdemServico() {
        UUID idOs = UUID.randomUUID();
        OrcamentoResponse response = new OrcamentoResponse(UUID.randomUUID(), idOs, "PREVENTIVA", "PENDENTE", LocalDateTime.now(), LocalDateTime.now(), null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of());
        when(orcamentoService.listarPorOrdemServico(idOs)).thenReturn(List.of(response));

        List<OrcamentoResponse> result = controller.listarPorOrcamentoOrdemDeServico(idOs);

        assertEquals(List.of(response), result);
        verify(orcamentoService).listarPorOrdemServico(idOs);
    }
}
