#!/bin/sh
echo "Waiting for MySQL to be ready..."
until mysql -h mysql -u"$SPRING_DATASOURCE_USERNAME" -p"$SPRING_DATASOURCE_PASSWORD" -e "SELECT 1;" &> /dev/null
do
  echo "MySQL is unavailable - sleeping"
  sleep 3
done

echo "MySQL is up - starting Spring Boot app"
exec java -jar app.jar
