# Plan de Etapas - Backend Spring Boot

Este documento define el proceso incremental para construir y validar el backend de Deportal. La prioridad es entregar una API local funcional con Spring Boot MVC, Java 21 LTS, H2, JWT, Swagger UI y pruebas unitarias con Mockito.

## Objetivo

Construir un backend modular que cumpla los requerimientos funcionales de la prueba tecnica: canchas, reservas, disponibilidad, cancelacion con reembolso, descuentos, lista de espera y reportes de utilizacion.

## Decisiones Tecnicas

- Java 21 LTS.
- Spring Boot MVC sin WebFlux.
- Spring Data JPA.
- H2 para ejecucion local.
- JWT sencillo sin cookies con expiracion de 8 horas.
- Spring Security.
- Swagger UI con springdoc-openapi.
- JUnit 5 y Mockito para pruebas unitarias.
- Docker opcional para ejecutar el backend de forma aislada.
- Preparacion futura para AWS mediante configuracion por perfiles y posible migracion a PostgreSQL/RDS.

## Estructura Objetivo

```txt
src/main/java/com/deportal/
├── auth/
├── users/
├── courts/
├── products/
├── reservations/
├── payments/
├── reports/
├── waitlist/
├── security/
├── config/
└── shared/
```

Cada modulo funcional debe mantener separacion similar:

```txt
controller/
service/
repository/
entity/
dto/
mapper/
enums/
```

## Etapa 1 - Base Spring Boot Sin Seguridad

Responsabilidades:

- Crear proyecto Spring Boot con Java 21.
- Agregar dependencias base: Web, Validation, JPA, H2, Lombok opcional, Swagger.
- Crear endpoint de salud simple.
- Configurar perfiles locales.
- Configurar manejo global de errores.

Criterios de aceptacion:

- El backend inicia localmente.
- Existe endpoint `GET /api/health`.
- Swagger UI abre localmente.
- H2 esta configurado pero todavia sin reglas de negocio complejas.

Validaciones:

- Ejecutar `./mvnw spring-boot:run`.
- Abrir `http://localhost:8080/swagger-ui.html`.
- Abrir `http://localhost:8080/h2-console` si esta habilitado.

## Etapa 2 - Modelo Relacional H2/JPA

Responsabilidades:

- Crear entidades JPA principales.
- Crear repositorios Spring Data JPA.
- Crear enums de negocio.
- Crear datos iniciales de canchas y usuarios.

Tablas objetivo:

- `users`
- `courts`
- `products`
- `reservations`
- `waitlist_entries`

Criterios de aceptacion:

- La aplicacion crea el esquema local.
- Los datos de referencia se cargan correctamente.
- Las entidades no se exponen directamente en controladores.

Validaciones:

- Consultar H2 Console.
- Verificar canchas de ejemplo.
- Verificar usuarios de ejemplo.

## Etapa 3 - DTOs, Mappers Y Validaciones

Responsabilidades:

- Crear requests y responses por modulo.
- Crear mappers explicitos.
- Configurar rechazo de campos desconocidos.
- Agregar validaciones con `jakarta.validation`.
- Agregar sanitizacion basica de strings.

Criterios de aceptacion:

- Los controladores reciben DTOs, no entidades.
- Las respuestas no exponen `passwordHash` ni datos internos.
- Requests invalidos retornan errores claros.

Validaciones:

- Probar payload con campos extra.
- Probar campos obligatorios vacios.
- Probar enums invalidos.

## Etapa 4 - Canchas

Responsabilidades:

- Implementar registro de canchas.
- Implementar listado y consulta por id.
- Validar nombre unico.
- Validar tipo de deporte, capacidad, horario y tarifa.
- Validar horario global 06:00 a 22:00.

Criterios de aceptacion:

- `POST /api/courts` crea cancha valida.
- `GET /api/courts` lista canchas.
- Se rechazan reglas invalidas del RF-1.

Validaciones:

- Nombre duplicado.
- Capacidad menor a 1 o mayor a 50.
- Apertura posterior al cierre.
- Tarifa menor a 5.

## Etapa 5 - Reservas Sin Seguridad

Responsabilidades:

- Implementar creacion de reservas con usuario/cancha existentes.
- Validar fecha no pasada.
- Validar duracion entre 1 y 8 horas.
- Validar horario de cancha.
- Validar horario global.
- Implementar disponibilidad y limpieza de 1 hora.

Criterios de aceptacion:

- `POST /api/reservations` crea reserva valida.
- Se rechazan solapamientos.
- Se rechazan reservas sin tiempo de limpieza.

Validaciones:

- Reserva 10:00-12:00 existente.
- Solicitar 11:00-13:00 debe fallar.
- Solicitar 12:00-13:00 debe fallar.
- Solicitar 13:00-14:00 debe funcionar.

## Etapa 6 - Pagos Y Descuentos

Responsabilidades:

- Crear `PaymentCalculator`.
- Calcular tarifa base.
- Aplicar descuento miembro 10%.
- Aplicar descuento off-peak 20% antes de 10:00 o despues de 19:00.
- Respetar descuento maximo efectivo de 30%.
- Guardar montos en reserva.

Criterios de aceptacion:

- El total coincide con el ejemplo del enunciado.
- Los montos se devuelven en `ReservationResponse`.

Validaciones:

- Miembro sin off-peak.
- No miembro off-peak.
- Miembro off-peak.
- Caso con maximo 30%.

## Etapa 7 - Cancelacion, Reembolso Y Lista De Espera

Responsabilidades:

- Implementar cancelacion de reservas futuras.
- Calcular reembolso segun diferencia contra hora actual.
- Liberar disponibilidad al cancelar.
- Implementar waitlist para no miembros en reservas del mismo dia.
- Activar automaticamente una reserva en espera compatible cuando se cancele otra.

Criterios de aceptacion:

- Mas de 24 horas: 100%.
- Entre 2 y 24 horas: 50%.
- Menos de 2 horas: 0%.
- Reservas ocurridas no se cancelan.
- Waitlist solo aplica para reservas del mismo dia.

Validaciones:

- Cancelacion con cada rango de reembolso.
- Crear waitlist para no miembro del mismo dia sin disponibilidad.
- Cancelar reserva confirmada y activar espera compatible.

## Etapa 8 - Seguridad JWT

Responsabilidades:

- Implementar registro y login.
- Hashear contrasenas con BCrypt.
- Generar JWT con expiracion de 8 horas.
- Proteger endpoints.
- Configurar CORS local para Angular.
- Documentar Bearer Auth en Swagger.

Endpoints publicos:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/health`

Endpoints protegidos:

- `/api/users/**`
- `/api/courts/**`
- `/api/products/**`
- `/api/reservations/**`
- `/api/reports/**`

Criterios de aceptacion:

- Login retorna token.
- Token expira en 8 horas.
- Endpoints protegidos rechazan requests sin token.
- Swagger permite usar Bearer token.

Validaciones:

- Probar endpoint protegido sin token.
- Probar endpoint protegido con token.
- Verificar expiracion configurada.

## Etapa 9 - Reportes

Responsabilidades:

- Implementar reporte de utilizacion por rango de fechas.
- Calcular reservas totales por cancha.
- Calcular horas reservadas vs horas disponibles.
- Calcular ingresos.
- Calcular tasa de ocupacion.

Criterios de aceptacion:

- `GET /api/reports/utilization?from=YYYY-MM-DD&to=YYYY-MM-DD` responde datos correctos.
- Solo considera reservas confirmadas para ocupacion e ingresos.
- El rango de fechas se valida.

Validaciones:

- Rango con reservas.
- Rango sin reservas.
- Fecha inicial posterior a final.

## Etapa 10 - Pruebas Unitarias Con Mockito

Responsabilidades:

- Crear pruebas de servicios con repositorios mockeados.
- Probar reglas de negocio principales.
- Evitar depender de H2 en pruebas unitarias puras.
- Agregar pruebas de repositorio con H2 solo si una query lo amerita.

Pruebas prioritarias:

- `AuthServiceTest`
- `JwtServiceTest`
- `CourtServiceTest`
- `ReservationServiceTest`
- `PaymentCalculatorTest`
- `CancellationServiceTest`
- `ReportServiceTest`

Criterios de aceptacion:

- `./mvnw test` ejecuta correctamente.
- Las reglas del enunciado tienen cobertura minima.
- Mockito simula repositorios y servicios dependientes.

## Etapa 11 - Swagger Y Documentacion Tecnica

Responsabilidades:

- Documentar controladores con OpenAPI.
- Configurar metadata de API.
- Configurar esquema Bearer JWT.
- Documentar errores comunes.
- Mantener README con ejecucion local.

Criterios de aceptacion:

- Swagger UI permite probar login.
- Swagger UI permite autorizar con JWT.
- Endpoints principales tienen descripcion clara.

## Etapa 12 - Preparacion Futura AWS

Responsabilidades:

- Mantener configuracion por perfiles.
- Dejar preparado cambio futuro de H2 a PostgreSQL/RDS.
- No acoplar logica de negocio a H2.
- Mantener CORS configurable.
- Documentar camino recomendado: EC2/ECS + RDS + S3/CloudFront.

Criterios de aceptacion:

- La app funciona localmente primero.
- La migracion futura esta documentada sin introducir complejidad innecesaria.

## Prioridades

- Alta: canchas, reservas, disponibilidad, pagos, cancelacion, reportes, tests, README.
- Media: auth JWT, Swagger detallado, productos/servicios basicos.
- Baja: despliegue real AWS, Swagger estatico S3, CI/CD.

## Checklist Final Backend

- Backend inicia localmente.
- H2 carga datos iniciales.
- Swagger UI funciona.
- Auth JWT funciona sin cookies.
- JWT expira en 8 horas.
- CRUD de canchas cumple RF-1.
- Reservas cumplen RF-2 y RF-3.
- Cancelacion cumple RF-4.
- Descuentos cumplen RF-5.
- Reporte cumple RF-6.
- Waitlist cumple RF-7.
- Tests unitarios ejecutan con Mockito.
- README explica arquitectura, ejecucion, endpoints y pruebas.
