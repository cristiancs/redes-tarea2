Este código fue probado utilizando Amazon Coretto

https://aws.amazon.com/es/corretto/

Compilación
ant

Ejecución
java -jar build/jar/ftp-server.jar

Explicación del servidor

Se recomienda utilizar telnet para probar la tarea, el puerto de escucha es el 59898

Handshake

El servidor enviara un mensaje con "HELLO" cuando un cliente se conecta, el servidor espera de vuelta un "HELLO"

ls

Se debe enviar el comando ls y se retorna la lista de directorios y archivos en la carpeta files del servidor, termina con la palabra END

delete file.txt

Se debe enviar el comando, se intentara eliminar el archivo del servidor y se comunica el resultado de la operación

get file.txt
En caso de existir, el servidor enviara el contenido del archivo codificado en base64 seguido de un END, en caso contrario, la respuesta es NOFILE

put file.txt
Luego de esta linea el servidor espera que la siguiente linea sea el contenido del archivo codificado en base64.
