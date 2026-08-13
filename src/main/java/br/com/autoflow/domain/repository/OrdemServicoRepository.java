package br.com.autoflow.domain.repository;

import br.com.autoflow.domain.entity.OrdemServico;
import br.com.autoflow.domain.enums.StatusOS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, UUID> {
    List<OrdemServico> findByIdCliente(UUID idCliente);
    List<OrdemServico> findByIdVeiculo(UUID idVeiculo);
    List<OrdemServico> findByStOs(String stOs);
    //  lógica - Conte para mim todas as Ordens de Serviço cujo status NÃO SEJA 'ENTREGUE'
    long countByStOsNot(StatusOS stOs);
}