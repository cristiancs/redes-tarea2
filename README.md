# Integrantes

- Cristian Navarrete 201573549-2
- Benjamin Seider 201573541-7

# Compilación del Edge

Se requiere ant para correr el edge, para esto en centos se puede instalar con

    yum install ant

    cd edge
    ant

# Ejecución del Edge

Desde la carpeta server

    java -jar build/jar/ftpserver.jar puerto

# Compilación del Cliente

    cd client
    ant

# Ejecución del Cliente

Desde la carpeta client

    java -jar build/jar/ftpclient.jar servidor puerto

# Compilacion del Proxy

El proxy requiere maven, para esto se puede instalar ejecutando

    sudo yum install maven

Luego, para ejecutarlo

    cd proxy
    mvn clean package assembly:assembly

# Ejecucion del Proxy

    java -cp target/proxy-1.0-SNAPSHOT-jar-with-dependencies.jar  proxy.App puerto

se pueden utilizar todos los comandos que utiliza el servidor, para salir enviar una linea vacía

### Handshake

El servidor enviara un mensaje con "HELLO" cuando un cliente se conecta, el servidor espera de vuelta un "HELLO"

# Comandos

### ls

Se debe enviar el comando ls y se retorna la lista de directorios y archivos en la carpeta files del servidor, termina con la palabra END

### delete file.txt

Se debe enviar el comando, se intentara eliminar el archivo del servidor y se comunica el resultado de la operación

### get file.txt

En caso de existir, el servidor enviara el contenido del archivo codificado en base64 seguido de un END, en caso contrario, la respuesta es NOFILE

### put file.txt

Luego de esta linea el servidor espera que la siguiente linea sea el contenido del archivo codificado en base64.

# Notas

- Este código fue probado utilizando Amazon Coretto 11 https://aws.amazon.com/es/corretto/
- Los archivos se leen y escriben desde la carpeta files dentro de server y cliente según corresponda
