import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const falhasTecnicas = new Rate('falhas_tecnicas');

export const options = {
    stages: [
        { duration: '20s', target: 5 },
        { duration: '40s', target: 5 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<2500'],
        falhas_tecnicas: ['rate<0.05'],
    },
};

const BASE_URL = 'http://host.docker.internal:8080';

export default function () {
    const loginPayload = JSON.stringify({
        login: 'admin@autoflow.com',
        senha: '123456'
    });

    const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    falhasTecnicas.add(loginRes.status !== 200);

    const token = loginRes.json('token');

    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
    };

    const veiculos = [
        { id: "d19e98f1-226f-40da-bcdb-4dcf6431b763", placa: "OCP3949" },
        { id: "4f22b500-ffd3-4fd3-93c9-41bcba4d4952", placa: "HOU4327" },
        { id: "584158ea-ef33-4b96-9cad-1910adcacea9", placa: "IXC6752" },
        { id: "a045d24e-0fea-4325-8b6c-a48592a03afe", placa: "KAV1700" },
        { id: "bfd2a539-ff68-4588-a017-d26cf03ed828", placa: "NEQ3162" },
        { id: "9b54fceb-bd16-456a-82dd-345aca46ed62", placa: "MVB3134" },
        { id: "f82ded99-6c6b-4925-909b-a0e9bd81d9fe", placa: "JYC6544" }
    ];

    const indexVeiculo = (__VU + __ITER) % veiculos.length;
    const veiculoSelecionado = veiculos[indexVeiculo];

    const payload = JSON.stringify({
        dsRelatoCliente: "Revisão 150000 km.",
        stTermoAceito: true,
        dtAceiteTermo: "2026-08-30T10:00:00",
        nrKmEntrada: 150000,
        placaVeiculo: veiculoSelecionado.placa,
        idCliente: "145cf2bf-61a6-43b2-9485-db73cb15234d",
        idVeiculo: veiculoSelecionado.id
    });

    const res = http.post(`${BASE_URL}/ordens-servico?agendamento=true`, payload, {
        headers: headers
    });

    console.log(`STATUS: ${res.status} | RESPOSTA: ${res.body}`);

    const eErroTecnico = res.status !== 201 && res.status !== 400;
    falhasTecnicas.add(eErroTecnico);

    check(res, {
        'status é sucesso (201) ou regra de negócio respeitada (400)': (r) => r.status === 201 || r.status === 400,
    });

    sleep(1);
}