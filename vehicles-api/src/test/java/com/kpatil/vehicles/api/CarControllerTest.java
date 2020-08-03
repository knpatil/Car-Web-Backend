package com.kpatil.vehicles.api;

import com.kpatil.vehicles.client.maps.MapsClient;
import com.kpatil.vehicles.client.prices.PriceClient;
import com.kpatil.vehicles.domain.Condition;
import com.kpatil.vehicles.domain.Location;
import com.kpatil.vehicles.domain.car.Car;
import com.kpatil.vehicles.domain.car.Details;
import com.kpatil.vehicles.domain.manufacturer.Manufacturer;
import com.kpatil.vehicles.service.CarService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Implements testing of the CarController class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class CarControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<Car> json;

    @MockBean
    private CarService carService;

    @MockBean
    private PriceClient priceClient;

    @MockBean
    private MapsClient mapsClient;

    /**
     * Creates pre-requisites for testing, such as an example car.
     */
    @Before
    public void setup() {
        Car car = getCar();
        car.setId(1L);
        given(carService.save(any())).willReturn(car);
        given(carService.findById(any())).willReturn(car);
        given(carService.list()).willReturn(Collections.singletonList(car));
    }

    /**
     * Tests for successful creation of new car in the system
     *
     * @throws Exception when car creation fails in the system
     */
    @Test
    public void createCar() throws Exception {
        Car car = getCar();
        mvc.perform(
                post(new URI("/cars"))
                        .content(json.write(car).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated());
        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(carService, times(1)).save(carCaptor.capture());
        assertThat(carCaptor.getValue().getLocation().getLat()).isEqualTo(car.getLocation().getLat());
        assertThat(carCaptor.getValue().getCondition()).isEqualTo(car.getCondition());
        assertThat(carCaptor.getValue().getDetails().getManufacturer().getName()).isEqualTo(car.getDetails().getManufacturer().getName());
    }

    /**
     * Tests for successful update of a car in the system
     *
     * @throws Exception when car creation fails in the system
     */
    @Test
    public void updateCar() throws Exception {
        Car car = getCar();
        // update car fields
        car.setCondition(Condition.NEW);
        car.getDetails().setExternalColor("red");
        car.getDetails().setManufacturer(new Manufacturer(999, "Ferrari"));

        mvc.perform(
                put(new URI("/cars/1"))
                        .content(json.write(car).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(carService, times(1)).save(carCaptor.capture());

        assertThat(carCaptor.getValue().getCondition()).isEqualTo(Condition.NEW);
        assertThat(carCaptor.getValue().getDetails().getExternalColor()).isEqualTo("red");
        assertThat(carCaptor.getValue().getDetails().getManufacturer().getCode()).isEqualTo(999);
        assertThat(carCaptor.getValue().getDetails().getManufacturer().getName()).isEqualTo("Ferrari");
    }

    /**
     * Tests if the read operation appropriately returns a list of vehicles.
     *
     * @throws Exception if the read operation of the vehicle list fails
     */
    @Test
    public void listCars() throws Exception {
        Car car = getCar();
        mvc.perform(get(new URI("/cars")).accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.carList[0].location.lat", is(car.getLocation().getLat())))
                .andExpect(jsonPath("_embedded.carList[0].location.lon", is(car.getLocation().getLon())))
                .andExpect(jsonPath("_embedded.carList[0].details.body", is(car.getDetails().getBody())))
                .andExpect(jsonPath("_embedded.carList[0].details.engine", is(car.getDetails().getEngine())))
                .andExpect(jsonPath("_embedded.carList[0].details.fuelType", is(car.getDetails().getFuelType())))
                .andExpect(jsonPath("_embedded.carList[0].details.numberOfDoors", is(car.getDetails().getNumberOfDoors())))
                .andExpect(jsonPath("_embedded.carList[0].condition", is(car.getCondition().name())));
        verify(carService, times(1)).list();
    }

    /**
     * Tests the read operation for a single car by ID.
     *
     * @throws Exception if the read operation for a single car fails
     */
    @Test
    public void findCar() throws Exception {
        Car car = getCar();
        mvc.perform(get(new URI("/cars/1")).accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("location.lat", is(car.getLocation().getLat())))
                .andExpect(jsonPath("location.lon", is(car.getLocation().getLon())))
                .andExpect(jsonPath("details.body", is(car.getDetails().getBody())))
                .andExpect(jsonPath("details.externalColor", is(car.getDetails().getExternalColor())))
                .andExpect(jsonPath("details.productionYear", is(car.getDetails().getProductionYear())))
                .andExpect(jsonPath("condition", is(car.getCondition().name())));
        verify(carService, times(1)).findById(1L);
    }

    /**
     * Tests the deletion of a single car by ID.
     *
     * @throws Exception if the delete operation of a vehicle fails
     */
    @Test
    public void deleteCar() throws Exception {
        // delete car with id 1
        mvc.perform(delete(new URI("/cars/1")))
                .andExpect(status().is2xxSuccessful());
        verify(carService, times(1)).delete(1L);
    }

    /**
     * Creates an example Car object for use in testing.
     *
     * @return an example Car object
     */
    private Car getCar() {
        Car car = new Car();
        car.setLocation(new Location(40.730610, -73.935242));
        Details details = new Details();
        Manufacturer manufacturer = new Manufacturer(101, "Chevrolet");
        details.setManufacturer(manufacturer);
        details.setModel("Impala");
        details.setMileage(32280);
        details.setExternalColor("white");
        details.setBody("sedan");
        details.setEngine("3.6L V6");
        details.setFuelType("Gasoline");
        details.setModelYear(2018);
        details.setProductionYear(2018);
        details.setNumberOfDoors(4);
        car.setDetails(details);
        car.setCondition(Condition.USED);
        return car;
    }
}