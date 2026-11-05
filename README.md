markdown
# 🧪 Mockito Playground — юнит-тесты на Java

Учебный проект для демонстрации основных приёмов **Mockito** в изоляции от реальной базы данных.

Проект показывает, как тестировать бизнес-логику (`UserService`) с помощью моков (`UserRepository`), не поднимая БД и не завися от внешних сервисов.

---

## 📊 Что покрыто (6 тестов)

| № | Тест | Приём Mockito | Что проверяет |
|---|------|---------------|---------------|
| 1 | `shouldReturnGreetingWithUsername` | `thenReturn` | «Hello, Alice!» при наличии пользователя |
| 2 | `shouldReturnGuestGreetingWhenUserNotFound` | `thenReturn(null)` | «Hello, guest!» при отсутствии пользователя |
| 3 | `shouldReturnGuestGreetingWhenDatabaseFails` | `thenThrow` | Fallback при `DatabaseException` |
| 4 | `shouldPassNormalizedIdToRepository` | `@Captor` | Перехват аргумента (`Math.abs(-1) = 1`) |
| 5 | `shouldReturnGreetingForDifferentUsers` | `thenAnswer` | Динамический ответ (разные имена по ID) |
| 6 | `spyShouldCallRealMethodButStubOne` | `spy()` | Частичный мок (реальный объект + подмена метода) |

---

## 🛠️ Стек

- **Java 22**
- **JUnit 5** — тест-раннер
- **Mockito** — моки и стабы
- **Maven** — сборка

---

## 🚀 Как запустить

### 1. Клонировать репозиторий

```bash
git clone https://github.com/Ilya67460/mockito-playground.git
cd mockito-playground
2. Запустить тесты
bash
mvn clean test
Ожидаемый результат: 6 зелёных тестов.

📁 Структура проекта
text
mockito-playground/
├── src/
│   ├── main/java/com/example/
│   │   ├── UserRepository.java      # интерфейс (зависимость)
│   │   ├── UserService.java         # класс, который тестируем
│   │   └── DatabaseException.java   # имитация ошибки БД
│   └── test/java/com/example/
│       └── UserServiceTest.java     # 6 тестов с Mockito
├── pom.xml
└── README.md
🎯 Ключевые приёмы Mockito
1. thenReturn — фиксированный ответ
java
when(userRepository.findUsernameById(1)).thenReturn("Alice");
String result = userService.getGreeting(1);
assertEquals("Hello, Alice!", result);
2. thenThrow — имитация исключения
java
when(userRepository.findUsernameById(1))
    .thenThrow(new DatabaseException("DB is down"));
String result = userService.getGreeting(1);
assertEquals("Hello, guest!", result); // fallback
3. @Captor — перехват аргументов
java
@Captor
private ArgumentCaptor<Integer> userIdCaptor;

// ...
verify(userRepository).findUsernameById(userIdCaptor.capture());
assertEquals(1, userIdCaptor.getValue()); // Math.abs(-1) = 1
4. thenAnswer — динамический ответ
java
when(userRepository.findUsernameById(anyInt())).thenAnswer(invocation -> {
    int id = invocation.getArgument(0);
    switch (id) {
        case 1: return "Alice";
        case 2: return "Bob";
        case 3: return "Charlie";
        default: return null;
    }
});
5. spy() — частичный мок
java
UserService spyService = spy(new UserService(userRepository));
doReturn("Hello, Spy!").when(spyService).getGreeting(1);
assertEquals("Hello, Spy!", spyService.getGreeting(1));
⚠️ Для spy() используй doReturn().when(), а не when().thenReturn() — иначе вызовется реальный метод.


Создание моков через @Mock и @InjectMocks.

Настройка поведения: thenReturn, thenThrow, thenAnswer.

Проверка взаимодействий: verify(), times(), @Captor.

Разница между mock() (полная заглушка) и spy() (реальный объект + подмена).

Изоляция бизнес-логики от БД без поднятия реальной инфраструктуры.


🔗 Ссылки
Mockito Documentation

JUnit 5 User Guide
