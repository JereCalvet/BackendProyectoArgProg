# Backend proyecto Argentina Programa

Este repositorio es parte de la entrega final del curso de programación `Argentina Programa #YoProgramo`.

Aquí está hosteado el proyecto en vivo:

* Frontend: [link](https://portfolio-e8aa4.web.app/) o [link alternativo](https://portfolio-e8aa4.firebaseapp.com/).
* Backend: [link](https://portfolio-argentina-programa7.herokuapp.com)

Está hecho con Java 11, Spring y Mysql utilizando gitflow.

## Deploy

Se hace automáticamente con una acción de github cuando se pushea a main.

## Errores y/o observaciones

### Funcionamiento fullstack

El backend ([repo](https://github.com/JereCalvet/BackendProyectoArgProg)) esta hosteado en Heroku ([link](https://portfolio-argentina-programa7.herokuapp.com)) en tier free. Por lo tanto, después de 30 minutos de inactividad, la aplicación se apaga y tras una request del frontend, intentará iniciar de nuevo desde cero. Durante este proceso debé conectarse a la base de datos, la cual se encuentra hosteada en clevercloud, tambien tier free y puede suceder que no responda o demore, entonces el inicio del backend falla y el front no funciona (se verá en consola un error 503 de cors). Cuando el backend crashea, después de 10 minutos intentará iniciar de nuevo automáticamente.

Para ver los logs de producción o si necesitan que revise el estado de la aplicación, me pueden contactar por [email](mailto:jereecalvet@gmail.com).
## To-Do / Mejoras

* Agregar test.
  