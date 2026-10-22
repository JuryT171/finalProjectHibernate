package entity;

import jakarta.persistence.*;

    @Entity
    @Table(schema = "world", name = "city") // маппинг на таблицу city именно в схеме world
    public class City {
        @Id // первич ключ
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        private String name;
        // связь «много городов одна страна,у Каира одна страна Египет, у Египта — много городов
        @ManyToOne(fetch = FetchType.LAZY) //
        @JoinColumn(name = "country_id")
        private Country country;

        private String district;

        private Integer population;


        //Getters and Setters omitted


        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Country getCountry() {
            return country;
        }

        public void setCountry(Country country) {
            this.country = country;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public Integer getPopulation() {
            return population;
        }

        public void setPopulation(Integer population) {
            this.population = population;
        }
    }

