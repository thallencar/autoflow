package br.com.autoflow.interfaces.controller;

import br.com.autoflow.application.dto.ServicoRequest;
import br.com.autoflow.application.dto.ServicoResponse;
import br.com.autoflow.application.service.ServicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoControllerTest {

    @Mock
    private ServicoService servicoService;

    @InjectMocks
    private ServicoController controller;

    @Test
    void deveCriar() {
        ServicoRequest request = new ServicoRequest("Troca de óleo", BigDecimal.valueOf(150), 90);
        ServicoResponse response = new ServicoResponse(UUID.randomUUID(), "Troca de óleo", BigDecimal.valueOf(150), 90);
        when(servicoService.criar(request)).thenReturn(response);

        ServicoResponse result = controller.criar(request);

        assertEquals(response, result);
        verify(servicoService).criar(request);
    }

    @Test
    void deveListarTodos() {
        Pageable pageable = PageRequest.of(0, 10);
        ServicoResponse response = new ServicoResponse(UUID.randomUUID(), "Troca de óleo", BigDecimal.valueOf(150), 90);
        Page<ServicoResponse> page = new PageImpl<>(List.of(response), pageable, 1);
        when(servicoService.listarTodos(pageable)).thenReturn(page);

        Page<ServicoResponse> result = controller.listarTodos(pageable);

        assertEquals(page, result);
        verify(servicoService).listarTodos(pageable);
    }

    @Test
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        ServicoResponse response = new ServicoResponse(id, "Troca de óleo", BigDecimal.valueOf(150), 90);
        when(servicoService.buscarPorId(id)).thenReturn(response);

        ServicoResponse result = controller.buscarPorId(id);

        assertEquals(response, result);
        verify(servicoService).buscarPorId(id);
    }

    @Test
    void deveAtualizar() {
        UUID id = UUID.randomUUID();
        ServicoRequest request = new ServicoRequest("Revisão completa", BigDecimal.valueOf(220), 120);
        ServicoResponse response = new ServicoResponse(id, "Revisão completa", BigDecimal.valueOf(220), 120);
        when(servicoService.atualizar(id, request)).thenReturn(response);

        ServicoResponse result = controller.atualizar(id, request);

        assertEquals(response, result);
        verify(servicoService).atualizar(id, request);
    }

    @Test
    void deveDeletar() {
        UUID id = UUID.randomUUID();

        controller.deletar(id);

        verify(servicoService).deletar(id);
    }
}
