# Registrar una solicitud de prestamo

## Descripción de negocio

Como cliente, quiero enviar mi solicitud de préstamo con la información necesaria (monto y plazo deseado) para que
CrediYa pueda evaluarla

## Descripción técnica

- microservicio SOLICITUDES con WebFlux.
- El endpoint (POST /api/v1/solicitud)
- El endpoint debe validar la información del cliente y del préstamo.
- El código debe manejar las transacciones de forma reactiva, aprovechando las ventajas de WebFlux.
- Se deben agregar logs de traza para monitorear
- toda excepción que se produzca debe ser manejada y procesada, para que el usuario que usa la api no le lleguen
  mensajes inesperados

## Criterios de aceptación

- Se puede enviar una solicitud de crédito que incluya información del cliente (documento de identidad) y los detalles
  del préstamo (monto, plazo), el tipo de prestamos que se quiere hacer
- La solicitud se registra automáticamente con un estado inicial de "Pendiente de revisión".
- El sistema valida que el tipo de préstamo seleccionado sea uno de los tipos de préstamo existentes.