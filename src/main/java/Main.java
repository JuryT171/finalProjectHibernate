import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class Main {
    public static void main(String[] args) {
        SessionFactory factory = HibernateUtil.getSessionFactory();
        try (Session session = factory.openSession()) {
            Object one = session.createNativeQuery("SELECT 1").getSingleResult();
            System.out.println("=== Коннект есть, SELECT 1 вернул: " + one + " ===");
        }
        factory.close();
    }
}