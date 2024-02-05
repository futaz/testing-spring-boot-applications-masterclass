package de.rieckpil.courses.book.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookSynchronizationListenerTest {

  private final static String VALID_ISBN = "1234567891234";

  @Mock
  private BookRepository bookRepository;

  @Mock
  private OpenLibraryApiClient openLibraryApiClient;

  @InjectMocks
  private BookSynchronizationListener cut; // class under test

  @Captor
  private ArgumentCaptor<Book> bookArgumentCaptor;

  @Test
  void shouldRejectBookWhenIsbnIsMalformed() {
    BookSynchronization sync = new BookSynchronization("42");
    cut.consumeBookUpdates(sync);
    verifyNoInteractions(openLibraryApiClient, bookRepository);
  }

  @Test
  void shouldNotOverrideWhenBookAlreadyExists() {
    BookSynchronization sync = new BookSynchronization("1234567890123");
    when(bookRepository.findByIsbn("1234567890123")).thenReturn(new Book());
    cut.consumeBookUpdates(sync);
    verifyNoInteractions(openLibraryApiClient);
    verify(bookRepository, times(0)).save(ArgumentMatchers.any());
  }

  @Test
  void shouldThrowExceptionWhenProcessingFails() {
    BookSynchronization sync = new BookSynchronization("1234567890123");
    when(bookRepository.findByIsbn("1234567890123")).thenReturn(null);
    when(openLibraryApiClient.fetchMetadataForBook("1234567890123")).thenThrow(new RuntimeException("Network timeout"));
    assertThrows(RuntimeException.class, () -> cut.consumeBookUpdates(sync));
  }

  @Test
  void shouldStoreBookWhenNewAndCorrectIsbn() {
    BookSynchronization sync = new BookSynchronization(VALID_ISBN);
    Book book = new Book();
    book.setTitle("Java book");
    book.setIsbn(VALID_ISBN);

    when(bookRepository.findByIsbn(VALID_ISBN)).thenReturn(null);
    when(openLibraryApiClient.fetchMetadataForBook(VALID_ISBN)).thenReturn(book);
    when(bookRepository.save(ArgumentMatchers.any())).then(invocation -> {
      Book methodArg = invocation.getArgument(0);
      methodArg.setId(1L);
      return methodArg;
    });

    cut.consumeBookUpdates(sync);

    verify(openLibraryApiClient).fetchMetadataForBook(VALID_ISBN);
    verify(bookRepository).save(bookArgumentCaptor.capture());

    assertEquals("Java book", bookArgumentCaptor.getValue().getTitle());
    assertEquals(VALID_ISBN, bookArgumentCaptor.getValue().getIsbn());
  }

}
