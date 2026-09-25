package br.com.autoflow.application.usecase;

import br.com.autoflow.application.validator.ServicoValidator;
import br.com.autoflow.domain.model.Servico;
import br.com.autoflow.ports.outbound.ServicoRepositoryPort;
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
    private ServicoRepositoryPort servicoRepository;

    @Mock
    private ServicoValidator servicoValidator;

    @InjectMocks
    private ServicoUseCaseImpl servicoService;

    @Test
    @DisplayName("Deve criar um serviço com sucesso")
    void deveCriarServicoComSucesso() {
        Servico servicoInput = new Servico(null, "Troca de Óleo", BigDecimal.valueOf(150.00), 30);
        UUID idGerado = UUID.randomUUID();
        Servico servicoSalvo = new Servico(idGerado, "Troca de Óleo", BigDecimal.valueOf(150.00), 30);

        doNothing().when(servicoValidator).validarCriacao(servicoInput);
        when(servicoRepository.save(servicoInput)).thenReturn(servicoSalvo);

        Servico response = servicoService.criar(servicoInput);

        assertNotNull(response);
        assertEquals(idGerado, response.getIdServico());
        assertEquals("Troca de Óleo", response.getDsServico());
        verify(servicoValidator, times(1)).validarCriacao(servicoInput);
        verify(servicoRepository, times(1)).save(servicoInput);
    }

    @Test
    @DisplayName("Deve listar todos os serviços paginados com sucesso")
    void deveListarTodosServicosComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID idServico = UUID.randomUUID();

        Servico servico = new Servico(idServico, "Alinhamento", BigDecimal.valueOf(80.00), 30);
        Page<Servico> paginaEntity = new PageImpl<>(List.of(servico), pageable, 1);

        when(servicoRepository.findAll(pageable)).thenReturn(paginaEntity);

        Page<Servico> resultado = servicoService.listarTodos(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Alinhamento", resultado.getContent().get(0).getDsServico());
        verify(servicoRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar serviço por ID com sucesso")
    void deveBuscarServicoPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Servico servico = new Servico(id, "Balanceamento", BigDecimal.valueOf(60.00), 40);

        when(servicoValidator.buscarPorId(id)).thenReturn(servico);

        Servico response = servicoService.buscarPorId(id);

        assertNotNull(response);
        assertEquals(id, response.getIdServico());
        assertEquals("Balanceamento", response.getDsServico());
        verify(servicoValidator, times(1)).buscarPorId(id);
    }

    @Test
    @DisplayName("Deve atualizar um serviço com sucesso")
    void deveAtualizarServicoComSucesso() {
        UUID id = UUID.randomUUID();
        Servico servicoParam = new Servico(null, "Revisão Completa", BigDecimal.valueOf(500.00), 30);
        Servico servicoExistente = new Servico(id, "Revisão Antiga", BigDecimal.valueOf(400.00), 30);

        doNothing().when(servicoValidator).validarAtualizacao(id, servicoParam);
        when(servicoValidator.buscarPorId(id)).thenReturn(servicoExistente);
        when(servicoRepository.save(servicoExistente)).thenReturn(servicoExistente);

        Servico response = servicoService.atualizar(id, servicoParam);

        assertNotNull(response);
        assertEquals("Revisão Completa", response.getDsServico());
        assertEquals(BigDecimal.valueOf(500.00), response.getVlServico());
        verify(servicoValidator, times(1)).validarAtualizacao(id, servicoParam);
        verify(servicoValidator, times(1)).buscarPorId(id);
        verify(servicoRepository, times(1)).save(servicoExistente);
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