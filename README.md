# 🎧 Sistema de Gestão de Chamados – Equipe 3

Projeto acadêmico de um **help desk** para uma software house que mantém aplicações web, APIs e bancos de dados de vários clientes.
O sistema organiza o atendimento em três níveis de suporte (**N1 → N2 → N3**), calcula a prioridade e o prazo (SLA) de cada chamado e guarda todo o histórico do atendimento.

## 🛠️ Tecnologias

- Java 17+ · Spring Boot 3 (Web, Data JPA, Security, Validation)
- Thymeleaf + Bootstrap 5
- MySQL 8

## ✨ Funcionalidades

- **Perfis:** Solicitante, Técnico (N1, N2, N3) e Administrador/Supervisor
- **Abertura de chamados** com tipo, categoria, impacto e urgência → **prioridade e SLA calculados automaticamente**
- **Encaminhamento automático** para o nível responsável (N1, N2 ou N3)
- **Escalonamento** entre níveis com justificativa, **retransferência** e **transferência para um técnico específico**
- **Histórico completo** de tudo que acontece no chamado (quem, quando, de qual nível para qual)
- **Base de conhecimento** e **atendimento via suporte remoto** (com autorização do usuário)
- **Validação da solução** pelo solicitante e atualização manual de status
- **Alertas de prazo** (próximo de vencer e SLA vencido para toda a equipe)
- **Horário de atendimento** (07:00 às 20:00) e **logout automático** por inatividade
- **Dashboard** com indicadores por status, prioridade, categoria e nível

## ▶️ Como executar

1. Tenha o **Java 17+** e o **MySQL 8** instalados e com o MySQL ligado.
2. Em `src/main/resources/application.properties`, ajuste o usuário e a senha do MySQL:
   ```properties
   spring.datasource.username=${DB_USER:root}
   spring.datasource.password=${DB_PASSWORD:sua_senha}
   ```
3. Rode o projeto pela IDE (classe `ChamadosApplication`) ou pelo terminal:
   ```bash
   mvn spring-boot:run
   ```
4. Acesse **http://localhost:8080**

> O banco `chamados_db`, as tabelas e os dados de teste são criados automaticamente ao iniciar.
> Por padrão o banco é **recriado a cada inicialização** (ideal para demonstração).

## 👤 Usuários de teste

Senha de todos: **`123456`**

| Perfil | E-mail |
|---|---|
| Supervisor | `supervisor@softhouse.com` |
| Técnico N1 | `ana.n1@softhouse.com` · `bruno.n1@softhouse.com` |
| Técnico N2 | `carla.n2@softhouse.com` · `gustavo.n2@softhouse.com` |
| Técnico N3 | `daniel.n3@softhouse.com` · `eduardo.n3@softhouse.com` |
| Solicitante | `joao@alfavarejo.com.br` · `pedro@betalogistica.com.br` · `patricia@gamasaude.com.br` |

## 📁 Estrutura

```
src/main/java/br/com/equipe3/chamados
├── model/        entidades e enums
├── repository/   acesso ao banco (Spring Data JPA)
├── service/      regras de negócio
├── web/          controllers
└── config/       segurança (Spring Security)
src/main/resources
├── templates/    telas (Thymeleaf)
├── schema.sql    criação das tabelas
└── data.sql      dados iniciais e os 10 chamados de teste
docs/             documentação técnica e fluxograma
```

## 📄 Documentação

A documentação completa da atividade está em [`docs/DOCUMENTACAO.md`](docs/DOCUMENTACAO.md). Ela traz a justificativa da escolha por um sistema próprio em vez do GLPI, a matriz de prioridade, as regras de escalonamento, o fluxograma de atendimento, a tabela dos 10 chamados de teste e as respostas das perguntas do roteiro.

---

Projeto desenvolvido pela **Equipe 3** para fins acadêmicos.
