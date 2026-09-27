# AutoFlow

AutoFlow é uma aplicação backend voltada para gestão e autenticação de usuários, com foco em segurança, controle de acesso e renovação de sessões por tokens JWT.

A estrutura atual do projeto foi pensada para separar responsabilidades em camadas, facilitando manutenção, evolução e testes da aplicação.

## Estrutura do projeto

```text
AutoFlow/
├─ api/
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ java/
│  │  │  │  └─ com/br/auto_flow/
│  │  │  │     ├─ application/     # DTOs, casos de uso, serviços de aplicação
│  │  │  │     ├─ domain/          # entidades, regras de domínio, enums e contratos
│  │  │  │     ├─ infrastructure/  # configuração, segurança, persistência e repositórios
│  │  │  │     ├─ presentation/    # controllers, tratamento de exceções
│  │  │  │     └─ ...
│  │  │  └─ resources/
│  │  │     └─ application.properties
│  │  └─ test/
│  ├─ pom.xml
│  └─ Dockerfile
├─ app/
│  └─ (aplicação frontend / cliente da API)
├─ docker-compose.yml
├─ README.md
└─ ...
```

### Visão geral das camadas

- `application`: contém DTOs, casos de uso e interfaces de serviço, como autenticação e registro de usuários.
- `domain`: centraliza as regras de negócio, modelos e enums, como `Role` e `User`.
- `infrastructure`: implementa integração com banco de dados, JWT, segurança e persistência.
- `presentation`: expõe os endpoints da API e trata erros de forma padronizada.

## Autenticação e autorização

### Autenticação

A autenticação do sistema é feita com JWT (JSON Web Token). Quando o usuário faz login, a API gera um token de acesso que será enviado em requisições subsequentes via cabeçalho de autorização.

No projeto, a autenticação é tratada por:

- `SecurityConfig`: configura a segurança HTTP e define regras de acesso.
- `JwtAuthenticationFilter`: valida o token em cada requisição.
- `TokenProvider`: responsável pela criação e expiração dos tokens.

A aplicação usa autenticação stateless, ou seja, não mantém sessão do usuário no servidor. Cada requisição é validada pelo token recebido.

### Autorização

A autorização é configurada por regras de acesso HTTP e também por segurança de métodos. O projeto usa `@EnableMethodSecurity` e define permissões conforme o papel do usuário.

Os papéis existentes são:

- `ADMIN`
- `USER`

No `SecurityConfig`, endpoints públicos como cadastro e login são liberados, enquanto as demais rotas exigem autenticação.

### Refresh Token

O refresh token é utilizado para renovar o token de acesso sem exigir novo login. Ele é armazenado de forma segura e validado antes de emitir um novo access token.

O fluxo de refresh implementado no projeto segue a lógica:

1. O usuário faz login e recebe access token + refresh token.
2. Quando o access token expira, o cliente envia o refresh token para `/api/auth/refresh`.
3. A aplicação verifica se o token:
   - existe;
   - não foi revogado;
   - não expirou;
   - pertence a um usuário válido.
4. Se tudo estiver correto, um novo access token é emitido e o refresh token antigo pode ser rotacionado ou invalidado.

Essa estratégia melhora a experiência do usuário, mantendo a sessão segura e reduzindo a necessidade de autenticação frequente.

## Tecnologias utilizadas

- Java 25
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (jjwt)
- Maven
- Lombok
- MapStruct
- Docker / Docker Compose

## Execução local

### Com Docker

```bash
docker compose up --build -d
```

### Sem Docker

No diretório da API:

```bash
cd api
./mvnw spring-boot:run
```

> Ajuste as variáveis de ambiente do projeto conforme necessário no arquivo `application.properties`.

## Variáveis de ambiente

O projeto usa variáveis de ambiente para separar configurações sensíveis e específicas do ambiente de execução. Isso vale tanto para o Docker quanto para a aplicação Spring Boot.

### 1. Configuração no Docker Compose

O arquivo [docker-compose.yml](docker-compose.yml) lê valores do arquivo `.env` na raiz do projeto. Se a variável não for definida, o compose usa valores padrão com `:-`.

Exemplo de `.env` na raiz do projeto:

```env
POSTGRES_DB=autoflow
POSTGRES_USER=autoflow
POSTGRES_PASSWORD=sua_senha_forte
POSTGRES_HOST_PORT=5433
APP_PORT=8080
HIBERNATE_DDL_AUTO=update
JWT_EXPIRATION_MS=3600000
REFRESH_TOKEN_EXPIRATION_MS=2592000000
```

Essas variáveis serão consumidas pelo compose da seguinte forma:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-autoflow}
  SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-autoflow}
  SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-autoflow}
```

Se você quiser alterar a porta do banco, usuário, senha ou porta da API, basta ajustar o `.env` e reiniciar o ambiente:

```bash
docker compose down
docker compose up --build -d
```

### 2. Configuração no application.properties

O arquivo [api/src/main/resources/application.properties](api/src/main/resources/application.properties) também usa placeholders para ler variáveis de ambiente. O padrão é:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://postgres:5432/autoflow}
```

Isso significa:

- se a variável `SPRING_DATASOURCE_URL` estiver definida, ela será usada;
- caso contrário, será usado o valor padrão informado após os dois pontos.

Exemplo de configuração externa com variáveis de ambiente no ambiente do sistema:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/autoflow
export SPRING_DATASOURCE_USERNAME=autoflow
export SPRING_DATASOURCE_PASSWORD=sua_senha_forte
export JWT_PRIVATE_KEY_PATH=keys/jwt-private.pem
export JWT_PUBLIC_KEY_PATH=keys/jwt-public.pem
export JWT_EXPIRATION_MS=3600000
export JWT_ISSUER=auto_flow
export REFRESH_TOKEN_EXPIRATION_MS=2592000000
```

### 3. Boas práticas

- nunca commitar senhas reais no repositório;
- manter o `.env` no ambiente local e fora do Git;
- usar valores diferentes para desenvolvimento, homologação e produção;
- preferir segredos e arquivos PEM fora do código-fonte quando a aplicação estiver em produção.

> Em ambientes reais, recomenda-se utilizar um gerenciador de segredos, como Docker secrets, Kubernetes Secrets ou variáveis de ambiente do provedor da infraestrutura.

## Endpoints principais

- `POST /api/users/register` - cadastro de usuário
- `POST /api/auth/login` - autenticação do usuário
- `POST /api/auth/refresh` - renovação do token
- `POST /api/auth/logout` - encerramento da sessão/token

## Observações

Este README serve como base inicial do projeto e pode ser expandido conforme a API evoluir, incluindo:

- documentação detalhada dos endpoints;
- exemplos de payloads JSON;
- fluxos de negócio;
- instruções de deploy e ambiente de produção.
