import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import org.apache.ignite.Ignite
import org.apache.ignite.client.IgniteClient
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@Retry
class IgniteThinClientConfiguration extends Specification {
    @Shared
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:2.8.0")
        .withExposedPorts(10800)

    void "test ignite client instance is created"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"                  : true,
            "ignite.thin-client.default.addresses": ["127.0.0.1:${ignite.getMappedPort(10800)}"],
            "ignite.thin-client.other.addresses"  : ["127.0.0.1:${ignite.getMappedPort(10800)}"]
        ])
        when:
        IgniteClient defaultInstance = ctx.getBean(IgniteClient.class, Qualifiers.byName("default"))
        IgniteClient otherInstance = ctx.getBean(IgniteClient.class, Qualifiers.byName("other"))

        then:
        defaultInstance != null
        otherInstance != null
    }
}
