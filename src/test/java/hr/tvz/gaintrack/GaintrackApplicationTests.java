package hr.tvz.gaintrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GaintrackApplicationTests {

	private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18");

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer(POSTGRES_IMAGE);

	@Test
	void contextLoads() {
	}

}
