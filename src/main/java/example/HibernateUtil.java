package example;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    // Создаём один раз при первом обращении к классу
    private static final SessionFactory factory = build();

    private static SessionFactory build() {
        try { // создаем конфиг
            return new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e); // конфиг сломан — роняем класс
        }
    }

    public static SessionFactory getSessionFactory() { return factory; }
}
