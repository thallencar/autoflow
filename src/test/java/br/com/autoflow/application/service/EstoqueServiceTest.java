package br.com.autoflow.application.service;

import br.com.autoflow.application.dto.EstoqueRequest;
import br.com.autoflow.application.dto.EstoqueResponse;
import br.com.autoflow.domain.enums.TipoItemEstoque;
import br.com.autoflow.domain.model.Estoque;
import br.com.autoflow.domain.repository.EstoqueRepository;
import br.com.autoflow.infrastructure.mapper.EstoqueMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any; // <-- Import correto do Mockito
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EstoqueServiceTest {

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private EstoqueMapper estoqueMapper;

    @InjectMocks
    private EstoqueService estoqueService;

    @Test
    @DisplayName("Deve criar item no estoque com sucesso.")
    void deveCriarItemNoEstoqueComSucesso() {
        // 1. Cenário (Given) - Tudo dentro do método!
        EstoqueRequest request = new EstoqueRequest(
                "Filtro de óleo",
                "Tecfil",
                BigDecimal.valueOf(55),
                35,
                5,
                TipoItemEstoque.INSUMO
        );

        Estoque estoqueEntity = new Estoque();
        Estoque estoqueSalvo = new Estoque();
        UUID idGerado = UUID.randomUUID();

        // Ajuste caso o seu EstoqueResponse também use Enum no tipoCategoria
        EstoqueResponse responseEsperado = new EstoqueResponse(
                idGerado,
                "Filtro de óleo",
                "Tecfil",
                BigDecimal.valueOf(55),
                35,
                5,
                TipoItemEstoque.INSUMO
        );

        // Ensinando o Mockito como o Mapper e o Repository devem se comportar
        when(estoqueMapper.toEntity(request)).thenReturn(estoqueEntity);
        when(estoqueRepository.save(any(Estoque.class))).thenReturn(estoqueSalvo);
        when(estoqueMapper.toResponse(estoqueSalvo)).thenReturn(responseEsperado);

        // 2. Ação (When)
        EstoqueResponse resultado = estoqueService.criar(request);

        // 3. Verificação (Then)
        assertNotNull(resultado);
        assertEquals("Filtro de óleo", resultado.nomeItem());
        assertEquals(TipoItemEstoque.INSUMO, resultado.tipoCategoria());

        // Garante que o método save foi chamado exatamente uma vez
        verify(estoqueRepository, times(1)).save(any(Estoque.class));
    }
}