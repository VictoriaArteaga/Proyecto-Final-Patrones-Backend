# 🏛️ Proyecto Final — Backend API

> Plataforma backend para la **generación asistida por IA de diseños arquitectónicos y mobiliario**, que transforma una fotografía de un terreno o espacio en un render 2D realista y, finalmente, en un **modelo 3D navegable**.

<p align="left">
  <img alt="Java" src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.12-6DB33F?logo=springboot&logoColor=white">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-Supabase-336791?logo=postgresql&logoColor=white">
  <img alt="Maven" src="https://img.shields.io/badge/Maven-build-C71A36?logo=apachemaven&logoColor=white">
  <img alt="Docker" src="https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white">
  <img alt="License" src="https://img.shields.io/badge/license-Académico-lightgrey">
</p>

---

## 📑 Tabla de contenidos

- [Descripción del proyecto](#-descripción-del-proyecto)
- [¿Para quién va dirigido?](#-para-quién-va-dirigido)
- [Características principales](#-características-principales)
- [Tecnologías utilizadas](#-tecnologías-utilizadas)
- [Arquitectura](#-arquitectura)
- [Patrones de diseño aplicados](#-patrones-de-diseño-aplicados)
- [Flujo del proyecto (máquina de estados)](#-flujo-del-proyecto-máquina-de-estados)
- [Endpoints de la API](#-endpoints-de-la-api)
- [Requisitos previos](#-requisitos-previos)
- [Variables de entorno](#-variables-de-entorno)
- [Instalación y ejecución](#-instalación-y-ejecución)
- [Ejecución con Docker](#-ejecución-con-docker)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Seguridad](#-seguridad)
- [Autores](#-autores)

---

## 📖 Descripción del proyecto

**Backend API** es el núcleo de servicios de una plataforma de diseño asistido por inteligencia artificial. Su objetivo es acompañar al usuario en un flujo completo y guiado de creación de diseños:

1. **Carga** una fotografía del terreno o del espacio que desea intervenir.
2. El sistema **genera un render 2D** realista mediante IA, a partir de la imagen y unos parámetros de diseño (estilo, materiales, etc.).
3. El usuario **aprueba o rechaza** el resultado. Si lo rechaza, puede ajustar los parámetros y **regenerar** el render hasta quedar conforme.
4. Una vez aprobado, el sistema construye un **modelo 3D** del diseño usando el servicio de generación de TripoAI.
5. El usuario recibe **notificaciones** del avance y puede consultar el detalle de cada proyecto.

El backend expone una **API REST** que es consumida por un cliente web/móvil, gestiona la autenticación de usuarios, almacena los activos en la nube (Supabase Storage) y orquesta las integraciones con servicios externos de IA.

Este proyecto se desarrolló como **proyecto final de la asignatura Patrones y Estructuras**, por lo que pone especial énfasis en la aplicación rigurosa de **patrones de diseño de software** y en una arquitectura limpia y mantenible.

---

## 🎯 ¿Para quién va dirigido?

| Audiencia | Uso |
|-----------|-----|
| 👩‍🎨 **Diseñadores de interiores y arquitectos** | Prototipar rápidamente ideas de diseño a partir de fotos reales, sin necesidad de software CAD avanzado. |
| 🏠 **Propietarios y clientes finales** | Visualizar cómo quedaría una construcción o un mueble en su espacio antes de invertir. |
| 🧑‍💻 **Equipos de desarrollo / frontend** | Consumir una API REST documentada para construir aplicaciones web o móviles de diseño. |
| 🎓 **Estudiantes y docentes** | Estudiar una implementación real y bien estructurada de patrones de diseño aplicados sobre Spring Boot. |

---

## ✨ Características principales

- 🔐 **Autenticación y autorización** con JWT en cookies `HttpOnly`, registro, login y logout.
- 🛡️ **Autenticación de dos factores (2FA)** vía código enviado por correo.
- 🔑 **Recuperación de contraseña** mediante token temporal y correo de recuperación.
- 🖼️ **Generación de renders 2D** a partir de una imagen y parámetros de diseño.
- 🧊 **Generación de modelos 3D** asíncrona con polling de estado (integración TripoAI).
- 🏗️ Dos categorías de diseño: **arquitectura exterior** y **mobiliario / objetos**.
- 🗂️ **Gestión de proyectos** con versiones, parámetros y máquina de estados de 9 fases.
- 🔔 **Sistema de notificaciones** por usuario.
- 👤 **Perfil de usuario** con avatar.
- ☁️ **Almacenamiento en la nube** de imágenes y modelos mediante Supabase Storage.
- 📧 **Envío de correos** transaccionales (SMTP / Gmail).
- ⚙️ Manejo **global de excepciones** y respuestas de error estandarizadas.

---

## 🛠️ Tecnologías utilizadas

### Lenguaje y framework
- **Java 21**
- **Spring Boot 3.5.12**
  - Spring Web (API REST)
  - Spring WebFlux / `WebClient` (clientes HTTP reactivos para servicios externos)
  - Spring Data JPA + Hibernate
  - Spring Security
  - Spring Validation
  - Spring Mail
  - Spring Boot Actuator (monitoreo y *health checks*)

### Persistencia
- **PostgreSQL** (alojado en **Supabase**)
- **HikariCP** como *connection pool*

### Seguridad
- **JWT** (`jjwt` 0.11.5) en cookies `HttpOnly`
- **BCrypt** para el hashing de contraseñas
- **2FA** por correo
- **HSTS** y configuración de **CORS**

### Integraciones externas
- **TripoAI** — generación de imágenes 2D y modelos 3D
- **Supabase Storage** — almacenamiento de activos
- **JavaCV / FFmpeg** embebido — extracción de *frames* de video

### Utilidades y build
- **Lombok** — reducción de *boilerplate*
- **spring-dotenv** — carga de variables desde `.env`
- **Maven** — gestión de dependencias y *build*
- **Docker** — empaquetado y despliegue (multi-stage build)

---

## 🏛️ Arquitectura

El proyecto sigue una **arquitectura en capas (layered architecture)** con separación clara de responsabilidades, complementada con **módulos por dominio** (`ai`, `render3d`) que encapsulan las integraciones de IA.

```
┌──────────────────────────────────────────────────────────────┐
│                        Cliente (Web / Móvil)                   │
└───────────────────────────────┬──────────────────────────────┘
                                 │ HTTPS / REST + Cookie JWT
┌───────────────────────────────▼──────────────────────────────┐
│  Controller        →  Exponen los endpoints REST y validan     │
│  (REST Layer)         las peticiones entrantes (DTO + @Valid)  │
├───────────────────────────────────────────────────────────────┤
│  Service           →  Lógica de negocio, orquestación del flujo│
│  (Business Layer)     y transiciones de la máquina de estados  │
├───────────────────────────────────────────────────────────────┤
│  Integration       →  Clientes hacia servicios externos        │
│  (Integration)        (TripoAI, Supabase) vía WebClient        │
├───────────────────────────────────────────────────────────────┤
│  Repository        →  Acceso a datos con Spring Data JPA       │
│  (Persistence)        sobre PostgreSQL                         │
├───────────────────────────────────────────────────────────────┤
│  Model / Entity    →  Entidades JPA y enums del dominio        │
└───────────────────────────────────────────────────────────────┘
        ▲                    ▲                       ▲
   Security (JWT)       Config (CORS,           Exception
   Filtros + 2FA        WebClient, Async)     Handler global
```

**Principios y conceptos clave:**

- **Separación por capas:** `controller` → `service` → `repository` → `model`.
- **DTOs** para el transporte de datos: las entidades nunca se exponen directamente; se traducen mediante **Mappers** (`ProjectMapper`, `AiMapper`).
- **Módulos de dominio aislados:** el paquete `ai` (generación 2D y *prompts*) y `render3d` (generación 3D asíncrona) encapsulan sus *clients*, *DTOs*, validadores y servicios.
- **Procesamiento asíncrono:** la generación 3D se ejecuta en segundo plano (`@Async`) y el cliente consulta el progreso mediante *polling*.
- **Stateless:** la API no mantiene sesión en servidor; la identidad viaja en el JWT.

---

## 🧩 Patrones de diseño aplicados

Al tratarse de un proyecto de la asignatura *Patrones y Estructuras*, el diseño aplica varios patrones GoF y arquitectónicos de forma explícita:

| Patrón | Tipo | Dónde se aplica |
|--------|------|-----------------|
| **Builder** | Creacional | `Project.Builder` (construcción validada de entidades), y `@Builder` de Lombok en DTOs/entidades. |
| **Factory** | Creacional | `PromptBuilderFactory` y `ParametersValidatorFactory`: resuelven la implementación correcta según la `DesignCategory`. |
| **Strategy** | Comportamiento | `PromptBuilder` (`ExteriorArchitecturePromptBuilder`, `FurnitureItemPromptBuilder`) y `ParametersValidator`: distintas estrategias intercambiables según la categoría de diseño. |
| **Template Method** | Comportamiento | `BasePromptBuilder`: define el esqueleto de construcción de *prompts* y delega los pasos específicos en las subclases. |
| **State (máquina de estados)** | Comportamiento | `ProjectState`: gobierna las transiciones válidas del ciclo de vida del proyecto. |
| **Repository** | Arquitectónico | `ProjectRepository`, `UserRepository`, `NotificationRepository`, `GenerationTaskRepository` (Spring Data JPA). |
| **DTO + Mapper** | Estructural | Separación entre el modelo de dominio y los contratos de la API. |
| **Dependency Injection** | Arquitectónico | Inyección de dependencias por constructor en todo el proyecto (Spring + `@RequiredArgsConstructor`). |
| **Factory Method (Cookies)** | Creacional | `AuthCookieFactory`: centraliza la creación/limpieza de cookies de autenticación. |

---

## 🔄 Flujo del proyecto (máquina de estados)

Cada proyecto avanza por una máquina de estados de **9 fases** (`ProjectState`):

```
IMAGE_UPLOADED
      │  POST /generate-2d
      ▼
GENERATING_2D ─────────────► WAITING_2D_APPROVAL
                                   │            │
                          approve  │            │ reject
                                   ▼            ▼
                       WAITING_FINAL_APPROVAL  REJECTED_2D
                                   │                │ update params + regenerate
                       generate-3d │                ▼
                                   │      GENERATING_2D_WITH_PARAMS
                                   ▼                │
                          GENERATING_3D_MODEL ◄─────┘ (vuelve a aprobación)
                                   │
                                   ▼
                              COMPLETED

   (Cualquier fallo → FAILED   ·   Eliminación → DELETED [soft delete])
```

---

## 🌐 Endpoints de la API

> Base path: `/api/v1` — Todas las rutas (excepto `auth`) requieren autenticación vía cookie JWT.

### 🔐 Autenticación — `/api/v1/auth`
| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/register` | Registro con auto-login. |
| `POST` | `/login` | Inicio de sesión. |
| `POST` | `/verify-2fa` | Verificar código 2FA. |
| `POST` | `/logout` | Cerrar sesión (borra la cookie). |
| `POST` | `/toggle-2fa` | Activar / desactivar 2FA. |
| `POST` | `/forgot-password` | Solicitar recuperación de contraseña. |
| `POST` | `/reset-password` | Confirmar nueva contraseña con token. |

### 🗂️ Proyectos — `/api/v1/projects`
| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/` | Crear proyecto subiendo la imagen inicial. |
| `GET` | `/` | Listar proyectos del usuario. |
| `GET` | `/{id}` | Detalle de un proyecto. |
| `DELETE` | `/{id}` | Eliminar proyecto. |
| `POST` | `/{id}/generate-2d` | Generar render 2D. |
| `POST` | `/{id}/approve` | Aprobar diseño 2D. |
| `POST` | `/{id}/reject` | Rechazar diseño 2D. |
| `PUT` | `/{id}/parameters` | Actualizar parámetros tras un rechazo. |
| `POST` | `/{id}/regenerate-2d` | Regenerar render 2D con nuevos parámetros. |
| `POST` | `/{id}/generate-3d` | Generar modelo 3D. |
| `POST` | `/{id}/cancel-3d` | Cancelar la generación 3D. |

### 🧊 Render 3D — `/api/render`
| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/generate-3d` | Inicia la generación 3D (respuesta inmediata, `202 Accepted`). |
| `GET` | `/status/{taskId}` | Consulta el estado de la tarea (*polling*). |

### 👤 Usuarios — `/api/v1/users`
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/me` | Datos del usuario autenticado. |
| `PUT` | `/me/avatar` | Actualizar foto de perfil. |
| `DELETE` | `/me/avatar` | Eliminar foto de perfil. |

### 🔔 Notificaciones — `/api/v1/users/me/notifications`
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/` | Listar notificaciones. |
| `POST` | `/` | Crear notificación. |
| `PATCH` | `/read` | Marcar todas como leídas. |
| `DELETE` | `/` | Borrar todas las notificaciones. |

---

## ✅ Requisitos previos

- **Java JDK 21**
- **Maven 3.9+** (o usar el *wrapper* incluido `mvnw` / `mvnw.cmd`)
- Una base de datos **PostgreSQL** (por ejemplo, vía Supabase)
- Cuenta y credenciales de **Supabase** (Storage)
- **API Key de TripoAI**
- Credenciales SMTP (Gmail u otro) para el envío de correos
- *(Opcional)* **Docker** para empaquetado y despliegue

---

## 🔑 Variables de entorno

El proyecto carga la configuración desde un archivo `.env` en la raíz (vía `spring-dotenv`). Crea uno con las siguientes claves:

```env
# Base de datos (PostgreSQL / Supabase)
DB_URL=jdbc:postgresql://<host>:<puerto>/<database>
DB_USERNAME=<usuario>
DB_PASSWORD=<contraseña>

# JWT
JWT_SECRET=<clave-secreta-larga-y-segura>

# Supabase Storage
SUPABASE_URL=https://<tu-proyecto>.supabase.co
SUPABASE_KEY=<anon-key>
SUPABASE_SERVICE_ROLE_KEY=<service-role-key>
SUPABASE_BUCKET=<nombre-del-bucket>

# TripoAI
TRIPO_API_KEY=<tu-api-key>

# Correo (SMTP)
MAIL_USERNAME=<correo@gmail.com>
MAIL_PASSWORD=<app-password>

# CORS
ALLOWED_ORIGINS=http://localhost:3000,https://tu-frontend.com
```

> ⚠️ **Nunca** subas el archivo `.env` al repositorio. Asegúrate de que esté listado en `.gitignore`.

---

## 🚀 Instalación y ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/VictoriaArteaga/Proyecto-Final-Patrones-Backend.git
cd Proyecto-Final-Patrones-Backend

# 2. Crear el archivo .env con tus variables (ver sección anterior)

# 3. Ejecutar con el wrapper de Maven
#    Windows
./mvnw.cmd spring-boot:run
#    Linux / macOS
./mvnw spring-boot:run
```

La aplicación arrancará por defecto en **`http://localhost:8080`**.

El punto de entrada es la clase ejecutable:
`src/main/java/com/proyectofinal/backendapi/BackendapiApplication.java`

### Compilar el `.jar`

```bash
./mvnw clean package
java -jar target/backendapi-0.0.1-SNAPSHOT.jar
```

---

## 🐳 Ejecución con Docker

El proyecto incluye un **Dockerfile multi-stage** (build con Maven + runtime con JDK 21):

```bash
# Construir la imagen
docker build -t backendapi .

# Ejecutar el contenedor (pasando el archivo .env)
docker run --env-file .env -p 8080:8080 backendapi
```

---

## 📁 Estructura del proyecto

```
src/main/java/com/proyectofinal/backendapi/
├── BackendapiApplication.java        # Punto de entrada
│
├── ai/                               # Módulo de generación 2D con IA
│   ├── controller/                   # AiImageGenerationController
│   ├── dto/                          # DTOs de generación de renders
│   ├── integration/                  # TripoImageClient, resolución de templates
│   ├── mapper/                       # AiMapper
│   ├── prompt/                       # Strategy + Factory + Template Method (prompts)
│   ├── service/                      # AiImageGenerationService (+ impl)
│   └── validation/                   # Validadores de parámetros (Strategy + Factory)
│
├── render3d/                         # Módulo de generación 3D asíncrona
│   ├── config/                       # AsyncConfig
│   ├── controller/                   # RenderController
│   ├── dto/                          # Generate3DRequest, TaskStatusResponse
│   ├── entity/                       # GenerationTask, TaskStatus, TaskType
│   ├── integration/                  # TripoClient + repositorio de tareas
│   └── service/                      # RenderService
│
├── config/                           # CORS, Security, Supabase, WebClient
├── controller/                       # Auth, Project, User, Notification, Home
├── dto/                              # DTOs por dominio (auth, project, user, …)
├── exception/                        # Excepciones + GlobalExceptionHandler
├── mapper/                           # ProjectMapper
├── model/                            # Entidades JPA y enums (Project, User, …)
├── repository/                       # Repositorios Spring Data JPA
├── security/                         # JwtAuthFilter, JwtService, AuthCookieFactory
└── service/                          # Servicios de negocio (Project, User, Email, …)

src/main/resources/
└── application.yaml                  # Configuración de Spring Boot
```

---

## 🔒 Seguridad

- **Autenticación stateless con JWT** transportado en cookies `HttpOnly` (el token nunca queda expuesto a JavaScript).
- **Contraseñas** almacenadas con hashing **BCrypt**.
- **2FA opcional** mediante código de un solo uso enviado por correo.
- **Recuperación de contraseña** con token temporal y fecha de expiración.
- **CORS** configurable por variable de entorno (`ALLOWED_ORIGINS`).
- **HSTS** habilitado (fuerza HTTPS por 1 año, incluidos subdominios).
- **Política de sesión `STATELESS`** y CSRF deshabilitado (API sin estado).
- Filtro `JwtAuthFilter` que valida el token en cada petición protegida.

---

## 👩‍💻 Autores

Proyecto desarrollado para la asignatura **Patrones y Estructuras** (Cuarto Semestre).

- **María Victoria Arteaga** — [@VictoriaArteaga](https://github.com/VictoriaArteaga)

---

<p align="center">
  <sub>Construido con ❤️ usando Spring Boot · Proyecto Final — Patrones y Estructuras</sub>
</p>
