package com.javarush;

import com.javarush.dao.*;
import com.javarush.entity.*;
import com.javarush.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    private final SessionFactory sessionFactory;
    private HibernateUtil hibernateUtil;

    private final ActorDAO actorDAO;
    private final AddressDAO addressDAO;
    private final CategoryDAO categoryDAO;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;
    private final CustomerDAO customerDAO;
    private final FilmDAO filmDAO;
    private final FilmTextDAO filmTextDAO;
    private final InventoryDAO inventoryDAO;
    private final LanguageDAO languageDAO;
    private final PaymentDAO paymentDAO;
    private final RentalDAO rentalDAO;
    private final StaffDAO staffDAO;
    private final StoreDAO storeDAO;

    public Main() {
        hibernateUtil = new HibernateUtil();
        sessionFactory = hibernateUtil.getSessionFactory();

        actorDAO = new ActorDAO(sessionFactory);
        addressDAO = new AddressDAO(sessionFactory);
        categoryDAO = new CategoryDAO(sessionFactory);
        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
        customerDAO = new CustomerDAO(sessionFactory);
        filmDAO = new FilmDAO(sessionFactory);
        filmTextDAO = new FilmTextDAO(sessionFactory);
        inventoryDAO = new InventoryDAO(sessionFactory);
        languageDAO = new LanguageDAO(sessionFactory);
        paymentDAO = new PaymentDAO(sessionFactory);
        rentalDAO = new RentalDAO(sessionFactory);
        staffDAO = new StaffDAO(sessionFactory);
        storeDAO = new StoreDAO(sessionFactory);
    }

    public static void main(String[] args) {
        Main main = new Main();
        Customer customer = main.createCustomer();

        main.customerReturnInventoryToStore();

        main.customerRentInventory(customer);

        main.newFilmWasMade();
    }

    private Customer createCustomer() {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Store store = storeDAO.getItems(0, 1).get(0);

            City city = cityDAO.getByName("Kragujevac");

            Address address = new Address();
            address.setAddress("Indep str, 48");
            address.setCity(city);
            address.setDistrict("Indep district");
            address.setPhone("999-999-999");
            addressDAO.save(address);

            Customer customer = new Customer();
            customer.setStore(store);
            customer.setAddress(address);
            customer.setFirstName("Jonh");
            customer.setLastName("Smith");
            customer.setEmail("test1@gmail.com");
            customer.setActive(true);
            customerDAO.save(customer);

            session.getTransaction().commit();
            return customer;
        }
    }

    private void customerReturnInventoryToStore() {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Rental rental = rentalDAO.getAnyUnreturnedRental();
            rental.setReturnDate(LocalDateTime.now());
            rentalDAO.save(rental);

            session.getTransaction().commit();
        }
    }

    private void customerRentInventory(Customer customer) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Film film = filmDAO.getFirstAvaliableFilmForRent();
            Store store = storeDAO.getItems(0, 1).get(0);

            Inventory inventory = new Inventory();
            inventory.setFilm(film);
            inventory.setStore(store);
            inventoryDAO.save(inventory);

            Staff staff = store.getStaff();

            Rental rental = new Rental();
            rental.setCustomer(customer);
            rental.setInventory(inventory);
            rental.setStaff(staff);
            rental.setRentalDate(LocalDateTime.now());
            rentalDAO.save(rental);

            Payment payment = new Payment();
            payment.setCustomer(customer);
            payment.setRental(rental);
            payment.setStaff(staff);
            payment.setAmount(BigDecimal.valueOf(55.77));
            payment.setPaymentDate(LocalDateTime.now());
            paymentDAO.save(payment);

            session.getTransaction().commit();
        }
    }

    private void newFilmWasMade() {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Language language = languageDAO.getItems(0, 20).stream().unordered().findAny().get();
            List<Category> categories = categoryDAO.getItems(0, 5);
            List<Actor> actors = actorDAO.getItems(0, 20);

            Film film = new Film();
            film.setTitle("scary my-movie");
            film.setDescription("new scary film");
            film.setReleaseYear(Year.now());
            film.setLanguage(language);
            film.setOriginalLanguage(language);
            film.setRentalDuration((byte) 44);
            film.setRentalRate(BigDecimal.ZERO);
            film.setLength((short) 156);
            film.setReplacementCost(BigDecimal.TEN);
            film.setRating(Rating.PG);
            film.setSpecialFeatures(Set.of(Feature.TRAILERS, Feature.COMMENTARIES));
            film.setActors(new HashSet<>(actors));
            film.setCategories(new HashSet<>(categories));
            filmDAO.save(film);

            FilmText filmText = new FilmText();
            filmText.setId(film.getId());
            filmText.setFilm(film);
            filmText.setTitle("new scary film");
            filmText.setDescription("new scary film");
            filmTextDAO.save(filmText);

            session.getTransaction().commit();
        }
    }
}
