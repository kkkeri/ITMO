package ru.itmo.web;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("APP INIT: Application started");
        // тут твои действия при запуске
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("APP INIT: Application stopped");
    }
}