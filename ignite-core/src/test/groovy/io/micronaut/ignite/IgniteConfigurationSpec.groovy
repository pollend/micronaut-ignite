package io.micronaut.ignite

import io.micronaut.context.ApplicationContext
import io.micronaut.ignite.configuration.DefaultCacheConfiguration
import io.micronaut.ignite.configuration.DefaultIgniteConfiguration
import io.micronaut.inject.qualifiers.Qualifiers
import org.apache.ignite.Ignite
import org.apache.ignite.cache.QueryEntity
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.communication.CommunicationSpi
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.AutoCleanup
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@Retry
class IgniteConfigurationSpec extends Specification {
    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared @AutoCleanup
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(10800)

    def "test ignite client instance is created"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"    : true,
            "ignite.communication-spi.local-port": "${ignite.getMappedPort(10800)}",
            "ignite.discovery-spi.ip-finder-type": "STATIC",
            "ignite.discovery-spi.static-ip-finder.addresses[0]": "127.0.0.1:47500",
            "ignite.discovery-spi.static-ip-finder.addresses[0]": "127.0.0.1:47501",
        ])
        when:
        Ignite inst = ctx.getBean(Ignite.class)
        IgniteConfiguration cfg = ctx.getBean(IgniteConfiguration.class);

        then:
        inst != null
        cfg.clientMode

        TcpDiscoveryIpFinder ipFinder = ((TcpDiscoverySpi)cfg.discoverySpi).ipFinder;
        ipFinder instanceof TcpDiscoveryVmIpFinder
        ipFinder.registeredAddresses.contains("127.0.0.1:45700") || ipFinder.registeredAddresses.contains("127.0.0.1:45701")

        cleanup:
        ctx.close()
    }

}
