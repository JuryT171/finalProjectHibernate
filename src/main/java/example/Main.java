package example;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CityDAO;
import dao.CountryDAO;
import dto.CityCountry;
import entity.City;
import entity.Country;
import entity.CountryLanguage;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.nonNull;

public class Main {

    private final SessionFactory sessionFactory;
    private final ObjectMapper mapper;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;
    private final RedisClient redisClient;

    public Main() {
        sessionFactory = HibernateUtil.getSessionFactory();
        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        redisClient = prepareRedisClient();
    }

    private RedisClient prepareRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            System.out.println("\nConnected to Redis\n");
        }
        return redisClient;
    }

    public static void main(String[] args) {
        Main main = new Main();

        List<CityCountry> preparedData = main.fetchData();
        main.pushToRedis(preparedData);

        main.sessionFactory.getCurrentSession().close();

        List<Integer> ids = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);

        long startRedis = System.currentTimeMillis();
        main.testRedisData(ids);
        long stopRedis = System.currentTimeMillis();

        long startMysql = System.currentTimeMillis();
        main.testMysqlData(ids);
        long stopMysql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMysql - startMysql));

        main.shutdown();
    }

    private List<CityCountry> fetchData() {
        long t0 = System.currentTimeMillis();
        List<CityCountry> result;
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            List<Country> countries = countryDAO.getAll();
            System.out.println("Стран: " + countries.size());

            int totalCount = cityDAO.getTotalCount();
            int step = 500;
            List<City> allCities = new ArrayList<>();
            for (int i = 0; i < totalCount; i += step) {
                allCities.addAll(cityDAO.getItems(i, step));
            }
            System.out.println("Городов: " + totalCount);

            for (String code : List.of("CHN", "RUS", "USA")) {
                Country country = countries.stream()
                        .filter(c -> c.getCode().equals(code))
                        .findFirst()
                        .orElseThrow();
                System.out.println(country.getCode() + " → " + country.getContinent());
            }

            result = transformData(allCities);   // строго до commit — сессия открыта

            session.getTransaction().commit();
        }
        System.out.println("Города из MySQL: " + (System.currentTimeMillis() - t0) + " ms");
        return result;
    }

    private List<CityCountry> transformData(List<City> allCities) {
        List<CityCountry> preparedData = new ArrayList<>();
        for (City city : allCities) {
            CityCountry res = new CityCountry();
            res.setId(city.getId());
            res.setName(city.getName());
            res.setDistrict(city.getDistrict());
            res.setPopulation(city.getPopulation());
            res.setCountryCode(city.getCountry().getCode());
            res.setAlternativeCountryCode(city.getCountry().getAlternativeCode());
            res.setCountryName(city.getCountry().getName());
            res.setContinent(city.getCountry().getContinent());
            res.setCountryRegion(city.getCountry().getRegion());
            res.setCountrySurfaceArea(city.getCountry().getSurfaceArea());
            res.setCountryPopulation(city.getCountry().getPopulation());

            Set<CityCountry.Language> languages = new HashSet<>();
            for (CountryLanguage cl : city.getCountry().getLanguages()) {
                CityCountry.Language language = new CityCountry.Language();
                language.setId(cl.getId());
                language.setLanguage(cl.getLanguage());
                language.setOfficial(cl.getOfficial());
                language.setPercentage(cl.getPercentage());
                languages.add(language);
            }
            res.setLanguages(languages);
            preparedData.add(res);
        }
        return preparedData;
    }

    private void pushToRedis(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void testRedisData(List<Integer> ids) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id : ids) {
                String value = sync.get(String.valueOf(id));
                try {
                    mapper.readValue(value, CityCountry.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void testMysqlData(List<Integer> ids) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                City city = cityDAO.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
            }
            session.getTransaction().commit();
        }
    }

    private void shutdown() {
        if (nonNull(sessionFactory)) {
            sessionFactory.close();
        }
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }
}