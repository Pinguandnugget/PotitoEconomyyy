# PotitoEconomy 0.3 (experimental)
Plugin puente Vault para DEDsafio4FAN 1.4.0 / Arclight 1.21.1.

## Compilar en GitHub
Sube TODO el contenido del ZIP a la raíz del repositorio (incluyendo `libs/Vault.jar` y `.github/workflows/build.yml`). Abre Actions y descarga el artefacto de compilación.

## Prueba segura
1. Haz copia de seguridad del mundo y `world/data/ruleta_dedsafio.dat`.
2. Apaga servidor, retira la versión anterior del plugin e instala el nuevo jar.
3. Deja `enable-transactions: false` inicialmente.
4. Ejecuta `/potitoeco estado`, `/potitoeco saldo` y `/vault-info`.
5. Verifica en consola que Vault usa PotitoEconomy. NO pruebes compras con el modo de solo lectura.
6. En servidor de pruebas, cambia `enable-transactions: true` y reinicia; usa una cuenta de prueba y una venta pequeña en AuctionHouse. Verifica ambos saldos antes/después y el vendedor desconectado.

**Avisos:** Sin pruebas en Arclight de esta versión; las operaciones Vault deben correr en hilo principal. El acceso al banco es por reflexión a nombres ofuscados del mod y puede romperse si se actualiza. Los precios con logos en AuctionHouse requieren cambios adicionales. `format()` todavía devuelve texto simple.

Permisos: `potitoeco.saldo` (todos), `potitoeco.admin` (OP).
