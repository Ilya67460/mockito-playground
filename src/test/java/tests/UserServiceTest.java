/**
 * Юнит-тесты для UserService с использованием Mockito.
 *
 * ВАЖНО: это НЕ E2E-тесты (в отличие от SecurityTest, RegisterTest и т.д.).
 * Здесь мы тестируем ТОЛЬКО логику UserService в изоляции от базы данных.
 * UserRepository — это «заглушка» (mock), которая не ходит в реальную БД.
 *
 * В проекте также есть E2E-тесты на Playwright,
 * где моки НЕ используются, потому что там тестируется реальная система.
 */

package tests;

import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pages.DatabaseException;
import pages.UserRepository;
import pages.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Epic("Unit Tests")
@Feature("UserService")
@Story("Greeting logic")
@Owner("Ilya Testinov")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<Integer> userIdCaptor;

    // ─────────────────────────────────────────────────────────────
    // ВСПОМОГАТЕЛЬНЫЕ ШАГИ (для Allure)
    // ─────────────────────────────────────────────────────────────

    @Step("Настроить мок: findUsernameById({id}) вернёт '{name}'")
    private void mockUsername(int id, String name) {
        when(userRepository.findUsernameById(id)).thenReturn(name);
    }

    @Step("Настроить мок: findUsernameById({id}) бросит DatabaseException")
    private void mockDatabaseFailure(int id) {
        when(userRepository.findUsernameById(id))
                .thenThrow(new DatabaseException("DB is down"));
    }

    @Step("Проверить, что приветствие равно '{expected}'")
    private void assertGreeting(String expected, String actual) {
        assertEquals(expected, actual);
    }

    // ─────────────────────────────────────────────────────────────
    // ТЕСТЫ
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UserService: возвращает приветствие с именем пользователя")
    @Description("Проверяет, что UserService возвращает 'Hello, Alice!' при наличии пользователя")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnGreetingWithUsername() {
        mockUsername(1, "Alice");

        String result = userService.getGreeting(1);

        assertGreeting("Hello, Alice!", result);
        verify(userRepository, times(1)).findUsernameById(1);

        Allure.addAttachment("Result", "text/plain", result, "txt");
    }

    @Test
    @DisplayName("UserService: возвращает приветствие гостя, если пользователь не найден")
    @Description("Проверяет, что UserService возвращает 'Hello, guest!' при отсутствии пользователя")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnGuestGreetingWhenUserNotFound() {
        mockUsername(999, null);

        String result = userService.getGreeting(999);

        assertGreeting("Hello, guest!", result);
        verify(userRepository).findUsernameById(999);

        Allure.addAttachment("Result", "text/plain", result, "txt");
    }

    @Test
    @DisplayName("UserService: возвращает приветствие гостя при ошибке БД")
    @Description("Проверяет, что UserService обрабатывает DatabaseException и возвращает fallback")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnGuestGreetingWhenDatabaseFails() {
        mockDatabaseFailure(1);

        String result = userService.getGreeting(1);

        assertGreeting("Hello, guest!", result);
        verify(userRepository).findUsernameById(1);

        Allure.addAttachment("Result", "text/plain", result, "txt");
        Allure.addAttachment("Exception", "text/plain",
                "DatabaseException: DB is down", "txt");
    }

    @Test
    @DisplayName("UserService: передаёт нормализованный ID в репозиторий")
    @Description("Проверяет, что UserService передаёт Math.abs(userId) в UserRepository")
    @Severity(SeverityLevel.MINOR)
    void shouldPassNormalizedIdToRepository() {
        when(userRepository.findUsernameById(anyInt())).thenReturn("Alice");

        userService.getGreeting(-1);

        verify(userRepository).findUsernameById(userIdCaptor.capture());
        assertEquals(1, userIdCaptor.getValue());

        Allure.addAttachment("Captured argument", "text/plain",
                "findUsernameById called with: " + userIdCaptor.getValue(), "txt");
    }

    @Test
    @DisplayName("UserService: возвращает разные приветствия для разных пользователей")
    @Description("Проверяет, что thenAnswer динамически возвращает имя по ID")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnGreetingForDifferentUsers() {
        when(userRepository.findUsernameById(anyInt())).thenAnswer(invocation -> {
            int id = invocation.getArgument(0);
            switch (id) {
                case 1: return "Alice";
                case 2: return "Bob";
                case 3: return "Charlie";
                default: return null;
            }
        });

        assertEquals("Hello, Alice!", userService.getGreeting(1));
        assertEquals("Hello, Bob!", userService.getGreeting(2));
        assertEquals("Hello, Charlie!", userService.getGreeting(3));
        assertEquals("Hello, guest!", userService.getGreeting(999));

        Allure.addAttachment("Result", "text/plain",
                "Alice, Bob, Charlie, guest — all OK", "txt");
    }

    @Test
    @DisplayName("UserService: spy() вызывает реальный метод, но подменяет один")
    @Description("Проверяет, что spy() позволяет подменить только getGreeting(1)")
    @Severity(SeverityLevel.MINOR)
    void spyShouldCallRealMethodButStubOne() {
        UserService spyService = spy(new UserService(userRepository));

        doReturn("Hello, Spy!").when(spyService).getGreeting(1);

        assertEquals("Hello, Spy!", spyService.getGreeting(1));
        verify(spyService).getGreeting(1);

        Allure.addAttachment("Result", "text/plain",
                "spyService.getGreeting(1) = Hello, Spy!", "txt");
    }
}