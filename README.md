# Deportal Backend

Backend Spring Boot 3.5 con Java 21 para gestion de reservas deportivas.

## Ejecutar con Docker Compose

Construir y levantar el backend:

```bash
docker compose up --build -d
```

Ver logs:

```bash
docker compose logs -f backend
```

Detener el contenedor:

```bash
docker compose down
```

Detener y borrar tambien el volumen de H2:

```bash
docker compose down -v
```

## URLs locales

- API health: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`

## Pruebas

Ejecutar pruebas unitarias y de contexto:

```bash
mvn test
```

La suite usa JUnit 5 y Mockito para aislar servicios de negocio con repositorios mockeados. H2 solo se usa en pruebas de contexto Spring.

## H2 en Docker

El contenedor usa un volumen Docker para persistir la base de datos H2 en `/app/data`.

Datos de conexion H2:

- JDBC URL: `jdbc:h2:file:/app/data/deportal`
- Usuario: `sa`
- Password: vacio

## Variables principales

Las variables estan definidas en `docker-compose.yml`:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `APP_CORS_ALLOWED_ORIGINS`
