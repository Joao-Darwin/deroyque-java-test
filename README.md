# Activation Service

API REST desenvolvida com Java e Spring Boot para gerenciamento de solicitações de ativação. A aplicação permite registrar solicitações, consultar seus respectivos status e realizar a integração com um provedor externo por meio de processamento assíncrono.

## Tecnologias utilizadas

- Java 17
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Maven
- Docker e Docker Compose
- Testcontainers
- Resilience4j

## Como executar

### Pré-requisitos

- Java 17 ou superior.
- Docker e Docker Compose.
- Git.

### 1. Clonar o repositório

```bash
git clone https://github.com/Joao-Darwin/deroyque-java-test.git activation_service
cd activation_service
```

### 2. Configurar as variáveis de ambiente

Configure as variáveis necessárias para a conexão com o PostgreSQL e para a integração com o provedor externo. Pode utilizar de exemplo o arquivo `.env`

Exemplo:

```bash
PORT=8080

WORKER_DELAY=5000

DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
DATABASE_NAME=activation-service
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=activation-service-db

PROVIDER_BASE_URL=http://provider
```

As configurações específicas do provedor externo também devem ser definidas conforme o ambiente.

### 3. Executar com Docker Compose

Com o Docker em execução, inicialize os serviços:

```bash
docker compose up --build -d
```

Para acompanhar os logs:

```bash
docker compose logs -f
```

Para encerrar os serviços:

```bash
docker compose down
```

### 4. Executar localmente

Caso prefira executar a aplicação fora do Docker, primeiro disponibilize uma instância PostgreSQL e configure as variáveis de ambiente.

Em seguida, execute:

```bash
./mvnw spring-boot:run
```

A aplicação utiliza a porta `8080` e o contexto `/api`.

### 5. Executar os testes

Para executar os testes unitários:

```bash
./mvnw test
```

Para executar o ciclo completo de verificação:

```bash
./mvnw verify
```

Os testes de integração utilizam Testcontainers para disponibilizar uma instância isolada do PostgreSQL. Portanto, é necessário ter o Docker em execução.

## Endpoints

A API disponibiliza os seguintes endpoints:

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v1/requests` | Cria uma solicitação de ativação. |
| GET | `/api/v1/requests/{id}` | Consulta uma solicitação pelo identificador. |
| GET | `/api/v1/requests` | Consulta solicitações com paginação e filtros. |

### Consulta paginada

O endpoint de consulta permite filtrar solicitações por status e identificador do cliente.

Exemplo:

```http
GET /api/v1/requests?page=0&size=10&status=PENDING&customerId=123
```

## Decisões técnicas

### Arquitetura e organização

A aplicação foi organizada com separação de responsabilidades entre a camada HTTP, os casos de uso, as regras de negócio e a persistência.

Os controllers recebem as requisições e delegam a execução aos casos de uso. A lógica de negócio permanece independente dos detalhes de transporte HTTP, enquanto os repositórios concentram as operações de persistência.

Essa organização facilita a manutenção, os testes e a evolução da aplicação.

### Processamento assíncrono

A criação de uma solicitação não depende da conclusão da integração com o provedor externo.

Ao receber uma solicitação válida, a aplicação registra a operação com o status `PENDING` e disponibiliza um identificador para que o cliente acompanhe seu processamento.

Um worker é responsável por buscar solicitações elegíveis e realizar a comunicação com o provedor.

Os estados possíveis são:

| Status | Descrição |
|---|---|
| `PENDING` | Solicitação aguardando processamento ou uma nova tentativa. |
| `PROCESSING` | Solicitação em processamento. |
| `COMPLETED` | Integração concluída com sucesso. |
| `FAILED` | Processamento encerrado sem sucesso. |

### Prevenção de duplicidade e idempotência

O campo `externalId` identifica unicamente uma solicitação externa.

A prevenção de duplicidade é garantida principalmente por uma restrição de unicidade no banco de dados. Uma consulta prévia antes da inserção, isoladamente, não seria suficiente para impedir duplicidades em cenários de concorrência.

O comportamento adotado é:

- Uma nova solicitação é persistida normalmente.
- Uma solicitação repetida com o mesmo `externalId` e o mesmo conteúdo é tratada como idempotente.
- Uma solicitação com o mesmo `externalId`, mas conteúdo diferente, resulta em conflito HTTP `409 Conflict`.

A restrição no banco de dados funciona como última garantia de integridade, inclusive quando duas requisições concorrentes tentam registrar a mesma solicitação.

### Integração com o provedor externo

A integração é desacoplada da requisição HTTP inicial.

O worker realiza o processamento das solicitações pendentes e atualiza seus estados de acordo com o resultado da comunicação:

- Sucesso: status `COMPLETED`.
- Falha permanente: status `FAILED`.
- Falha temporária: retorna para `PENDING`, com agendamento de uma nova tentativa.

O processamento possui limite de três tentativas, evitando execuções indefinidas.

As falhas são classificadas entre temporárias e permanentes. Somente erros considerados recuperáveis são submetidos a novas tentativas.

A estratégia de retry utiliza Resilience4j para controlar as tentativas da integração, conforme a configuração da aplicação.

### Concorrência no processamento

Para evitar que múltiplos workers processem simultaneamente a mesma solicitação, a aquisição de uma solicitação utiliza uma atualização condicional no banco de dados.

A operação considera o status atual, a quantidade de tentativas e o horário da próxima execução.

Dessa maneira, somente um worker consegue assumir uma solicitação elegível.

A utilização de controle otimista de concorrência também auxilia na identificação de atualizações conflitantes.

### Persistência

O PostgreSQL foi escolhido como banco de dados relacional.

O Flyway gerencia a evolução do esquema por meio de migrações versionadas. O Hibernate utiliza a configuração `ddl-auto: validate`, evitando que a aplicação altere automaticamente a estrutura do banco.

### Tratamento de erros

A aplicação centraliza o tratamento de exceções por meio de um mecanismo global de tratamento HTTP.

As respostas seguem o formato `ProblemDetail`, permitindo padronizar informações de erro e códigos HTTP.

Entre os principais cenários tratados estão:

- Erros de validação: HTTP `400 Bad Request`.
- Solicitação não encontrada: HTTP `404 Not Found`.
- Conflito de idempotência: HTTP `409 Conflict`.
- Erros inesperados: HTTP `500 Internal Server Error`.

### Segurança

A autenticação e a autorização não fazem parte da implementação atual.

Como evolução, seria adotado OAuth2 com JWT para proteger os endpoints da API.

Nesse cenário, um servidor de autorização seria responsável pela emissão dos tokens, enquanto a aplicação atuaria como Resource Server, validando os JWTs recebidos.

Também seriam definidos escopos ou permissões para controlar o acesso aos recursos.

Credenciais e informações sensíveis seriam armazenadas em um serviço de gerenciamento de segredos, evitando sua exposição no código-fonte ou nos logs.

## Cenário de análise

### 1. O que investigaria primeiro?

Começaria analisando a evolução da latência da API e correlacionando os horários de lentidão com as consultas ao banco e as chamadas ao provedor externo.

Como ambos apresentaram aumento significativo de latência, investigaria primeiro esses dois componentes, verificando também se houve alguma alteração recente na aplicação ou na infraestrutura.

### 2. Como identificaria se o problema está na aplicação, no banco ou na integração externa?

Utilizaria métricas e tracing distribuído para decompor o tempo total de uma requisição.

- **Aplicação:** analisaria o tempo de execução interno, consumo de CPU e memória, threads e possíveis bloqueios.
- **Banco de dados:** verificaria consultas lentas, planos de execução, índices, locks e utilização do pool de conexões.
- **Provedor externo:** analisaria latência, timeouts, taxa de erros e disponibilidade do serviço.

### 3. Quais logs ou métricas seriam úteis?

- Latência da API, considerando percentis P50, P95 e P99.
- Latência e taxa de erro das chamadas ao provedor.
- Tempo de execução das consultas ao banco.
- Utilização e saturação do pool de conexões.
- Quantidade de solicitações por status.
- Quantidade de retries e solicitações pendentes.
- Logs estruturados com identificador de correlação (*correlation ID*).

### 4. Que tipo de melhoria poderia ser considerada?

Dependendo da causa identificada, consideraria otimizar consultas e índices do banco, revisar o pool de conexões, configurar timeouts adequados e melhorar a resiliência da integração externa com circuit breaker e políticas de retry com backoff exponencial.

Também avaliaria a possibilidade de processamento assíncrono para operações que não precisam bloquear a resposta HTTP.

### 5. Os três sintomas podem ter uma causa comum?

Sim. Uma hipótese seria a lentidão do provedor externo provocar o acúmulo de requisições em processamento, mantendo threads e conexões ocupadas por mais tempo.

Esse cenário poderia aumentar a concorrência, pressionar o pool de conexões do banco e elevar a latência geral da aplicação.

Para confirmar, analisaria traces distribuídos, tempo de espera por conexões, quantidade de requisições simultâneas e duração das chamadas externas, correlacionando esses indicadores no mesmo intervalo.

### 6. Como validaria que a correção funcionou depois de publicada?

Após a publicação, acompanharia os mesmos indicadores utilizados no diagnóstico, comparando os resultados com a linha de base anterior ao problema.

Também executaria testes de carga e observaria os percentis de latência, taxa de erros, consumo de recursos e comportamento do processamento assíncrono.

A correção seria considerada satisfatória quando os indicadores apresentassem melhora consistente, sem introduzir regressões ou comprometer a integridade das solicitações.

## Produção

### Docker

A aplicação é empacotada em uma imagem Docker, permitindo padronizar sua execução entre os ambientes.

Para disponibilizar uma nova versão, o processo envolveria:

1. Compilar a aplicação.
2. Executar os testes automatizados.
3. Construir a imagem Docker.
4. Publicar a imagem em um container registry.
5. Implantar a versão no ambiente de produção.

A execução seria realizada por meio de containers, com configurações externalizadas por variáveis de ambiente.

Em produção, as credenciais seriam fornecidas por um gerenciador de segredos, evitando armazená-las diretamente no código ou na imagem.

### Observabilidade

Para acompanhar a aplicação em produção, adotaria uma estratégia de observabilidade baseada em três pilares:

- **Logs:** logs estruturados em JSON, com identificador de correlação propagado desde a requisição HTTP até a chamada ao provedor.
- **Métricas:** Micrometer com Prometheus para acompanhar latência, taxa de erros, consumo de recursos e quantidade de solicitações por status.
- **Tracing:** OpenTelemetry para rastrear requisições e identificar gargalos entre os componentes.

Também utilizaria Spring Boot Actuator para disponibilizar endpoints de saúde, incluindo liveness e readiness.

Nenhum dado sensível, como tokens ou credenciais, seria registrado nos logs.

### Microsserviços e cloud

Em um ambiente de microsserviços, múltiplas instâncias da aplicação poderiam executar simultaneamente atrás de um balanceador de carga.

A prevenção de duplicidade permaneceria garantida pela restrição de unicidade no banco de dados. Para o processamento, a aquisição de solicitações seria realizada por uma atualização condicional e atômica, evitando que diferentes workers assumissem o mesmo registro.

As configurações seriam externalizadas por variáveis de ambiente, enquanto credenciais e outros dados sensíveis seriam armazenados em serviços de gerenciamento de segredos da plataforma cloud.

Quando uma ativação fosse concluída, a aplicação poderia publicar um evento, como `ActivationCompleted`, em um broker de mensageria. Outros microsserviços poderiam consumir esse evento sem criar dependência direta entre eles.

O pipeline de CI/CD contemplaria:

1. Compilação da aplicação.
2. Execução de testes unitários e de integração.
3. Construção da imagem Docker.
4. Publicação da imagem em um container registry.
5. Deploy automatizado no ambiente de execução.
6. Verificação de health checks e monitoramento após a publicação.

## Pontos pendentes

Os seguintes pontos não fazem parte da implementação atual ou precisam de evolução:

- **Autenticação e autorização:** implementar OAuth2/JWT para proteger os endpoints.
- **Recuperação de solicitações travadas:** implementar um mecanismo de recuperação de registros que permaneçam em `PROCESSING` após uma interrupção inesperada.
- **Observabilidade completa:** adicionar métricas de negócio, logs estruturados e tracing distribuído.
- **Health checks:** disponibilizar e configurar os endpoints de liveness e readiness com Spring Boot Actuator, caso ainda não estejam configurados.
- **Testes da integração externa:** ampliar os cenários de testes utilizando um servidor HTTP simulado, como WireMock.
- **Tratamento de falhas na persistência:** aprimorar o tratamento de situações em que o provedor conclui uma operação, mas a aplicação falha ao persistir o resultado.
- **Desligamento gracioso:** garantir que o encerramento da aplicação não interrompa operações em andamento de maneira inconsistente.

## Melhorias futuras

Caso a aplicação evolua para um ambiente de produção, algumas melhorias seriam consideradas:

1. **Mensageria:** substituir ou complementar o polling do worker com uma solução de mensageria, como RabbitMQ ou Kafka, permitindo maior desacoplamento entre a criação e o processamento das solicitações.

2. **Escalabilidade dos workers:** permitir que diferentes instâncias processem solicitações em paralelo, mantendo a garantia de que uma solicitação não seja processada simultaneamente por múltiplos workers.

3. **Resiliência da integração:** implementar circuit breaker, timeouts configuráveis e políticas de retry com backoff exponencial.

4. **Idempotência no provedor:** utilizar uma chave de idempotência na comunicação externa, caso o provedor ofereça suporte, reduzindo o risco de operações duplicadas em situações de timeout ou falhas de comunicação.

5. **Observabilidade:** disponibilizar dashboards e alertas para acompanhar volume de solicitações, tempo de processamento, taxa de falhas e quantidade de retentativas.

6. **Segurança:** implementar autenticação, autorização e políticas de acesso específicas para cada operação.

7. **Automação de infraestrutura:** utilizar infraestrutura como código para padronizar a criação dos ambientes.

8. **Publicação de eventos:** implementar o padrão Transactional Outbox para garantir maior confiabilidade na publicação de eventos após a conclusão de uma ativação.

9. **Recuperação automática:** implementar mecanismos de lease ou timeout para recuperar solicitações abandonadas em estado `PROCESSING`.

## Uso de IA Diferencial

Ferramentas de inteligência artificial foram utilizadas como apoio durante o desenvolvimento do projeto, principalmente nas seguintes atividades:

    Apoio arquitetural: discussão e avaliação de alternativas para organização das camadas e separação de responsabilidades.

    Revisão de decisões técnicas: análise de estratégias de idempotência, prevenção de duplicidade, concorrência e processamento assíncrono.

    Apoio na implementação: esclarecimento de dúvidas sobre Spring Boot, JPA, transações, tratamento de exceções e integração HTTP.

    Estratégia de testes: apoio na elaboração de cenários de testes unitários e de integração, incluindo o uso de Testcontainers.

    Documentação: auxílio na organização e descrição das decisões técnicas e dos pontos de evolução.

As sugestões geradas por IA foram utilizadas como suporte ao processo de desenvolvimento, com análise e validação das soluções antes de sua adoção.