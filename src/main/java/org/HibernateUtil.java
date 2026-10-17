package org;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {
    private static final SessionFactory factory = build();

    private static SessionFactory build() {
        try {
            Configuration cfg = new Configuration().configure("hibernate.cfg.xml");
            return cfg.buildSessionFactory(
                    new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build());
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e); // конфиг сломан — роняем класс честно
        }
    }

    public static SessionFactory getSessionFactory() { return factory; }
}
