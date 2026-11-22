package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.PostgresContainerConfig;
import com.onurbcd.cli.config.JpaConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Repository slice against a real PostgreSQL (Flyway schema, {@code ddl-auto=validate}). Each test runs in a
 * transaction that is rolled back, so tests stay independent.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresContainerConfig.class, JpaConfig.class})
abstract class AbstractRepositoryIT {

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected EntityManager em;

    protected RepositoryFixtures fixtures;

    @BeforeEach
    void initFixtures() {
        fixtures = new RepositoryFixtures(jdbc);
    }

    /**
     * Drops the persistence context so the next repository read goes to the database.
     */
    protected void clear() {
        em.flush();
        em.clear();
    }
}
