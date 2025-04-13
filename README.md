### Инструкция для сборки
1) cd ./common-dto/
2) mvn clean package (или mvn clean install, если в библиотеку были добавлены изменения)
3) cd ../app/
4) mvn clean package
5) cd ../file_service/
6) mvn clean package
7) cd ../email_service/
8) mvn clean package
9) cd .. 
10) docker build -t app ./app
11) docker build -t file_service ./file_service
12) docker build -t email_service ./email_service
13) docker compose up