package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.ServicoRequest;
import br.com.autoflow.application.dto.ServicoResponse;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.domain.repository.ServicoRepository;
import br.com.autoflow.infrastructure.mapper.ServicoMapper;
import org.junit.jupiter.api.DisplayName;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ServicoMapper servicoMapper;

    @Mock
    private ServicoValidator servicoValidator;

    @InjectMocks
    private ServicoService servicoService;

    @Test
    @DisplayName("Deve criar um serviço com sucesso")
    void deveCriarServicoComSucesso() {
        ServicoRequest request = new ServicoRequest("Troca de Óleo", BigDecimal.valueOf(150.00),30);
        Servico entity = new Servico();
        Servico entitySalva = new Servico();
        ServicoResponse responseEsperado = new ServicoResponse(UUID.randomUUID(), "Troca de Óleo", BigDecimal.valueOf(150.00),30);

        doNothing().when(servicoValidator).validarCriacao(request);
        when(servicoMapper.toEntity(request)).thenReturn(entity);
        when(servicoRepository.save(entity)).thenReturn(entitySalva);
        when(servicoMapper.toResponse(entitySalva)).thenReturn(responseEsperado);

        ServicoResponse response = servicoService.criar(request);

        assertNotNull(response);
        assertEquals(responseEsperado, response);
        verify(servicoValidator, times(1)).validarCriacao(request);
        verify(servicoRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("Deve listar todos os serviços paginados com sucesso")
    void deveListarTodosServicosComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Servico servico = new Servico();
        Page<Servico> paginaEntity = new PageImpl<>(List.of(servico), pageable, 1);
        ServicoResponse responseDto = new ServicoResponse(UUID.randomUUID(), "Alinhamento", BigDecimal.valueOf(80.00),30);

        when(servicoRepository.findAll(pageable)).thenReturn(paginaEntity);
        when(servicoMapper.toResponse(servico)).thenReturn(responseDto);

        Page<ServicoResponse> resultado = servicoService.listarTodos(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(responseDto, resultado.getContent().get(0));
        verify(servicoRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar serviço por ID com sucesso")
    void deveBuscarServicoPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Servico servico = new Servico();
        ServicoResponse responseEsperado = new ServicoResponse(id, "Balanceamento", BigDecimal.valueOf(60.00),40);

        when(servicoValidator.buscarPorId(id)).thenReturn(servico);
        when(servicoMapper.toResponse(servico)).thenReturn(responseEsperado);

        ServicoResponse response = servicoService.buscarPorId(id);

        assertNotNull(response);
        assertEquals(responseEsperado, response);
        verify(servicoValidator, times(1)).buscarPorId(id);
    }

    @Test
    @DisplayName("Deve atualizar um serviço com sucesso")
    void deveAtualizarServicoComSucesso() {
        UUID id = UUID.randomUUID();
        ServicoRequest request = new ServicoRequest("Revisão Completa", BigDecimal.valueOf(500.00),30);
        Servico entity = new Servico();
        ServicoResponse responseEsperado = new ServicoResponse(id, "Revisão Completa", BigDecimal.valueOf(500.00), 30);

        doNothing().when(servicoValidator).validarAtualizacao(id, request);
        when(servicoRepository.getReferenceById(id)).thenReturn(entity);
        doNothing().when(servicoMapper).updateEntityFromDto(request, entity);
        when(servicoRepository.save(entity)).thenReturn(entity);
        when(servicoMapper.toResponse(entity)).thenReturn(responseEsperado);

        ServicoResponse response = servicoService.atualizar(id, request);

        assertNotNull(response);
        assertEquals(responseEsperado, response);
        verify(servicoValidator, times(1)).validarAtualizacao(id, request);
        verify(servicoRepository, times(1)).getReferenceById(id);
        verify(servicoMapper, times(1)).updateEntityFromDto(request, entity);
        verify(servicoRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("Deve deletar um serviço com sucesso")
    void deveDeletarServicoComSucesso() {
        UUID id = UUID.randomUUID();

        doNothing().when(servicoValidator).validarExclusao(id);
        doNothing().when(servicoRepository).deleteById(id);

        assertDoesNotThrow(() -> servicoService.deletar(id));

        verify(servicoValidator, times(1)).validarExclusao(id);
        verify(servicoRepository, times(1)).deleteById(id);
    }
}