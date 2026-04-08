# 📱 Aplicación de Consulta por Voz de Sucursales

## 🧩 Descripción General

Esta aplicación móvil desarrollada en Android permite a los usuarios consultar, mediante comandos de voz, las direcciones IP de los dispositivos asociados a distintas sucursales.

El usuario simplemente dice el nombre de una sucursal y la aplicación responde:

- 🔊 Por voz (síntesis de audio)
- 📺 Mostrando la información en pantalla

---

## 🌿 Branches del Proyecto

Este repositorio contiene dos versiones principales:

### 🔹 `Button`
- La escucha de voz se activa mientras el usuario mantiene presionado un botón en pantalla.

### 🔹 `KeyWord`
- La aplicación se mantiene en espera hasta detectar la palabra clave **"net"**, tras lo cual inicia el reconocimiento de voz.

> ⚠️ Ambas versiones comparten toda la lógica y funcionalidades, diferenciándose únicamente en la forma de activar la escucha.

---

## 🚀 Funcionalidades

### 👤 Usuario general
- Consulta de sucursales mediante voz
- Respuesta por voz y visualización en pantalla
- Interfaz sencilla e intuitiva

### 🔐 Administrador
Acceso mediante:
- Usuario: `ss`
- Contraseña: `securitysofware`

Funciones disponibles:
- ➕ Agregar sucursales
- ✏️ Editar sucursales
- ❌ Eliminar sucursales
- 📡 Gestionar dispositivos e IPs asociadas

---

## 💾 Importación y Exportación de Datos

La aplicación permite manejar la base de datos de forma portable:

### 📤 Exportar
- Genera un archivo en formato JSON con todos los datos actuales

### 📥 Importar
- Carga un archivo JSON previamente exportado
- ⚠️ **IMPORTANTE:** Al importar, se eliminan todos los datos existentes y se reemplazan por los del archivo

---

## 🛠️ Tecnologías Utilizadas

- Kotlin
- Java
- Android SDK
- Google Speech Recognizer API

---

## 🌐 Requisitos

- Conexión a internet (necesaria para el reconocimiento de voz)
- Dispositivo Android compatible

---

## ⚙️ Instalación

1. Descargar el archivo APK
2. Habilitar instalación desde fuentes desconocidas
3. Instalar en el dispositivo

---

## 📌 Notas

- El reconocimiento de voz depende del servicio de Google, por lo que su precisión puede variar.
- Se recomienda probar en un entorno con poco ruido para mejores resultados.

---

## 👨‍💻 Autor

Proyecto desarrollado como solución de consulta rápida de infraestructura de red mediante voz.

---

## 📄 Licencia

Uso interno: Security Software
