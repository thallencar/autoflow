package br.com.autoflow.ports.inbound.servico;

import br.com.autoflow.domain.model.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListarServicosUseCase {
    Page<Servico> listarTodos(Pageable pageable);
}
