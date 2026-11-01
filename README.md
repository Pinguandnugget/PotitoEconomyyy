# PotitoEconomy Probe 0.1.0

Prueba **solo lectura** para Minecraft 1.21.1 / Java 21 / Arclight.
No integra Vault ni modifica saldos.

## GitHub Actions
1. Crea un repositorio vacío.
2. Sube **el contenido de esta carpeta**, incluyendo `.github/workflows/build.yml`, a la raíz del repositorio.
3. Abre Actions > Compilar PotitoEconomy Probe > Run workflow, o espera el build del push.
4. Descarga el artifact `PotitoEconomyProbe-jar`, descomprímelo y extrae el `.jar`.
5. Con copia de seguridad y en un servidor de prueba, coloca el `.jar` en `plugins/` y reinicia.
6. Prueba `/potitoeco estado` y `/potitoeco saldo` como OP.

El acceso a clases del mod desde el classloader Bukkit **no está garantizado**. Si falla, comparte el error del log para adaptar el puente.

No cambies todavía la economía de Vault.


## v0.2 - Logos originales
`/potitoeco saldo` desglosa el saldo en deditas rojas (10000), verdes (100) y azules (1) con la fuente `ruleta:monedas` del mod. Solo lectura. Requiere cliente con los recursos del mod.
