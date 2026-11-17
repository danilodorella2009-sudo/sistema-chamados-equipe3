package br.com.equipe3.chamados.service;

import br.com.equipe3.chamados.model.Impacto;
import br.com.equipe3.chamados.model.TipoChamado;
import br.com.equipe3.chamados.model.Urgencia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cálculo do nível responsável (N1, N2 ou N3) na abertura do chamado.
 *
 *  Pontos = peso do Impacto + peso da Urgência   (Baixo/Baixa=1, Médio/Média=2, Alto/Alta=3, Crítico/Crítica=4)
 *
 *  1) Solicitação de Melhoria        -> N3 (desenvolvimento), sempre
 *  2) Dúvida / Solicitação de Serviço -> N1, sempre (resolvidos por procedimento / base de conhecimento)
 *  3) Incidente / Bug, pelos pontos:
 *       pontos >= app.encaminhamento.pontos-n3  -> N3
 *       pontos >= app.encaminhamento.pontos-n2  -> N2
 *       senão                                   -> N1
 *     Padrão: 2 a 6 pontos -> N1 | 7 a 8 pontos (prioridade Crítica) -> N2 | N3 desativado (9 = nunca)
 */
@Service
public class EncaminhamentoService {

    private final int pontosN2;
    private final int pontosN3;

    public EncaminhamentoService(@Value("${app.encaminhamento.pontos-n2:7}") int pontosN2,
                                 @Value("${app.encaminhamento.pontos-n3:9}") int pontosN3) {
        this.pontosN2 = pontosN2;
        this.pontosN3 = pontosN3;
    }

    public int pontos(Impacto impacto, Urgencia urgencia) {
        return impacto.getPeso() + urgencia.getPeso();
    }

    /** Sigla do grupo responsável: "N1", "N2" ou "N3". */
    public String calcularNivel(TipoChamado tipo, Impacto impacto, Urgencia urgencia) {
        if (tipo == TipoChamado.SOLICITACAO_DE_MELHORIA) return "N3";
        if (tipo == TipoChamado.DUVIDA || tipo == TipoChamado.SOLICITACAO_DE_SERVICO) return "N1";
        int p = pontos(impacto, urgencia);
        if (p >= pontosN3) return "N3";
        if (p >= pontosN2) return "N2";
        return "N1";
    }

    /** Explicação do cálculo, gravada no histórico e mostrada na tela. */
    public String explicar(TipoChamado tipo, Impacto impacto, Urgencia urgencia) {
        String nivel = calcularNivel(tipo, impacto, urgencia);
        if (tipo == TipoChamado.SOLICITACAO_DE_MELHORIA) return "melhoria vai direto ao desenvolvimento (N3)";
        if (tipo == TipoChamado.DUVIDA || tipo == TipoChamado.SOLICITACAO_DE_SERVICO) {
            return tipo.getDescricao().toLowerCase() + " é atendida pelo N1";
        }
        return pontos(impacto, urgencia) + " pontos (" + impacto.getPeso() + " impacto + " + urgencia.getPeso()
                + " urgência) => " + nivel;
    }

    public int getPontosN2() { return pontosN2; }
    public int getPontosN3() { return pontosN3; }
}
