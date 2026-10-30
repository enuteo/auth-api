# auth-api

API de login que autentica usuários no Keycloak e retorna os tokens do usuário.

## Configuração

A aplicação usa por padrão o Keycloak local em `http://localhost:8080`, o realm
`authtest` e o client ID `auth-api-test`. É possível configurar esses valores
pelas variáveis `KEYCLOAK_BASE_URL`, `KEYCLOAK_REALM` e `KEYCLOAK_CLIENT_ID`.

O client do Keycloak precisa ser público e ter **Direct Access Grants**
habilitado. O script `start-keycloak-postgres.sh` do repositório `api-authtest`
cria o client `auth-api-test` com essas opções.

## Login

Envie as credenciais do usuário para `POST /auth/login`:

```bash
curl --request POST http://localhost:8081/auth/login \
  --header 'Content-Type: application/json' \
  --data '{"username":"user","password":"userpass"}'
```

Uma autenticação bem-sucedida retorna o JSON de tokens do Keycloak. Credenciais
inválidas retornam `401`; falhas de comunicação ou configuração do Keycloak
retornam `502`.

Execute a aplicação com `./mvnw spring-boot:run`. O client ID é configurado no
servidor e não deve ser enviado como substituto das credenciais do usuário.
