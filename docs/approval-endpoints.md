# Endpoints de Aprobación y Rechazo de Solicitudes

## 🎯 **Endpoint Implementado**

### **PUT /api/v1/solicitud/{applicationId}/status**

Endpoint único para aprobar o rechazar solicitudes de préstamo.

#### **Request:**
```http
PUT /api/v1/solicitud/123e4567-e89b-12d3-a456-426614174000/status
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "advisorId": "ADV-001",
  "newStatus": "APROBADA",
  "reason": "Cliente cumple todos los criterios de evaluación. Ingresos verificados y estables."
}
```

#### **Request para Rechazo:**
```http
PUT /api/v1/solicitud/123e4567-e89b-12d3-a456-426614174000/status
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "advisorId": "ADV-001",
  "newStatus": "RECHAZADA",
  "reason": "Ingresos insuficientes para el monto solicitado. Se recomienda reducir el monto o incluir un codeudor."
}
```

#### **Response Aprobación:**
```json
{
  "applicationId": "123e4567-e89b-12d3-a456-426614174000",
  "status": "APROBADA",
  "advisorId": "ADV-001",
  "reason": "Cliente cumple todos los criterios de evaluación...",
  "appliedInterestRate": 0.185,
  "monthlyPayment": 267850.25,
  "updatedAt": "2024-09-22T15:30:00",
  "message": "Solicitud aprobada exitosamente"
}
```

#### **Response Rechazo:**
```json
{
  "applicationId": "123e4567-e89b-12d3-a456-426614174000",
  "status": "RECHAZADA",
  "advisorId": "ADV-001",
  "reason": "Ingresos insuficientes para el monto solicitado...",
  "appliedInterestRate": null,
  "monthlyPayment": null,
  "updatedAt": "2024-09-22T15:30:00",
  "message": "Solicitud rechazada"
}
```

## 🔧 **Validaciones Implementadas**

### **1. Validaciones de Request:**
- `advisorId`: Requerido, no vacío
- `newStatus`: Requerido, solo acepta "APROBADA" o "RECHAZADA"
- `reason`: Requerida, no vacía

### **2. Validaciones de Negocio:**
- La solicitud debe existir
- La solicitud debe estar en estado "PENDIENTE"
- El ID de solicitud debe ser un UUID válido

### **3. Cálculo Automático para Aprobaciones:**
- ✅ **Tasa de interés**: Se consulta automáticamente de la tabla `tipo_prestamo`
- ✅ **Cuota mensual**: Se calcula con fórmula de cuota fija
- ✅ **Monto y plazo**: Se usan los valores originales de la solicitud

## 🗄️ **Cambios en Base de Datos**

### **Script SQL Aplicado:**
```sql
-- Ejecutar: sql/05-solicitudes-approval-fields.sql

ALTER TABLE solicitudes.solicitud
ADD COLUMN id_asesor VARCHAR(50),
ADD COLUMN razon_decision TEXT,
ADD COLUMN tasa_interes_aplicada DECIMAL(5,4),
ADD COLUMN cuota_mensual DECIMAL(15,2),
ADD COLUMN fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
```

### **Nuevos Campos:**
- `id_asesor`: ID del asesor que evaluó
- `razon_decision`: Motivo de aprobación/rechazo
- `tasa_interes_aplicada`: Tasa usada en el cálculo
- `cuota_mensual`: Cuota calculada para aprobadas
- `fecha_creacion`: Timestamp de creación
- `fecha_actualizacion`: Timestamp de última modificación (auto-actualizado)

## 🏗️ **Arquitectura Implementada**

### **Clean Architecture:**
```
Entry Point (REST)
    ↓
ApplicationCommandHandler
    ↓
UpdateApplicationStatusUseCase
    ↓
ApplicationGateway + LoanTypeGateway
    ↓
Database (PostgreSQL)
```

### **Componentes Creados:**

#### **1. DTOs:**
- `UpdateApplicationStatusRequestRecord`
- `ApplicationStatusUpdateResponseRecord`

#### **2. Use Case:**
- `UpdateApplicationStatusUseCase`

#### **3. Domain Methods:**
- `Application.approve()`
- `Application.reject()`
- `Application.isPending()`

#### **4. Router:**
- Nuevo endpoint PUT en `RouterRest`

## 🧮 **Fórmula de Cálculo de Cuota**

### **Cuota Fija Mensual:**
```
PMT = P * [r(1+r)^n] / [(1+r)^n - 1]

Donde:
P = Monto principal (amount)
r = Tasa mensual (interest_rate / 12)
n = Número de cuotas (term)
```

### **Ejemplo:**
```
Monto: $5,000,000
Tasa anual: 18.5% (0.185)
Plazo: 24 meses

Tasa mensual: 0.185 / 12 = 0.01542
Cuota mensual: $267,850.25
```

## 🔄 **Flujo de Negocio**

### **1. Aprobación:**
```
1. Validar solicitud existe y está PENDIENTE
2. Consultar tasa actual del tipo de préstamo
3. Calcular cuota mensual
4. Actualizar estado a APROBADA
5. Guardar: asesor, razón, tasa, cuota, timestamps
6. Retornar response con datos calculados
```

### **2. Rechazo:**
```
1. Validar solicitud existe y está PENDIENTE
2. Actualizar estado a RECHAZADA
3. Guardar: asesor, razón, timestamps
4. Retornar response con motivo
```

## 🚀 **Próximos Pasos**

1. **Integrar con Notificaciones**: Enviar eventos a SQS cuando cambie el estado
2. **Testing**: Crear tests unitarios e integración
3. **Seguridad**: Validar que solo asesores puedan usar el endpoint
4. **Métricas**: Agregar logs y métricas de negocio

## 📝 **Testing Manual**

```bash
# 1. Crear solicitud (como cliente)
curl -X POST http://localhost:8081/api/v1/solicitud \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <CLIENT_TOKEN>" \
  -d '{...solicitud_data...}'

# 2. Aprobar solicitud (como asesor)
curl -X PUT http://localhost:8081/api/v1/solicitud/{applicationId}/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADVISOR_TOKEN>" \
  -d '{
    "advisorId": "ADV-001",
    "newStatus": "APROBADA",
    "reason": "Cliente cumple criterios"
  }'
```