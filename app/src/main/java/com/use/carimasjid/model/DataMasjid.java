package com.use.carimasjid.model;

public class DataMasjid {
    private String id, person_id, kecamatan_id, name, type, image, adress, location, ceo_name, ceo_contact, vice_name, vice_contact, facilities;
    private double rating;
    private String jarak;

    public DataMasjid(String id, String person_id, String kecamatan_id, String name, String type, String image, String adress, String location, String ceo_name, String ceo_contact, String vice_name, String vice_contact, String facilities, double rating) {
        this.id = id;
        this.person_id = person_id;
        this.kecamatan_id = kecamatan_id;
        this.name = name;
        this.type = type;
        this.image = image;
        this.adress = adress;
        this.location = location;
        this.ceo_name = ceo_name;
        this.ceo_contact = ceo_contact;
        this.vice_name = vice_name;
        this.vice_contact = vice_contact;
        this.facilities = facilities;
        this.rating = rating;
    }

    public DataMasjid(String id, String person_id, String kecamatan_id, String name, String type, String image, String adress, String location, String ceo_name, String ceo_contact, String vice_name, String vice_contact, String facilities, float rating, String jarak) {
        this.id = id;
        this.person_id = person_id;
        this.kecamatan_id = kecamatan_id;
        this.name = name;
        this.type = type;
        this.image = image;
        this.adress = adress;
        this.location = location;
        this.ceo_name = ceo_name;
        this.ceo_contact = ceo_contact;
        this.vice_name = vice_name;
        this.vice_contact = vice_contact;
        this.facilities = facilities;
        this.rating = rating;
        this.jarak = jarak;
    }

    public double getRating() {
        return rating;
    }

    public String getKecamatan_id() {
        return kecamatan_id;
    }

    public String getJarak() {
        return jarak;
    }

    public void setJarak(String jarak) {
        this.jarak = jarak;
    }

    public String getId() {
        return id;
    }

    public String getPerson_id() {
        return person_id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getImage() {
        return image;
    }

    public String getAdress() {
        return adress;
    }

    public String getLocation() {
        return location;
    }

    public String getCeo_name() {
        return ceo_name;
    }

    public String getCeo_contact() {
        return ceo_contact;
    }

    public String getVice_name() {
        return vice_name;
    }

    public String getVice_contact() {
        return vice_contact;
    }

    public String getFacilities() {
        return facilities;
    }
}
