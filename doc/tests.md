## 9. Тесты

### 9.1. ClientServiceTest
Тестирование CRUD операций ClientService на отдельной базе данных. Перед запуском теста необходимо создать базу данных
crmtest, на которой будет происходить тестирование, настройки хранятся в файле 
[test/resources/application-test.properties](../src/test/resources/application-test.properties), если убрать (properties = "spring.profiles.active=test") в аннотации 
@SpringBootTest - тестирование произодет на реальной базе проекта.
Проверяет:
* равен ли созданный клиент клиенту, добавленному в базу методом add() клиент-сервиса,
* равен ли созданный клиент клиенту, прочитанному из базы методом getClientByID() клиент-сервиса,
* равен ли измененный клиент клиенту, прочитанному из базы после работы метода updateClient() клиент-сервиса,
* равен ли удаленный клиент null после работы метода delete() клиент-сервиса.

### 9.2. ClientServiceTestsMocked
Тестирование CRUD операций СlientService с заглушенным при помощи mockito репозиторием СlientRepository без использования базы данных.
Проверяет, происходит ли делегирование в методах add(), getClientByID(), update() и delete() клиент-сервиса методам saveAndFlush(), findById(), 
saveAndFlush(), delete() клиент-репозитория соответственно.

### 9.3. HrDtoForBoardTest
Проверяет:
* корректность расчета среднего количества звонков в день на конкретного HR,
* корректность расчета количества карточек клиентов, назначенных конкретному HR,
* корректность расчета общего количества звонков для конкретного HR.

### 9.4. ParseMailToClientTest
Проверяет корректность создания и сохранения клиента в базе данных при парсинге email с заявкой на обучение с сайта 
java-mentor.com.

### 9.5. StudentServiceTest
Тестирование методов StudentService на отдельной базе данных. Перед запуском теста создать базу данных crmtest, на 
которой будет происходить тестирование, настройки хранятся в файле [test/resources/application-test.properties](../src/test/resources/application-test.properties), если 
убрать (properties = "spring.profiles.active=test"), то тестирование произодет на реальной базе проекта.
Проверяет:
* корректность сохранения студента в базу,
* корректность получения студента из базы по по клиентскому id,
* корректность добавления студента для клиента, используя данные из projectProperties и клиентского статуса,
* корректность получения студента из базы по его email,
* корректность получения из базы студентов, с включенными уведомлениями и с сегодняшней датой об оплате,
* корректность получения из базы студентов, имеющих статус с указанным id, 
* корректность получения из базы всех студентов, имеющих не менее одного социального профиля, за исключением тех 
студентов, которые имеют единственный социальный профиль указанного типа,
* корректность отделения сущности студента от pesistence context,
* корректность сброса цвета для студента.

### 9.6. StudentServiceTestsMocked 
Тестирование методов StudentService с заглушенным репозиторием studentRepository при помощи mockito, без использования 
базы данных.