package com.kpatil.vehicles.service;

import com.kpatil.vehicles.client.maps.MapsClient;
import com.kpatil.vehicles.client.prices.PriceClient;
import com.kpatil.vehicles.domain.Location;
import com.kpatil.vehicles.domain.car.Car;
import com.kpatil.vehicles.domain.car.CarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private static final Logger logger =
            LoggerFactory.getLogger(CarService.class);

    private final CarRepository carRepository;
    private final PriceClient priceClient;
    private final MapsClient mapsClient;

    public CarService(CarRepository carRepository, PriceClient priceClient, MapsClient mapsClient) {
        this.carRepository = carRepository;
        this.priceClient = priceClient;
        this.mapsClient = mapsClient;
    }

    /**
     * Gathers a list of all vehicles
     *
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        logger.info("Getting list of all cars ...");
        return carRepository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     *
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        logger.info("Looking for car with id " + id);

        Optional<Car> carOptional = carRepository.findById(id);
        if (carOptional.isEmpty()) {
            throw new CarNotFoundException("Car not found for id = " + id);
        }

        Car car = carOptional.get();
        String price = priceClient.getPrice(id);
        car.setPrice(price);

        Location location = mapsClient.getAddress(car.getLocation());
        car.setLocation(location);
        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     *
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            logger.info("Updating info for car : " + car.getId());
            return carRepository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setCondition(car.getCondition());
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return carRepository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        logger.info("Creating new car record ...");
        return carRepository.save(car);
    }

    /**
     * Deletes a given car by ID
     *
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        logger.info("Trying to delete car with id " + id);
        Optional<Car> carOptional = carRepository.findById(id);
        if (carOptional.isEmpty()) {
            throw new CarNotFoundException("Car not found for id : " + id);
        }
        carRepository.delete(carOptional.get());
    }
}
