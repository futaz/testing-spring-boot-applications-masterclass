package de.rieckpil.courses.book.review;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static de.rieckpil.courses.book.review.RandomReviewParameterResolverExtension.RandomReview;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(RandomReviewParameterResolverExtension.class)
//@IndicativeSentencesGeneration(generator = DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewVerifierTest {

  private ReviewVerifier reviewVerifier;

  @BeforeEach
  void setup() {
    reviewVerifier = new ReviewVerifier();
  }

  @Test
  void shouldFailWhenReviewContainsSwearWord() {
    String review = "This book is shit";
    boolean result = reviewVerifier.doesMeetQualityStandards(review);
    assertFalse(result, "ReviewVerifier did not detect swear word");
  }

  @Test
  @DisplayName("Should fail when review contains 'lorem ipsum'")
  void testLoremIpsum() {
    String review = "This book is good as much as lorem ipsum does";
    boolean result = reviewVerifier.doesMeetQualityStandards(review);
    assertFalse(result, "ReviewVerifier did not detect swear word");
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/badReview.csv")
  void shouldFailWhenReviewIsOfBadQuality(String review) {
    final boolean result = reviewVerifier.doesMeetQualityStandards(review);
    assertFalse(result, "Verifier did not fail on bad review");
  }

  @RepeatedTest(5)
  void shouldFailWhenRandomReviewQualityIsBad(@RandomReview String review) {
    final boolean result = reviewVerifier.doesMeetQualityStandards(review);
    assertFalse(result, "Verifier did not fail on bad review");
  }

  @Test
  void shouldPassWhenReviewIsGood() {
    final String review = "I can totally recommend this book for those who interested in learning how to write Java code.";
    final boolean result = reviewVerifier.doesMeetQualityStandards(review);
    assertTrue(result, "Verifier did not pass a good review");
  }

  @Test
  void shouldPassWhenReviewIsGoodHamcrest() {
    String goodReview = """
      I can totally recommend this book for those who interested in learning how to write Java code.
      """;

    boolean result = reviewVerifier.doesMeetQualityStandards(goodReview);

    MatcherAssert.assertThat("ReviewVerifier did not pass a good review", result, Matchers.equalTo(true));
  }

  @Test
  void shouldPassWhenReviewIsGoodAssertJ() {
    String goodReview = "I can totally recommend this book for those who interested in learning how to write Java code.";
    boolean result = reviewVerifier.doesMeetQualityStandards(goodReview);

    Assertions
      .assertThat(result)
      .withFailMessage("ReviewVerifier did not pass a good review")
      .isTrue();
  }
}
