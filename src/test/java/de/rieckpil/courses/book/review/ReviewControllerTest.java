package de.rieckpil.courses.book.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.rieckpil.courses.config.WebSecurityConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    verifyNoInteractions(reviewService);
  }

  @Test
//  @WithMockUser(username = "duke") // spring-security-test is required
  void shouldReturnReviewStatisticsWhenUserIsAuthenticated() throws Exception {
    mockMvc
      .perform(
        get("/api/books/reviews/statistics")
//          .with(SecurityMockMvcRequestPostProcessors.user("duke"))
          .with(jwt())
      )
      .andExpect(status().isOk())
      .andDo(print());

    verify(reviewService).getReviewStatistics();
  }

  @Test
  void shouldCreateNewBookReviewForAuthenticatedUserWithValidPayload() throws Exception {
    String requestBody = """
      {
        "reviewTitle": "Great Java Book!",
        "reviewContent": "I really like this book!",
        "rating": 4
      }
      """;

    when(reviewService.createBookReview(eq("42"), any(BookReviewRequest.class), eq("duke"), eq("duke@example.com"))).thenReturn(100L);

    mockMvc
      .perform(
        post("/api/books/{isbn}/reviews", 42)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody)
          .with(jwt().jwt(builder -> builder
            .claim("preferred_username", "duke")
            .claim("email", "duke@example.com"))))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", Matchers.containsString("/books/42/reviews/100")));
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
