# SLIOR — Sistema Logístico Inteligente de Optimización de Rutas

1. Ejecutar `script.ps1` para configurar el JWT Secret (solo debe de realizarse la primera vez).
2. Dirigirse a la carpeta `contenedores`, y ejecutar el comando, en una terminal: `docker compose up -d --build`; asegurándonos que, al haber ejecutado el script.ps1, se haya creado un fichero .env.
3. Deberemos haber configurado nuestra app con nuestra IP de Tailscale de nuestro servidor (previamente instalado en nuestro dispositivo servidor y en nuestro dispositivo Android), configurándola en el archivo , en buildConfigField("String", "BASE_URL", "\"IP_DEL_SERVIDOR_EN_TAILSCALE"") (P.EJ: buildConfigField("String", "BASE_URL", "\"http://100.115.5.3:8080/\""))

- Para cualquier duda sobre arquitectura, funcionamiento y/o instalación, referirse al documento `SLIOR_Memoria_TFG.pdf`

## Licencia

Este proyecto está bajo la Licencia **MIT**. Consulta el archivo `LICENSE` (si existe) o el Anexo B de la memoria para más detalles.

---
**Autor:** José Ramos Contioso  
**Centro:** I.E.S. La Marisma (Huelva)  
**Año:** 2026
