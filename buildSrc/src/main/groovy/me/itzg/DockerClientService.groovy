package me.itzg

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class DockerClientService implements BuildService<Params>, Closeable {
    final DockerClient client

    interface Params extends BuildServiceParameters {}

    DockerClientService() {
        final DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();

        final ZerodepDockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        client = DockerClientImpl.getInstance(config, httpClient)
    }

    @Override
    void close() throws IOException {
        println 'Closing DockerClientService'
        client.close()
    }
}
