# Deportal Backend

API backend en Spring Boot para la gestion de reservas de canchas deportivas. Incluye autenticacion JWT, gestion de canchas, reservas, cancelaciones, lista de espera, calculo de pagos, reportes de utilizacion, Swagger UI y base de datos H2 local.

## Stack Principal

| Tecnologia | Version / Uso |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.8 |
| Maven | Build y dependencias |
| Spring Security | JWT Bearer stateless |
| Spring Data JPA | Persistencia |
| H2 | Base de datos local en archivo |
| Docker Compose | Ejecucion local del backend |
| Swagger UI | Prueba y documentacion de endpoints |

## Requisitos Previos

Para ejecutar con Docker:

| Herramienta | Requisito |
|---|---|
| Docker | Instalado y en ejecucion |
| Docker Compose | Version v2 o compatible |
| Git | Para clonar el repositorio |

Para ejecutar sin Docker:

| Herramienta | Requisito |
|---|---|
| JDK | Java 21 |
| Maven | 3.9.x recomendado |

## Ejecucion Recomendada con Docker Compose

Desde la raiz del proyecto:

```bash
docker compose up --build -d
```

Validar que el servicio este arriba:

```bash
curl -fsS http://localhost:8080/api/health
```

Respuesta esperada:

```json
{
  "status": "UP",
  "timestamp": "..."
}
```

Ver logs del backend:

```bash
docker compose logs -f backend
```

Detener el contenedor:

```bash
docker compose down
```

Detener y borrar tambien la base de datos local H2 persistida en Docker:

```bash
docker compose down -v
```

## Ejecucion Local con Maven

Exportar variables locales recomendadas:

```bash
export JWT_SECRET="deportal-local-development-secret-key-must-be-at-least-32-bytes"
export APP_CORS_ALLOWED_ORIGINS="http://localhost:4200"
```

Ejecutar pruebas:

```bash
mvn test
```

Levantar la aplicacion:

```bash
mvn spring-boot:run
```

Empaquetar JAR:

```bash
mvn clean package
```

Ejecutar JAR generado:

```bash
java -jar target/deportal-api-0.0.1-SNAPSHOT.jar
```

## URLs Locales

| Servicio | URL |
|---|---|
| Health check | `http://localhost:8080/api/health` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| H2 Console | `http://localhost:8080/h2-console` |

## Credenciales de Prueba

Al iniciar la aplicacion se cargan datos semilla si la base de datos esta vacia.

| Usuario | Email | Password | Rol |
|---|---|---|---|
| Administrador Deportal | `admin@deportal.local` | `Deportal123` | `ADMIN` |
| Juan Perez | `juan.perez@deportal.local` | `Deportal123` | `USER` |
| Maria Garcia | `maria.garcia@deportal.local` | `Deportal123` | `USER` |
| Carlos Lopez | `carlos.lopez@deportal.local` | `Deportal123` | `USER` |

## Probar la API desde Swagger

1. Abrir `http://localhost:8080/swagger-ui.html`.
2. Ejecutar `POST /api/auth/login`.
3. Usar estas credenciales:

```json
{
  "email": "admin@deportal.local",
  "password": "Deportal123"
}
```

4. Copiar el valor `token` de la respuesta.
5. Presionar `Authorize`.
6. Pegar solamente el token JWT.
7. Ejecutar endpoints protegidos como `/api/courts`, `/api/reservations` o `/api/reports/utilization`.

Swagger enviara automaticamente el header:

```text
Authorization: Bearer <token>
```

## Probar la API con curl

Login:

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@deportal.local","password":"Deportal123"}'
```

Guardar token en una variable:

```bash
TOKEN="PEGAR_TOKEN_AQUI"
```

Listar canchas:

```bash
curl -s http://localhost:8080/api/courts \
  -H "Authorization: Bearer $TOKEN"
```

Consultar reporte de utilizacion:

```bash
curl -s 'http://localhost:8080/api/reports/utilization?from=2026-06-01&to=2026-06-30' \
  -H "Authorization: Bearer $TOKEN"
```

## Endpoints Principales

| Metodo | Ruta | Descripcion | Auth |
|---|---|---|---|
| GET | `/api/health` | Estado basico de la API | No |
| POST | `/api/auth/register` | Registro de usuario | No |
| POST | `/api/auth/login` | Login y generacion de JWT | No |
| GET | `/api/auth/me` | Usuario autenticado | Si |
| GET | `/api/courts` | Listar canchas | Si |
| GET | `/api/courts/{courtId}` | Consultar cancha | Si |
| POST | `/api/courts` | Crear cancha | Si |
| GET | `/api/reservations` | Listar reservas | Si |
| GET | `/api/reservations/{reservationId}` | Consultar reserva | Si |
| POST | `/api/reservations` | Crear reserva | Si |
| POST | `/api/reservations/{reservationId}/cancel` | Cancelar reserva | Si |
| GET | `/api/reports/utilization` | Reporte por rango de fechas | Si |

## Base de Datos H2

La aplicacion usa H2 en modo archivo y compatibilidad PostgreSQL para desarrollo local.

Conexion al ejecutar con Docker:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:/app/data/deportal` |
| Usuario | `sa` |
| Password | vacio |

Conexion al ejecutar con Maven:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/deportal` |
| Usuario | `sa` |
| Password | vacio |

La consola H2 esta disponible en `http://localhost:8080/h2-console`.

## Variables de Entorno

Las variables principales estan configuradas en `docker-compose.yml` y pueden sobrescribirse segun el entorno.

| Variable | Uso | Valor local |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de H2 | `jdbc:h2:file:/app/data/deportal;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` |
| `SPRING_DATASOURCE_USERNAME` | Usuario DB | `sa` |
| `SPRING_DATASOURCE_PASSWORD` | Password DB | vacio |
| `JWT_SECRET` | Secreto para firmar JWT | `deportal-local-development-secret-key-must-be-at-least-32-bytes` |
| `APP_CORS_ALLOWED_ORIGINS` | Origen permitido para frontend | `http://localhost:4200` |

## Pruebas Automatizadas

Ejecutar la suite:

```bash
mvn test
```

La suite usa JUnit 5 y Mockito para servicios de negocio. Tambien incluye prueba de contexto Spring.

## Integracion Continua

El repositorio incluye GitHub Actions en `.github/workflows/backend-ci.yml`.

El workflow se ejecuta en `push` y `pull_request` y realiza:

1. Setup de Java 21.
2. Ejecucion de `mvn test`.
3. Validacion de `docker compose config`.
4. Build de la imagen Docker del backend.
5. Levantamiento del contenedor.
6. Smoke test contra `http://localhost:8080/api/health`.
7. Apagado de contenedores.

## Documentacion Adicional

La carpeta `docs/` contiene documentacion ampliada:

| Archivo | Contenido |
|---|---|
| `docs/architecture.md` | Arquitectura tecnica, modelo de datos y flujos criticos |
| `docs/developer.md` | Guia de onboarding para desarrolladores |
| `docs/functional.md` | Documentacion funcional del producto |
| `docs/security.md` | Controles y recomendaciones de seguridad |
| `docs/ui-ux.md` | Guia UX/UI para un frontend consumidor |

## Solucion de Problemas

| Problema | Causa probable | Solucion |
|---|---|---|
| `localhost:8080` no responde | Contenedor no inicio o puerto ocupado | Revisar `docker compose logs -f backend` |
| Error al firmar JWT | `JWT_SECRET` muy corto | Usar un secreto de al menos 32 bytes |
| Swagger devuelve 401 | Token ausente o expirado | Hacer login nuevamente y autorizar en Swagger |
| H2 Console no conecta | JDBC URL incorrecta | Usar la URL correspondiente a Docker o Maven |
| Datos semilla no aparecen | Base existente ya tenia datos | Borrar volumen con `docker compose down -v` o limpiar `data/` local |
| Tests fallan por Java | Version JDK incorrecta | Verificar `java -version` y usar Java 21 |

## Notas de Seguridad para Empresa

Esta configuracion esta pensada para ejecucion local y pruebas tecnicas.

Antes de produccion se recomienda:

1. Reemplazar H2 por una base de datos productiva.
2. Usar un `JWT_SECRET` seguro desde un gestor de secretos.
3. Deshabilitar H2 Console.
4. Restringir Swagger UI o protegerlo por autenticacion.
5. Publicar la API detras de HTTPS.
6. Configurar CORS con los dominios reales del frontend.
