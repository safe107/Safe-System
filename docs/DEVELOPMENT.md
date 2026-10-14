# Desenvolvimento

Use o checkout existente; o ambiente de tarefa já é isolado. Não crie worktrees durante onboarding salvo pedido explícito.

## Convenções

Java 21, packages por domínio, services transacionais, entrada/saída por records, validações em DTO/serviço e constraints PostgreSQL. Controllers sem lógica de negócio e entidades sem representação HTTP. Migrações já aplicadas não devem ser editadas: adicione `V5__…sql` em diante.

TypeScript estrito, componentes funcionais, TanStack Query para dados de servidor, RHF/Zod para formulários, i18next para área pública. Não injete HTML do conteúdo: React renderiza texto. Componentes Radix gerenciam foco no modal destrutivo; labels e foco visível são obrigatórios.

Formatação: `./mvnw fmt:format` na API e `npx prettier --write src e2e scripts` no web. `./mvnw fmt:check` verifica Java. Linters/testes não substituem um teste funcional com API e banco.

## Conteúdo

1. Entre como administrador provisionado explicitamente.
2. Cadastre competências confirmadas e publique as que quiser tornar públicas.
3. Cadastre projetos/experiências; a primeira tradução PT-BR é obrigatória. EN é opcional e possui fallback explícito.
4. Anexe um PDF real ao certificado/currículo. Conteúdo fica em rascunho até publicação.
5. Edite os três perfis de apresentação, seus destaques, vínculos e currículo.
6. Complete contato e disponibilidade somente com informações reais.
7. Atualize cursos/candidaturas na área privada; alterações de etapa geram eventos de histórico.
8. Habilite analytics conscientemente nas configurações. Métricas começam em zero e representam somente eventos registrados.

Nenhum seed inventa certificados, datas ou URLs. Projetos/experiências fornecidos na especificação podem ser inseridos manualmente com descrições confirmadas.

## Testes e banco isolado

`ApiIntegrationTest` usa PostgreSQL real em `safe_test`, Flyway e MockMvc com Spring Security. O `@BeforeEach` trunca os dados de conteúdo **desse banco**. Use `TEST_DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD` para substituir conexão de teste. O teste provisiona uma conta isolada; não use credenciais de produção.

Playwright usa banco `safe_e2e`, outra conta e armazenamento separados. Não limpe banco de usuário como preparação de teste. Cada registro E2E é marcado como teste; mantenha esse banco separado do banco de desenvolvimento `safe`.

Para autenticação no browser, não crie bypasses. Tests unitários do web simulam respostas da API; a jornada Playwright usa API/postgres reais. CI usa serviço PostgreSQL e configurações efêmeras explícitas.

## Ambiente Codex

`infra/development/cloud-install.sh` prepara caches, toolchain e builds; `cloud-start.sh` inicia banco/API/Vite e faz requests de readiness. Ambos são idempotentes quanto à configuração local: não sobrescrevem `.local/dev.env`. Logs e PID ficam em `.local/`, ignorado pelo Git. A conta local existe apenas por configuração explícita nessa pasta.

Maven no ambiente usa o proxy HTTPS fornecido e o truststore Java do sistema, mantendo verificação TLS. O JDK de desenvolvimento é obtido de uma imagem oficial com integridade OCI; Maven wrapper verifica SHA-256 da distribuição. Nunca desabilite checagem de certificados para contornar rede.

Volumetria Docker, segredos de runtime e processos não devem ser presumidos restaurados em uma nova tarefa. Faça backup do banco antes de publicação/migração de máquina quando houver dados relevantes. A snapshot da configuração de nuvem não é um deploy do aplicativo.
