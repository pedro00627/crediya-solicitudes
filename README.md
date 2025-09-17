# Microservicio de Solicitudes - CrediYa

Microservicio responsable de la gestión del ciclo completo de solicitudes de préstamos en el ecosistema CrediYa.

## Funcionalidades

- Registro de solicitudes de préstamo
- Evaluación automatizada de capacidad de endeudamiento
- Gestión de estados de solicitudes
- Listado de solicitudes para revisión manual
- Validación de tipos de préstamo

## Tecnologías

- **Spring Boot 3.5.4** con WebFlux (Reactivo)
- **R2DBC** para acceso reactivo a PostgreSQL
- **MapStruct** para mapeo de objetos
- **Swagger/OpenAPI** para documentación de API

## API Endpoints

- `POST /api/v1/solicitud` - Registrar nueva solicitud de préstamo
- `GET /api/v1/solicitud` - Listar solicitudes (requiere rol Asesor)
- `GET /api/v1/solicitud/{id}` - Obtener solicitud específica
- `PUT /api/v1/solicitud/{id}/estado` - Actualizar estado de solicitud

## Variables de Entorno

```env
SPRING_R2DBC_URL=r2dbc:postgresql://solicitudes-db:5432/{db_name}
SPRING_R2DBC_USERNAME={db_user}
SPRING_R2DBC_PASSWORD={db_password}
```

## Docker

El servicio se ejecuta en el puerto **8080** y se conecta a la base de datos `solicitudes-db` en el puerto 5433.

## Historia de Usuario Implementada

### HU1: Registrar una solicitud de préstamo

**Como cliente**, quiero enviar mi solicitud de préstamo con la información necesaria (monto y plazo deseado) para que
CrediYa pueda evaluarla.

#### Criterios de Aceptación

- ✅ Se puede enviar una solicitud de crédito con información del cliente y detalles del préstamo
- ✅ La solicitud se registra automáticamente con estado "Pendiente de revisión"
- ✅ El sistema valida que el tipo de préstamo seleccionado exista

### HU2: Listado de solicitudes para revisión manual

**Como Asesor**, quiero ver un listado de todas las solicitudes que necesitan mi revisión para tomar la decisión final.

#### Criterios de Aceptación

- ✅ Lista paginada y filtrable de solicitudes pendientes
- ✅ Retorna información completa de solicitudes (monto, plazo, email, nombre, tipo_préstamo, tasa_interés,
  estado_solicitud, salario_base, deuda_total_mensual)

## Arquitectura Clean Architecture

El proyecto implementa Clean Architecture con los siguientes módulos:

### Domain

Encapsula la lógica y reglas del negocio para el procesamiento de solicitudes.

### Usecases

Implementa los casos de uso para registro, evaluación y gestión de solicitudes.

### Infrastructure

- **Driven Adapters**: Conexión a PostgreSQL con R2DBC
- **Entry Points**: APIs REST para gestión de solicitudes

### Application

Ensambla los módulos, resuelve dependencias y configura la aplicación.

**Los beans de los casos de uso se disponibilizan automáticamente gracias a '@ComponentScan'.**