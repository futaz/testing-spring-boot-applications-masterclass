package de.rieckpil.courses.book.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.rieckpil.courses.config.WebSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
// When using a SecurityFilterChain bean to configure Spring Security (see deprecation of the
// WebSecurityConfigurerAdapater), we must explicitly import the security configuration with
// @Import(WebSecurityConfig.class), as this bean is no longer auto-configured for us when using @WebMvcTest.
// see https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#migrating-from-websecurityconfigureradapter-to-securityfilterchain
@Import(WebSecurityConfig.class)
class ReviewControllerTest {

  @MockBean
  private ReviewService reviewService;

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @BeforeEach
  void beforeEach() {
    objectMapper = new ObjectMapper();
  }

  @Test
  void shouldReturnTwentyReviewsWithoutAnyOrderWhenNoParametersAreSpecified() throws Exception {
    final ArrayNode array = objectMapper.createArrayNode();

    final ObjectNode book = objectMapper.createObjectNode();
    book.put("bookId", 1);
    book.put("isbn", "42");
    book.put("avg", 89.3);
    book.put("ratings", 2);

    array.add(book);

    when(reviewService.getAllReviews(20, "none")).thenReturn(array);

    mockMvc
      .perform(get("/api/books/reviews"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.size()", is(1)));
  }

  @Test
  void shouldNotReturnReviewStatisticsWhenUserIsUnauthenticated() throws Exception {
    mockMvc
      .perform(get("/api/books/reviews/statistics"))
      .andExpect(status().isUnauthorized())
      .andDo(print());
  }

  @Test
  @WithMockUser(username = "duke") // spring-security-test is required
  void shouldReturnReviewStatisticsWhenUserIsAuthenticated() throws Exception {
    mockMvc
      .perform(get("/api/books/reviews/statistics"))
      .andExpect(status().isOk())
      .andDo(print());
  }

  @Test
  void shouldCreateNewBookReviewForAuthenticatedUserWithValidPayload() throws Exception {
  }

  @Test
  void shouldRejectNewBookReviewForAuthenticatedUsersWithInvalidPayload() throws Exception {
  }

  @Test
  void shouldNotAllowDeletingReviewsWhenUserIsAuthenticatedWithoutModeratorRole() throws Exception {
  }

  @Test
  @WithMockUser(roles = "moderator")
  void shouldAllowDeletingReviewsWhenUserIsAuthenticatedAndHasModeratorRole() throws Exception {
  }
}
