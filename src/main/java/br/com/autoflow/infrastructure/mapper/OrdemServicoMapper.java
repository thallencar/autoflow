package br.com.autoflow.infrastructure.mapper;

import br.com.autoflow.application.dto.OrdemServicoRequest;
import br.com.autoflow.application.dto.OrdemServicoResponse;
import br.com.autoflow.domain.entity.OrdemServico;
import br.com.autoflow.domain.enums.StatusOS;
import org.springframework.stereotype.Component;

@Component
public class OrdemServicoMapper {

    public OrdemServico toEntity(OrdemServicoRequest request) {
        if (request == null) return null;

        return OrdemServico.builder()
                .dsRelatoCliente(request.dsRelatoCliente())
                .dsDiagnostico(request.dsDiagnostico())
                .stTermoAceito(request.stTermoAceito() != null ? request.stTermoAceito() : false)
                .dtAceiteTermo(request.dtAceiteTermo())
                .nrKmEntrada(request.nrKmEntrada())
                .stOs(request.stOs() != null ? request.stOs() : StatusOS.RECEBIDA)
                .stPagamento(request.stPagamento() != null ? request.stPagamento() : "Pendente")
                .idCliente(request.idCliente())
                .idVeiculo(request.idVeiculo())
                .idFuncionario(request.idFuncionario())
                .idOrcamento(request.idOrcamento())
                .build();
    }

    public OrdemServicoResponse toResponse(OrdemServico os) {
        if (os == null) return null;

        return new OrdemServicoResponse(
                os.getIdOs(),
                os.getStOs(),
                os.getDsRelatoCliente(),
                os.getDsDiagnostico(),
                os.getStTermoAceito(),
                os.getDtAceiteTermo(),
                os.getNrKmEntrada(),
                os.getDtAberturaOs(),
                os.getDtIncioDiagnostico(),
                os.getDtFimDiagnostico(),
                os.getDtAprovacaoOrcamento(),
                os.getDtEncerramentoOs(),
                os.getDtReagendamentoOs(),
                os.getStPagamento(),
                os.getDsMotivoCancelamento(),
                os.getIdCliente(),
                os.getIdVeiculo(),
                os.getIdFuncionario(),
                os.getIdOrcamento()
        );
    }

    public void updateEntityFromRequest(OrdemServico os, OrdemServicoRequest request) {
        if (os == null || request == null) return;

        os.setDsRelatoCliente(request.dsRelatoCliente());
        os.setDsDiagnostico(request.dsDiagnostico());
        if (request.stTermoAceito() != null) os.setStTermoAceito(request.stTermoAceito());
        os.setDtAceiteTermo(request.dtAceiteTermo());
        os.setNrKmEntrada(request.nrKmEntrada());
        if (request.stOs() != null) os.setStOs(request.stOs());
        if (request.stPagamento() != null) os.setStPagamento(request.stPagamento());
        os.setDsMotivoCancelamento(request.dsMotivoCancelamento());
    }
}