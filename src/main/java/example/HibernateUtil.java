package example;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory factory = build();

    private static SessionFactory build() {
        try {
            return new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e); // конфиг сломан — роняем класс честно
        }
    }

    public static SessionFactory getSessionFactory() { return factory; }
}
