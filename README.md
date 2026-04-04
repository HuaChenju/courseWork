## Data Aggregator
Консольное приложение для агрегации данных из нескольких REST API с возможностью параллельного выполнения запросов и сохранения результатов в файл.
# Описание
## Функциональность:
- Получение данных из нескольких API
- Параллельный опрос источников
- Ограничение количества потоков
- Настройка интервала запросов
- Поддержка форматов: JSON и  CSV
- Два режима работы:  автоматический и интерактивный
## Технологии:
- JUnit + Mockito
- Maven
- Jackson
- HTTP, REST API
- java.util.concurrent (ExecutorService)
## Используемые API
- bible-api.com
- api.github.com
- api.jikan.moe
# Запуск
git clone https://github.com/HuaChenju/java-data-aggregator

cd java-data-aggregator

mvn clean install

java -jar target/CourseWork-1.0-SNAPSHOT.jar

# Тестирование
mvn test
