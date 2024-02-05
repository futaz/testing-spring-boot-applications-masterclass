package de.rieckpil.courses.book.review;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest(properties = {
  "spring.flyway.enabled=false",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "spring.datasource.driver-class-name=com.p6spy.engine.spy.P6SpyDriver", // P6Spy
  "spring.datasource.url=jdbc:p6spy:h2:mem:testing;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false" // P6Spy
})
/**
 * @see spy.properties
 */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // P6Spy miatt fent megadtuk a DS-t, ezért nem kell azt automatán létrehozni
class ReviewRepositoryTest {

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private ReviewRepository cut;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private TestEntityManager testEntityManager;

  @BeforeEach
  void beforEach() {
    // Default behavior is rolling back every change after a test execution, so every test should start on an empty database
    assertEquals(0, cut.count());
  }

  @Test
  void notNull() throws SQLException {
    assertNotNull(entityManager);
    assertNotNull(cut);
    assertNotNull(dataSource);
    assertNotNull(testEntityManager);

    System.out.println("Database used by test is " + dataSource.getConnection().getMetaData().getDatabaseProductName());

    Review review = new Review();
    review.setBook(null);
    review.setContent("Great book!");
    review.setRating(4);
    review.setUser(null);
    review.setTitle("Great!");
    review.setCreatedAt(LocalDateTime.now());

    cut.save(review);

    assertNotNull(review.getId());
  }

  @Test
  void transactionalSupportTest() {
    Review review = new Review();
    review.setBook(null);
    review.setContent("Great book!");
    review.setRating(4);
    review.setUser(null);
    review.setTitle("Great!");
    review.setCreatedAt(LocalDateTime.now());

    cut.save(review);

    assertNotNull(review.getId());
  }
}
