setup:
	@make build
	@make up

build:
	sudo docker-compose build

up:
	sudo docker-compose up -d

down:
	sudo docker-compose down

status:
	sudo docker-compose ps

start:
	sudo docker exec -i debian_slim bash -c "./mvnw"

console:
	sudo docker exec -it debian_slim /bin/bash