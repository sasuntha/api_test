package com.apiframework.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("name")
    private Name name;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("phone")
    private String phone;

    public UserRequest() {}

    // ── Inner classes ─────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Name {
        @JsonProperty("firstname") private String firstname;
        @JsonProperty("lastname")  private String lastname;

        public Name() {}
        public Name(String firstname, String lastname) {
            this.firstname = firstname;
            this.lastname = lastname;
        }
        public String getFirstname() { return firstname; }
        public void setFirstname(String firstname) { this.firstname = firstname; }
        public String getLastname() { return lastname; }
        public void setLastname(String lastname) { this.lastname = lastname; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geolocation {
        @JsonProperty("lat")  private String lat;
        @JsonProperty("long") private String lng;

        public Geolocation() {}
        public Geolocation(String lat, String lng) { this.lat = lat; this.lng = lng; }
        public String getLat() { return lat; }
        public void setLat(String lat) { this.lat = lat; }
        public String getLng() { return lng; }
        public void setLng(String lng) { this.lng = lng; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        @JsonProperty("city")        private String city;
        @JsonProperty("street")      private String street;
        @JsonProperty("number")      private int number;
        @JsonProperty("zipcode")     private String zipcode;
        @JsonProperty("geolocation") private Geolocation geolocation;

        public Address() {}
        public Address(String city, String street, int number, String zipcode, Geolocation geo) {
            this.city = city; this.street = street; this.number = number;
            this.zipcode = zipcode; this.geolocation = geo;
        }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }
        public String getZipcode() { return zipcode; }
        public void setZipcode(String zipcode) { this.zipcode = zipcode; }
        public Geolocation getGeolocation() { return geolocation; }
        public void setGeolocation(Geolocation geolocation) { this.geolocation = geolocation; }
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Name getName() { return name; }
    public void setName(Name name) { this.name = name; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    /** Factory method to build a test user payload. */
    public static UserRequest sample() {
        UserRequest u = new UserRequest();
        u.setEmail("john.doe.test@example.com");
        u.setUsername("johndoe_test");
        u.setPassword("p@ssw0rd!");
        u.setPhone("1-570-236-7033");
        u.setName(new Name("John", "Doe"));
        u.setAddress(new Address("San Francisco", "Market St", 42,
                "94102", new Geolocation("-37.3159", "81.1496")));
        return u;
    }
}
