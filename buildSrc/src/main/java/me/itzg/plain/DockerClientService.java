package me.itzg.plain;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import java.io.IOException;
import me.itzg.plain.DockerClientService.Params;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

public abstract class DockerClientService implements BuildService<Params>, AutoCloseable {

    private final DockerClient client;

    interface Params extends BuildServiceParameters {

    }

    public DockerClientService() {
        final DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .build();

        final ZerodepDockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .build();

        client = DockerClientImpl.getInstance(config, httpClient);
    }

    public DockerClient getClient() {
        return client;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
