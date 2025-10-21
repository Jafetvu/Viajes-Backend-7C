package com.utez.edu.mx.viajesbackend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Clase de configuración para establecer la conexión a la base de datos MySQL.
 * Utiliza valores configurados en el archivo de propiedades (application.properties o application.yml)
 * para crear un bean de tipo DataSource que se utiliza para la conexión a la base de datos.
 */
@Configuration
public class DBConnection {

    // Valores de configuración inyectados desde el archivo de propiedades
    @Value("${db.url}")
    private String DB_URL; // URL de la base de datos

    @Value("${db.name}")
    private String DB_NAME; // Nombre de la base de datos

    @Value("${db.username}")
    private String DB_USERNAME; // Nombre de usuario para la conexión

    @Value("${db.password}")
    private String DB_PASSWORD; // Contraseña para la conexión

    /**
     * Configura y devuelve un DataSource que será utilizado por Spring para la conexión a la base de datos.
     *
     * @return DataSource configurado para conectarse a la base de datos MySQL.
     */
    @Bean
    public DataSource dataSource() {
        // Crear una instancia de DriverManagerDataSource
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        // Establecer el controlador JDBC para MySQL
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Establecer la URL de la base de datos, combinando el URL base con el nombre de la base de datos
        dataSource.setUrl(DB_URL + "/" + DB_NAME);

        // Establecer las credenciales de acceso
        dataSource.setUsername(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);

        // Devolver el objeto DataSource configurado
        return dataSource;
    }
}
