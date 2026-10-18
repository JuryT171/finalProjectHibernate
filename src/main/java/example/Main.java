package example;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CityDAO;
import dao.CountryDAO;
import entity.City;
import entity.Country;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

public class Main {
    private final SessionFactory sessionFactory;

    private final ObjectMapper mapper;

    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;


    public Main() {
            sessionFactory = HibernateUtil.getSessionFactory();

            cityDAO = new CityDAO(sessionFactory);
            countryDAO = new CountryDAO(sessionFactory);
            mapper = new ObjectMapper();
        }

    public static void main(String[] args) {
        Main main = new Main();
        main.fetchData();
        main.shutdown();
    }

    private List<City> fetchData() {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            List<Country> countries = countryDAO.getAll();   // ЭТАП 1: прогноз 240
            System.out.println("Стран: " + countries.size());

            List<City> allCities = new ArrayList<>();
            int totalCount = cityDAO.getTotalCount();
            int step = 500;
            for (int i = 0; i < totalCount; i += step) {     // ЭТАП 2
                allCities.addAll(cityDAO.getItems(i, step));
            }
            System.out.println("Городов: " + allCities.size());
            countries.stream()
                    .filter(c -> List.of("RUS", "USA", "CHN").contains(c.getCode()))
                    .forEach(c -> System.out.println(c.getCode() + " → " + c.getContinent()));

            session.getTransaction().commit();
            return allCities;
        }
    }


    private void shutdown() {
        if (nonNull(sessionFactory)) {
            sessionFactory.close();
        }
    }
}