# Usar gratuitamente no seu computador

Você não precisa de Railway, Vercel, domínio nem bucket pago para usar o SAFE System localmente. Site, painel, PostgreSQL e arquivos ficam no seu computador. O acesso fica limitado a esse computador; não é um link público para recrutadores. Ele precisa estar ligado e com Docker iniciado para você usar o sistema.

## Preparar

Instale **Docker Desktop** (Windows/macOS) ou **Docker Engine com plugin Compose** (Linux). Docker Desktop é gratuito para uso pessoal conforme os termos do fornecedor. Windows usa containers Linux, normalmente com WSL 2. Instale Git se quiser clonar; também pode baixar o ZIP no botão **Code → Download ZIP** do GitHub e extrair.

Com Git:

```bash
git clone https://github.com/safe107/Safe-System.git
cd Safe-System
```

Não precisa instalar Java, Node, Maven ou Python para este modo. O Docker baixa as dependências na primeira execução; precisa de internet e espaço em disco, e pode levar vários minutos. O código continua disponível para execução sem Docker pelo README.

## Iniciar

No Windows, abra PowerShell na pasta extraída/clonada:

```powershell
powershell -ExecutionPolicy Bypass -File .\infra\development\start-local.ps1
```

A opção de execução se aplica só ao processo acima; não muda a política global do Windows.

No Linux ou macOS, abra um terminal na pasta:

```bash
bash infra/development/start-local.sh
```

O script gera credenciais locais aleatórias apenas na primeira execução, compila a imagem e espera a saúde dos serviços. Não substitui senhas existentes. Em Linux/macOS, o arquivo gerado é legível apenas pelo usuário; em Windows, mantenha a pasta em um local privado da sua conta. O Git ignora `.local`.

Abra no navegador do **mesmo computador**:

- `http://localhost:5173/pt-BR`: portfólio;
- `http://localhost:5173/admin/login`: painel.

Abra `.local/dev.env` em um editor de texto. Use `ADMIN_USERNAME` (por padrão `local-admin`) e o valor de `ADMIN_PASSWORD` para entrar. Não envie esse arquivo ou sua senha para terceiros. Trocar a variável não redefine a senha de um administrador já criado no banco.

No painel, configure apresentação e contato, cadastre conteúdos, envie PDFs e publique os registros que devem aparecer no portfólio. Cursos e candidaturas são privados.

## Fechar e abrir novamente

Para abrir outra vez, execute o mesmo script. Ele mantém banco, arquivos e credenciais. Para parar, na pasta do projeto:

```bash
docker compose -p safe-system-local --env-file .local/dev.env -f compose.local.yml stop
```

Dados e arquivos ficam nos volumes Docker `safe-system-local_local-postgres` e `safe-system-local_local-files`. Parar serviços não apaga dados. **Não use `down -v` nem apague esses volumes** se quiser preservar os registros. Faça backup do banco e arquivos antes de limpar/reinstalar Docker; copiar somente a pasta do código não copia os volumes.

Se a porta 5173 estiver ocupada, escolha outra antes de iniciar. PowerShell: `$env:SAFE_LOCAL_PORT="5180"`; Linux/macOS: `export SAFE_LOCAL_PORT=5180`. Use a porta escolhida no navegador.

Para verificar um problema:

```bash
docker compose -p safe-system-local --env-file .local/dev.env -f compose.local.yml ps
docker compose -p safe-system-local --env-file .local/dev.env -f compose.local.yml logs --tail 100 system
```

Não encaminhe logs contendo segredos. Este modo usa HTTP e arquivos locais, com a porta vinculada a `127.0.0.1`; não exponha esse modo na internet. Para publicação pública com HTTPS e configurações de produção, consulte PUBLISH-RAILWAY.md ou DEPLOYMENT.md.
