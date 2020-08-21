package io.micronaut.ignite

import io.micronaut.context.ApplicationContext
import org.apache.ignite.client.IgniteClient
import org.apache.ignite.client.SslMode
import org.apache.ignite.configuration.ClientConfiguration
import org.apache.ignite.transactions.TransactionConcurrency
import org.apache.ignite.transactions.TransactionIsolation
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@Retry
class IgniteThinClientConfigurationSpec extends Specification {
    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(10800)

    void "test ignite thin client instance is created"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite-thin.enabled"                  : true,
            "ignite-thin.addresses": ["127.0.0.1:${ignite.getMappedPort(10800)}"],
        ])

        when:
        IgniteClient instance = ctx.getBean(IgniteClient.class)

        then:
        instance != null
    }

    void "test ignite thin client configuration"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
            "ignite-thin.enabled"                                                  : true,
            "ignite-thin.addresses"                                : ["localhost:1080"],
            "ignite-thin.ssl-mode"                                 : "REQUIRED",
            "ignite-thin.ssl-client-certificate-key-store-password": "password",
            "ignite-thin.timeout"                                  : 5000,
            "ignite-thin.send-buffer-size"                         : 200
        ])
        when:
        ClientConfiguration configuration = ctx.getBean(ClientConfiguration.class)

        then:
        configuration != null
        configuration.sslMode == SslMode.REQUIRED
        configuration.sslClientCertificateKeyStorePassword == "password"
        configuration.timeout == 5000
        configuration.sendBufferSize == 200
    }


    void "test ignite thin client transaction configuration"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
            "ignite-thin.enabled"                                                         : true,
            "ignite-thin.addresses"                                       : ["localhost:1080"],
            "ignite-thin.transaction-configuration.default-tx-isolation"  : "REPEATABLE_READ",
            "ignite-thin.transaction-configuration.default-tx-concurrency": "PESSIMISTIC",
            "ignite-thin.transaction-configuration.default-tx-timeout"    : 5000,
        ])
        when:
        ClientConfiguration clientConfiguration = ctx.getBean(ClientConfiguration.class)

        then:
        clientConfiguration != null
        clientConfiguration.transactionConfiguration.defaultTxIsolation == TransactionIsolation.REPEATABLE_READ
        clientConfiguration.transactionConfiguration.defaultTxTimeout == 5000
        clientConfiguration.transactionConfiguration.defaultTxConcurrency == TransactionConcurrency.PESSIMISTIC
    }
}
