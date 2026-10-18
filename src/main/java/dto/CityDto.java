package dto;

import entity.City;

public class CityDto {
    public int id;
    public String name;
    public int population;
    public String district;
    public String countryCode;

    public CityDto() { }              // обязателен для чтения JSON

    public CityDto(City city) {
        this.id = city.getId();
        this.name = city.getName();
        this.population = city.getPopulation();
        this.district = city.getDistrict();
        this.countryCode = city.getCountry().getCode();  // только внутри сессии!
    }
}