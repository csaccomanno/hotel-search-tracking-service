# Hotel Search Tracking Service

Servicio backend para registrar búsquedas de disponibilidad hotelera, publicarlas en Kafka, persistirlas en Oracle y consultar cuántas búsquedas tienen exactamente los mismos criterios.

## Cómo levantar el sistema

Para ejecutar el entorno completo solamente se necesita Docker Desktop con Docker Compose. Los siguientes comandos están preparados para Git Bash y deben ejecutarse desde la raíz del repositorio.

Primero, crear el archivo local de configuración a partir de la plantilla:

```bash
cp .env.example .env
```

El archivo `.env` es obligatorio. Antes de continuar se pueden modificar los puertos, las credenciales iniciales de Oracle y el nombre del tópico de Kafka.

Luego, compilar la aplicación y levantar todos los servicios:

```bash
docker compose up --build -d
```

El Dockerfile ejecuta `./mvnw verify` con Java 21 durante la construcción de la imagen. Por lo tanto, no es necesario tener Java ni Maven instalados en el equipo.

Verificar el estado de los contenedores:

```bash
docker compose ps
```

Si la API todavía no está disponible, consultar sus logs:

```bash
docker compose logs -f hotel-search-api
```

Oracle puede tardar algunos segundos en completar su inicialización. La API se inicia cuando Oracle y Kafka informan que están saludables.

## URLs disponibles

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Kafka UI:

```text
http://localhost:8090
```

Health check:

```text
http://localhost:8080/actuator/health
```

Las URLs anteriores utilizan los puertos predeterminados de `.env.example`. Si se modifica algún puerto en `.env`, también debe ajustarse la URL correspondiente.

## Cómo probar la API desde Swagger

### Registrar una búsqueda

Abrir Swagger UI, desplegar `POST /search`, seleccionar `Try it out` y utilizar este payload:

```json
{
  "hotelId": "1234aBc",
  "checkIn": "29/12/2026",
  "checkOut": "31/12/2026",
  "ages": [30, 29, 1, 3]
}
```

La API responde con estado HTTP `202 Accepted` y un identificador único:

```json
{
  "searchId": "e36b5a1e-0bce-4a35-b7ea-316928d51f09"
}
```

El UUID del ejemplo es ilustrativo. Cada petición genera un `searchId` diferente, aunque el contenido de dos búsquedas sea igual.

### Consultar búsquedas iguales

Desplegar `GET /count`, seleccionar `Try it out` y completar el parámetro `searchId` con el UUID devuelto por el POST.

El GET no recibe un cuerpo JSON. El identificador se envía como query parameter:

```text
GET /count?searchId=e36b5a1e-0bce-4a35-b7ea-316928d51f09
```

La respuesta esperada tiene esta estructura:

```json
{
  "searchId": "e36b5a1e-0bce-4a35-b7ea-316928d51f09",
  "search": {
    "hotelId": "1234aBc",
    "checkIn": "29/12/2026",
    "checkOut": "31/12/2026",
    "ages": [30, 29, 1, 3]
  },
  "count": 1
}
```

La persistencia es asíncrona. Si el primer GET devuelve `404 Not Found`, esperar un instante y volver a ejecutarlo. Al registrar nuevamente el mismo contenido se genera otro `searchId`, pero el conteo de búsquedas iguales aumenta.

El orden de las edades forma parte del criterio de igualdad. Por ejemplo, `[30, 29, 1, 3]` y `[1, 3, 29, 30]` se consideran búsquedas diferentes.

## Flujo de procesamiento

Cuando la API recibe un POST, valida el payload, lo convierte al modelo de dominio y genera un UUID único. El valor del mensaje de Kafka contiene los cuatro campos recibidos: `hotelId`, `checkIn`, `checkOut` y `ages`; el `searchId` se envía como clave para conservar el payload original.

El consumidor recupera el UUID desde la clave, reconstruye la búsqueda y utiliza el mismo identificador como clave primaria en Oracle. Finalmente, el GET localiza la búsqueda por su `searchId` y cuenta todos los registros con el mismo hotel, fechas y edades en el mismo orden.

El POST devuelve `202 Accepted` porque la publicación en Kafka se completa antes de que el consumidor finalice la persistencia en Oracle.

## Inspeccionar los mensajes de Kafka

Abrir Kafka UI y realizar los siguientes pasos:

1. Seleccionar el cluster `local`.
2. Ingresar en `Topics`.
3. Abrir el tópico `hotel_availability_searches`.
4. Seleccionar `Messages`.
5. Buscar el `searchId` devuelto por el POST.

El `searchId` aparece como clave. El valor conserva el JSON funcional enviado a la API:

```json
{
  "hotelId": "1234aBc",
  "checkIn": "29/12/2026",
  "checkOut": "31/12/2026",
  "ages": [30, 29, 1, 3]
}
```

Si se cambia `KAFKA_TOPIC` en `.env`, se debe buscar el tópico con ese nombre.

## Conexión a Oracle

Se puede utilizar DBeaver, IntelliJ IDEA Database Tools o cualquier cliente compatible con Oracle.

La conexión utiliza los siguientes datos:

- Host: `localhost`.
- Puerto: valor de `ORACLE_PORT` en `.env`.
- Service name: `FREEPDB1`.
- Usuario: valor de `ORACLE_APP_USER` en `.env`.
- Contraseña: valor de `ORACLE_APP_PASSWORD` en `.env`.

Con el puerto predeterminado, la URL JDBC es:

```text
jdbc:oracle:thin:@localhost:1521/FREEPDB1
```

## Arquitectura

La aplicación utiliza arquitectura hexagonal sin separar el proyecto en módulos Maven.

El paquete `domain` contiene el modelo y las validaciones de negocio. No depende de Spring, REST, Kafka, JPA ni Oracle.

El paquete `application` contiene los casos de uso y los puertos de entrada y salida. Conoce solamente al dominio y define las operaciones que necesita sin depender de implementaciones técnicas.

El paquete `infrastructure` contiene los adaptadores REST, Kafka y Oracle, además de la configuración que conecta los casos de uso con sus implementaciones.

El productor y el consumidor de Kafka están implementados en clases separadas. La dirección de las dependencias se comprueba automáticamente mediante pruebas ArchUnit.

## Validaciones e inmutabilidad

La API valida que:

- `hotelId` no sea nulo ni vacío.
- `checkIn` y `checkOut` no sean nulos ni vacíos y respeten el formato `dd/MM/yyyy`.
- `checkIn` sea anterior a `checkOut`.
- `ages` no sea nulo ni vacío.
- Cada edad sea distinta de nulo y mayor o igual que cero.

Los errores de validación responden con HTTP `400 Bad Request` y los mensajes correspondientes. Un `searchId` inexistente responde con HTTP `404 Not Found`.

Los contratos REST, los mensajes Kafka y los objetos del dominio utilizan records. Las listas se copian mediante `List.copyOf` para evitar modificaciones desde referencias externas. La entidad JPA mantiene solamente la mutabilidad requerida por Hibernate, no expone setters y está marcada como de solo lectura. Las fechas se convierten una sola vez y se representan internamente mediante `LocalDate`.

## Criterio de igualdad de edades

Para contar búsquedas equivalentes, las edades se convierten en una representación ordenada y se calcula un hash SHA-256. Por este motivo, cambiar el orden de la lista produce un hash diferente.

El hash se almacena en Oracle y forma parte de un índice junto con el hotel y las fechas, evitando comparar toda la colección en cada consulta. Las edades originales también se persisten con su posición para poder reconstruir la búsqueda. Este hash funciona como una huella para la comparación y no tiene un propósito criptográfico de seguridad.

## Persistencia, concurrencia y seguridad

Cada petición genera un UUID nuevo. El mismo identificador se utiliza como clave de Kafka y como clave primaria en Oracle. La persistencia es idempotente por `searchId`, por lo que una eventual reentrega del mismo mensaje no crea otro registro.

La transacción ACID se limita a la escritura en Oracle. La comunicación entre la API, Kafka y la base de datos es asíncrona y presenta consistencia eventual.

El productor Kafka utiliza confirmación `acks=all` e idempotencia. El tópico tiene tres particiones y el listener mantiene una concurrencia de tres. Los componentes involucrados no almacenan estado mutable compartido.

Los virtual threads de Java 21 están habilitados mediante Spring Boot y se utilizan durante el procesamiento del consumidor Kafka. Los logs informan si el mensaje está siendo procesado por un virtual thread. El pool de Oracle permanece limitado a diez conexiones: los virtual threads reducen el costo de espera de operaciones bloqueantes, pero no aumentan la capacidad de la base de datos.

Las consultas a Oracle utilizan parámetros de JPA y no concatenan valores recibidos en sentencias SQL.

## Tecnologías

- Java 21.
- Spring Boot 4.1.
- Apache Kafka 4.3.
- Oracle Free 23ai.
- Spring Data JPA.
- Flyway.
- Springdoc OpenAPI y Swagger UI.
- Maven Wrapper.
- JUnit, Mockito y ArchUnit.
- JaCoCo.
- Docker Compose.

## Pruebas y cobertura

Con Java 21 instalado se puede ejecutar localmente:

```bash
./mvnw clean verify
```

El mismo comando se ejecuta durante la construcción de la imagen Docker. El build ejecuta las pruebas unitarias, valida las dependencias entre las capas con ArchUnit y falla si la cobertura de líneas, branches, instrucciones o métodos queda por debajo del 80%.

El informe generado por JaCoCo queda disponible en:

```text
target/site/jacoco/index.html
```

## Logs

Los logs de la API se consultan con:

```bash
docker compose logs -f hotel-search-api
```

Las operaciones principales registran el mismo `searchId`, lo que permite seguir una búsqueda desde su recepción HTTP hasta su publicación, consumo y persistencia.

## Detener o reiniciar el sistema

Para detener los contenedores conservando los datos de Oracle y Kafka:

```bash
docker compose down
```

Para volver a iniciarlos con los datos existentes:

```bash
docker compose up -d
```

Para eliminar los datos y comenzar desde cero:

```bash
docker compose down -v
docker compose up --build -d
```

El comando `docker compose down -v` elimina las búsquedas almacenadas, los mensajes de Kafka y los offsets del consumidor.

Las credenciales de Oracle se aplican cuando se crea el volumen por primera vez. Si se modifican `ORACLE_SYSTEM_PASSWORD`, `ORACLE_APP_USER` u `ORACLE_APP_PASSWORD` en `.env`, es necesario recrear los volúmenes con `docker compose down -v` antes de levantar nuevamente el entorno.
