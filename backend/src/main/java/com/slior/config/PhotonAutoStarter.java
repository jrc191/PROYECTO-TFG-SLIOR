package com.slior.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class PhotonAutoStarter implements CommandLineRunner {

    @Value("${photon.autostart.enabled:true}")
    private boolean autostartEnabled;

    @Value("${photon.jar-path:photon/photon-1.0.1.jar}")
    private String jarPath;

    @Value("${photon.data-path:photon_data}")
    private String dataPath;

    @Value("${photon.port:2322}")
    private int photonPort;

    @Value("${photon.max-memory:-Xmx2G}")
    private String maxMemory;

    private Process photonProcess;

    @Override
    public void run(String... args) {
        if (!autostartEnabled) {
            log.info("Photon autostart desactivado (photon.autostart.enabled=false)");
            return;
        }

        if (isPortOpen("127.0.0.1", photonPort)) {
            log.info("Photon ya está escuchando en el puerto {}", photonPort);
            return;
        }

        Path jar = Paths.get(jarPath).toAbsolutePath();
        Path dataDir = Paths.get(dataPath).toAbsolutePath();

        if (!Files.exists(jar)) {
            log.warn("No se encontró el JAR de Photon en {}", jar);
            return;
        }
        if (!Files.exists(dataDir)) {
            log.warn("No se encontró el directorio de datos de Photon en {}", dataDir);
            return;
        }

        List<String> command = List.of(
                "java",
                maxMemory,
                "-jar", jar.toString(),
                "serve",
                "-data-dir", dataDir.toString(),
                "-listen-ip", "127.0.0.1",
                "-listen-port", String.valueOf(photonPort)
        );

        try {
            File workingDir = jar.getParent() != null ? jar.getParent().toFile() : new File(".");
            photonProcess = new ProcessBuilder(command)
                    .directory(workingDir)
                    .redirectErrorStream(true)
                    .redirectOutput(new File("photon.log"))
                    .start();

            waitForPort(Duration.ofSeconds(15));
            log.info("Photon arrancado en http://127.0.0.1:{} (log en photon.log)", photonPort);
        } catch (IOException e) {
            log.error("No se pudo arrancar Photon con el comando {}", command, e);
        }
    }

    private void waitForPort(Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (isPortOpen("127.0.0.1", photonPort)) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.warn("No se confirmó el arranque de Photon antes de que expirara el timeout");
    }

    private boolean isPortOpen(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 500);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @PreDestroy
    public void stopPhoton() {
        if (photonProcess != null && photonProcess.isAlive()) {
            photonProcess.destroy();
            log.info("Photon detenido");
        }
    }
}
