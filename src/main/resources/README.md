Запуск приложения на сервере

Первый вариант (через службу). Пробовал на Ubuntu на локальной виртуальной машине
https://www.youtube.com/watch?v=b9iyMR48zCQ&t=1410s

1) Сформировать jar файл с помощью maven
2) С помощью WinSCP скопировать файл в Ubuntu в каталог home/alex/JavaTest/mySimpleTelegramBot.jar
3) Создать службу MySimpleTelegramBot.service в каталоге /etc/systemd/system

Содержание файла:

[Unit]
Description=MyBot
After=syslog.target network.target

[Service]
SuccessExitStatus=143

User=root
Group=root

Type=simple
ExecStart=java -jar home/alex/JavaTest/mySimpleTelegramBot.jar

[Install]
WantedBy=multi-user.target

4) Запуск службы, которая будет запускать jar

Использованные команды
544  sudo nano MySimpleTelegramBot.service
547  sudo touch MySimpleTelegramBot.service
Запуск службы
550  sudo systemctl start MySimpleTelegramBot.service
Статус службы
561  systemctl status MySimpleTelegramBot.service
Остановка службы
567  systemctl stop MySimpleTelegramBot.service


Второй вариант. Пробовал на платном VDS
https://habr.com/ru/articles/718536/
Этот материал не пробовал, но похоже, что о том же
https://www.youtube.com/watch?v=UC0VzH1ICEA


Использованные команды в Ubuntu

Обновляем всё    
1  sudo apt update
Установка jdk 11 версии
2  apt install openjdk-11-jre-headless
Также нам потребуется утилита screen - консольная утилита позволяющая в действующей SSH сессии открывать 
неограниченное количество независимых виртуальных терминалов.
3  apt install screen
Создание нового скрина по имени bot
5  sudo screen -S bot

Внутри скрина запускаем jar
$ java -jar BOTNAME-0.0.1-SNAPSHOT.jar

Список скринов
6  screen -list
Вход в скин по имени bot
7  screen -r bot


Следующий урок https://www.youtube.com/watch?v=QUuzM-euwdY&list=PL7ZzXmLk6CYUl4exDW4S_2sQbYpQNswfR&index=4
Добавлены команды и их описание. Весь функционал в TelegramBot


Следующий урок https://www.youtube.com/watch?v=DAsq3rB4zZw&list=PL7ZzXmLk6CYUl4exDW4S_2sQbYpQNswfR&index=4
Добавляем БД, используя Spring JPA

Добавлено подключение к PostgreSQL
Добавлена регистрация пользователя при первом обращении к /start

Добавляем поддержку смайлов / emoji
https://www.youtube.com/watch?v=rJ3RCBHj-I4&list=PL7ZzXmLk6CYUl4exDW4S_2sQbYpQNswfR&index=5

https://www.emojipedia.org/ - подбор смайликов

Добавляем поддержку экранной клавиатуры ReplyKeyboard
https://www.youtube.com/watch?v=eGYSPo5MzcE&list=PL7ZzXmLk6CYUl4exDW4S_2sQbYpQNswfR&index=6
Виртуальная клавиатура - это кнопки под чатом с быстрыми ответами

Добавляем кнопки к сообщению, редактируем сообщение
https://www.youtube.com/watch?v=UvPoPrIzDwc&list=PL7ZzXmLk6CYUl4exDW4S_2sQbYpQNswfR&index=7

Отправляем сообщения всем пользователям бота
https://www.youtube.com/watch?v=TuCpUAavpG0&list=PL7ZzXmLk6CYUl4exDW4S_2sQbYpQNswfR&index=8

Для отправки сообщения всем пользователям: /send сообщение

@Scheduled для автоматической отправки сообщений из БД
https://www.youtube.com/watch?v=haf9Vb-YYug&list=PL7ZzXmLk6CYUl4exDW4S_2sQbYpQNswfR&index=10


    1  sudo sh -c 'echo "deb https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
    2  wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
    3  sudo apt-get update
    4  sudo apt-get -y install postgresql
    6  sudo systemctl status postgresql
    7  su postgres
    8  sudo -i -u postgres
    9  sudo apt update
   10  apt install openjdk-17-jre-headless
   11  ll
   12  cd /etc/systemd/system/
   13  nano cashdancebot.service
   14  systemctl status cashdancebot.service
   15  systemctl start cashdancebot.service
   19  sudo update

   22  sudo apt update
   23  apt list --upgradable
   24  sudo apt full-upgrade
   25  sudo reboot
   27  systemctl status cashdancebot.service
   28  systemctl start cashdancebot.service
   
   30  sudo -u postgres psql template1


https://jeka.by/post/1104/postgresql-connect-via-terminal/ 